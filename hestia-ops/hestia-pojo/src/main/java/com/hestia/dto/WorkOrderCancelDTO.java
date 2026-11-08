package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 取消工单
 */
@Data
public class WorkOrderCancelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单 */
    private Long orderId;

    /** 取消原因 */
    private String reason;

}
