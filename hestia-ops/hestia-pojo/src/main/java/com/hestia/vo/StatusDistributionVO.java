package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 状态分布
 */
@Data
public class StatusDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer status;

    /** 状态名称 */
    private String statusName;

    /** 数量 */
    private Long count;

}
