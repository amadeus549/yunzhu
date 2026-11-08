package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务规格
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 服务项ID */
    private Long itemId;

    /** 规格名称 */
    private String name;

    /** 该规格价格 */
    private BigDecimal price;

    /** 该规格耗时 */
    private Integer durationMin;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
