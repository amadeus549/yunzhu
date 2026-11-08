package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工单明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 工单ID */
    private Long orderId;

    /** 服务项ID */
    private Long itemId;

    /** 服务项名称快照 */
    private String itemName;

    /** 规格ID */
    private Long specId;

    /** 规格名称快照 */
    private String specName;

    /** 单价快照 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 小计 */
    private BigDecimal amount;

}
