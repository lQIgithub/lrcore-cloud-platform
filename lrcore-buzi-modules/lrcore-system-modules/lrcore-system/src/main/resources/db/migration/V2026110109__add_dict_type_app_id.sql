ALTER TABLE `sys_dict_type`
    ADD COLUMN `app_id` BIGINT NOT NULL DEFAULT 0 COMMENT '应用ID（0表示平台级字典）' AFTER `dict_type`;

ALTER TABLE `sys_dict_type`
    ADD INDEX `idx_dict_type_app_id` (`app_id` ASC) USING BTREE;
