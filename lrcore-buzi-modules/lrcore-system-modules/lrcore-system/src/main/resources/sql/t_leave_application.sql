-- =====================================================================
-- 请假申请表 t_leave_application
-- 业务：公司请假流程（申请提交 -> 审批 -> 状态跟踪 -> 统计）
-- 关联：business_key / process_instance_id 与 Flowable 流程实例关联
-- =====================================================================
CREATE TABLE IF NOT EXISTS `t_leave_application` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花ID）',
  `business_key` VARCHAR(64) DEFAULT NULL COMMENT '业务主键（流程 businessKey）',
  `process_instance_id` VARCHAR(64) DEFAULT NULL COMMENT '流程实例ID（启动后回写）',
  `apply_user_id` BIGINT DEFAULT NULL COMMENT '申请人ID',
  `apply_user_name` VARCHAR(64) DEFAULT NULL COMMENT '申请人姓名',
  `leave_type` TINYINT DEFAULT NULL COMMENT '请假类型：1事假 2病假 3年假 4调休 5其他',
  `start_date` DATE DEFAULT NULL COMMENT '开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期',
  `days` DECIMAL(5,1) DEFAULT NULL COMMENT '请假天数',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '请假事由',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待审批 approved已通过 rejected已驳回 completed已通过流程进行中 cancelled已撤销',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新者ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0未删除 1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_leave_business_key` (`business_key`),
  KEY `idx_leave_process_instance_id` (`process_instance_id`),
  KEY `idx_leave_apply_user_id` (`apply_user_id`),
  KEY `idx_leave_status` (`status`),
  KEY `idx_leave_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假申请表';
