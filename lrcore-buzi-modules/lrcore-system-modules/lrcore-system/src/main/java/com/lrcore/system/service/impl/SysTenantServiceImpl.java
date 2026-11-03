package com.lrcore.system.service.impl;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.system.domain.SysTenantEntity;
import com.lrcore.system.mapper.SysTenantMapper;
import com.lrcore.system.service.ISysTenantService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统租户 服务类
 * @ClassName: SysTenantServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/5/16 10:50
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenantEntity> implements ISysTenantService {

    /**
     * 批量更新租户排序
     *
     * @param items 排序项
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSort(List<SortItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        List<Long> ids = items.stream()
                .map(SortItem::id)
                .filter(Objects::nonNull)
                .toList();
        List<SysTenantEntity> tenants = this.listByIds(ids);
        if (tenants.size() != ids.size()) {
            throw new ServiceException("租户不存在");
        }

        for (SortItem item : items) {
            if (item.id() == null || item.sort() == null) {
                throw new ServiceException("租户排序参数不能为空");
            }

            SysTenantEntity tenant = new SysTenantEntity();
            tenant.setId(item.id());
            tenant.setSortOrder(item.sort());
            if (!updateById(tenant)) {
                return false;
            }
        }
        return true;
    }
}
