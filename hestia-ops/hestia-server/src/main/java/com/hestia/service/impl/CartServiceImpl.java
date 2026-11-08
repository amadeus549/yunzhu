package com.hestia.service.impl;

import com.hestia.dto.CartDTO;
import com.hestia.entity.ServiceCart;
import com.hestia.entity.ServiceItem;
import com.hestia.entity.ServiceSpec;
import com.hestia.exception.ServiceCartBusinessException;
import com.hestia.mapper.ServiceCartMapper;
import com.hestia.mapper.ServiceItemMapper;
import com.hestia.mapper.ServiceSpecMapper;
import com.hestia.service.CartService;
import com.hestia.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 住户服务清单实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ServiceCartMapper cartMapper;
    private final ServiceItemMapper itemMapper;
    private final ServiceSpecMapper specMapper;

    @Override
    public List<CartVO> list(Long residentId) {
        List<CartVO> result = new ArrayList<>();
        for (ServiceCart c : cartMapper.selectByResidentId(residentId)) {
            ServiceItem item = itemMapper.selectById(c.getItemId());
            if (item == null) {
                continue;
            }
            CartVO vo = new CartVO();
            vo.setId(c.getId());
            vo.setItemId(c.getItemId());
            vo.setItemName(item.getName());
            BigDecimal price = item.getPrice();
            if (c.getSpecId() != null) {
                ServiceSpec spec = specMapper.selectById(c.getSpecId());
                if (spec != null) {
                    vo.setSpecId(spec.getId());
                    vo.setSpecName(spec.getName());
                    price = spec.getPrice();
                }
            }
            vo.setPrice(price);
            vo.setQuantity(c.getQuantity());
            vo.setAmount(price.multiply(BigDecimal.valueOf(c.getQuantity())));
            result.add(vo);
        }
        return result;
    }

    @Override
    public void add(Long residentId, CartDTO dto) {
        if (itemMapper.selectById(dto.getItemId()) == null) {
            throw new ServiceCartBusinessException("服务项不存在");
        }
        int quantity = dto.getQuantity() == null || dto.getQuantity() <= 0 ? 1 : dto.getQuantity();
        ServiceCart exist = cartMapper.selectOne(residentId, dto.getItemId(), dto.getSpecId());
        if (exist != null) {
            cartMapper.updateQuantity(exist.getId(), exist.getQuantity() + quantity);
            return;
        }
        ServiceCart cart = new ServiceCart();
        cart.setResidentId(residentId);
        cart.setItemId(dto.getItemId());
        cart.setSpecId(dto.getSpecId());
        cart.setQuantity(quantity);
        cart.setCreateTime(LocalDateTime.now());
        cartMapper.insert(cart);
    }

    @Override
    public void updateQuantity(Long residentId, Long cartId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            cartMapper.deleteById(cartId);
            return;
        }
        cartMapper.updateQuantity(cartId, quantity);
    }

    @Override
    public void remove(Long residentId, Long cartId) {
        cartMapper.deleteById(cartId);
    }

    @Override
    public void clear(Long residentId) {
        cartMapper.deleteByResidentId(residentId);
    }
}
