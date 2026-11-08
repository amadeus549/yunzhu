package com.hestia.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务包明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 服务包ID */
    private Long packageId;

    /** 服务项ID */
    private Long itemId;

    /** 单次数量 */
    private Integer quantity;

    /** 包含次数 */
    private Integer times;

}
