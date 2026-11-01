package com.lrcore.system.domain;

import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

@Table(value = "sys_notice")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SysNoticeEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private String content;
    private Integer type;
    private String level;
    private Integer targetType;
    private String targetUserIds;
    private String publisherName;
    private Integer publishStatus;
    private LocalDateTime publishTime;
    private LocalDateTime revokeTime;
}
