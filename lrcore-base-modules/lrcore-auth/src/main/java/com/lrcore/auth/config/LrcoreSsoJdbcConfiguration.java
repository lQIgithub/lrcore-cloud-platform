//package com.lrcore.auth.config;
//
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import javax.sql.DataSource;
//
///**
// * <p>类模块说明</p>
// *
// * @Describe: SSO 生产环境 JDBC 数据源装配（lrcore.auth.jdbc.enabled=true 时生效）。
// *            lrcore-auth 的启动类排除了 DataSourceAutoConfiguration（服务默认无数据库），
// *            开启 OAuth2 JDBC 持久化时由本配置按 Nacos 下发的 spring.datasource.* 显式建库连接：
// * <pre>
// * spring:
// *   datasource:
// *     url: jdbc:mysql://host:3306/lrcore_auth?preserveInstants=true&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
// *     username: xxx
// *     password: xxx
// *     driver-class-name: com.mysql.cj.jdbc.Driver
// * lrcore:
// *   auth:
// *     jdbc:
// *       enabled: true
// * </pre>
// * 表结构执行 lrcore-common-auth 的 sql/oauth2-server-mysql.sql（幂等）。
// * 注意：JDBC URL 必须携带 preserveInstants/connectionTimeZone 参数（官方强制，保证时间戳精确）。
// * @ClassName LrcoreSsoJdbcConfiguration
// * @Author lrcore
// * @Date 2026/8/21
// * @Version 1.0
// */
//@Configuration
//@ConditionalOnProperty(prefix = "lrcore.auth.jdbc", name = "enabled", havingValue = "true")
//public class LrcoreSsoJdbcConfiguration {
//
//    /**
//     * OAuth2 持久化数据源（HikariCP）。
//     */
//    @Bean(destroyMethod = "close")
//    @ConditionalOnMissingBean
//    public DataSource lrcoreSsoDataSource(
//            @Value("${spring.datasource.url}") String url,
//            @Value("${spring.datasource.username:}") String username,
//            @Value("${spring.datasource.password:}") String password,
//            @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}") String driverClassName) {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setJdbcUrl(url);
//        dataSource.setUsername(username);
//        dataSource.setPassword(password);
//        dataSource.setDriverClassName(driverClassName);
//        dataSource.setPoolName("lrcore-sso-pool");
//        dataSource.setMaximumPoolSize(10);
//        dataSource.setMinimumIdle(2);
//        return dataSource;
//    }
//
//    /**
//     * JDBC 操作模板（starter JDBC 装配依赖 JdbcOperations Bean）。
//     */
//    @Bean
//    @ConditionalOnMissingBean
//    public JdbcTemplate lrcoreSsoJdbcTemplate(DataSource lrcoreSsoDataSource) {
//        return new JdbcTemplate(lrcoreSsoDataSource);
//    }
//}
