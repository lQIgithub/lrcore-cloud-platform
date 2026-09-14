package com.lrcore.ai;


import com.lrcore.common.annotations.annotation.LrcoreCloudApplication;
import org.springframework.boot.SpringApplication;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 规则管理启动类
 * @ClassName: LrcoreRuleApplication
 * @Author: Qi Liu
 * @Date: 2026/8/4 16:29
 * @Version: 1.0
 */
@LrcoreCloudApplication
public class LrcoreAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LrcoreAiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  AI管理模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
