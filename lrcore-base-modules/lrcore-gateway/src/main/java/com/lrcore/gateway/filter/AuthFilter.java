package com.lrcore.gateway.filter;

import com.lrcore.common.core.constant.HttpStatus;
import com.lrcore.common.core.constant.SecurityConstants;
import com.lrcore.common.core.constant.TokenConstants;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.ServletUtils;
import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
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
 * @Describe: 网关鉴权 —— 全面基于 SSO/SAS（Spring Authorization Server）：
 * <ol>
 *   <li>白名单路径（security.ignore.whites）直接放行，并清除内部请求来源标记；</li>
 *   <li>其余路由统一校验授权服务器（SAS 与网关共用 JWKS 公钥）签发的 RS256 JWT
 *       （验签 + iss/exp），通过后将 user_id / username / tenant_id / ent_id / dept_id
 *       claims 映射为下游请求头（下游业务模块据头部还原当前用户）；</li>
 *   <li>令牌缺失/无效统一返回 401 JSON（供前端识别 TOKEN 过期并触发静默/重新授权）。
 *       若依遗留的 HS512 双 Token + Redis 登录态链路已全面移除。</li>
 * </ol>
 * 说明：上游 GatewaySecurityConfig 的资源服务器过滤链已对"无法解码的令牌"按匿名放行，
 * 本过滤器仅会收到（a）白名单路径的任意请求（直接跳过）与（b）能通过 SAS 解码校验的合法令牌。
 *
 * @ClassName: AuthFilter
 * @Author: lrcore
 * @Date 2026/8/24
 * @Version: 2.0
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    // 排除过滤的 uri 地址，nacos自行添加
    private final IgnoreWhiteProperties ignoreWhite;
    // 授权服务器（SAS）RS256 解码器；@Lazy 避免与资源服务器解码器 Bean 的装配顺序耦合
    @Lazy
    @Qualifier("lrcoreSasReactiveJwtDecoder")
    private final ReactiveJwtDecoder sasJwtDecoder;

    public AuthFilter(IgnoreWhiteProperties ignoreWhite,
                      @Lazy @Qualifier("lrcoreSasReactiveJwtDecoder") ReactiveJwtDecoder sasJwtDecoder) {
        this.ignoreWhite = ignoreWhite;
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
        // 统一按 SAS RS256 JWT 校验（JWKS 验签 + iss/exp），通过后映射下游请求头；无状态，不查 Redis
        return sasJwtDecoder.decode(token)
                .flatMap(jwt -> sasPass(exchange, chain, mutate, jwt))
                .onErrorResume(ex -> {
                    log.error("[鉴权异常处理]请求路径:{},错误信息:{}",
                            exchange.getRequest().getPath(), ex.getMessage());
                    return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
                });
    }

    /**
     * 授权服务器（SAS）链路：RS256 JWT 校验通过后，
     * 将业务 claims（契约：user_id / username / tenant_id / ent_id / dept_id）
     * 映射为下游请求头；无状态，不查 Redis。
     */
    private Mono<Void> sasPass(ServerWebExchange exchange, GatewayFilterChain chain,
                               ServerHttpRequest.Builder mutate, Jwt jwt) {
        String userid = FunConvertStr(jwt.getClaim("user_id"));
        if (FunStrUtils.isEmpty(userid)) {
            // 非用户主体令牌（如 client_credentials）无 user_id，回退 sub
            userid = jwt.getSubject();
        }
        String username = FunConvertStr(jwt.getClaim("username"));
        if (FunStrUtils.isEmpty(userid) || FunStrUtils.isEmpty(username)) {
            return unauthorizedResponse(exchange, "令牌验证失败");
        }
        // 与下游契约一致的请求头：user_key 使用 user_id（SAS 令牌无 Redis userkey）
        addHeader(mutate, SecurityConstants.USER_KEY, userid);
        addHeader(mutate, SecurityConstants.DETAILS_USER_ID, userid);
        addHeader(mutate, SecurityConstants.DETAILS_USERNAME, username);
        addHeader(mutate, SecurityConstants.DETAILS_TENANT_ID, FunConvertStr(jwt.getClaim("tenant_id")));
        addHeader(mutate, SecurityConstants.DETAILS_ENT_ID, FunConvertStr(jwt.getClaim("ent_id")));
        addHeader(mutate, SecurityConstants.DETAILS_DEPT_ID, FunConvertStr(jwt.getClaim("dept_id")));
        // 内部请求来源参数清除
        removeHeader(mutate, SecurityConstants.FROM_SOURCE);
        return chain.filter(exchange.mutate().request(mutate.build()).build());
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
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, HttpStatus.UNAUTHORIZED + "");
    }

    /**
     * 获取请求token
     */
    private String getToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        token = trimBearer(token);
        return token;
    }

    private String trimBearer(String token) {
        if (FunStrUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX)) {
            return token.replaceFirst(TokenConstants.PREFIX, FunStrUtils.EMPTY);
        }
        return token;
    }

    @Override
    public int getOrder() {
        return -200;
    }
}