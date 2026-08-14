package com.lrcore.auth;

import com.lrcore.common.annotations.annotation.LrcoreCloudApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 认证授权中心
 * @ClassName: LrcoreAuthApplication
 * @Author: Qi Liu
 * @Date: 2026/3/25 20:38
 * @Version: 1.0
 */
@LrcoreCloudApplication(exclude = {DataSourceAutoConfiguration.class})
public class LrcoreAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(LrcoreAuthApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  认证授权中心启动成功   ლ(´ڡ`ლ)ﾞ \n");
    }
}
