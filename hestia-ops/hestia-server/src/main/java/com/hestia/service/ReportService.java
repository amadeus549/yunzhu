package com.hestia.service;

import com.hestia.dto.ReportQueryDTO;
import com.hestia.vo.CategoryReportVO;
import com.hestia.vo.SlaReportVO;
import com.hestia.vo.StaffPerformanceVO;
import com.hestia.vo.TrendVO;

import java.util.List;

/**
 * 报表服务
 */
public interface ReportService {

    SlaReportVO slaReport(ReportQueryDTO dto);

    List<TrendVO> trend(ReportQueryDTO dto);

    List<CategoryReportVO> categoryDistribution(ReportQueryDTO dto);

    List<StaffPerformanceVO> staffPerformance(ReportQueryDTO dto);

    /** 导出运营报表 Excel */
    byte[] export(ReportQueryDTO dto);
}
