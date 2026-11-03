package com.lrcore.system.service;

import com.lrcore.system.domain.SysDictDataEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 字典 业务层
 * @ClassName: ISysDictDataService
 * @Author: Qi Liu
 * @Date: 2026/3/26 14:57
 * @Version: 1.0
 */
public interface ISysDictDataService extends IService<SysDictDataEntity> {

    /**
     * 批量更新字典项排序
     *
     * @param dictCode 字典编码
     * @param items    排序项
     * @return 是否更新成功
     */
    boolean updateSort(String dictCode, List<SortItem> items);

    record SortItem(Long id, Integer sort) {}
}
