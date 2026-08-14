package com.lrcore.rule.domain;

import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;


/**
 * <p>类模块说明</p>
 *
 * @Describe: 业务执行规则配置
 * @ClassName: SysBizRuleConfigEntity
 * @Author: Qi Liu
 * @Date: 2026/8/4 13:25
 * @Version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_biz_rule_config")
@Schema(description = "业务执行规则配置")
public class SysBizRuleConfigEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务编码")
    @NotBlank(message = "业务编码不能为空")
    @Size(max = 20, message = "业务编码长度不能超过20个字符")
    private String bizCode;

    @Schema(description = "规则编码")
    @NotBlank(message = "规则编码不能为空")
    @Size(max = 20, message = "规则编码长度不能超过20个字符")
    private String ruleCode;

    @Schema(description = "规则执行排序，越小越优先执行")
    private Integer sortNum = 0;

    @Schema(description = "1-启用； 0-禁用")
    private Integer status = 0;
}
