CREATE TABLE IF NOT EXISTS `sys_dict_type` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `tenant_id` BIGINT NULL COMMENT '租户ID',
    `create_user_id` BIGINT NULL COMMENT '创建者ID',
    `update_user_id` BIGINT NULL COMMENT '更新者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0未删除 1已删除）',
    INDEX `idx_dict_type_tenant_id` (`tenant_id` ASC) USING BTREE,
    INDEX `idx_dict_type_deleted` (`deleted` ASC) USING BTREE,
    INDEX `idx_dict_type_status` (`status` ASC) USING BTREE,
    UNIQUE INDEX `uk_dict_type_tenant_code` (`tenant_id` ASC, `dict_type` ASC, `deleted` ASC) USING BTREE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic
  COMMENT = '字典类型表';

CREATE TABLE IF NOT EXISTS `sys_dict_data` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `dict_code` VARCHAR(100) NULL COMMENT '字典数据编码',
    `dict_sort` VARCHAR(20) NOT NULL DEFAULT '0' COMMENT '字典排序',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '关联字典类型',
    `css_class` VARCHAR(100) NULL COMMENT '样式属性',
    `list_class` VARCHAR(100) NULL COMMENT '表格样式',
    `default_flag` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `tenant_id` BIGINT NULL COMMENT '租户ID',
    `create_user_id` BIGINT NULL COMMENT '创建者ID',
    `update_user_id` BIGINT NULL COMMENT '更新者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0未删除 1已删除）',
    INDEX `idx_dict_data_tenant_id` (`tenant_id` ASC) USING BTREE,
    INDEX `idx_dict_data_deleted` (`deleted` ASC) USING BTREE,
    INDEX `idx_dict_data_status` (`status` ASC) USING BTREE,
    UNIQUE INDEX `uk_dict_data_tenant_type_value` (`tenant_id` ASC, `dict_type` ASC, `dict_value` ASC, `deleted` ASC) USING BTREE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic
  COMMENT = '字典数据表';

INSERT INTO `sys_dict_type`
    (`id`, `dict_name`, `dict_type`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000201, '用户性别', 'gender', 0, 7264590000000000001, '用户性别字典', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'gender' AND `deleted` = 0);

INSERT INTO `sys_dict_type`
    (`id`, `dict_name`, `dict_type`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000202, '通知类型', 'notice_type', 0, 7264590000000000001, '系统通知类型', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'notice_type' AND `deleted` = 0);

INSERT INTO `sys_dict_type`
    (`id`, `dict_name`, `dict_type`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000203, '通知级别', 'notice_level', 0, 7264590000000000001, '系统通知级别', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'notice_level' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000211, 'gender', '1', '男', '1', 'gender', NULL, 'primary', 'Y', 0, 7264590000000000001, '男性', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'gender' AND `dict_value` = '1' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000212, 'gender', '2', '女', '2', 'gender', NULL, 'danger', 'N', 0, 7264590000000000001, '女性', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'gender' AND `dict_value` = '2' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000213, 'gender', '3', '未知', '0', 'gender', NULL, 'info', 'N', 0, 7264590000000000001, '未知性别', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'gender' AND `dict_value` = '0' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000221, 'notice_type', '1', '通知', '1', 'notice_type', NULL, 'primary', 'Y', 0, 7264590000000000001, '普通通知', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'notice_type' AND `dict_value` = '1' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000222, 'notice_type', '2', '公告', '2', 'notice_type', NULL, 'success', 'N', 0, 7264590000000000001, '系统公告', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'notice_type' AND `dict_value` = '2' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000231, 'notice_level', '1', '普通', 'L', 'notice_level', NULL, 'info', 'Y', 0, 7264590000000000001, '普通级别', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'notice_level' AND `dict_value` = 'L' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000232, 'notice_level', '2', '重要', 'M', 'notice_level', NULL, 'warning', 'N', 0, 7264590000000000001, '重要级别', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'notice_level' AND `dict_value` = 'M' AND `deleted` = 0);

INSERT INTO `sys_dict_data`
    (`id`, `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `default_flag`, `status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000233, 'notice_level', '3', '紧急', 'H', 'notice_level', NULL, 'danger', 'N', 0, 7264590000000000001, '紧急级别', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `tenant_id` = 7264590000000000001 AND `dict_type` = 'notice_level' AND `dict_value` = 'H' AND `deleted` = 0);

INSERT INTO `sys_config`
    (`id`, `config_name`, `config_key`, `config_value`, `config_type`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000241, '系统名称', 'site.name', 'Lrcore云平台', 'Y', 7264590000000000001, '前台展示的系统名称', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `tenant_id` = 7264590000000000001 AND `config_key` = 'site.name' AND `deleted` = 0);

INSERT INTO `sys_config`
    (`id`, `config_name`, `config_key`, `config_value`, `config_type`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000242, '系统版权', 'site.copyright', '© 2026 Lrcore', 'Y', 7264590000000000001, '前台展示的版权信息', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `tenant_id` = 7264590000000000001 AND `config_key` = 'site.copyright' AND `deleted` = 0);

INSERT INTO `sys_config`
    (`id`, `config_name`, `config_key`, `config_value`, `config_type`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000243, '用户初始密码', 'user.init.password', '123456', 'Y', 7264590000000000001, '新用户默认初始密码', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `tenant_id` = 7264590000000000001 AND `config_key` = 'user.init.password' AND `deleted` = 0);
