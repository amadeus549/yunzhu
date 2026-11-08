package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工单类型列表项
 */
@Data
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 业务类型 */
    private Integer bizType;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private Integer status;

    /** 关联服务项数 */
    private Long itemCount;

}
