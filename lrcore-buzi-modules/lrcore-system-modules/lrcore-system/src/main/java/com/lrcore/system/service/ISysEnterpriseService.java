package com.lrcore.system.service;

import com.lrcore.common.core.web.domain.login.SysEnterpriseInfo;
import com.lrcore.system.domain.SysEnterpriseEntity;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 企业信息表 服务层
 *
 * @author lrcore
 */
public interface ISysEnterpriseService extends IService<SysEnterpriseEntity> {

    /**
     * 获取当前用户所属企业信息列表
     *
     * @param userId 用户ID
     * @return 企业信息列表
     */
    List<SysEnterpriseInfo> getSysEnterpriseInfoList(Long userId);

    /**
     * 查询企业树形结构
     *
     * @param enterpriseId 企业ID（为空时查询所有）
     * @return 企业树列表
     */
    List<SysEnterpriseEntity> selectEnterpriseTree(Long enterpriseId);
}
