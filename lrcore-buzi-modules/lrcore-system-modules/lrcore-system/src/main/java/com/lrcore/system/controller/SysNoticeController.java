package com.lrcore.system.controller;

import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysNoticeEntity;
import com.lrcore.system.domain.SysNoticeReadEntity;
import com.lrcore.system.domain.apt.SysNoticeAPT;
import com.lrcore.system.domain.apt.SysNoticeReadAPT;
import com.lrcore.system.service.ISysNoticeReadService;
import com.lrcore.system.service.ISysNoticeService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Tag(name = "通知公告")
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Schema(description = "通知公告 控制器")
public class SysNoticeController extends BaseController {

    private final ISysNoticeService noticeService;
    private final ISysNoticeReadService noticeReadService;

    @Operation(summary = "通知公告分页列表")
    @GetMapping
    public ApiResult<PageResponse<NoticeItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer publishStatus
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysNoticeAPT.SYS_NOTICE.DELETED.eq(0));
        if (Objects.nonNull(title) && !title.isBlank()) {
            query.and(SysNoticeAPT.SYS_NOTICE.TITLE.like(title.trim()));
        }
        if (Objects.nonNull(publishStatus)) {
            query.and(SysNoticeAPT.SYS_NOTICE.PUBLISH_STATUS.eq(publishStatus));
        }
        query.orderBy(SysNoticeAPT.SYS_NOTICE.PUBLISH_STATUS.asc())
                .orderBy(SysNoticeAPT.SYS_NOTICE.CREATE_TIME.desc());

        Page<SysNoticeEntity> result = noticeService.page(Page.of(pageNum, pageSize), query);
        Long userId = SecurityUtils.getUserId();
        List<NoticeItem> items = result.getRecords().stream()
                .map(item -> toItem(item, userId))
                .toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "我的通知分页列表")
    @GetMapping("/my")
    public ApiResult<PageResponse<NoticeItem>> myPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer isRead
    ) {
        Long userId = SecurityUtils.getUserId();
        QueryWrapper query = QueryWrapper.create()
                .where(SysNoticeAPT.SYS_NOTICE.DELETED.eq(0))
                .and(SysNoticeAPT.SYS_NOTICE.PUBLISH_STATUS.eq(1))
                .and(SysNoticeAPT.SYS_NOTICE.TARGET_TYPE.eq(1)
                        .or(SysNoticeAPT.SYS_NOTICE.TARGET_TYPE.eq(2)
                                .and(SysNoticeAPT.SYS_NOTICE.TARGET_USER_IDS.like("," + userId + ",")
                                        .or(SysNoticeAPT.SYS_NOTICE.TARGET_USER_IDS.like(userId + ","))
                                        .or(SysNoticeAPT.SYS_NOTICE.TARGET_USER_IDS.like("," + userId))
                                        .or(SysNoticeAPT.SYS_NOTICE.TARGET_USER_IDS.eq(String.valueOf(userId))))));
        if (Objects.nonNull(isRead)) {
            QueryWrapper readQuery = QueryWrapper.create()
                    .select(SysNoticeReadAPT.SYS_NOTICE_READ.NOTICE_ID)
                    .from(SysNoticeReadAPT.SYS_NOTICE_READ)
                    .where(SysNoticeReadAPT.SYS_NOTICE_READ.DELETED.eq(0))
                    .and(SysNoticeReadAPT.SYS_NOTICE_READ.USER_ID.eq(userId));
            if (Objects.equals(isRead, 1)) {
                query.and(SysNoticeAPT.SYS_NOTICE.ID.in(readQuery));
            } else {
                query.and(SysNoticeAPT.SYS_NOTICE.ID.notIn(readQuery));
            }
        }
        query.orderBy(SysNoticeAPT.SYS_NOTICE.PUBLISH_TIME.desc());
        Page<SysNoticeEntity> result = noticeService.page(Page.of(pageNum, pageSize), query);
        List<NoticeItem> items = result.getRecords().stream()
                .map(item -> toItem(item, userId))
                .toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "通知公告表单数据")
    @GetMapping("/{id}/form")
    public ApiResult<NoticeForm> form(@PathVariable Long id) {
        SysNoticeEntity notice = noticeService.getById(id);
        return ApiResult.success(Objects.isNull(notice) ? null : toForm(notice));
    }

    @Operation(summary = "通知公告详情")
    @GetMapping("/{id}/detail")
    public ApiResult<NoticeDetail> detail(@PathVariable Long id) {
        SysNoticeEntity notice = noticeService.getById(id);
        if (Objects.isNull(notice)) {
            return ApiResult.success(null);
        }
        Long userId = SecurityUtils.getUserId();
        markRead(id, userId);
        return ApiResult.success(toDetail(notice));
    }

    @Operation(summary = "新增通知公告")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody NoticeForm form) {
        SysNoticeEntity entity = toEntity(form, null);
        entity.setPublishStatus(0);
        entity.setDeleted(0);
        return ApiResult.success(noticeService.save(entity));
    }

    @Operation(summary = "修改通知公告")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody NoticeForm form) {
        return ApiResult.success(noticeService.updateById(toEntity(form, id)));
    }

    @Operation(summary = "删除通知公告")
    @DeleteMapping("/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
        return ApiResult.success(noticeService.removeByIds(idList));
    }

    @Operation(summary = "发布通知公告")
    @PutMapping("/{id}/publish")
    public ApiResult<Boolean> publish(@PathVariable Long id) {
        SysNoticeEntity entity = noticeService.getById(id);
        if (Objects.isNull(entity)) {
            return ApiResult.success(false);
        }
        entity.setPublishStatus(1);
        entity.setPublishTime(LocalDateTime.now());
        entity.setRevokeTime(null);
        entity.setPublisherName(SecurityUtils.getUsername());
        return ApiResult.success(noticeService.updateById(entity));
    }

    @Operation(summary = "撤回通知公告")
    @PutMapping("/{id}/revoke")
    public ApiResult<Boolean> revoke(@PathVariable Long id) {
        SysNoticeEntity entity = noticeService.getById(id);
        if (Objects.isNull(entity)) {
            return ApiResult.success(false);
        }
        entity.setPublishStatus(-1);
        entity.setRevokeTime(LocalDateTime.now());
        return ApiResult.success(noticeService.updateById(entity));
    }

    @Operation(summary = "我的通知全部已读")
    @PutMapping("/read-all")
    public ApiResult<Boolean> readAll() {
        Long userId = SecurityUtils.getUserId();
        QueryWrapper query = QueryWrapper.create()
                .where(SysNoticeAPT.SYS_NOTICE.DELETED.eq(0))
                .and(SysNoticeAPT.SYS_NOTICE.PUBLISH_STATUS.eq(1));
        noticeService.list(query).forEach(notice -> markRead(notice.getId(), userId));
        return ApiResult.success(true);
    }

    private void markRead(Long noticeId, Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysNoticeReadAPT.SYS_NOTICE_READ.NOTICE_ID.eq(noticeId))
                .and(SysNoticeReadAPT.SYS_NOTICE_READ.USER_ID.eq(userId))
                .and(SysNoticeReadAPT.SYS_NOTICE_READ.DELETED.eq(0));
        SysNoticeReadEntity existing = noticeReadService.getMapper().selectOneByQuery(query);
        if (Objects.nonNull(existing)) {
            return;
        }
        SysNoticeReadEntity read = SysNoticeReadEntity.builder()
                .noticeId(noticeId)
                .userId(userId)
                .readTime(LocalDateTime.now())
                .build();
        read.setDeleted(0);
        noticeReadService.save(read);
    }

    private NoticeItem toItem(SysNoticeEntity entity, Long userId) {
        return new NoticeItem(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getContent(),
                entity.getType(),
                entity.getLevel(),
                entity.getTargetType(),
                entity.getPublishStatus(),
                isRead(entity.getId(), userId),
                entity.getPublishTime(),
                entity.getRevokeTime()
        );
    }

    private NoticeForm toForm(SysNoticeEntity entity) {
        return new NoticeForm(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getContent(),
                entity.getType(),
                entity.getLevel(),
                entity.getPublishStatus(),
                entity.getTargetType(),
                parseTargetUserIds(entity.getTargetUserIds())
        );
    }

    private NoticeDetail toDetail(SysNoticeEntity entity) {
        return new NoticeDetail(
                entity.getId().toString(),
                entity.getTitle(),
                entity.getContent(),
                entity.getType(),
                entity.getLevel(),
                entity.getPublishStatus(),
                entity.getTargetUserIds(),
                entity.getPublisherName(),
                entity.getPublishTime()
        );
    }

    private SysNoticeEntity toEntity(NoticeForm form, Long id) {
        SysNoticeEntity entity = new SysNoticeEntity();
        entity.setId(id);
        entity.setTitle(form.title());
        entity.setContent(form.content());
        entity.setType(form.type());
        entity.setLevel(form.level());
        entity.setTargetType(form.targetType());
        entity.setTargetUserIds(form.targetUserIds() == null || form.targetUserIds().isEmpty()
                ? "" : "," + String.join(",", form.targetUserIds().stream().map(String::valueOf).toList()) + ",");
        entity.setPublishStatus(form.status());
        return entity;
    }

    private List<Long> parseTargetUserIds(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.replace("[", "").replace("]", "").replace("\"", "").split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(Long::valueOf)
                .toList();
    }

    private Integer isRead(Long noticeId, Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysNoticeReadAPT.SYS_NOTICE_READ.NOTICE_ID.eq(noticeId))
                .and(SysNoticeReadAPT.SYS_NOTICE_READ.USER_ID.eq(userId))
                .and(SysNoticeReadAPT.SYS_NOTICE_READ.DELETED.eq(0));
        return noticeReadService.count(query) > 0 ? 1 : 0;
    }

    private record PageResponse<T>(List<T> list, long total) {}
    private record NoticeItem(
            String id,
            String title,
            String content,
            Integer type,
            String level,
            Integer targetType,
            Integer publishStatus,
            Integer isRead,
            LocalDateTime publishTime,
            LocalDateTime revokeTime
    ) {}
    private record NoticeForm(
            String id,
            String title,
            String content,
            Integer type,
            String level,
            Integer status,
            Integer targetType,
            List<Long> targetUserIds
    ) {}
    private record NoticeDetail(
            String id,
            String title,
            String content,
            Integer type,
            String level,
            Integer publishStatus,
            String targetUserIds,
            String publisherName,
            LocalDateTime publishTime
    ) {}
}
