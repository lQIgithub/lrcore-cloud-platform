package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>角色-分组关联表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理角色与分组的关联关系</li>
 *   <li>支持优先级配置</li>
 *   <li>支持状态管理</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_role_group_member")
@Schema(description = "角色-分组关联实体")
public class SysRoleGroupMemberEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色分组ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupId;

    @Schema(description = "角色ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roleId;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "状态：0=有效 1=无效")
    private Integer status;

}
