package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 类型分布
 */
@Data
public class CategoryReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 类型ID */
    private Long categoryId;

    /** 类型名称 */
    private String categoryName;

    /** 工单数 */
    private Long count;

}
