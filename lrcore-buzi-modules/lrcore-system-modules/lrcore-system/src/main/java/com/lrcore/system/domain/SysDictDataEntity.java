package com.lrcore.system.domain;

import com.lrcore.common.core.annotation.Excel;
import com.lrcore.common.core.annotation.Excel.ColumnType;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 字典数据表 实体类 - 存储系统中的字典数据项
 * @ClassName: SysDictDataEntity
 * @Author: lrcore
 * @Date: 2026/05/16
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>存储系统中各种字典类型的数据项</li>
 *   <li>支持下拉框、单选框等前端组件的数据源</li>
 *   <li>支持数据的排序、状态管理</li>
 * </ul>
 *
 * <p>数据约束：</p>
 * <ul>
 *   <li>主键：继承自BaseEntity的id字段</li>
 *   <li>字典类型dictType：关联 sys_dict_type 表</li>
 *   <li>字典标签dictLabel：同一字典类型下唯一</li>
 *   <li>字典键值dictValue：同一字典类型下唯一</li>
 *   <li>是否默认defaultFlag：Y-是，N-否</li>
 *   <li>状态status：0-正常，1-停用</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>表单下拉框：性别、状态等选择项</li>
 *   <li>表格列展示：将数字状态转换为中文显示</li>
 *   <li>数据验证：后端校验输入值的合法性</li>
 * </ul>
 *
 * <p>与其它表的关系：</p>
 * <ul>
 *   <li>sys_dict_type：字典类型表，通过dictType字段关联</li>
 *   <li>一个字典类型可包含多条字典数据</li>
 * </ul>
 *
 * <p>字典数据示例（sys_user_sex 类型）：</p>
 * <ul>
 *   <li>dictLabel: 男, dictValue: 0, defaultFlag: Y</li>
 *   <li>dictLabel: 女, dictValue: 1, defaultFlag: N</li>
 *   <li>dictLabel: 未知, dictValue: 2, defaultFlag: N</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)     // 继承父类属性参与比较
@ToString(callSuper = true)              // 父类属性也打印
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_dict_data")
@Schema(description = "字典数据实体")
public class SysDictDataEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典编码
     * <p>功能说明：字典数据的唯一标识</p>
     * <p>数据约束：用于系统内部识别字典数据项</p>
     * <p>业务逻辑：通常由系统自动生成</p>
     */
    @Schema(description = "字典编码")
    @Excel(name = "字典编码", cellType = ColumnType.NUMERIC)
    private String dictCode;

    /**
     * 字典排序
     * <p>功能说明：控制字典数据在同一类型中的显示顺序</p>
     * <p>数据约束：数值类型，数字越小排序越靠前</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>用于控制下拉框选项的排列顺序</li>
     *   <li>可以配合拖拽功能动态调整</li>
     *   <li>默认为0，数值相同时按创建时间排序</li>
     * </ul>
     */
    @Schema(description = "字典排序")
    @Excel(name = "字典排序", cellType = ColumnType.NUMERIC)
    private String dictSort;

    /**
     * 字典标签
     * <p>功能说明：字典数据在前端显示的文本</p>
     * <p>数据约束：最大100个字符，不能为空，同一字典类型下唯一</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>用于前端下拉框、单选框的选项文本</li>
     *   <li>用于表格列的状态展示</li>
     *   <li>通常使用中文或友好的描述性文本</li>
     * </ul>
     * <p>示例：男、女；正常、停用；启用、禁用</p>
     */
    @Schema(description = "字典标签")
    @Excel(name = "字典标签")
    @NotBlank(message = "字典标签不能为空")
    @Size(min = 0, max = 100, message = "字典标签长度不能超过100个字符")
    private String dictLabel;

    /**
     * 字典键值
     * <p>功能说明：字典数据存储的实际值，用于后端处理</p>
     * <p>数据约束：最大100个字符，不能为空，同一字典类型下唯一</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>用于数据库存储和后端逻辑处理</li>
     *   <li>通常是数字或英文字符串</li>
     *   <li>与 dictLabel 配合使用，label用于展示，value用于存储</li>
     * </ul>
     * <p>示例：0、1、2；Y、N；active、inactive</p>
     */
    @Schema(description = "字典键值")
    @Excel(name = "字典键值")
    @NotBlank(message = "字典键值不能为空")
    @Size(min = 0, max = 100, message = "字典键值长度不能超过100个字符")
    private String dictValue;

    /**
     * 字典类型
     * <p>功能说明：关联的字典类型编码</p>
     * <p>数据约束：关联 sys_dict_type 表的 dictType 字段，不能为空</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>用于将字典数据归类到对应的字典类型下</li>
     *   <li>同一字典类型下的数据拥有相同的 dictType</li>
     *   <li>查询时可按字典类型筛选数据</li>
     * </ul>
     */
    @Schema(description = "字典类型")
    @Excel(name = "字典类型")
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型长度不能超过100个字符")
    private String dictType;

    /**
     * 样式属性
     * <p>功能说明：用于扩展字典数据的样式配置</p>
     * <p>数据约束：最大100个字符，可为空</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>可用于自定义标签的CSS样式类名</li>
     *   <li>支持前端根据值显示不同的颜色或图标</li>
     *   <li>具体实现依赖于前端组件的支持</li>
     * </ul>
     * <p>示例：success（绿色）、danger（红色）、warning（黄色）</p>
     */
    @Schema(description = "样式属性")
    @Size(min = 0, max = 100, message = "样式属性长度不能超过100个字符")
    private String cssClass;

    /**
     * 表格字典样式
     * <p>功能说明：用于控制表格中该字典值的显示样式</p>
     * <p>数据约束：可为空</p>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>用于表格列的特定样式渲染</li>
     *   <li>可以设置表格中标签的class样式</li>
     *   <li>配合前端表格组件使用</li>
     * </ul>
     * <p>示例：primary、success、danger、warning</p>
     */
    @Schema(description = "表格字典样式")
    private String listClass;

    /**
     * 是否默认
     * <p>功能说明：标记该字典数据是否为默认选项</p>
     * <p>数据约束：Y-是默认项，N-非默认项</p>
     * <p>状态说明：</p>
     * <ul>
     *   <li>Y-是：作为下拉框的默认选中项</li>
     *   <li>N-否：普通选项，需要用户手动选择</li>
     * </ul>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>每个字典类型下只能有一个默认项</li>
     *   <li>新建数据时默认设为非默认</li>
     *   <li>表单初始化时可自动填充默认值</li>
     * </ul>
     */
    @Schema(description = "是否默认（Y是 N否）")
    @Excel(name = "是否默认", readConverterExp = "Y=是,N=否")
    private String defaultFlag;

    /**
     * 状态
     * <p>功能说明：控制字典数据的使用状态</p>
     * <p>数据约束：0-正常（可用），1-停用（不可用）</p>
     * <p>状态说明：</p>
     * <ul>
     *   <li>0-正常：字典数据可以使用，可被前端组件加载</li>
     *   <li>1-停用：字典数据被临时禁用，不显示在下拉框中</li>
     * </ul>
     * <p>业务逻辑：</p>
     * <ul>
     *   <li>停用字典数据不会删除数据，只是隐藏显示</li>
     *   <li>适用于废弃的选项或临时不需要的选项</li>
     *   <li>可以重新启用恢复使用</li>
     *   <li>停用后历史数据仍可正常显示</li>
     * </ul>
     */
    @Schema(description = "状态（0正常 1停用）")
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private Integer status;

}
