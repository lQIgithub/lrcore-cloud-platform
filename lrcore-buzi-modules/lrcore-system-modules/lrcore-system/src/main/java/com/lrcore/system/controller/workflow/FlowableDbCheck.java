package com.lrcore.system.controller.workflow;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
@Component
public class FlowableDbCheck implements CommandLineRunner {
    @Autowired(required = false)
    private ProcessEngine processEngine;

    @Override
    public void run(String... args) throws Exception {
        if (processEngine == null) {
            log.error("ProcessEngine创建失败，Flowable引擎未初始化，ACT_*表不会自动创建");
            return;
        }
        DataSource ds = processEngine.getProcessEngineConfiguration().getDataSource();
        log.info("Flowable数据源类: {}", ds.getClass().getName());
        try (Connection conn = ds.getConnection()) {
            log.info("Flowable JDBC URL: {}", conn.getMetaData().getURL());
            log.info("Flowable JDBC User: {}", conn.getMetaData().getUserName());
            PreparedStatement ps = conn.prepareStatement("SELECT NAME_,VALUE_ FROM ACT_GE_PROPERTY WHERE NAME_='schema.version'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                log.info("Flowable schema.version: {}", rs.getString("VALUE_"));
            }
            log.info("Flowable引擎初始化成功，ACT_*表已创建");
        }
    }
}