package com.hestia.controller.admin;

import com.hestia.result.Result;
import com.hestia.service.WorkspaceService;
import com.hestia.vo.StatusDistributionVO;
import com.hestia.vo.TrendVO;
import com.hestia.vo.WorkspaceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作台
 */
@RestController
@RequestMapping("/admin/workspace")
@Api(tags = "管理端-工作台")
@Slf4j
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/overview")
    @ApiOperation("核心指标概览")
    public Result<WorkspaceVO> overview() {
        return Result.success(workspaceService.overview());
    }

    @GetMapping("/status-distribution")
    @ApiOperation("近 30 天工单状态分布")
    public Result<List<StatusDistributionVO>> statusDistribution() {
        return Result.success(workspaceService.statusDistribution());
    }

    @GetMapping("/trend")
    @ApiOperation("近 N 天工单趋势")
    public Result<List<TrendVO>> trend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(workspaceService.recentTrend(days));
    }
}
