package com.hestia.service;

import com.hestia.entity.WorkOrder;
import com.hestia.vo.WorkOrderVO;

import java.time.LocalDateTime;

/**
 * SLA 时效服务
 */
public interface SlaService {

    /** 根据优先级计算响应/解决截止时间 */
    void applySla(WorkOrder order, LocalDateTime baseTime);

    /** 补充展示用的 SLA 状态与剩余时间 */
    void fillSlaView(WorkOrderVO vo);

    /** 扫描并处理超时工单，返回本次处理数量 */
    int scanTimeout();

    /** 扫描即将到期的工单并预警，返回预警数量 */
    int scanWarning();
}
