package com.hestia.controller.admin;

import com.hestia.context.BaseContext;
import com.hestia.dto.InspectionExecuteDTO;
import com.hestia.dto.InspectionPlanDTO;
import com.hestia.dto.InspectionPlanPageQueryDTO;
import com.hestia.dto.InspectionTaskPageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.InspectionService;
import com.hestia.vo.InspectionTaskVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 巡检计划与任务
 */
@RestController
@RequestMapping("/admin/inspection")
@Api(tags = "管理端-巡检")
@Slf4j
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @GetMapping("/plan/page")
    @ApiOperation("巡检计划分页")
    public Result<PageResult> planPage(InspectionPlanPageQueryDTO query) {
        return Result.success(inspectionService.planPageQuery(query));
    }

    @PostMapping("/plan")
    @ApiOperation("新增巡检计划")
    public Result<?> savePlan(@RequestBody InspectionPlanDTO dto) {
        inspectionService.savePlan(dto);
        return Result.success();
    }

    @PutMapping("/plan")
    @ApiOperation("编辑巡检计划")
    public Result<?> updatePlan(@RequestBody InspectionPlanDTO dto) {
        inspectionService.updatePlan(dto);
        return Result.success();
    }

    @PutMapping("/plan/{id}/status")
    @ApiOperation("启用/停用计划")
    public Result<?> updatePlanStatus(@PathVariable Long id, @RequestParam Integer status) {
        inspectionService.updatePlanStatus(id, status);
        return Result.success();
    }

    @PostMapping("/plan/{id}/generate")
    @ApiOperation("立即生成巡检任务（幂等）")
    public Result<Integer> generate(@PathVariable Long id) {
        return Result.success(inspectionService.generateTasks(id));
    }

    @GetMapping("/task/page")
    @ApiOperation("巡检任务分页")
    public Result<PageResult> taskPage(InspectionTaskPageQueryDTO query) {
        return Result.success(inspectionService.taskPageQuery(query));
    }

    @GetMapping("/task/mine")
    @ApiOperation("待我执行的巡检任务")
    public Result<List<InspectionTaskVO>> mine(@RequestParam(required = false) Integer status) {
        return Result.success(inspectionService.listByExecutor(BaseContext.getCurrentId(), status));
    }

    @PostMapping("/task/execute")
    @ApiOperation("执行巡检任务，异常可转工单")
    public Result<Long> execute(@RequestBody InspectionExecuteDTO dto) {
        return Result.success(inspectionService.execute(dto, BaseContext.getCurrentId()));
    }
}
