package com.lrcore.system.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 数据权限规则类型
 * @ClassName: ColumnPermTypeEnum
 * @Author: Qi Liu
 * @Date: 2026/6/9 20:39
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum DataRuleTypeEnum {
    ROW(1, "行级权限", "行级权限"),
    COLUMN(2, "字段级权限", "字段级权限"),
    DATA_SCOPE(3, "数据范围", "数据范围"),
    CUSTOM_SQL(4, "自定义SQL", "自定义SQL");

    @EnumValue
    private final int code;
    private final String name;
    private final String desc;

    /**
     * 根据code获取对应的枚举实例
     *
     * @param code 数据规则类型编码
     * @return 匹配的枚举实例，未找到时返回null
     */
    public static DataRuleTypeEnum getDataRuleTypeEnumByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DataRuleTypeEnum dataRuleTypeEnum : DataRuleTypeEnum.values()) {
            if (dataRuleTypeEnum.getCode() == code) {
                return dataRuleTypeEnum;
            }
        }
        return null;
    }

}
