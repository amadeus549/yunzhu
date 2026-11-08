package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.dto.CategoryDTO;
import com.hestia.dto.CategoryPageQueryDTO;
import com.hestia.entity.WorkOrderCategory;
import com.hestia.mapper.WorkOrderCategoryMapper;
import com.hestia.result.PageResult;
import com.hestia.service.CategoryService;
import com.hestia.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单类型服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final WorkOrderCategoryMapper categoryMapper;

    @Override
    public List<WorkOrderCategory> listEnabled() {
        return categoryMapper.selectEnabled();
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<CategoryVO> page = (Page<CategoryVO>) categoryMapper.pageQuery(query);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void save(CategoryDTO dto) {
        WorkOrderCategory c = new WorkOrderCategory();
        BeanUtils.copyProperties(dto, c);
        // biz_type / status 列均为 NOT NULL，调用方漏传时给默认值，避免插入直接报错
        if (c.getBizType() == null) {
            c.setBizType(1);
        }
        if (c.getStatus() == null) {
            c.setStatus(1);
        }
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(c);
    }

    @Override
    public void update(CategoryDTO dto) {
        WorkOrderCategory c = new WorkOrderCategory();
        BeanUtils.copyProperties(dto, c);
        c.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(c);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        WorkOrderCategory c = new WorkOrderCategory();
        c.setId(id);
        c.setStatus(status);
        c.setUpdateTime(LocalDateTime.now());
        categoryMapper.update(c);
    }
}
