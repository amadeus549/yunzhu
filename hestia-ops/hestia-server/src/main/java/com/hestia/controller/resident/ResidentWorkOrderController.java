package com.hestia.controller.resident;

import com.hestia.context.BaseContext;
import com.hestia.dto.WorkOrderCancelDTO;
import com.hestia.dto.WorkOrderRatingDTO;
import com.hestia.dto.WorkOrderRejectDTO;
import com.hestia.dto.WorkOrderSubmitDTO;
import com.hestia.enumeration.OperatorType;
import com.hestia.result.PageResult;
import com.hestia.result.Result;
import com.hestia.service.WorkOrderService;
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

/**
 * 住户端报修工单
 */
@RestController
@RequestMapping("/resident/work-order")
@Api(tags = "住户端-报修工单")
@Slf4j
@RequiredArgsConstructor
public class ResidentWorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    @ApiOperation("提交报修")
    public Result<Long> submit(@RequestBody WorkOrderSubmitDTO dto) {
        return Result.success(workOrderService.submit(dto, BaseContext.getCurrentId()));
    }

    @GetMapping("/page")
    @ApiOperation("我的工单分页")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer status) {
        return Result.success(workOrderService.pageQueryByResident(
                BaseContext.getCurrentId(), page, pageSize, status));
    }

    @GetMapping("/{id}")
    @ApiOperation("工单详情")
    public Result<WorkOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(workOrderService.detail(id));
    }

    @PutMapping("/{id}/confirm")
    @ApiOperation("验收通过并评价")
    public Result<?> confirm(@PathVariable Long id, @RequestBody WorkOrderRatingDTO dto) {
        dto.setOrderId(id);
        workOrderService.confirm(id, dto, BaseContext.getCurrentId());
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @ApiOperation("验收驳回")
    public Result<?> reject(@PathVariable Long id, @RequestBody WorkOrderRejectDTO dto) {
        dto.setOrderId(id);
        workOrderService.rejectAcceptance(dto, BaseContext.getCurrentId());
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消工单")
    public Result<?> cancel(@PathVariable Long id, @RequestBody WorkOrderCancelDTO dto) {
        dto.setOrderId(id);
        workOrderService.cancel(dto, BaseContext.getCurrentId(), OperatorType.RESIDENT.getCode());
        return Result.success();
    }

    @PostMapping("/{id}/urge")
    @ApiOperation("催单")
    public Result<?> urge(@PathVariable Long id) {
        workOrderService.urge(id, BaseContext.getCurrentId());
        return Result.success();
    }
}
