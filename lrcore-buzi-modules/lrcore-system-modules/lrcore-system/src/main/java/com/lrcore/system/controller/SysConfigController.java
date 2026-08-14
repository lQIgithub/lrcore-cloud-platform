package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 参数配置 控制器
 * @ClassName: SysConfigController
 * @Author: Qi Liu
 * @Date: 2026/3/26 22:31
 * @Version: 1.0
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Schema(description = "参数配置 控制器")
public class SysConfigController extends BaseController {
    private final ISysConfigService configService;
}
