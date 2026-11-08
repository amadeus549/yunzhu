package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 巡检执行
 */
@Data
public class InspectionExecuteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务 */
    private Long taskId;

    /** 结果 */
    private Integer result;

    /** 异常说明 */
    private String abnormalDesc;

    /** 是否转工单 0否 1是 */
    private Integer createOrder;

}
