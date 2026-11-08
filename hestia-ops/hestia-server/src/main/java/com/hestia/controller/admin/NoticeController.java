package com.hestia.controller.admin;

import com.hestia.context.BaseContext;
import com.hestia.entity.Notice;
import com.hestia.result.Result;
import com.hestia.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 站内通知
 */
@RestController
@RequestMapping("/admin/notice")
@Api(tags = "管理端-通知")
@Slf4j
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    @ApiOperation("我的通知")
    public Result<List<Notice>> list() {
        return Result.success(noticeService.list(BaseContext.getCurrentId(), 1));
    }

    @GetMapping("/unread-count")
    @ApiOperation("未读数量")
    public Result<Long> unreadCount() {
        return Result.success(noticeService.countUnread(BaseContext.getCurrentId(), 1));
    }

    @PutMapping("/{id}/read")
    @ApiOperation("标记已读")
    public Result<?> read(@PathVariable Long id) {
        noticeService.markRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @ApiOperation("全部已读")
    public Result<?> readAll() {
        noticeService.markAllRead(BaseContext.getCurrentId(), 1);
        return Result.success();
    }
}
