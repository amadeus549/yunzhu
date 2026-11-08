package com.hestia.service;

import com.hestia.dto.CartDTO;
import com.hestia.vo.CartVO;

import java.util.List;

/**
 * 住户服务清单（报修草稿篮）
 */
public interface CartService {

    List<CartVO> list(Long residentId);

    void add(Long residentId, CartDTO dto);

    void updateQuantity(Long residentId, Long cartId, Integer quantity);

    void remove(Long residentId, Long cartId);

    void clear(Long residentId);
}
