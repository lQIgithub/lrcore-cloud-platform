package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.enums.SexEnum;
import com.lrcore.common.core.enums.UserStatusEnum;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>用户基础信息表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理系统中的所有用户信息</li>
 *   <li>支持多租户场景</li>
 *   <li>支持用户归属企业和部门</li>
 *   <li>记录用户登录信息</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_user")
@Schema(description = "用户基础信息实体")
public class SysUserEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账号")
    @NotBlank(message = "账号不能为空")
    @Size(max = 64, message = "账号长度不能超过64个字符")
    private String username;

    @Schema(description = "密码（SHA-256加密存储）")
    @NotBlank(message = "密码不能为空")
    @Size(max = 256, message = "密码长度不能超过256个字符")
    private String password;

    @Schema(description = "姓名")
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "用户昵称")
    @Size(max = 50, message = "用户昵称长度不能超过50个字符")
    private String nickName;

    @Schema(description = "手机号码")
    @Size(max = 20, message = "手机号码长度不能超过20个字符")
    private String phone;

    @Schema(description = "电子邮箱")
    @Size(max = 100, message = "电子邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "用户性别（1-男 2-女 3-未知）")
    private SexEnum sex;

    @Schema(description = "头像")
    @Size(max = 100, message = "头像长度不能超过100个字符")
    private String avatar;

    @Schema(description = "所属企业ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long enterpriseId;

    @Schema(description = "所属部门ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long deptId;

    @Schema(description = "用户状态（0启用 1禁用 2锁定）")
    private UserStatusEnum status;

    @Schema(description = "最后登录IP")
    @Size(max = 50, message = "最后登录IP长度不能超过50个字符")
    private String lastLoginIp;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

}
