package com.lrcore.gateway;

import com.lrcore.common.annotations.annotation.LrcoreCloudApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;


/**
 * <p>类模块说明</p>
 *
 * @Describe:
 * @ClassName: LrcoreGatewayApplication
 * @Author: Qi Liu
 * @Date: 2026/3/25 14:49
 * @Version: 1.0
 */
@LrcoreCloudApplication(exclude = {DataSourceAutoConfiguration.class})
public class LrcoreGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LrcoreGatewayApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  服务网关启动成功   ლ(´ڡ`ლ)ﾞ \n");
    }
}
