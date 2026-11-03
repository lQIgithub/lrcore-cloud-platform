ALTER TABLE `sys_tenant`
    ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序' AFTER `status`;

UPDATE `sys_tenant` AS `target`
INNER JOIN (
    SELECT
        `id`,
        ROW_NUMBER() OVER (ORDER BY `create_time` DESC) AS `sort_no`
    FROM `sys_tenant`
    WHERE `deleted` = 0
) AS `ranked` ON `ranked`.`id` = `target`.`id`
SET `target`.`sort_order` = `ranked`.`sort_no`
WHERE `target`.`deleted` = 0;
