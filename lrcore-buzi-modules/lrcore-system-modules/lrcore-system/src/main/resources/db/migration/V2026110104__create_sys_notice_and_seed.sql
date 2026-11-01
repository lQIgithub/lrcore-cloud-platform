CREATE TABLE IF NOT EXISTS `sys_notice` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '通知类型',
    `level` VARCHAR(10) NOT NULL DEFAULT 'L' COMMENT '通知级别',
    `target_type` TINYINT NOT NULL DEFAULT 1 COMMENT '目标类型（1全体 2指定用户）',
    `target_user_ids` VARCHAR(2000) NULL COMMENT '目标用户ID',
    `publisher_name` VARCHAR(100) NULL COMMENT '发布人',
    `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态（0草稿 1已发布 -1已撤回）',
    `publish_time` DATETIME NULL COMMENT '发布时间',
    `revoke_time` DATETIME NULL COMMENT '撤回时间',
    `tenant_id` BIGINT NULL COMMENT '租户ID',
    `create_user_id` BIGINT NULL COMMENT '创建者ID',
    `update_user_id` BIGINT NULL COMMENT '更新者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0未删除 1已删除）',
    INDEX `idx_sys_notice_tenant_id` (`tenant_id` ASC) USING BTREE,
    INDEX `idx_sys_notice_status` (`publish_status` ASC) USING BTREE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知公告表';

CREATE TABLE IF NOT EXISTS `sys_notice_read` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `notice_id` BIGINT NOT NULL COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `read_time` DATETIME NOT NULL COMMENT '阅读时间',
    `tenant_id` BIGINT NULL COMMENT '租户ID',
    `create_user_id` BIGINT NULL COMMENT '创建者ID',
    `update_user_id` BIGINT NULL COMMENT '更新者ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志（0未删除 1已删除）',
    UNIQUE INDEX `uk_sys_notice_read` (`tenant_id` ASC, `user_id` ASC, `notice_id` ASC, `deleted` ASC) USING BTREE,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知公告阅读记录表';

INSERT INTO `sys_notice`
    (`id`, `title`, `content`, `type`, `level`, `target_type`, `target_user_ids`, `publisher_name`, `publish_status`, `publish_time`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000251, '平台维护通知', '平台将于本周六凌晨进行例行维护，请提前保存工作数据。', 1, 'M', 1, '', '系统管理员', 1, NOW(), 7264590000000000001, '维护通知测试数据', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_notice` WHERE `id` = 7264590000000000251);

INSERT INTO `sys_notice`
    (`id`, `title`, `content`, `type`, `level`, `target_type`, `target_user_ids`, `publisher_name`, `publish_status`, `publish_time`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000252, '新版云平台发布公告', '系统管理模块已完成部门、菜单、字典和配置管理能力升级。', 2, 'H', 1, '', '系统管理员', 1, NOW(), 7264590000000000001, '升级公告测试数据', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_notice` WHERE `id` = 7264590000000000252);

INSERT INTO `sys_notice`
    (`id`, `title`, `content`, `type`, `level`, `target_type`, `target_user_ids`, `publisher_name`, `publish_status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000253, '测试用户专属通知', '这是一条面向指定测试用户的定向通知。', 1, 'L', 2, ',7264590000000000071,', '系统管理员', 1, 7264590000000000001, '定向通知测试数据', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_notice` WHERE `id` = 7264590000000000253);

INSERT INTO `sys_notice`
    (`id`, `title`, `content`, `type`, `level`, `target_type`, `target_user_ids`, `publish_status`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000254, '待发布通知', '该通知仍处于草稿状态，用于验证草稿编辑和发布。', 1, 'L', 1, '', 0, 7264590000000000001, '草稿测试数据', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_notice` WHERE `id` = 7264590000000000254);

INSERT INTO `sys_notice`
    (`id`, `title`, `content`, `type`, `level`, `target_type`, `target_user_ids`, `publisher_name`, `publish_status`, `publish_time`, `revoke_time`, `tenant_id`, `remark`, `deleted`)
SELECT 7264590000000000255, '已撤回通知', '该通知用于验证撤回和重新发布流程。', 1, 'L', 1, '', '系统管理员', -1, NOW(), NOW(), 7264590000000000001, '撤回测试数据', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_notice` WHERE `id` = 7264590000000000255);

INSERT INTO `sys_notice_read`
    (`id`, `notice_id`, `user_id`, `read_time`, `tenant_id`, `deleted`)
SELECT 7264590000000000256, 7264590000000000252, 7264590000000000071, NOW(), 7264590000000000001, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_notice_read` WHERE `id` = 7264590000000000256);
