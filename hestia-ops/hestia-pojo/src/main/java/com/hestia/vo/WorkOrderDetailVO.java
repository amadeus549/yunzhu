package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工单详情
 */
@Data
public class WorkOrderDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单主信息 */
    private WorkOrderVO order;

    /** 服务项明细 */
    private List<WorkOrderItemVO> items;

    /** 流转日志 */
    private List<WorkOrderLogVO> logs;

    /** 附件地址 */
    private List<String> attachments;

}
