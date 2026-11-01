ALTER TABLE sys_dept
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '显示排序' AFTER status;
