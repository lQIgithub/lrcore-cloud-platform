package com.lrcore.rule.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <>类模块说明</p>
 *
 * @Describe: 返回规则信息
 * @ClassName: RuleDto
 * @Author: Qi Liu
 * @Date: 2026/8/4 11:46
 * @Version: 1.0
 */
@Data
@Schema(description = "规则信息")
public class RuleDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(description = "规则编码")
    private String ruleCode;
    @Schema(description = "规则名称")
    private String ruleName;
}
