package com.lrcore.rule.registry;

import com.lrcore.common.rule.interfaces.ValidateRule;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <>类模块说明</p>
 *
 * @Describe:
 * @ClassName: RuleRegistry
 * @Author: Qi Liu
 * @Date: 2026/8/4 09:11
 * @Version: 1.0
 */
@Component
public class RuleRegistry {
    /**
     * 规则缓存
     */
    private final ConcurrentHashMap<String, ValidateRule> ruleMap = new ConcurrentHashMap<>();

    /**
     * <p>方法说明</p>
     *
     * @Describe: 注册规则 只新增，不覆盖，彻底解决旧对象无法替换的问题
     * @Param: [rule: 规则]
     * @Author: Qi Liu
     * @Date: 2026/8/4 09:16
     * @Return void
     * @Version: 1.0
     */
    public void registry(ValidateRule rule) {
        ruleMap.putIfAbsent(rule.getRuleCode(), rule);
    }

    /**
     * <p>方法说明</p>
     *
     * @Describe: 根据规则编码获取具体的规则
     * @Param: [ruleCode: 规则编码]
     * @Author: Qi Liu
     * @Date: 2026/8/4 09:17
     * @Return com.lrcore.common.rule.interfaces.ValidateRule
     * @Version: 1.0
     */
    public ValidateRule getRule(String ruleCode) {
        return ruleMap.get(ruleCode);
    }

    /**
     * <p>方法说明</p>
     *
     * @Describe: 获取所有规则列表
     * @Param: []
     * @Author: Qi Liu
     * @Date: 2026/8/4 09:18
     * @Return java.util.concurrent.ConcurrentHashMap<java.lang.String, ValidateRule>
     * @Version: 1.0
     */
    public ConcurrentHashMap<String, ValidateRule> list() {
        return ruleMap;
    }

    /**
     * <p>方法说明</p>
     *
     * @Describe: 清空规则列表
     * @Param: []
     * @Author: Qi Liu
     * @Date: 2026/8/4 09:18
     * @Return void
     * @Version: 1.0
     */
    public void clear() {
        ruleMap.clear();
    }
}
