package com.lrcore.rule.service.impl;

import com.lrcore.common.core.utils.FunCollectUtils;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.rule.interfaces.ValidateRule;
import com.lrcore.rule.classload.RuleClassLoader;
import com.lrcore.rule.domain.model.RuleDto;
import com.lrcore.rule.registry.RuleRegistry;
import com.lrcore.rule.service.IJarRuleLoadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <>类模块说明</p>
 *
 * @Describe: jar规则加载服务实现类
 * @ClassName: JarRuleLoadServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/8/4 09:21
 * @Version: 1.0
 */
@Slf4j
@Service
@RefreshScope
@RequiredArgsConstructor
public class JarRuleLoadServiceImpl implements IJarRuleLoadService {

    @Value("${plugin.jar.path}")
    private String jarPath;

    private final RuleRegistry ruleRegistry;

    // 缓存jar文件名与md5，避免重复加载
    private final ConcurrentHashMap<String, String> jarMd5Cache = new ConcurrentHashMap<>();

    @Override
    public void scanAndLoadJarRule() {
        // 判断jarPath是否为空
        if (FunStrUtils.isEmpty(jarPath)) {
            log.warn("jarPath: {} 未配置", jarPath);
            return;
        }
        File dir = new File(jarPath);
        if (!dir.exists()) {
            log.warn("jarPath: {} 不存在", jarPath);
            return;
        }
        File[] jarFiles = dir.listFiles(f -> f.getName().endsWith(".jar"));
        if (FunCollectUtils.isEmpty(jarFiles)) {
            log.warn("jarPath: {} 下没有jar文件", jarPath);
            return;
        }
        for (File jarFile : jarFiles) {
            try {
                if (!jarIsModified(jarFile)) {
                    continue;
                }
                loadJarRule(jarFile);
            } catch (Exception e) {
                log.error("处理jar异常 {}", jarFile.getName(), e);
            }
        }
        log.info("本次扫描加载完成，总规则数：{}", ruleRegistry.list().size());
    }


    @Override
    public ApiResult<List<RuleDto>> getRuleDtoList() {
        ConcurrentHashMap<String, ValidateRule> hash = ruleRegistry.list();
        if (!hash.isEmpty()) {
            List<RuleDto> ruleDtoList = hash.keySet().stream().map(ruleCode -> {
                ValidateRule validateRule = hash.get(ruleCode);
                RuleDto ruleDto = new RuleDto();
                ruleDto.setRuleCode(validateRule.getRuleCode());
                ruleDto.setRuleName(validateRule.getRuleName());
                return ruleDto;
            }).toList();
            return ApiResult.success(ruleDtoList);
        }
        return ApiResult.success();
    }


    private boolean jarIsModified(File jarFile) throws IOException {
        log.info("判断文件：{} 是否已经修改", jarFile.getName());
        String md5 = getFileMD5(jarFile);
        String old = jarMd5Cache.get(jarFile.getName());
        if (md5.equals(old)) {
            log.info("文件：{} 未修改", jarFile.getName());
            return false;
        }
        log.info("文件：{} 修改了", jarFile.getName());
        jarMd5Cache.put(jarFile.getName(), md5);
        return true;
    }

    private String getFileMD5(File file) throws IOException {
        // 实现文件md5计算
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        }
    }
    private void loadJarRule(File jarFile) {
        log.info("开始加载jar文件：{}", jarFile.getName());
        try (JarFile jar = new JarFile(jarFile)) { // try-with-resources 自动关闭流，防止异常
            try (RuleClassLoader ruleClassLoader = RuleClassLoader.create(jarFile.toPath(), this.getClass().getClassLoader());) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String name = jarEntry.getName();
                    log.info("正在类文件加载：{}", name);
                    if (!name.endsWith(".class")) {
                        continue;
                    }
                    String className = name.replace(".class", "").replace("/", ".");
                    log.info("正在加载类：{}", className);
                    Class<?> aClass = ruleClassLoader.loadClass(className);
                    if (ValidateRule.class.isAssignableFrom(aClass) && !aClass.isInterface()) {
                        ValidateRule rule = (ValidateRule) aClass.getDeclaredConstructor().newInstance();
                        ruleRegistry.registry(rule);
                        log.info("注册新规则 code={},name={}", rule.getRuleCode(), rule.getRuleName());
                    }
                }
            } catch (Exception e) {
                log.error("class文件加载异常：{}", jarFile.getName(), e);
            }

        } catch (Exception e) {
            log.error("读取jar文件失败：{}", jarFile.getName(), e);
        }

    }
}
