package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.enums.PermimssionStatusEnum;
import com.lrcore.common.core.enums.PermimssionTypeEnum;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.List;

/**
 * <p>权限表（合并菜单+按钮+接口权限） 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>定义系统中的所有权限点</li>
 *   <li>支持菜单权限、按钮权限、接口权限三种类型</li>
 *   <li>权限归属于特定应用</li>
 *   <li>支持权限层级结构</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_permission")
@Schema(description = "权限实体（菜单+按钮+接口）")
public class SysPermissionEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属应用ID（关联sys_app表）")
    @NotBlank(message = "所属应用ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

    @Schema(description = "父权限ID，0代表根节点")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long pid;

    @Schema(description = "权限类型：0=目录 1=菜单 2=按钮 3=接口")
    @NotNull(message = "权限类型不能为空")
    private PermimssionTypeEnum type;

    //路由唯一名称（必填，英文首字母大写）
    //用于router-link命名跳转：to="{name:'User'}"
    //配合keepAlive页面缓存（必须和组件内name一致才生效）
    //路由权限、菜单匹配标识
    @Schema(description = "组件名称")
    @NotBlank(message = "组件名称不能为空")
    @Size(max = 100, message = "组件名称长度不能超过100个字符")
    private String name;

    //组件标题  菜单|标签页显示文字
    //侧边栏菜单文字：用户管理
    //浏览器标签页标题也会读取该值
    @Schema(description = "组件标题")
    @NotBlank(message = "组件标题不能为空")
    @Size(max = 100, message = "组件标题长度不能超过100个字符")
    private String title;

    @Schema(description = "权限唯一编码")
    @NotBlank(message = "权限唯一编码不能为空")
    @Size(max = 64, message = "权限唯一编码长度不能超过64个字符")
    private String permCode;

    //路由访问路径，浏览器地址栏显示。
    //写法：纯字符串（不带/，代表子路由）
    //访问地址示例：/system/user
    //作用：匹配路由跳转 this.$router.push('/system/user')
    @Schema(description = "路由地址")
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String path;

    //页面组件文件路径，对应 views 下的页面文件。
    //system/user/index → src/views/system/user/index.vue
    //框架会自动加载该 vue 文件作为页面主体
    @Schema(description = "页面组件文件路径")
    private String component;

    //重定向地址 如二级地址访问/doc/innernal-doc
    @Schema(description = "重定向地址")
    private String redirect;

    @Schema(description = "请求方法")
    @Size(max = 30, message = "请求方法长度不能超过20个字符")
    private String reqMethod;

    @Schema(description = "接口请求URL")
    @Size(max = 200, message = "接口请求URL长度不能超过200个字符")
    private String reqUrl;

    @Schema(description = "菜单图标")
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    private String icon;

    @Schema(description = "菜单排序")
    private Integer sort;

    @Schema(description = "状态：0=禁用 1=启用")
    private PermimssionStatusEnum status;

    //控制侧边栏是否隐藏该菜单
    @Schema(description = "菜单可见性")
    private Boolean hidden;

    //是否开启页面缓存（标签页关闭前保留表单 / 查询条件）
    //true：缓存页面，再次进入不重新加载接口、不重置搜索条件
    //false：每次进入页面都刷新、重置页面状态
    @Schema(description = "是否开启页面缓存（标签页关闭前保留表单 / 查询条件）")
    private Boolean keepLive;

    //针对一级父菜单生效（当前是子路由，该字段无效）
    //false：如果该父菜单下只有一个子路由，则直接显示子菜单，不渲染父菜单折叠栏
    //true：强制显示父菜单折叠栏，哪怕只有一个子页面
    @Schema(description = "是否总是折叠菜单")
    private Boolean alwaysShow;

    @Schema(description = "版本号")
    private Integer versionNum;

    @Schema(description = "是否包含字段级权限（0=否 1=是）")
    private Integer hasColumnPermission;

    @Schema(description = "子权限列表（用于构建权限树，非数据库字段）")
    @Column(ignore = true)
    private transient List<SysPermissionEntity> children;

}
