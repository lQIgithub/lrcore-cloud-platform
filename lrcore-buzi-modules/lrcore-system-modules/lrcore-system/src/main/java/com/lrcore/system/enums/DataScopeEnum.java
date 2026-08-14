package com.lrcore.system.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 权限操作符枚举
 * @ClassName: PermOperatorEnum
 * @Author: Qi Liu
 * @Date: 2026/6/9 20:42
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {
    ALL(1, "全部", "全部数据"),
    THIS_ENTERPRISE(2, "本企业", "本企业数据"),
    THIS_DEPARTMENT(3, "本部门", "本部门数据"),
    THIS_DEPARTMENT_AND_SUBORDINATE(4, "本部门及下级", "本部门及下级数据"),
    THIS_USER(5, "本人", "本人数据"),
    CUSTOM(6, "自定义", "自定义数据");

    @EnumValue
    private final Integer code; // 权限编码
    private final String name; // 权限名称
    private final String desc; // 权限描述

    public static String getSymbolByCode(Integer code) {
        for (DataScopeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getName();
            }
        }
        return null;
    }
}
