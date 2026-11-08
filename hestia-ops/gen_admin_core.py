# -*- coding: utf-8 -*-
"""生成管理端核心 Controller"""
import os

CP = 'hestia-server/src/main/java/com/hestia/controller/admin'

TPL = '''package com.hestia.controller.admin;

{imports}

/**
 * {doc}
 */
@RestController
@RequestMapping("{path}")
@Api(tags = "{tag}")
@Slf4j
@RequiredArgsConstructor
public class {cls} {{

{deps}
{body}}}
'''

def imp(*lines):
    return '\n'.join(lines)


CTRLS = []

CTRLS.append(dict(
    cls='WorkOrderController', doc='工单管理', path='/admin/work-order', tag='管理端-工单',
    imports=imp(
        'import com.hestia.context.BaseContext;',
        'import com.hestia.dto.WorkOrderCancelDTO;',
        'import com.hestia.dto.WorkOrderCreateDTO;',
        'import com.hestia.dto.WorkOrderDispatchDTO;',
        'import com.hestia.dto.WorkOrderPageQueryDTO;',
        'import com.hestia.enumeration.OperatorType;',
        'import com.hestia.result.PageResult;',
        'import com.hestia.result.Result;',
        'import com.hestia.service.DispatchService;',
        'import com.hestia.service.WorkOrderService;',
        'import com.hestia.vo.DispatchCandidateVO;',
        'import com.hestia.vo.WorkOrderDetailVO;',
        'import io.swagger.annotations.Api;',
        'import io.swagger.annotations.ApiOperation;',
        'import lombok.RequiredArgsConstructor;',
        'import lombok.extern.slf4j.Slf4j;',
        'import org.springframework.web.bind.annotation.GetMapping;',
        'import org.springframework.web.bind.annotation.PathVariable;',
        'import org.springframework.web.bind.annotation.PostMapping;',
        'import org.springframework.web.bind.annotation.PutMapping;',
        'import org.springframework.web.bind.annotation.RequestBody;',
        'import org.springframework.web.bind.annotation.RequestMapping;',
        'import org.springframework.web.bind.annotation.RestController;',
        '',
        'import java.util.List;'),
    deps='''    private final WorkOrderService workOrderService;
    private final DispatchService dispatchService;
''',
    body='''    @GetMapping("/page")
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
'''))

CTRLS.append(dict(
    cls='DeviceController', doc='设备台账管理', path='/admin/device', tag='管理端-设备台账',
    imports=imp(
        'import com.hestia.dto.DeviceDTO;',
        'import com.hestia.dto.DevicePageQueryDTO;',
        'import com.hestia.result.PageResult;',
        'import com.hestia.result.Result;',
        'import com.hestia.service.DeviceService;',
        'import com.hestia.vo.DeviceVO;',
        'import io.swagger.annotations.Api;',
        'import io.swagger.annotations.ApiOperation;',
        'import lombok.RequiredArgsConstructor;',
        'import lombok.extern.slf4j.Slf4j;',
        'import org.springframework.web.bind.annotation.DeleteMapping;',
        'import org.springframework.web.bind.annotation.GetMapping;',
        'import org.springframework.web.bind.annotation.PathVariable;',
        'import org.springframework.web.bind.annotation.PostMapping;',
        'import org.springframework.web.bind.annotation.PutMapping;',
        'import org.springframework.web.bind.annotation.RequestBody;',
        'import org.springframework.web.bind.annotation.RequestMapping;',
        'import org.springframework.web.bind.annotation.RestController;',
        '',
        'import java.util.List;'),
    deps='''    private final DeviceService deviceService;
''',
    body='''    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(DevicePageQueryDTO query) {
        return Result.success(deviceService.pageQuery(query));
    }

    @GetMapping("/categories")
    @ApiOperation("设备类别列表")
    public Result<List<String>> categories() {
        return Result.success(deviceService.listCategories());
    }

    @GetMapping("/maintain-due")
    @ApiOperation("到期待维保设备")
    public Result<List<DeviceVO>> maintainDue() {
        return Result.success(deviceService.listMaintainDue());
    }

    @GetMapping("/maintain-due/count")
    @ApiOperation("待到期待维保数量")
    public Result<Long> maintainDueCount() {
        return Result.success(deviceService.countMaintainDue());
    }

    @GetMapping("/{id}")
    @ApiOperation("详情")
    public Result<DeviceVO> detail(@PathVariable Long id) {
        return Result.success(deviceService.detail(id));
    }

    @PostMapping
    @ApiOperation("新增")
    public Result<?> save(@RequestBody DeviceDTO dto) {
        deviceService.save(dto);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑")
    public Result<?> update(@RequestBody DeviceDTO dto) {
        deviceService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除")
    public Result<?> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }
'''))

