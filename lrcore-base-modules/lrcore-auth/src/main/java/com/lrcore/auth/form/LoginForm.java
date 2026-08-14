package com.lrcore.auth.form;

import com.lrcore.common.core.validators.OtherGroup;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 电脑端登录表单
 * @ClassName: LoginForm
 * @Author: Qi Liu
 * @Date: 2026/5/15 22:31
 * @Version: 1.0
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LoginForm implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 用户名
     */
    @NotEmpty(message = "用户名不能为空", groups = {OtherGroup.class})
    private String username;
    /**
     * 用户密码
     */
    @NotEmpty(message = "密码不能为空", groups = {OtherGroup.class})
    @Length(min = 8, max = 20, message = "密码长度必须介于 8 和 20 之间", groups = {OtherGroup.class})
    private String password;

}
