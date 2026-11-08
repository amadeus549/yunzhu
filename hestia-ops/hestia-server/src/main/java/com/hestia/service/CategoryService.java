package com.hestia.service;

import com.hestia.dto.CategoryDTO;
import com.hestia.dto.CategoryPageQueryDTO;
import com.hestia.entity.WorkOrderCategory;
import com.hestia.result.PageResult;

import java.util.List;

/**
 * 工单类型服务
 */
public interface CategoryService {

    List<WorkOrderCategory> listEnabled();

    PageResult pageQuery(CategoryPageQueryDTO query);

    void save(CategoryDTO dto);

    void update(CategoryDTO dto);

    void updateStatus(Long id, Integer status);
}
