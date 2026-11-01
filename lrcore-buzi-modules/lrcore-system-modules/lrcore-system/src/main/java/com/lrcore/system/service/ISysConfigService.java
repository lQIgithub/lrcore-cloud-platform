package com.lrcore.system.service;

import com.lrcore.system.domain.SysConfigEntity;
import com.mybatisflex.core.service.IService;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 参数配置 服务层
 * @ClassName: ISysConfigService
 * @Author: Qi Liu
 * @Date: 2026/3/26 14:49
 * @Version: 1.0
 */
public interface ISysConfigService extends IService<SysConfigEntity> {
    /**
     * 根据配置键获取配置
     *
     * @param configKey 配置键
     * @return 配置实体
     */
    SysConfigEntity getByConfigKey(String configKey);

    /**
     * 刷新配置缓存
     *
     * @return 是否刷新成功
     */
    boolean refreshCache();
}
