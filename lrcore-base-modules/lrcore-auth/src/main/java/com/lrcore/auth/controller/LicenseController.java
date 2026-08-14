package com.lrcore.auth.controller;

import com.lrcore.auth.form.LicenseVi;
import com.lrcore.common.core.validators.BeanValidators;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.license.creator.LrcoreLicenseCreator;
import com.lrcore.common.license.model.LrcoreLicenseContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.x500.X500Principal;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 证书生成控制器
 * @ClassName: LicenseController
 * @Author: Qi Liu
 * @Date: 2026/4/11 09:49
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Schema(description = "证书生成控制器")
@RequestMapping("/api/license")
public class LicenseController extends BaseController {

    private final LrcoreLicenseCreator licenseCreator;

    @Schema(description = "生成License证书")
    @GetMapping("genLicense")
    public ApiResult<?> genLicense(@RequestBody LicenseVi licenseVi) {
        BeanValidators.validate(licenseVi);
        LrcoreLicenseContent content = createLicenseContent(licenseVi);
        boolean result = licenseCreator.generateLicense(content);
        if (result) {
            System.out.println("License 证书生成成功！");
        } else {
            System.out.println("License 证书生成失败！");
        }
        return ApiResult.success("证书生成成功");
    }

    /**
     * <p>类模块说明</p>
     *
     * @Describe: 根据传入的参数，创建相对应的license证书
     * @ClassName: LicenseController
     * @Author: Qi Liu
     * @Date: 2026/4/11 10:07
     * @Version: 1.0
     */
    private static LrcoreLicenseContent createLicenseContent(LicenseVi licenseVi) {
        LrcoreLicenseContent content = new LrcoreLicenseContent();

        // 设置证书主体和签发者
        X500Principal holder = new X500Principal("CN=LRCore, OU=IT, O=LRCore Inc., L=Beijing, ST=Beijing, C=CN");
        X500Principal issuer = new X500Principal("CN=LRCore, OU=IT, O=LRCore Inc., L=Beijing, ST=Beijing, C=CN");

        content.setHolder(holder);
        content.setIssuer(issuer);
        content.setSubject(licenseVi.getSubject());

        // 设置日期
        Date now = new Date();
        content.setIssued(now);
        content.setNotBefore(now);

        // 设置过期时间（2个月 60天）
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 60);
        content.setNotAfter(calendar.getTime());

        // 设置消费者信息
        content.setConsumerType(licenseVi.getConsumerType());
        content.setConsumerAmount(licenseVi.getConsumerAmount());
        content.setInfo(licenseVi.getInfo());

        // 设置自定义信息 主机ip地址、mac地址、cpu序列号、主板序列号
        content.setCustomerName(licenseVi.getCustomerName());
        content.setIpAddress(licenseVi.getIpAddress());
        content.setMacAddress(licenseVi.getMacAddress());
        content.setCpuSerial(licenseVi.getCpuSerial());
        content.setMainBoardSerial(licenseVi.getMainBoardSerial());

        return content;
    }

}
