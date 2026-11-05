package com.lrcore.rule.feign;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.lrcore.common.rule.RemoteRuleApi;
import com.lrcore.common.rule.dto.RuleExecuteReq;
import com.lrcore.common.rule.dto.RuleExecuteResp;
import com.lrcore.common.rule.enums.RuleErrorTypeEnum;
import com.lrcore.common.rule.exceptions.ValidationException;
import com.lrcore.common.rule.interfaces.ValidateRule;
import com.lrcore.rule.domain.SysBizRuleConfigEntity;
import com.lrcore.rule.domain.apt.SysBizRuleConfigAPT;
import com.lrcore.rule.registry.RuleRegistry;
import com.lrcore.rule.service.ISysBizRuleService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 规则管理 控制器
 * @ClassName: RuleFeignClient
 * @Author: Qi Liu
 * @Date: 2026/4/1 12:59
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RuleFeignClient implements RemoteRuleApi {

    private final RuleRegistry ruleRegistry;
    private final ISysBizRuleService sysBizRuleService;

    @Qualifier("ruleRunExecutor")
    private final ExecutorService ruleRunExecutor;

    private LoadingCache<String, List<SysBizRuleConfigEntity>> bizRuleCache;

    @PostConstruct
    public void init() {
        this.bizRuleCache = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .build(bizCode -> {
                    QueryWrapper queryWrapper = QueryWrapper.create()
                            .where(SysBizRuleConfigAPT.SYS_BIZ_RULE_CONFIG.BIZ_CODE.eq(bizCode))
                            .orderBy(SysBizRuleConfigAPT.SYS_BIZ_RULE_CONFIG.SORT_NUM.asc());
                    return sysBizRuleService.list(queryWrapper);
                });
    }

    @Override
    public RuleExecuteResp executeRule(RuleExecuteReq ruleExecuteReq) {
        // @formatter::off
        RuleExecuteResp ruleExecuteResp = new RuleExecuteResp();
        try {
            log.info("开始执行规则：{}", ruleExecuteReq);
            String bizCode = ruleExecuteReq.getBizCode();
            List<SysBizRuleConfigEntity> sysBizRuleConfigEntityList = bizRuleCache.get(bizCode);
            log.info("数据库查询到规则列表，进行下一步规则验证...");
            for (SysBizRuleConfigEntity sysBizRuleConfigEntity : sysBizRuleConfigEntityList) {
                String ruleCode = sysBizRuleConfigEntity.getRuleCode();
                log.info("开始执行规则：{}", ruleCode);
                ValidateRule validateRule = ruleRegistry.getRule(ruleCode);
                if (Objects.isNull(validateRule)) {
                    log.info("规则 {} 不存在", ruleCode);
                    ruleExecuteResp.setPass(Boolean.FALSE);
                    ruleExecuteResp.setErrorType(RuleErrorTypeEnum.BIZ_ERROR);
                    ruleExecuteResp.setMsg(ruleCode + "规则不存在");
                    return ruleExecuteResp;
                }
                String ruleName = validateRule.getRuleName();
                try {
                    Future<Void> future = ruleRunExecutor.submit(() -> {
                        log.info("执行规则：{}", ruleName);
                        validateRule.validate(ruleExecuteReq.getContext());
                        log.info("规则 {} 执行成功", ruleName);
                        return null;
                    });
                    future.get(500, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    ruleExecuteResp.setPass(false);
                    ruleExecuteResp.setMsg("规则执行超时:" + sysBizRuleConfigEntity.getRuleCode());
                    ruleExecuteResp.setFailRuleCode(sysBizRuleConfigEntity.getRuleCode());
                    return ruleExecuteResp;
                } catch (ExecutionException e) {
                    Throwable realEx = e.getCause();
                    if (realEx instanceof ValidationException) {
                        ruleExecuteResp.setPass(false);
                        ruleExecuteResp.setMsg(realEx.getMessage());
                        ruleExecuteResp.setFailRuleCode(sysBizRuleConfigEntity.getRuleCode());
                        return ruleExecuteResp;
                    } else {
                        log.error("规则执行异常 {}", sysBizRuleConfigEntity.getRuleCode(), realEx);
                        ruleExecuteResp.setPass(false);
                        ruleExecuteResp.setMsg("规则内部执行异常");
                        ruleExecuteResp.setFailRuleCode(sysBizRuleConfigEntity.getRuleCode());
                        return ruleExecuteResp;
                    }
                } catch (Exception e) {
                    log.info("规则 {} 执行异常", ruleName, e);
                    ruleExecuteResp.setPass(Boolean.FALSE);
                    ruleExecuteResp.setErrorType(RuleErrorTypeEnum.EXCEP_ERROR);
                    ruleExecuteResp.setMsg(e.getMessage());
                    ruleExecuteResp.setFailRuleCode(sysBizRuleConfigEntity.getRuleCode());
                    return ruleExecuteResp;
                }
            }
            ruleExecuteResp.setPass(Boolean.TRUE);
            ruleExecuteResp.setMsg("规则执行成功");
            return ruleExecuteResp;
        } catch (Exception e) {
            ruleExecuteResp.setPass(Boolean.FALSE);
            ruleExecuteResp.setErrorType(RuleErrorTypeEnum.EXCEP_ERROR);
            ruleExecuteResp.setMsg(e.getMessage());
            return ruleExecuteResp;
        }
        // @formatter::on
    }

    @Override
    public void clearCache() {
        bizRuleCache.invalidateAll();
        log.info("清除缓存成功");
    }
}
