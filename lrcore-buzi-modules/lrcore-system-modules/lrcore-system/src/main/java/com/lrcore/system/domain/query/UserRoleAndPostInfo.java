package com.lrcore.system.domain.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 路由显示信息
 * @ClassName: UserRoleAndPostInfo
 * @Author: Qi Liu
 * @Date: 2026/3/26 15:26
 * @Version: 1.0
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleAndPostInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //    private SysUserDto sysUserDto;
    private String roleGroup;
    private String postGroup;

}
