package com.lrcore.gateway.filter;

import com.lrcore.common.core.constant.CacheConstants;
import com.lrcore.common.core.constant.HttpStatus;
import com.lrcore.common.core.constant.SecurityConstants;
import com.lrcore.common.core.constant.TokenConstants;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.JwtUtils;
import com.lrcore.common.core.utils.ServletUtils;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 网关鉴权（双轨）：
 * <ol>
 *   <li>白名单路径（Nacos security.ignore.whites）直接放行；</li>
 *   <li>旧双 Token 链路：HS512 令牌解析 + Redis 登录态校验，
 *       校验通过后将 user_key / user_id / username 注入下游请求头（现有接口零改动）；</li>
 *   <li>授权服务器链路（Spring Authorization Server 接入后新增）：旧格式解析失败时，
 *       回退为 SAS 签发的 RS256 JWT 校验（JWKS 验签 + iss/exp），
 *       通过后将 user_id / username / tenant_id 等 claims 映射为同一组下游请求头。
 *       SAS 令牌为无状态令牌，不查 Redis 登录态（即时吊销依赖 /oauth2/revoke + 短时效）。</li>
 * </ol>
 * 说明：上游 GatewaySecurityConfig 的资源服务器过滤链已对“无法解码的令牌”按匿名放行，
 * 本过滤器仅会收到（a）白名单路径的任意请求（直接跳过）与（b）能通过双轨解码校验的合法令牌。
 *
 * @ClassName: AuthFilter
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:54
 * @Version: 1.1
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    // 排除过滤的 uri 地址，nacos自行添加
    private final IgnoreWhiteProperties ignoreWhite;
    private final RedisService redisService;
    // 授权服务器（SAS）RS256 解码器；@Lazy 避免与资源服务器解码器 Bean 的装配顺序耦合
    @Lazy
    @Qualifier("lrcoreSasReactiveJwtDecoder")
    private final ReactiveJwtDecoder sasJwtDecoder;

    public AuthFilter(IgnoreWhiteProperties ignoreWhite,
                      RedisService redisService,
                      @Lazy @Qualifier("lrcoreSasReactiveJwtDecoder") ReactiveJwtDecoder sasJwtDecoder) {
        this.ignoreWhite = ignoreWhite;
        this.redisService = redisService;
        this.sasJwtDecoder = sasJwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();

        String url = request.getURI().getPath();
        // 跳过不需要验证的路径
        if (FunStrUtils.matches(url, ignoreWhite.getWhites())) {
            // 白名单直通前同样清除内部请求来源标记，防止外部客户端伪造 from-source: inner
            // 绕过下游资源服务器鉴权（内部 Feign 调用不经网关，不受影响）
            removeHeader(mutate, SecurityConstants.FROM_SOURCE);
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        }
        String token = getToken(request);
        if (FunStrUtils.isEmpty(token)) {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }
        // [双轨] 先按旧 HS512 双 Token 链路校验（含 Redis 登录态）；
        // 旧格式解析失败（新签发的 SAS 令牌 / 脏数据）时回退 SAS RS256 校验
        return Mono.fromCallable(() -> JwtUtils.parseToken(token))
                .flatMap(claims -> legacyPass(exchange, chain, mutate, claims))
                .onErrorResume(ex -> sasPass(exchange, chain, mutate, token))
                .onErrorResume(ex -> unauthorizedResponse(exchange, "令牌已过期或验证不正确！"));
    }

    /**
     * 旧双 Token 链路：Redis 登录态校验 + 请求头注入。
     */
    private Mono<Void> legacyPass(ServerWebExchange exchange, GatewayFilterChain chain,
                                  ServerHttpRequest.Builder mutate, Claims claims) {
        String userkey = JwtUtils.getUserKey(claims);
        boolean islogin = redisService.hasKey(getTokenKey(userkey));
        if (!islogin) {
            return unauthorizedResponse(exchange, "登录状态已过期");
        }
        String userid = JwtUtils.getUserId(claims);
        String username = JwtUtils.getUserName(claims);
        if (FunStrUtils.isEmpty(userid) || FunStrUtils.isEmpty(username)) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }
        // 设置用户信息到请求
        addHeader(mutate, SecurityConstants.USER_KEY, userkey);
        addHeader(mutate, SecurityConstants.DETAILS_USER_ID, userid);
        addHeader(mutate, SecurityConstants.DETAILS_USERNAME, username);
        // 内部请求来源参数清除
        removeHeader(mutate, SecurityConstants.FROM_SOURCE);
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }

    /**
     * 授权服务器（SAS）链路：RS256 JWT 校验（JWKS 验签 + iss/exp）通过后，
     * 将业务 claims（契约：user_id / username / tenant_id / ent_id / dept_id）
     * 映射为与旧链路一致的下游请求头；无状态，不查 Redis。
     */
    private Mono<Void> sasPass(ServerWebExchange exchange, GatewayFilterChain chain,
                               ServerHttpRequest.Builder mutate, String token) {
        return sasJwtDecoder.decode(token).flatMap(jwt -> {
            String userid = FunConvertStr(jwt.getClaim("user_id"));
            if (FunStrUtils.isEmpty(userid)) {
                // 非用户主体令牌（如 client_credentials）无 user_id，回退 sub
                userid = jwt.getSubject();
            }
            String username = FunConvertStr(jwt.getClaim("username"));
            if (FunStrUtils.isEmpty(userid) || FunStrUtils.isEmpty(username)) {
                return unauthorizedResponse(exchange, "令牌验证失败");
            }
            // 与旧链路保持一致的请求头契约：user_key 使用 user_id（SAS 令牌无 Redis userkey）
            addHeader(mutate, SecurityConstants.USER_KEY, userid);
            addHeader(mutate, SecurityConstants.DETAILS_USER_ID, userid);
            addHeader(mutate, SecurityConstants.DETAILS_USERNAME, username);
            addHeader(mutate, SecurityConstants.DETAILS_TENANT_ID, FunConvertStr(jwt.getClaim("tenant_id")));
            addHeader(mutate, SecurityConstants.DETAILS_ENT_ID, FunConvertStr(jwt.getClaim("ent_id")));
            addHeader(mutate, SecurityConstants.DETAILS_DEPT_ID, FunConvertStr(jwt.getClaim("dept_id")));
            // 内部请求来源参数清除
            removeHeader(mutate, SecurityConstants.FROM_SOURCE);
            return chain.filter(exchange.mutate().request(mutate.build()).build());
        });
    }

    /**
     * JWT claim → 字符串（缺失时返回 null）。
     */
    private String FunConvertStr(Object value) {
        return value == null ? null : value.toString();
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        if (valueStr.isBlank()) {
            return;
        }
        String valueEncode = ServletUtils.urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name) {
        mutate.headers(httpHeaders -> httpHeaders.remove(name));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg) {
        log.error("[鉴权异常处理]请求路径:{},错误信息:{}", exchange.getRequest().getPath(), msg);
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, HttpStatus.UNAUTHORIZED + "");
    }

    /**
     * 获取缓存key
     */
    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }

    /**
     * 获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (FunStrUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX)) {
            token = token.replaceFirst(TokenConstants.PREFIX, FunStrUtils.EMPTY);
        }
        return token;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
