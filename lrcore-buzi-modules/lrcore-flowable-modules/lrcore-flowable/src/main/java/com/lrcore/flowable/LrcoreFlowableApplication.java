package com.lrcore.flowable;

import com.lrcore.common.annotations.annotation.LrcoreCloudApplication;
import org.springframework.boot.SpringApplication;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 工作流模块启动类
 * @ClassName: LrcoreFlowableApplication
 * @Author: Qi Liu
 * @Date: 2026/08/10 10:07
 * @Version: 1.0
 */
@LrcoreCloudApplication
public class LrcoreFlowableApplication {
    public static void main(String[] args) {
        SpringApplication.run(LrcoreFlowableApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  工作流模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
