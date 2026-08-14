package com.lrcore.rule.plugin;

import com.lrcore.common.rule.context.ValidateContext;
import com.lrcore.common.rule.exceptions.ValidationException;
import com.lrcore.common.rule.interfaces.ValidateRule;

/**
 * <>类模块说明</p>
 *
 * @Describe: 验证实现类， 实现扩展接口ValidateRule
 * 这是个案例
 * @ClassName: AmountValidate
 * @Author: Qi Liu
 * @Date: 2026/8/5 9:56
 * @Version: 1.0
 */
public class AmountValidate implements ValidateRule {
    @Override
    public String getRuleCode() {
        return "";
    }

    @Override
    public String getRuleName() {
        return "";
    }

    @Override
    public void validate(ValidateContext context) throws ValidationException {

    }
}
