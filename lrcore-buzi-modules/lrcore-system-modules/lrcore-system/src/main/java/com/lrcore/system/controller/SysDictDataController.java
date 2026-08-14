package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.system.service.ISysDictDataService;
import com.lrcore.system.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 数据字典信息 控制器
 * @ClassName: SysDictDataController
 * @Author: Qi Liu
 * @Date: 2026/4/1 11:06
 * @Version: 1.0
 */
@RestController
@RequestMapping("/dict/data")
@RequiredArgsConstructor
@Schema(description = "数据字典信息 控制器")
public class SysDictDataController extends BaseController {

    private final ISysDictDataService dictDataService;
    private final ISysDictTypeService dictTypeService;

}
