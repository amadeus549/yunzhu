package com.hestia.controller.admin;

import com.hestia.dto.ReportQueryDTO;
import com.hestia.result.Result;
import com.hestia.service.ReportService;
import com.hestia.vo.CategoryReportVO;
import com.hestia.vo.SlaReportVO;
import com.hestia.vo.StaffPerformanceVO;
import com.hestia.vo.TrendVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 运营报表
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "管理端-报表")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sla")
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
}
