package com.lrcore.system.service.impl;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.system.domain.LeaveApplicationEntity;
import com.lrcore.system.domain.apt.LeaveApplicationAPT;
import com.lrcore.system.enums.LeaveStatus;
import com.lrcore.system.mapper.LeaveApplicationMapper;
import com.lrcore.system.service.ILeaveApplicationService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 请假申请 服务层实现
 * @ClassName: LeaveApplicationServiceImpl
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 */
@Slf4j
@Service
public class LeaveApplicationServiceImpl extends ServiceImpl<LeaveApplicationMapper, LeaveApplicationEntity> implements ILeaveApplicationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LeaveApplicationEntity buildAndSave(String businessKey, Long applyUserId, Map<String, Object> variables) {
        if (!FunStrUtils.hasText(businessKey)) {
            throw new ServiceException("业务主键 businessKey 不能为空");
        }
        LeaveApplicationEntity entity = new LeaveApplicationEntity();
        entity.setBusinessKey(businessKey);
        entity.setApplyUserId(applyUserId);
        entity.setApplyUserName(SecurityUtils.getUsername());
        entity.setLeaveType(toInteger(variables, "leaveType"));
        entity.setStartDate(toLocalDate(variables, "startDate"));
        entity.setEndDate(toLocalDate(variables, "endDate"));
        entity.setDays(toBigDecimal(variables, "days"));
        entity.setReason(toStr(variables, "reason"));
        entity.setStatus(LeaveStatus.pending);
        entity.setDeleted(0);
        entity.setTenantId(SecurityUtils.getTenantId());
        entity.setCreateUserId(applyUserId);
        save(entity);
        log.info("请假申请落库成功 businessKey={}, id={}", businessKey, entity.getId());
        return entity;
    }

    @Override
    public void bindProcessInstance(String businessKey, String processInstanceId) {
        LeaveApplicationAPT table = LeaveApplicationAPT.LEAVE_APPLICATION;
        LeaveApplicationEntity update = new LeaveApplicationEntity();
        update.setProcessInstanceId(processInstanceId);
        update.setUpdateTime(LocalDateTime.now());
        mapper.updateByQuery(update, QueryWrapper.create()
                .where(table.BUSINESS_KEY.eq(businessKey))
                .and(table.DELETED.eq(0)));
    }

    @Override
    public void markStatusByInstance(String processInstanceId, LeaveStatus status) {
        LeaveApplicationAPT table = LeaveApplicationAPT.LEAVE_APPLICATION;
        LeaveApplicationEntity update = new LeaveApplicationEntity();
        update.setStatus(status);
        update.setUpdateTime(LocalDateTime.now());
        mapper.updateByQuery(update, QueryWrapper.create()
                .where(table.PROCESS_INSTANCE_ID.eq(processInstanceId))
                .and(table.DELETED.eq(0)));
    }

    @Override
    public LeaveApplicationEntity getByBusinessKey(String businessKey) {
        LeaveApplicationAPT table = LeaveApplicationAPT.LEAVE_APPLICATION;
        return mapper.selectOneByQuery(QueryWrapper.create()
                .where(table.BUSINESS_KEY.eq(businessKey))
                .and(table.DELETED.eq(0)));
    }

    @Override
    public List<LeaveApplicationEntity> listByApplyUser(Long applyUserId) {
        LeaveApplicationAPT table = LeaveApplicationAPT.LEAVE_APPLICATION;
        return mapper.selectListByQuery(QueryWrapper.create()
                .where(table.APPLY_USER_ID.eq(applyUserId))
                .and(table.DELETED.eq(0))
                .orderBy(table.CREATE_TIME.desc(), table.ID.desc()));
    }

    /**
     * 从流程变量中安全读取字符串（对象直接 toString，空返回 null）
     */
    private String toStr(Map<String, Object> variables, String key) {
        Object value = variables == null ? null : variables.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 从流程变量中安全读取整数（兼容 Number / 字符串）
     */
    private Integer toInteger(Map<String, Object> variables, String key) {
        Object value = variables == null ? null : variables.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String str = String.valueOf(value);
        return FunStrUtils.hasText(str) ? Integer.valueOf(str) : null;
    }

    /**
     * 从流程变量中安全读取 BigDecimal（兼容 Number / 字符串）
     */
    private BigDecimal toBigDecimal(Map<String, Object> variables, String key) {
        Object value = variables == null ? null : variables.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String str = String.valueOf(value);
        return FunStrUtils.hasText(str) ? new BigDecimal(str) : null;
    }

    /**
     * 从流程变量中安全读取日期（兼容 LocalDate / "yyyy-MM-dd" 字符串）
     */
    private LocalDate toLocalDate(Map<String, Object> variables, String key) {
        Object value = variables == null ? null : variables.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        String str = String.valueOf(value);
        return FunStrUtils.hasText(str) ? LocalDate.parse(str, DATE_FORMATTER) : null;
    }

}
