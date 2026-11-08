package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住户服务清单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCart implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 住户ID */
    private Long residentId;

    /** 服务项ID */
    private Long itemId;

    /** 规格ID */
    private Long specId;

    /** 数量 */
    private Integer quantity;

    /** 创建时间 */
    private LocalDateTime createTime;

}
