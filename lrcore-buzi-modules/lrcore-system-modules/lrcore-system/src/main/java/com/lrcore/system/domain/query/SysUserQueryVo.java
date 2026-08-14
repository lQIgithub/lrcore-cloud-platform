package com.lrcore.system.domain.query;

import com.lrcore.common.core.web.domain.BaseQueryVo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * <>类模块说明</p>
 *
 * @Describe: 用户查询参数
 * @ClassName: SysUserQueryVo
 * @Author: Qi Liu
 * @Date: 2026/4/8 22:17
 * @Version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true) // 继承父类属性参与比较
@ToString(callSuper = true)          // 父类属性也打印
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户查询参数")
public class SysUserQueryVo extends BaseQueryVo {

    // 这里添加额外的查询参数
    @Schema(description = "用户名称")
    @NotEmpty
    private String username;
}
