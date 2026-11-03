package com.lrcore.system.service;


import com.lrcore.system.domain.SysTenantEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 系统租户表 服务层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
public interface ISysTenantService extends IService<SysTenantEntity> {

    /**
     * 批量更新租户排序
     *
     * @param items 排序项
     * @return 是否更新成功
     */
    boolean updateSort(List<SortItem> items);

    record SortItem(Long id, Integer sort) {}

}
