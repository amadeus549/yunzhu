package com.hestia.service.impl;

import com.hestia.entity.Staff;
import com.hestia.enumeration.WorkOrderStatus;
import com.hestia.mapper.StaffMapper;
import com.hestia.mapper.WorkOrderMapper;
import com.hestia.service.WorkspaceService;
import com.hestia.vo.StatusDistributionVO;
import com.hestia.vo.TrendVO;
import com.hestia.vo.WorkspaceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作台服务实现
 */
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkOrderMapper orderMapper;
    private final StaffMapper staffMapper;

    @Override
    public WorkspaceVO overview() {
        WorkspaceVO vo = new WorkspaceVO();
        vo.setTodayNew(nvl(orderMapper.countTodayNew()));
        vo.setPendingAccept(nvl(orderMapper.countByStatus(WorkOrderStatus.PENDING.getCode())));
        vo.setPendingDispatch(nvl(orderMapper.countByStatus(WorkOrderStatus.PENDING_DISPATCH.getCode())));
        // 处理中口径：已接单 + 处理中 + 已驳回（返工）
        vo.setProcessing(nvl(orderMapper.countByStatus(WorkOrderStatus.ACCEPTED.getCode()))
                + nvl(orderMapper.countByStatus(WorkOrderStatus.PROCESSING.getCode()))
                + nvl(orderMapper.countByStatus(WorkOrderStatus.REJECTED.getCode())));
        vo.setPendingAcceptance(nvl(orderMapper.countByStatus(WorkOrderStatus.PENDING_ACCEPT.getCode())));
        vo.setTodayCompleted(nvl(orderMapper.countTodayCompleted()));
        vo.setSlaOverdue(nvl(orderMapper.countSlaOverdue()));
        vo.setUrgentCount(nvl(orderMapper.countUrgentActive()));

        long busy = 0;
        List<Staff> engineers = staffMapper.selectEngineers(null);
        for (Staff s : engineers) {
            if (nvl(staffMapper.countActiveOrders(s.getId())) > 0) {
                busy++;
            }
        }
        vo.setEngineerBusy(busy);
        vo.setEngineerIdle(Math.max(0, engineers.size() - busy));

        LocalDateTime begin = LocalDate.now().atStartOfDay();
        LocalDateTime end = begin.plusDays(1);
        Double avg = orderMapper.avgResponseMinutes(begin, end);
        vo.setAvgResponseMinutes(avg == null ? 0.0 : Math.round(avg * 10) / 10.0);
        Double rate = orderMapper.onTimeRate(begin, end);
        vo.setOnTimeRate(rate == null ? 0.0 : rate);
        return vo;
    }

    @Override
    public List<StatusDistributionVO> statusDistribution() {
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime begin = end.minusDays(30);
        List<StatusDistributionVO> list = orderMapper.countGroupByStatus(begin, end);
        list.forEach(v -> v.setStatusName(WorkOrderStatus.descOf(v.getStatus())));
        return list;
    }

    @Override
    public List<TrendVO> recentTrend(int days) {
        int n = days <= 0 ? 7 : days;
        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusDays(n - 1L);
        List<TrendVO> raw = orderMapper.countTrend(begin.atStartOfDay(), end.plusDays(1).atStartOfDay());
        return fillDates(raw, begin, end);
    }

    /** 补齐没有数据的日期，保证前端折线图连续 */
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

    private long nvl(Long v) {
        return v == null ? 0L : v;
    }
}
