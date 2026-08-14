package com.lrcore.system.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 字典类型表 sys_dict_type
 * @ClassName: SysDictTypeDto
 * @Author: Qi Liu
 * @Date: 2026/3/25 16:12
 * @Version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true) // 继承父类属性参与比较
@ToString(callSuper = true)          // 父类属性也打印
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SysDictTypeDto extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典主键
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

}
