package com.lrcore.system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>类的说明</p>
 *
 * @Describe: 门户（子系统展示页）应用条目 —— 供单点登录门户页展示可进入的子系统清单。
 *            字段从 sys_app 抽取，供展示与"点击进入后台"使用。
 * @ClassName: PortalAppDto
 * @Author: lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Data
@Schema(description = "门户应用条目")
public class PortalAppDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用编码")
    private String appCode;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用描述")
    private String appDesc;

    @Schema(description = "访问地址（后端管理入口 URL）")
    private String appUrl;

    @Schema(description = "应用图标")
    private String appIcon;
}