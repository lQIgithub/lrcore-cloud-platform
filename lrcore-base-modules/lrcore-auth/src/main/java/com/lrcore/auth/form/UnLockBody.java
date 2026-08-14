package com.lrcore.auth.form;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统解锁对象
 * @ClassName: UnLockBody
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:57
 * @Version: 1.0
 */
public class UnLockBody {
    /**
     * 用户密码
     */
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
