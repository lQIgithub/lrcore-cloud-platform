package com.lrcore.rule;


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
public class LrcoreRuleApplication {
    public static void main(String[] args) {
        SpringApplication.run(LrcoreRuleApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  规则管理模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
