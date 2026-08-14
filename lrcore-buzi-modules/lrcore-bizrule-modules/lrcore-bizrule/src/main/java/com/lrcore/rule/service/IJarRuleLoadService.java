package com.lrcore.rule.service;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.rule.domain.model.RuleDto;

import java.util.List;

/**
 * <>类模块说明</p>
 *
 * @Describe: 规则加载服务
 * @ClassName: IJarRuleLoadService
 * @Author: Qi Liu
 * @Date: 2026/8/4 09:20
 * @Version: 1.0
 */
public interface IJarRuleLoadService {

    /**
     * <p>方法说明</p>
     *
     * @Describe: 扫描并加载规则
     * @Param: []
     * @Author: Qi Liu
     * @Date: 2026/8/4 11:46
     * @Return com.lrcore.common.core.web.domain.ApiResult<java.lang.Boolean>
     * @Version: 1.0
     */
    void scanAndLoadJarRule();

    /**
     * <p>方法说明</p>
     *
     * @Describe: 获取规则列表
     * @Param: []
     * @Author: Qi Liu
     * @Date: 2026/8/4 11:48
     * @Return com.lrcore.common.core.web.domain.ApiResult<java.util.List<com.lrcore.rule.model.RuleDto>>
     * @Version: 1.0
     */
    ApiResult<List<RuleDto>> getRuleDtoList();
}
