//package com.lrcore.auth.config;
//
//import com.lrcore.common.auth.client.LrcoreRegisteredClientInitializer;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
//import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
//import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
//
///**
// * <p>类模块说明</p>
// *
// * @Describe: SSO 无数据库内存兜底装配（G8）。
// *            开发/测试环境未配置数据源（lrcore.auth.jdbc.enabled=false，默认）时，
// *            提供官方内存实现，使授权码流程（authorize → 登录 → code → token）
// *            零依赖跑通；重启后授权/客户端注册状态清空（令牌随之失效，可接受）。
// * <p>
// * 让位条件：
// * <ul>
// *   <li>{@code lrcore.auth.jdbc.enabled=true} 时整体不生效（starter 的 JDBC 装配接管）；</li>
// *   <li>宿主自定义了 RegisteredClientRepository 时不生效。</li>
// * </ul>
// * 生产环境请使用 JDBC 持久化（lrcore_auth 库 + sql/oauth2-server-mysql.sql，
// * 见 lrcore-common-auth 集成指南 2.2 的 JDBC URL 参数要求）。
// * @ClassName LrcoreSsoInMemoryConfiguration
// * @Author lrcore
// * @Date 2026/8/21
// * @Version 1.0
// */
//@Configuration
//@ConditionalOnProperty(prefix = "lrcore.auth", name = "jdbc.enabled", havingValue = "false", matchIfMissing = true)
//@ConditionalOnMissingBean(RegisteredClientRepository.class)
//public class LrcoreSsoInMemoryConfiguration {
//
//    /**
//     * 内存版 OAuth2 客户端注册仓库（内置客户端由下方初始化器幂等写入）。
//     */
//    @Bean
//    public RegisteredClientRepository registeredClientRepository() {
//        return new InMemoryRegisteredClientRepository();
//    }
//
//    /**
//     * 内存版授权（令牌聚合）服务：授权码/访问/刷新令牌暂存于此，
//     * 支撑令牌交换与刷新（进程内有效）。
//     */
//    @Bean
//    public OAuth2AuthorizationService oAuth2AuthorizationService() {
//        return new InMemoryOAuth2AuthorizationService();
//    }
//
//    /**
//     * 内存版授权同意服务（web-admin-spa 免同意，保留组件完整性）。
//     */
//    @Bean
//    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService() {
//        return new InMemoryOAuth2AuthorizationConsentService();
//    }
//
//    /**
//     * 内置客户端幂等初始化器（web-admin / web-admin-spa / internal-service）。
//     */
//    @Bean
//    @ConditionalOnMissingBean
//    public LrcoreRegisteredClientInitializer lrcoreRegisteredClientInitializer(
//            RegisteredClientRepository registeredClientRepository) {
//        return new LrcoreRegisteredClientInitializer(registeredClientRepository);
//    }
//}
