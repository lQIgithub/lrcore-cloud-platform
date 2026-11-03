ALTER TABLE `sys_role`
    ADD COLUMN `role_scope` VARCHAR(20) NOT NULL DEFAULT 'APP' COMMENT '角色级别：PLATFORM=平台级，APP=应用级' AFTER `app_id`;

UPDATE `sys_role`
SET `role_scope` = 'PLATFORM'
WHERE `app_id` = 0;
