package com.hestia.service;

import com.hestia.dto.ServiceItemDTO;
import com.hestia.dto.ServiceItemPageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.vo.ServiceItemVO;

import java.util.List;

/**
 * 服务项服务
 */
public interface ServiceItemService {

    PageResult pageQuery(ServiceItemPageQueryDTO query);

    /** 按工单类型列出上架服务项（含规格） */
    List<ServiceItemVO> listByCategory(Long categoryId);

    ServiceItemVO detail(Long id);

    void save(ServiceItemDTO dto);

    void update(ServiceItemDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
