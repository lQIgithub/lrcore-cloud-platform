package com.lrcore.system.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 字段列权限类型枚举
 * @ClassName: ColumnPermTypeEnum
 * @Author: Qi Liu
 * @Date: 2026/6/9 20:36
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum ColumnPermTypeEnum {
    VISIBLE(1, "可见", "字段可见"),
    EDITABLE(2, "可编辑", "字段可编辑"),
    READ_ONLY(3, "只读", "字段只读"),
    HIDDEN(4, "隐藏", "字段隐藏"),
    ENCRYPT(5, "加密显示", "字段加密显示");

    @EnumValue
    private final int code;
    private final String name;
    private final String desc;

    // 根据 code 获取枚举
    public static ColumnPermTypeEnum getColumnPermTypeEnumByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ColumnPermTypeEnum columnPermTypeEnum : ColumnPermTypeEnum.values()) {
            if (columnPermTypeEnum.getCode() == code) {
                return columnPermTypeEnum;
            }
        }
        return null;
    }
}
