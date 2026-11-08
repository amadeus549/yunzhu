package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 验收评价
 */
@Data
public class WorkOrderRatingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单 */
    private Long orderId;

    /** 评分1-5 */
    private Integer rating;

    /** 评价内容 */
    private String comment;

}
