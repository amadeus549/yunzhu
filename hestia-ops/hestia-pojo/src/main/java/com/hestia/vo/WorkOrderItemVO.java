package com.hestia.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工单明细项
 */
@Data
public class WorkOrderItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 服务项ID */
    private Long itemId;

    /** 服务项 */
    private String itemName;

    /** 规格ID */
    private Long specId;

    /** 规格 */
    private String specName;

    /** 单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 小计 */
    private BigDecimal amount;

}
