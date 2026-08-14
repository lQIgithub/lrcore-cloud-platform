package com.lrcore.auth.form;

import com.lrcore.common.core.validators.AddGroup;
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
public class DbJasyptForm implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "服务器ip不能为空", groups = {AddGroup.class})
    private String ip;
    @NotEmpty(message = "服务器端口不能为空", groups = {AddGroup.class})
    private String port;
    @NotEmpty(message = "数据库不能为空", groups = {AddGroup.class})
    private String database;
    @NotEmpty(message = "密码不能为空", groups = {AddGroup.class})
    private String username;
    @NotEmpty(message = "密码不能为空", groups = {AddGroup.class})
    @Length(min = 8, max = 20, message = "密码长度必须介于 8 和 20 之间", groups = {AddGroup.class})
    private String password;


}
