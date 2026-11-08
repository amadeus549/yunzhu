package com.hestia.task;

import com.hestia.service.InspectionService;
import com.hestia.service.SlaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 运维定时任务。
 *
 * <p>拆成两个独立任务而不是塞进一个类，避免单个任务阻塞影响其它任务：
 * <ul>
 *   <li>SLA 扫描：分钟级，处理超时打标与升级通知</li>
 *   <li>巡检任务：天级生成 + 小时级逾期标记</li>
 * </ul>
 * 所有任务都做了幂等，重复执行不会产生脏数据。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OpsScheduledTask {

    private final SlaService slaService;
    private final InspectionService inspectionService;

    /**
     * SLA 超时扫描：默认每 60 秒一次，可用 hestia.sla.scan-interval 调整
     */
    @Scheduled(fixedDelayString = "${hestia.sla.scan-interval:60000}", initialDelay = 20000)
    public void scanSlaTimeout() {
        try {
            int handled = slaService.scanTimeout();
            if (handled > 0) {
                log.info("[定时任务] SLA 超时扫描：处理 {} 条工单", handled);
            }
        } catch (Exception e) {
            log.error("[定时任务] SLA 超时扫描异常", e);
        }
    }

    /**
     * SLA 预警扫描：每 10 分钟一次，Redis 去重避免重复提醒
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 60000)
    public void scanSlaWarning() {
        try {
            int warned = slaService.scanWarning();
            if (warned > 0) {
                log.info("[定时任务] SLA 预警扫描：预警 {} 条工单", warned);
            }
        } catch (Exception e) {
            log.error("[定时任务] SLA 预警扫描异常", e);
        }
    }

    /**
     * 巡检任务生成：每天 01:05 执行
     */
    @Scheduled(cron = "0 5 1 * * ?")
    public void generateInspectionTasks() {
        try {
            int created = inspectionService.scanAndGenerate();
            log.info("[定时任务] 巡检任务生成：新增 {} 条", created);
        } catch (Exception e) {
            log.error("[定时任务] 巡检任务生成异常", e);
        }
    }

    /**
     * 巡检任务逾期标记：每小时整点
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void markInspectionOverdue() {
        try {
            int count = inspectionService.markOverdue();
            if (count > 0) {
                log.info("[定时任务] 巡检任务逾期标记：{} 条", count);
            }
        } catch (Exception e) {
            log.error("[定时任务] 巡检任务逾期标记异常", e);
        }
    }
}
