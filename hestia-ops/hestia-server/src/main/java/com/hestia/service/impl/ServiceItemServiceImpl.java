package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.dto.ServiceItemDTO;
import com.hestia.dto.ServiceItemPageQueryDTO;
import com.hestia.dto.ServiceSpecDTO;
import com.hestia.entity.ServiceItem;
import com.hestia.entity.ServiceSpec;
import com.hestia.exception.DeletionNotAllowedException;
import com.hestia.mapper.ServiceItemMapper;
import com.hestia.mapper.ServiceSpecMapper;
import com.hestia.result.PageResult;
import com.hestia.service.ServiceItemService;
import com.hestia.vo.ServiceItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务项服务实现
 */
@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemMapper itemMapper;
    private final ServiceSpecMapper specMapper;

    @Override
    public PageResult pageQuery(ServiceItemPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<ServiceItemVO> page = (Page<ServiceItemVO>) itemMapper.pageQuery(query);
        page.getResult().forEach(vo -> vo.setSpecs(toSpecDTOList(specMapper.selectByItemId(vo.getId()))));
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<ServiceItemVO> listByCategory(Long categoryId) {
        return itemMapper.selectByCategoryId(categoryId).stream().map(i -> {
            ServiceItemVO vo = new ServiceItemVO();
            BeanUtils.copyProperties(i, vo);
            vo.setSpecs(toSpecDTOList(specMapper.selectByItemId(i.getId())));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ServiceItemVO detail(Long id) {
        ServiceItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new DeletionNotAllowedException("服务项不存在");
        }
        ServiceItemVO vo = new ServiceItemVO();
        BeanUtils.copyProperties(item, vo);
        vo.setSpecs(toSpecDTOList(specMapper.selectByItemId(id)));
        return vo;
    }

    @Override
    @Transactional
    public void save(ServiceItemDTO dto) {
        ServiceItem item = new ServiceItem();
        BeanUtils.copyProperties(dto, item);
        item.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.insert(item);
        saveSpecs(item.getId(), dto.getSpecs());
    }

    @Override
    @Transactional
    public void update(ServiceItemDTO dto) {
        ServiceItem item = new ServiceItem();
        BeanUtils.copyProperties(dto, item);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.update(item);
        if (dto.getSpecs() != null) {
            specMapper.deleteByItemId(dto.getId());
            saveSpecs(dto.getId(), dto.getSpecs());
        }
    }

    @Override
    public void delete(Long id) {
        itemMapper.deleteById(id);
        specMapper.deleteByItemId(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ServiceItem item = new ServiceItem();
        item.setId(id);
        item.setStatus(status);
        item.setUpdateTime(LocalDateTime.now());
        itemMapper.update(item);
    }

    private void saveSpecs(Long itemId, List<ServiceSpecDTO> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }
        for (int i = 0; i < specs.size(); i++) {
            ServiceSpecDTO d = specs.get(i);
            ServiceSpec s = new ServiceSpec();
            s.setItemId(itemId);
            s.setName(d.getName());
            s.setPrice(d.getPrice());
            s.setDurationMin(d.getDurationMin() == null ? 60 : d.getDurationMin());
            s.setSort(d.getSort() == null ? i * 10 : d.getSort());
            s.setCreateTime(LocalDateTime.now());
            s.setUpdateTime(LocalDateTime.now());
            specMapper.insert(s);
        }
    }

    private List<ServiceSpecDTO> toSpecDTOList(List<ServiceSpec> list) {
        List<ServiceSpecDTO> result = new ArrayList<>();
        for (ServiceSpec s : list) {
            ServiceSpecDTO d = new ServiceSpecDTO();
            BeanUtils.copyProperties(s, d);
            result.add(d);
        }
        return result;
    }
}
