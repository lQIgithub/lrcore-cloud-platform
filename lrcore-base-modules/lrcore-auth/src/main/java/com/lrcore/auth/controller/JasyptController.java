package com.lrcore.auth.controller;

import com.lrcore.auth.form.DbJasyptForm;
import com.lrcore.common.core.validators.AddGroup;
import com.lrcore.common.core.validators.BeanValidators;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 数据库连接加密服务
 * @ClassName: JasyptController
 * @Author: Qi Liu
 * @Date: 2026/5/6 14:37
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jasypt")
@Schema(description = "数据库连接加密服务控制器")
public class JasyptController extends BaseController {

    // textEncryptor必须与自定义注入的StrongTextEncryptor bean名称一致
    private final StrongTextEncryptor textEncryptor;

    /**
     * <p>方法说明</p>
     *
     * @Describe: 加密数据库连接信息
     * @Param: [ip 服务器地址, port 端口号, database 数据库名称, username 用户账号, password 用户密码]
     * @Author: Qi Liu
     * @Date: 2026/5/6 14:38
     * @Return java.util.Map<java.lang.String, java.lang.Object>
     * @Version: 1.0
     */
    @PostMapping("loadJasyptInfo")
    @Schema(description = "加密数据库连接信息")
    public ApiResult<Map<String, Object>> strongTextEncryptor(@RequestBody DbJasyptForm dbJasyptForm) throws IOException {
        // @formatter:off
        BeanValidators.validate(dbJasyptForm, AddGroup.class);
        Map<String, Object> result = new HashMap<>();
        String ip = dbJasyptForm.getIp();
        String port = dbJasyptForm.getPort();
        String database = dbJasyptForm.getDatabase();
        String username = dbJasyptForm.getUsername();
        String password = dbJasyptForm.getPassword();
        String jdbcUrl = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&nullCatalogMeansCurrent=true&useSSL=true&serverTimezone=GMT%2B8";
        String usernameAfterEncrypt = "ENC(" + textEncryptor.encrypt(username) + ")";
        String passwordAfterEncrypt = "ENC(" + textEncryptor.encrypt(password) + ")";
        String jdbcUrlAfterEncrypt = "ENC(" + textEncryptor.encrypt(jdbcUrl) + ")";

        result.put("jdbcUrl加密后", jdbcUrlAfterEncrypt);
        result.put("username加密后", usernameAfterEncrypt);
        result.put("password加密后", passwordAfterEncrypt);

        //url: ENC(i5ZNqLEppBV98M7ng5642tFayZM8Y91hls7IlkJ7Lo9/XtyHIMdE0MFwEhrVOwhkbP7kE0l1dwb4vf3VYqOD2oBgxzolrXfD09z1Kpdul860TizpaweziWn47rxpk/lCpimRPUTi6Ku1WhDu7iMXGozcTIQmO9ZLyiC07q1RyaUqWRVaIzUGEfYGgKZX9eTrWRG6sIIoDXCvw+3UTn7kYA==)
        //            username: ENC(KUAP3SzuO5YF9cOBQ88xKQ==)
        //            password: ENC(CIwr2Kr6wFls3gJMJvNZHiBwUiRxJQbg)
        // @formatter:on
        return ApiResult.success(result);
    }
}