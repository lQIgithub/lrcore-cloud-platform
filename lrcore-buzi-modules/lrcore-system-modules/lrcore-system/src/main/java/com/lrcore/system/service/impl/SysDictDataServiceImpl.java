package com.lrcore.system.service.impl;

import com.lrcore.system.domain.SysDictDataEntity;
import com.lrcore.system.mapper.SysDictDataMapper;
import com.lrcore.system.service.ISysDictDataService;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.system.domain.apt.SysDictDataAPT;
import com.lrcore.system.service.ISysDictDataService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 字典 业务层处理
 *
 * @author lrcore
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictDataEntity> implements ISysDictDataService {

    /**
     * 批量更新字典项排序
     *
     * @param dictCode 字典编码
     * @param items    排序项
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSort(String dictCode, List<SortItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }

        List<Long> ids = items.stream()
                .map(SortItem::id)
                .filter(Objects::nonNull)
                .toList();
        List<SysDictDataEntity> dataList = mapper.selectListByIds(ids);
        if (dataList.size() != ids.size()
                || dataList.stream().anyMatch(item -> !Objects.equals(item.getDictType(), dictCode))) {
            throw new ServiceException("字典数据不存在");
        }

        for (SortItem item : items) {
            if (item.id() == null || item.sort() == null) {
                throw new ServiceException("字典数据排序参数不能为空");
            }

            SysDictDataEntity entity = new SysDictDataEntity();
            entity.setId(item.id());
            entity.setDictSort(String.valueOf(item.sort()));
            if (!updateById(entity)) {
                return false;
            }
        }
        return true;
    }
}
