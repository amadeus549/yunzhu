package com.hestia.service.impl;

import com.hestia.dto.ReportQueryDTO;
import com.hestia.mapper.WorkOrderMapper;
import com.hestia.service.ReportService;
import com.hestia.vo.CategoryReportVO;
import com.hestia.vo.SlaReportVO;
import com.hestia.vo.StaffPerformanceVO;
import com.hestia.vo.TrendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 报表服务实现。
 *
 * <p>全部指标由 SQL 聚合完成，避免把明细拉回应用层再计算。
 * 导出的 Excel 分四个 Sheet：SLA 达成、工单趋势、工程师绩效、类型分布。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final WorkOrderMapper orderMapper;

    @Override
    public SlaReportVO slaReport(ReportQueryDTO dto) {
        LocalDate[] range = normalize(dto);
        SlaReportVO vo = orderMapper.slaReport(range[0].atStartOfDay(), range[1].plusDays(1).atStartOfDay());
        return vo == null ? new SlaReportVO() : vo;
    }

    @Override
    public List<TrendVO> trend(ReportQueryDTO dto) {
        LocalDate[] range = normalize(dto);
        List<TrendVO> raw = orderMapper.countTrend(range[0].atStartOfDay(), range[1].plusDays(1).atStartOfDay());
        return fillDates(raw, range[0], range[1]);
    }

    @Override
    public List<CategoryReportVO> categoryDistribution(ReportQueryDTO dto) {
        LocalDate[] range = normalize(dto);
        return orderMapper.countGroupByCategory(range[0].atStartOfDay(), range[1].plusDays(1).atStartOfDay());
    }

    @Override
    public List<StaffPerformanceVO> staffPerformance(ReportQueryDTO dto) {
        LocalDate[] range = normalize(dto);
        return orderMapper.staffPerformance(range[0].atStartOfDay(), range[1].plusDays(1).atStartOfDay());
    }

    @Override
    public byte[] export(ReportQueryDTO dto) {
        LocalDate[] range = normalize(dto);
        SlaReportVO sla = slaReport(dto);
        List<TrendVO> trend = trend(dto);
        List<StaffPerformanceVO> staff = staffPerformance(dto);
        List<CategoryReportVO> categories = categoryDistribution(dto);

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle head = headStyle(wb);

            Sheet s1 = wb.createSheet("SLA达成");
            int r = 0;
            r = writeRow(s1, r, head, "指标", "数值");
            r = writeRow(s1, r, null, "统计区间", range[0] + " ~ " + range[1]);
            r = writeRow(s1, r, null, "工单总数", str(sla.getTotal()));
            r = writeRow(s1, r, null, "响应达标数", str(sla.getResponseOnTime()));
            r = writeRow(s1, r, null, "响应达标率(%)", str(sla.getResponseRate()));
            r = writeRow(s1, r, null, "解决达标数", str(sla.getResolveOnTime()));
            r = writeRow(s1, r, null, "解决达标率(%)", str(sla.getResolveRate()));
            r = writeRow(s1, r, null, "平均响应时长(分钟)", str(sla.getAvgResponseMinutes()));
            writeRow(s1, r, null, "平均解决时长(分钟)", str(sla.getAvgResolveMinutes()));
            s1.setColumnWidth(0, 6000);
            s1.setColumnWidth(1, 8000);

            Sheet s2 = wb.createSheet("工单趋势");
            r = 0;
            r = writeRow(s2, r, head, "日期", "新增", "完成");
            for (TrendVO t : trend) {
                r = writeRow(s2, r, null, String.valueOf(t.getDate()), str(t.getCreated()), str(t.getCompleted()));
            }
            s2.setColumnWidth(0, 4000);

            Sheet s3 = wb.createSheet("工程师绩效");
            r = 0;
            r = writeRow(s3, r, head, "工程师", "接单总数", "完成数", "平均解决时长(分钟)", "按时率(%)", "平均评分");
            for (StaffPerformanceVO p : staff) {
                r = writeRow(s3, r, null, p.getStaffName(), str(p.getTotal()), str(p.getCompleted()),
                        str(p.getAvgResolveMinutes()), str(p.getOnTimeRate()), str(p.getAvgRating()));
            }
            s3.setColumnWidth(0, 4000);

            Sheet s4 = wb.createSheet("类型分布");
            r = 0;
            r = writeRow(s4, r, head, "工单类型", "工单数");
            for (CategoryReportVO c : categories) {
                r = writeRow(s4, r, null, c.getCategoryName(), str(c.getCount()));
            }
            s4.setColumnWidth(0, 5000);

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出报表失败", e);
            throw new RuntimeException("导出报表失败", e);
        }
    }

    // ==================== 私有 ====================

    private LocalDate[] normalize(ReportQueryDTO dto) {
        LocalDate end = (dto == null || dto.getEnd() == null) ? LocalDate.now() : dto.getEnd();
        LocalDate begin = (dto == null || dto.getBegin() == null) ? end.minusDays(29) : dto.getBegin();
        if (begin.isAfter(end)) {
            LocalDate tmp = begin;
            begin = end;
            end = tmp;
        }
        return new LocalDate[]{begin, end};
    }

    private List<TrendVO> fillDates(List<TrendVO> raw, LocalDate begin, LocalDate end) {
        List<TrendVO> result = new ArrayList<>();
        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            TrendVO hit = null;
            for (TrendVO t : raw) {
                if (d.equals(t.getDate())) {
                    hit = t;
                    break;
                }
            }
            if (hit == null) {
                hit = new TrendVO();
                hit.setDate(d);
                hit.setCreated(0L);
                hit.setCompleted(0L);
            }
            result.add(hit);
        }
        return result;
    }

    private CellStyle headStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private int writeRow(Sheet sheet, int rowIndex, CellStyle headStyle, String... values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values[i]);
            if (headStyle != null) {
                cell.setCellStyle(headStyle);
            }
        }
        return rowIndex + 1;
    }

    private String str(Object v) {
        return v == null ? "0" : String.valueOf(v);
    }
}
