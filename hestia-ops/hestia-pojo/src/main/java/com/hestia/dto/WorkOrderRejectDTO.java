package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 验收驳回
 */
@Data
public class WorkOrderRejectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单 */
    private Long orderId;

    /** 驳回原因 */
    private String reason;

}
