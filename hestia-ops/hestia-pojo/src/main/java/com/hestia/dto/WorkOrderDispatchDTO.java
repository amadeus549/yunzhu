package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 派单
 */
@Data
public class WorkOrderDispatchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单 */
    private Long orderId;

    /** 工程师 */
    private Long staffId;

    /** 备注 */
    private String remark;

}
