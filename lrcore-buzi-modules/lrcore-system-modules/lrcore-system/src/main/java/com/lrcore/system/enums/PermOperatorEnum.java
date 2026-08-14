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
public enum PermOperatorEnum {
    EQ(1, "=", "等于"),
    NE(2, "!=", "不等于"),
    IN(3, "IN", "包含"),
    NOT_IN(4, "NOT IN", "不包含"),
    LIKE(5, "LIKE", "模糊查询"),
    BETWEEN(6, "BETWEEN", "区间");

    @EnumValue
    private final Integer code; // 操作符编码
    private final String symbol; // 操作符
    private final String desc; // 描述

    public static String getSymbolByCode(Integer code) {
        for (PermOperatorEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getSymbol();
            }
        }
        return null;
    }
}
