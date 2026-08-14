package com.lrcore.system.domain;

import com.lrcore.common.core.annotation.Excel;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * 参数配置表 sys_config
 *
 * @author lrcore
 */
@Table(value = "sys_config")
@Data
@EqualsAndHashCode(callSuper = true) // 继承父类属性参与比较
@ToString(callSuper = true)          // 父类属性也打印
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SysConfigEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数名称
     */
    @Excel(name = "参数名称")
    private String configName;

    /**
     * 参数键名
     */
    @Excel(name = "参数键名")
    private String configKey;

    /**
     * 参数键值
     */
    @Excel(name = "参数键值")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    private String configType;

}
