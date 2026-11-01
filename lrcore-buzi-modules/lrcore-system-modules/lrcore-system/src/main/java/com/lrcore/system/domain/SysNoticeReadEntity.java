package com.lrcore.system.domain;

import com.lrcore.common.core.web.domain.BaseEntity;
import com.mybatisflex.annotation.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

@Table(value = "sys_notice_read")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SysNoticeReadEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long noticeId;
    private Long userId;
    private LocalDateTime readTime;
}
