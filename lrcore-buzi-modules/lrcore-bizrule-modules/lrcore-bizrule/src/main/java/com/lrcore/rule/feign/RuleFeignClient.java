package com.lrcore.rule.feign;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.lrcore.common.core.utils.FunCollectUtils;
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
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

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
@AllArgsConstructor
public class RuleFeignClient implements RemoteRuleApi {

    private final RuleRegistry ruleRegistry;
    private final ISysBizRuleService sysBizRuleService;

    @Qualifier("ruleRunExecutor")
    private ExecutorService ruleRunExecutor;

    // 本地缓存
    //各部分拆解
    //LoadingCache<String, List<SysBizRuleConfigEntity>>
    //LoadingCache：自带自动加载逻辑的缓存。
    //key：String，就是 bizCode（业务编码，例如 user_add、order_create）
    //value：List<SysBizRuleConfigEntity>，这个 bizCode 对应的、已经启用、按 sort_num 排好序的规则配置集合。

    //.expireAfterWrite(2, TimeUnit.SECONDS)
    //写入缓存之后 2 秒过期。
    //每一条缓存记录，存入内存开始计时，2 秒之后自动失效清除。

    //.build(key -> configMapper.selectByBizCode(key))
    //Lambda：缓存未命中 / 缓存过期的时候，自动执行这个逻辑去查数据库回填缓存。
    //key 就是传入的 bizCode
    //configMapper.selectByBizCode(key)：执行 Mybatis 查询，根据 bizCode 从数据库查出规则配置列表。
    private LoadingCache<String, List<SysBizRuleConfigEntity>> bizRuleCache;  // 去掉 final

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
                    // 提交隔离线程池执行动态规则逻辑，设置单条超时500ms
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

    /**
     * <p>方法说明</p>
     *
     * @Describe: 热更新完成后，清除配置缓存；这个方法由管理接口回调调用
     * @Param: []
     * @Author: Qi Liu
     * @Date: 2026/8/5 11:24
     * @Return void
     * @Version: 1.0
     */
    @Override
    public void clearCache() {
        bizRuleCache.invalidateAll();
        log.info("清除缓存成功");
    }
}
