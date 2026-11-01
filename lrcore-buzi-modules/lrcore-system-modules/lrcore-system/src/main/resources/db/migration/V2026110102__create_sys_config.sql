CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '配置值',
    `config_type` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
    `tenant_id` BIGINT NULL COMMENT '租户ID',
    `create_user_id` BIGINT NULL COMMENT '创建者ID',
    `update_user_id` BIGINT NULL COMMENT '更新者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0未删除 1已删除）',
    INDEX `idx_config_tenant_id` (`tenant_id` ASC) USING BTREE,
    INDEX `idx_config_deleted` (`deleted` ASC) USING BTREE,
    INDEX `idx_config_tenant_key` (`tenant_id` ASC, `config_key` ASC, `deleted` ASC) USING BTREE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic
  COMMENT = '系统参数配置表';
