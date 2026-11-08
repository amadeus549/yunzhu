package com.hestia.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务清单项
 */
@Data
public class CartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 服务项 */
    private Long itemId;

    /** 规格 */
    private Long specId;

    /** 数量 */
    private Integer quantity;

}