CTRLS.append(dict(
    cls='InspectionController', doc='巡检计划与任务', path='/admin/inspection', tag='管理端-巡检',
    imports=imp(
        'import com.hestia.context.BaseContext;',
        'import com.hestia.dto.InspectionExecuteDTO;',
        'import com.hestia.dto.InspectionPlanDTO;',
        'import com.hestia.dto.InspectionPlanPageQueryDTO;',
        'import com.hestia.dto.InspectionTaskPageQueryDTO;',
        'import com.hestia.result.PageResult;',
        'import com.hestia.result.Result;',
        'import com.hestia.service.InspectionService;',
        'import com.hestia.vo.InspectionTaskVO;',
        'import io.swagger.annotations.Api;',
        'import io.swagger.annotations.ApiOperation;',
        'import lombok.RequiredArgsConstructor;',
        'import lombok.extern.slf4j.Slf4j;',
        'import org.springframework.web.bind.annotation.GetMapping;',
        'import org.springframework.web.bind.annotation.PathVariable;',
        'import org.springframework.web.bind.annotation.PostMapping;',
        'import org.springframework.web.bind.annotation.PutMapping;',
        'import org.springframework.web.bind.annotation.RequestBody;',
        'import org.springframework.web.bind.annotation.RequestMapping;',
        'import org.springframework.web.bind.annotation.RequestParam;',
        'import org.springframework.web.bind.annotation.RestController;',
        '',
        'import java.util.List;'),
    deps='''    private final InspectionService inspectionService;
''',
    body='''    @GetMapping("/plan/page")
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
'''))

CTRLS.append(dict(
    cls='WorkspaceController', doc='工作台', path='/admin/workspace', tag='管理端-工作台',
    imports=imp(
        'import com.hestia.result.Result;',
        'import com.hestia.service.WorkspaceService;',
        'import com.hestia.vo.StatusDistributionVO;',
        'import com.hestia.vo.TrendVO;',
        'import com.hestia.vo.WorkspaceVO;',
        'import io.swagger.annotations.Api;',
        'import io.swagger.annotations.ApiOperation;',
        'import lombok.RequiredArgsConstructor;',
        'import lombok.extern.slf4j.Slf4j;',
        'import org.springframework.web.bind.annotation.GetMapping;',
        'import org.springframework.web.bind.annotation.RequestMapping;',
        'import org.springframework.web.bind.annotation.RequestParam;',
        'import org.springframework.web.bind.annotation.RestController;',
        '',
        'import java.util.List;'),
    deps='''    private final WorkspaceService workspaceService;
''',
    body='''    @GetMapping("/overview")
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
'''))

CTRLS.append(dict(
    cls='ReportController', doc='运营报表', path='/admin/report', tag='管理端-报表',
    imports=imp(
        'import com.hestia.dto.ReportQueryDTO;',
        'import com.hestia.result.Result;',
        'import com.hestia.service.ReportService;',
        'import com.hestia.vo.CategoryReportVO;',
        'import com.hestia.vo.SlaReportVO;',
        'import com.hestia.vo.StaffPerformanceVO;',
        'import com.hestia.vo.TrendVO;',
        'import io.swagger.annotations.Api;',
        'import io.swagger.annotations.ApiOperation;',
        'import lombok.RequiredArgsConstructor;',
        'import lombok.extern.slf4j.Slf4j;',
        'import org.springframework.web.bind.annotation.GetMapping;',
        'import org.springframework.web.bind.annotation.RequestMapping;',
        'import org.springframework.web.bind.annotation.RestController;',
        '',
        'import javax.servlet.http.HttpServletResponse;',
        'import java.io.IOException;',
        'import java.util.List;'),
    deps='''    private final ReportService reportService;
''',
    body='''    @GetMapping("/sla")
    @ApiOperation("SLA 达成情况")
    public Result<SlaReportVO> sla(ReportQueryDTO dto) {
        return Result.success(reportService.slaReport(dto));
    }

    @GetMapping("/trend")
    @ApiOperation("工单趋势")
    public Result<List<TrendVO>> trend(ReportQueryDTO dto) {
        return Result.success(reportService.trend(dto));
    }

    @GetMapping("/category")
    @ApiOperation("工单类型分布")
    public Result<List<CategoryReportVO>> category(ReportQueryDTO dto) {
        return Result.success(reportService.categoryDistribution(dto));
    }

    @GetMapping("/staff")
    @ApiOperation("工程师绩效")
    public Result<List<StaffPerformanceVO>> staff(ReportQueryDTO dto) {
        return Result.success(reportService.staffPerformance(dto));
    }

    @GetMapping("/export")
    @ApiOperation("导出运营报表 Excel")
    public void export(ReportQueryDTO dto, HttpServletResponse response) throws IOException {
        byte[] data = reportService.export(dto);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=hestia-report.xlsx");
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }
'''))

CTRLS.append(dict(
    cls='NoticeController', doc='站内通知', path='/admin/notice', tag='管理端-通知',
    imports=imp(
        'import com.hestia.context.BaseContext;',
        'import com.hestia.entity.Notice;',
        'import com.hestia.result.Result;',
        'import com.hestia.service.NoticeService;',
        'import io.swagger.annotations.Api;',
        'import io.swagger.annotations.ApiOperation;',
        'import lombok.RequiredArgsConstructor;',
        'import lombok.extern.slf4j.Slf4j;',
        'import org.springframework.web.bind.annotation.GetMapping;',
        'import org.springframework.web.bind.annotation.PathVariable;',
        'import org.springframework.web.bind.annotation.PutMapping;',
        'import org.springframework.web.bind.annotation.RequestMapping;',
        'import org.springframework.web.bind.annotation.RestController;',
        '',
        'import java.util.List;'),
    deps='''    private final NoticeService noticeService;
''',
    body='''    @GetMapping("/list")
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
'''))

for c in CTRLS:
    with open(os.path.join(CP, c['cls'] + '.java'), 'w', encoding='utf-8') as f:
        f.write(TPL.format(**c))

print('generated', len(CTRLS))
