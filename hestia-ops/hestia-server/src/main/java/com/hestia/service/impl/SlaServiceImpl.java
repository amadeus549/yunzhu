package com.hestia.service.impl;

import com.hestia.entity.SlaRule;
import com.hestia.entity.WorkOrder;
import com.hestia.enumeration.NoticeBizType;
import com.hestia.enumeration.WorkOrderStatus;
import com.hestia.mapper.SlaRuleMapper;
import com.hestia.mapper.WorkOrderMapper;
import com.hestia.service.NoticeService;
import com.hestia.service.SlaService;
import com.hestia.vo.WorkOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SLA 时效服务实现。
 *
 * <p>核心逻辑：
 * <ul>
 *   <li>创建工单时按优先级匹配 SLA 规则，落库响应/解决两个截止时间</li>
 *   <li>定时任务扫描已过截止时间但未标记超时的工单，打标并升级通知主管</li>
 *   <li>预警扫描用 Redis SETNX 去重，避免同一工单反复提醒</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SlaServiceImpl implements SlaService {

    /** 预警阈值：剩余时间小于该值（分钟）视为预警 */
    @Value("${hestia.sla.warn-minutes:30}")
    private long warnMinutes;

    private final SlaRuleMapper slaRuleMapper;
    private final WorkOrderMapper workOrderMapper;
    private final NoticeService noticeService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void applySla(WorkOrder order, LocalDateTime baseTime) {
        SlaRule rule = slaRuleMapper.selectByPriority(order.getPriority());
        if (rule == null) {
            log.warn("未配置优先级 {} 的 SLA 规则，跳过时效计算", order.getPriority());
            return;
        }
        order.setSlaRuleId(rule.getId());
        order.setResponseDeadline(baseTime.plusMinutes(rule.getResponseMinutes()));
        order.setResolveDeadline(baseTime.plusMinutes(rule.getResolveMinutes()));
    }

    @Override
    public void fillSlaView(WorkOrderVO vo) {
        if (vo == null || vo.getStatus() == null) {
            return;
        }
        WorkOrderStatus status = WorkOrderStatus.of(vo.getStatus());
        vo.setAllowedNext(status.allowedNext());
        if (!status.isActive()) {
            vo.setSlaStatus("NORMAL");
            vo.setSlaRemainMinutes(0L);
            return;
        }
        // 未接单看响应时限，接单之后看解决时限
        LocalDateTime deadline = vo.getStatus() <= WorkOrderStatus.DISPATCHED.getCode()
                ? vo.getResponseDeadline() : vo.getResolveDeadline();
        if (deadline == null) {
            vo.setSlaStatus("NORMAL");
            vo.setSlaRemainMinutes(null);
            return;
        }
        long remain = Duration.between(LocalDateTime.now(), deadline).toMinutes();
        vo.setSlaRemainMinutes(remain);
        if (remain < 0) {
            vo.setSlaStatus("OVERDUE");
        } else if (remain <= warnMinutes) {
            vo.setSlaStatus("WARNING");
        } else {
            vo.setSlaStatus("NORMAL");
        }
    }

    @Override
    @Transactional
    public int scanTimeout() {
        List<WorkOrder> list = workOrderMapper.selectTimeoutOrders(LocalDateTime.now());
        int count = 0;
        for (WorkOrder o : list) {
            boolean responsePhase = o.getStatus() <= WorkOrderStatus.DISPATCHED.getCode();
            WorkOrder update = new WorkOrder();
            update.setId(o.getId());
            update.setUpdateTime(LocalDateTime.now());
            if (responsePhase) {
                update.setResponseBreached(1);
            } else {
                update.setResolveBreached(1);
            }
            workOrderMapper.update(update);

            String phase = responsePhase ? "响应" : "解决";
            String title = "SLA 超时升级";
            String content = String.format("工单 %s（%s）%s已超时，请主管介入", o.getOrderNo(), o.getTitle(), phase);
            // 升级通知主管（role=3），演示环境固定通知 id=5 的主管
            noticeService.push(5L, 1, NoticeBizType.SLA_WARNING.getCode(), o.getId(), title, content);
            if (o.getAssigneeId() != null) {
                noticeService.push(o.getAssigneeId(), 1, NoticeBizType.SLA_WARNING.getCode(), o.getId(), title, content);
            }
            log.warn("工单 {} {}超时，已升级", o.getOrderNo(), phase);
            count++;
        }
        return count;
    }

    @Override
    public int scanWarning() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkOrder> list = workOrderMapper.selectWarningOrders(now, now.plusMinutes(warnMinutes));
        int count = 0;
        for (WorkOrder o : list) {
            String dedupKey = "hestia:sla:warn:" + o.getId();
            Boolean first = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", 6, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(first)) {
                continue;
            }
            long remain = Duration.between(now, o.getResolveDeadline()).toMinutes();
            String content = String.format("工单 %s（%s）距解决时限仅剩 %d 分钟", o.getOrderNo(), o.getTitle(), remain);
            if (o.getAssigneeId() != null) {
                noticeService.push(o.getAssigneeId(), 1, NoticeBizType.SLA_WARNING.getCode(), o.getId(),
                        "SLA 超时预警", content);
            } else {
                noticeService.push(1L, 1, NoticeBizType.SLA_WARNING.getCode(), o.getId(), "SLA 超时预警", content);
            }
            count++;
        }
        return count;
    }
}
