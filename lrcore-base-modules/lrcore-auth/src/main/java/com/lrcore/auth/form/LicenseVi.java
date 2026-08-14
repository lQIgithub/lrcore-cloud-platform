package com.lrcore.auth.form;

import com.lrcore.common.core.validators.OtherGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 提交表单登录
 * @ClassName: WebLoginForm
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:56
 * @Version: 1.0
 */
@Schema(description = "license信息")
@Data
public class LicenseVi implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主题")
    private String subject = "lrcoreLicense";
    @Schema(description = "签发时间")
    @NotNull(message = "签发时间不能为空", groups = {OtherGroup.class})
    private Date issued;
    @NotNull(message = "生效时间不能为空", groups = {OtherGroup.class})
    @Schema(description = "生效时间")
    private Date notBefore;
    @NotNull(message = "过期时间不能为空", groups = {OtherGroup.class})
    @Schema(description = "过期时间")
    private Date notAfter;
    @NotEmpty(message = "消费者类型不能为空", groups = {OtherGroup.class})
    @Schema(description = "消费者类型")
    private String consumerType;
    @NotEmpty(message = "消费者数量不能为空", groups = {OtherGroup.class})
    @Schema(description = "消费者数量")
    private int consumerAmount = 1;
    @NotEmpty(message = "许可证信息不能为空", groups = {OtherGroup.class})
    @Schema(description = "许可证信息")
    private String info;

    // 自定义许可证额外信息
    @Schema(description = "自定义名称")
    private String customerName;

    @Schema(description = "可被允许的IP地址集合")
    private List<String> ipAddress;

    @Schema(description = "可被允许的MAC地址")
    private List<String> macAddress;

    @Schema(description = "可被允许的CPU序列号")
    private String cpuSerial;

    @Schema(description = "可被允许的主板序列号")
    private String mainBoardSerial;

}
