package com.lrcore.system.service.impl;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.web.domain.login.SysEnterpriseInfo;
import com.lrcore.system.domain.SysEnterpriseEntity;
import com.lrcore.system.domain.apt.SysEnterpriseAPT;
import com.lrcore.system.domain.apt.SysUserEnterpriseAPT;
import com.lrcore.system.mapper.SysEnterpriseMapper;
import com.lrcore.system.service.ISysEnterpriseService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 企业信息服务实现
 *
 * @author lrcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysEnterpriseServiceImpl extends ServiceImpl<SysEnterpriseMapper, SysEnterpriseEntity> implements ISysEnterpriseService {

    @Override
    public List<SysEnterpriseInfo> getSysEnterpriseInfoList(Long userId) {
        log.info("获取用户[{}]所有管理企业信息列表", userId);

        if (Objects.isNull(userId)) {
            log.warn("用户ID为空，无法查询企业信息");
            return Collections.emptyList();
        }

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select(SysEnterpriseAPT.SYS_ENTERPRISE.DEFAULT_COLUMNS)
                    .from(SysEnterpriseAPT.SYS_ENTERPRISE)
                    .innerJoin(SysUserEnterpriseAPT.SYS_USER_ENTERPRISE)
                    .on(SysEnterpriseAPT.SYS_ENTERPRISE.ID.eq(SysUserEnterpriseAPT.SYS_USER_ENTERPRISE.ENTERPRISE_ID))
                    .where(SysUserEnterpriseAPT.SYS_USER_ENTERPRISE.USER_ID.eq(userId));

            List<SysEnterpriseInfo> result = mapper.selectListByQueryAs(queryWrapper, SysEnterpriseInfo.class);
            if (result != null) {
                log.info("查询到用户[{}]的企业信息数量: {}", userId, result.size());
            }
            return result;

        } catch (Exception e) {
            log.error("查询用户[{}]企业信息失败: {}", userId, e.getMessage(), e);
            throw new ServiceException("查询企业信息失败: " + e.getMessage());
        }
    }

    @Override
    public List<SysEnterpriseEntity> selectEnterpriseTree(Long enterpriseId) {
        log.info("查询企业树形结构，企业ID: {}", enterpriseId);

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .orderBy(SysEnterpriseEntity::getEntName, true);

            if (enterpriseId != null) {
                queryWrapper.where(SysEnterpriseEntity::getParentId).eq(enterpriseId);
            }

            List<SysEnterpriseEntity> enterprises = this.list(queryWrapper);
            return buildEnterpriseTree(enterprises, 0L);

        } catch (Exception e) {
            log.error("查询企业树形结构失败: {}", e.getMessage(), e);
            throw new ServiceException("查询企业树形结构失败: " + e.getMessage());
        }
    }

    /**
     * 构建企业树形结构
     *
     * @param enterprises 所有企业列表
     * @param parentId    父企业ID
     * @return 企业树列表
     */
    private List<SysEnterpriseEntity> buildEnterpriseTree(List<SysEnterpriseEntity> enterprises, Long parentId) {
        List<SysEnterpriseEntity> tree = new ArrayList<>();

        for (SysEnterpriseEntity enterprise : enterprises) {
            if (Objects.equals(parentId, enterprise.getParentId())) {
                tree.add(enterprise);
            }
        }

        return tree;
    }
}
