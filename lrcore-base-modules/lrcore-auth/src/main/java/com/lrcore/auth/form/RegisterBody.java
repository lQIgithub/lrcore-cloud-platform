package com.lrcore.auth.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 用户注册对象
 * @ClassName: RegisterBody
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:57
 * @Version: 1.0
 */
@Schema(description = "用户注册对象")
@Data
@EqualsAndHashCode(callSuper = true) // 继承父类属性参与比较
@ToString(callSuper = true)          // 父类属性也打印
@Accessors(chain = true)
public class RegisterBody extends LoginForm {

}
