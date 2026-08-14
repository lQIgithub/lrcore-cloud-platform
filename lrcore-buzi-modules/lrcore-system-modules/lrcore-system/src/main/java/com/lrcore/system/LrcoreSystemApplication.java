package com.lrcore.system;

import com.lrcore.common.annotations.annotation.LrcoreCloudApplication;
import org.springframework.boot.SpringApplication;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统模块启动类
 * @ClassName: LrcoreSystemApplication
 * @Author: Qi Liu
 * @Date: 2026/05/24 10:07
 * @Version: 1.0
 */
@LrcoreCloudApplication
public class LrcoreSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(LrcoreSystemApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  系统模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
