package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 数据字典类型 控制器
 * @ClassName: SysDictTypeController
 * @Author: Qi Liu
 * @Date: 2026/4/1 11:06
 * @Version: 1.0
 */
@RestController
@RequestMapping("/dict/type")
@RequiredArgsConstructor
@Schema(description = "数据字典类型 控制器")
public class SysDictTypeController extends BaseController {
    private final ISysDictTypeService dictTypeService;

}
