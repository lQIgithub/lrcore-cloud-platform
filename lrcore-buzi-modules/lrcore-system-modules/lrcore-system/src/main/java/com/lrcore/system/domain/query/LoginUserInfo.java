package com.lrcore.system.domain.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 登录用户信息
 * @ClassName: LoginUserInfo
 * @Author: Qi Liu
 * @Date: 2026/3/26 15:23
 * @Version: 1.0
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LoginUserInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    //    private SysUserDto sysUserDto;
    private Set<String> roles;
    private Set<String> permissions;
    private boolean isDefaultModifyPwd;
    private boolean isPasswordExpired;
}
