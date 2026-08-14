package com.lrcore.system.domain;

import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 字典类型表 实体类 - 定义系统中的字典类型
 * @ClassName: SysDictTypeEntity
 * @Author: lrcore
 * @Date: 2026/05/16
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理系统中的字典类型，用于分类管理字典数据</li>
 *   <li>支持字典类型的启用/禁用状态管理</li>
 *   <li>一个字典类型可包含多条字典数据</li>
 * </ul>
 *
 * <p>数据约束：</p>
 * <ul>
 *   <li>主键：继承自BaseEntity的id字段</li>
 *   <li>字典类型dictType：全局唯一，不能重复</li>
 *   <li>字典类型必须以字母开头，只能包含小写字母、数字和下划线</li>
 *   <li>状态status：0-正常，1-停用</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>下拉框数据：系统中的性别、状态等下拉选项</li>
 *   <li>配置管理：系统参数的分类管理</li>
 *   <li>数据标准化：统一系统中同类数据的取值范围</li>
 * </ul>
 *
 * <p>与其它表的关系：</p>
 * <ul>
 *   <li>sys_dict_data：字典数据表，通过dictType字段关联</li>
 *   <li>一条字典类型对应多条字典数据</li>
 * </ul>
 *
 * <p>字典类型编码示例：</p>
 * <ul>
 *   <li>sys_user_sex：用户性别</li>
 *   <li>sys_common_status：通用状态</li>
 *   <li>sysyes_no：是否类型</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)     // 继承父类属性参与比较
@ToString(callSuper = true)              // 父类属性也打印
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_dict_type")
@Schema(description = "字典类型实体")
public class SysDictTypeEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典名称
     * <p>功能说明：字典类型的中文名称，用于前端展示</p>
     * <p>数据约束：最大100个字符，不能为空</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>用于前端表单的下拉框标签</li>
     *   <li>用于字典管理页面的类型名称展示</li>
     *   <li>建议使用简洁明了的中文名称</li>
     * </ul>
     * <p>示例：用户性别、系统状态、是否启用</p>
     */
    @Schema(description = "字典名称")
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 0, max = 100, message = "字典类型名称长度不能超过100个字符")
    private String dictName;

    /**
     * 字典类型
     * <p>功能说明：字典类型的唯一标识编码，用于系统内部识别</p>
     * <p>数据约束：最大100个字符，必须以字母开头，只能包含小写字母、数字和下划线</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>全局唯一，不能与现有字典类型重复</li>
     *   <li>用于关联字典数据表（sys_dict_data）</li>
     *   <li>通常使用英文小写和下划线的命名方式</li>
     *   <li>创建后不建议修改，影响关联的字典数据</li>
     * </ul>
     * <p>正则表达式：^[a-z][a-z0-9_]*$</p>
     * <p>示例：sys_user_sex、sys_common_status</p>
     */
    @Schema(description = "字典类型编码")
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型编码长度不能超过100个字符")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "字典类型必须以字母开头，且只能为（小写字母，数字，下划线）")
    private String dictType;

    /**
     * 状态
     * <p>功能说明：控制字典类型的使用状态</p>
     * <p>数据约束：0-正常（可用），1-停用（不可用）</p>
     * <p>状态说明：</p>
     * <ul>
     *   <li>0-正常：字典类型可以使用，对应的字典数据可被查询</li>
     *   <li>1-停用：字典类型被临时禁用，对应的字典数据不可用</li>
     * </ul>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>停用字典类型不会删除数据，只是禁止使用</li>
     *   <li>适用于废弃的字典类型</li>
     *   <li>可以重新启用恢复使用</li>
     * </ul>
     */
    @Schema(description = "状态（0正常 1停用）")
    private Integer status;

}
