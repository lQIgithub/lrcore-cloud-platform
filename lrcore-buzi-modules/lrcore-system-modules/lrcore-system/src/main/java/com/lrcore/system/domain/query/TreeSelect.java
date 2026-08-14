package com.lrcore.system.domain.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: Treeselect树结构实体类
 * @ClassName: TreeSelect
 * @Author: Qi Liu
 * @Date: 2026/3/26 15:26
 * @Version: 1.0
 */
@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TreeSelect implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 节点禁用
     */
    private boolean disabled = false;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;


//    public TreeSelect(SysDeptDto dept) {
//        this.id = dept.getDeptId();
//        this.label = dept.getDeptName();
//        this.disabled = StringUtils.equals(GlobalConstants.DEPT_DISABLE, dept.getStatus());
//        this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
//    }

}
