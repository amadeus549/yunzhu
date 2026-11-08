package com.hestia.controller.admin;

import com.hestia.context.BaseContext;
import com.hestia.dto.WorkOrderCancelDTO;
import com.hestia.dto.WorkOrderCreateDTO;
import com.hestia.dto.WorkOrderDispatchDTO;
import com.hestia.dto.WorkOrderPageQueryDTO;
import com.hestia.dto.WorkOrderRatingDTO;
import com.hestia.dto.WorkOrderRejectDTO;
import com.hestia.enumeration.OperatorType;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.DispatchService;
import com.hestia.service.WorkOrderService;
import com.hestia.vo.DispatchCandidateVO;
import com.hestia.vo.WorkOrderDetailVO;
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
 * 工单管理
 */
@RestController
@RequestMapping("/admin/work-order")
@Api(tags = "管理端-工单")
@Slf4j
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final DispatchService dispatchService;

    @GetMapping("/page")
    @ApiOperation("多条件分页查询")
    public Result<PageResult> page(WorkOrderPageQueryDTO query) {
        return Result.success(workOrderService.pageQuery(query));
    }

    @GetMapping("/{id}")
    @ApiOperation("工单详情（含明细、日志、附件）")
    public Result<WorkOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(workOrderService.detail(id));
    }

    @PostMapping
    @ApiOperation("创建工单（管家代报/电话报修）")
    public Result<Long> create(@RequestBody WorkOrderCreateDTO dto) {
        return Result.success(workOrderService.create(dto));
    }

    @PutMapping("/{id}/accept")
    @ApiOperation("受理工单")
    public Result<?> accept(@PathVariable Long id) {
        workOrderService.accept(id, BaseContext.getCurrentId());
        return Result.success();
    }

    @PutMapping("/{id}/dispatch")
    @ApiOperation("手动派单")
    public Result<?> dispatch(@PathVariable Long id, @RequestBody WorkOrderDispatchDTO dto) {
        dto.setOrderId(id);
        workOrderService.dispatch(dto, BaseContext.getCurrentId());
        return Result.success();
    }

    @PutMapping("/{id}/auto-dispatch")
    @ApiOperation("智能派单（按技能+负载+间隔评分）")
    public Result<?> autoDispatch(@PathVariable Long id) {
        workOrderService.autoDispatch(id, BaseContext.getCurrentId());
        return Result.success();
    }

    @GetMapping("/{id}/candidates")
    @ApiOperation("派单候选人及评分明细")
    public Result<List<DispatchCandidateVO>> candidates(@PathVariable Long id) {
        return Result.success(dispatchService.candidates(id));
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消工单")
    public Result<?> cancel(@PathVariable Long id, @RequestBody WorkOrderCancelDTO dto) {
        dto.setOrderId(id);
        workOrderService.cancel(dto, BaseContext.getCurrentId(), OperatorType.STAFF.getCode());
        return Result.success();
    }

    @PutMapping("/{id}/take")
    @ApiOperation("接单（管理端可代工程师操作）")
    public Result<?> take(@PathVariable Long id) {
        workOrderService.take(id, null);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @ApiOperation("拒单，退回派单池")
    public Result<?> rejectDispatch(@PathVariable Long id, @RequestParam(required = false) String reason) {
        workOrderService.rejectDispatch(id, reason, null);
        return Result.success();
    }

    @PutMapping("/{id}/start")
    @ApiOperation("开始处理")
    public Result<?> start(@PathVariable Long id) {
        workOrderService.start(id, null);
        return Result.success();
    }

    @PutMapping("/{id}/finish")
    @ApiOperation("提交完工")
    public Result<?> finish(@PathVariable Long id, @RequestParam(required = false) String remark) {
        workOrderService.finish(id, remark, null);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @ApiOperation("验收通过（管理端代住户操作）")
    public Result<?> confirm(@PathVariable Long id, @RequestBody WorkOrderRatingDTO dto) {
        dto.setOrderId(id);
        workOrderService.confirm(id, dto, null);
        return Result.success();
    }

    @PutMapping("/{id}/reject-accept")
    @ApiOperation("验收驳回")
    public Result<?> rejectAccept(@PathVariable Long id, @RequestBody WorkOrderRejectDTO dto) {
        dto.setOrderId(id);
        workOrderService.rejectAcceptance(dto, null);
        return Result.success();
    }
}
