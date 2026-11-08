package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务包明细
 */
@Data
public class ServicePackageItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 服务项 */
    private Long itemId;

    /** 单次数量 */
    private Integer quantity;

    /** 次数 */
    private Integer times;

}
