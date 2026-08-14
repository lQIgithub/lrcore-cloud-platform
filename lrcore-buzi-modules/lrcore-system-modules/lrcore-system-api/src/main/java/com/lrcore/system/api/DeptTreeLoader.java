package com.lrcore.system.api;

import java.util.Set;

/**
 * 部门树数据加载器接口
 * <p>
 * 业务模块需要实现此接口，提供从数据库查询子部门ID的逻辑。
 * </p>
 *
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>构建部门树形结构</li>
 *   <li>查询指定部门的所有子部门</li>
 *   <li>实现数据权限控制</li>
 * </ul>
 *
 * <p><b>实现示例（使用 MyBatis-Flex）：</b></p>
 * <pre>{@code
 * @Component
 * public class DefaultDeptTreeLoader implements DeptTreeLoader {
 *
 *     private final SysDeptMapper deptMapper;
 *
 *     @Override
 *     public Set<String> loadChildDeptIds(String deptId) {
 *         List<SysDeptEntity> children = deptMapper.selectListByQuery(
 *             QueryWrapper.create()
 *                 .select(SysDeptEntity::getId)
 *                 .where(SysDeptEntity::getParentId).eq(deptId)
 *                 .and(SysDeptEntity::getDelFlag).eq("0")
 *         );
 *         return new HashSet<>(children.stream()
 *             .map(SysDeptEntity::getId)
 *             .collect(Collectors.toList()));
 *     }
 * }
 * }</pre>
 *
 * <p>类模块说明</p>
 *
 * @Describe: 部门树数据加载器接口
 * @ClassName: DeptTreeLoader
 * @Author: Qi Liu
 * @Date: 2026/5/8 22:56
 * @Version: 1.0
 */
public interface DeptTreeLoader {

    /**
     * 从数据源加载指定部门的所有子部门ID
     *
     * @param deptId 父部门ID
     * @return 子部门ID集合（不包含父部门自身）
     */
    Set<Long> loadChildDeptIds(String deptId);
}
