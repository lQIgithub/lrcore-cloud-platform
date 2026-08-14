package com.lrcore.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>权限缓存配置表 实体类</p>
 *
 * @Author: lrcore
 * @Date: 2026/05/24
 * @Version: 1.0
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>配置权限缓存策略</li>
 *   <li>支持多种缓存类型和层级</li>
 *   <li>支持预加载和自动刷新配置</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "sys_permission_cache_config")
@Schema(description = "权限缓存配置实体")
public class SysPermissionCacheConfigEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "缓存名称")
    @NotBlank(message = "缓存名称不能为空")
    @Size(max = 100, message = "缓存名称长度不能超过100个字符")
    private String cacheName;

    @Schema(description = "缓存类型：1=本地 2=分布式 3=混合")
    private Integer cacheType;

    @Schema(description = "缓存层级：1=一级 2=二级 3=三级")
    private Integer cacheLevel;

    @Schema(description = "缓存键前缀")
    @NotBlank(message = "缓存键前缀不能为空")
    @Size(max = 50, message = "缓存键前缀长度不能超过50个字符")
    private String cacheKeyPrefix;

    @Schema(description = "缓存键匹配模式")
    @NotBlank(message = "缓存键匹配模式不能为空")
    @Size(max = 200, message = "缓存键匹配模式长度不能超过200个字符")
    private String cacheKeyPattern;

    @Schema(description = "缓存生存时间（秒）")
    private Integer ttl;

    @Schema(description = "缓存最大容量（条数）")
    private Integer maxSize;

    @Schema(description = "是否启用预加载")
    private Integer preloadEnabled;

    @Schema(description = "预加载Cron表达式")
    @Size(max = 100, message = "预加载Cron表达式长度不能超过100个字符")
    private String preloadCron;

    @Schema(description = "是否启用自动刷新")
    private Integer refreshEnabled;

    @Schema(description = "自动刷新间隔（秒）")
    private Integer refreshInterval;

    @Schema(description = "是否启用压缩")
    private Integer compressionEnabled;

    @Schema(description = "是否启用加密")
    private Integer encryptionEnabled;

    @Schema(description = "序列化方式：0=JSON 1=Java 2=Protobuf")
    private Integer serializationType;

    @Schema(description = "缓存状态：0=启用 1=禁用")
    private Integer status;

    @Schema(description = "应用ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long appId;

}
