package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.dto.ServicePackageDTO;
import com.hestia.dto.ServicePackageItemDTO;
import com.hestia.dto.ServicePackagePageQueryDTO;
import com.hestia.entity.ServicePackage;
import com.hestia.entity.ServicePackageItem;
import com.hestia.mapper.ServicePackageItemMapper;
import com.hestia.mapper.ServicePackageMapper;
import com.hestia.result.PageResult;
import com.hestia.service.ServicePackageService;
import com.hestia.vo.ServicePackageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务包服务实现
 */
@Service
@RequiredArgsConstructor
public class ServicePackageServiceImpl implements ServicePackageService {

    private final ServicePackageMapper packageMapper;
    private final ServicePackageItemMapper packageItemMapper;

    @Override
    public PageResult pageQuery(ServicePackagePageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<ServicePackageVO> page = (Page<ServicePackageVO>) packageMapper.pageQuery(query);
        page.getResult().forEach(vo -> vo.setItems(toDTOList(packageItemMapper.selectByPackageId(vo.getId()))));
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public ServicePackageVO detail(Long id) {
        ServicePackage pkg = packageMapper.selectById(id);
        ServicePackageVO vo = new ServicePackageVO();
        BeanUtils.copyProperties(pkg, vo);
        vo.setItems(toDTOList(packageItemMapper.selectByPackageId(id)));
        return vo;
    }

    @Override
    @Transactional
    public void save(ServicePackageDTO dto) {
        ServicePackage pkg = new ServicePackage();
        BeanUtils.copyProperties(dto, pkg);
        pkg.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        pkg.setValidDays(dto.getValidDays() == null ? 365 : dto.getValidDays());
        pkg.setCreateTime(LocalDateTime.now());
        pkg.setUpdateTime(LocalDateTime.now());
        packageMapper.insert(pkg);
        saveItems(pkg.getId(), dto.getItems());
    }

    @Override
    @Transactional
    public void update(ServicePackageDTO dto) {
        ServicePackage pkg = new ServicePackage();
        BeanUtils.copyProperties(dto, pkg);
        pkg.setUpdateTime(LocalDateTime.now());
        packageMapper.update(pkg);
        if (dto.getItems() != null) {
            packageItemMapper.deleteByPackageId(dto.getId());
            saveItems(dto.getId(), dto.getItems());
        }
    }

    @Override
    public void delete(Long id) {
        packageMapper.deleteById(id);
        packageItemMapper.deleteByPackageId(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ServicePackage pkg = new ServicePackage();
        pkg.setId(id);
        pkg.setStatus(status);
        pkg.setUpdateTime(LocalDateTime.now());
        packageMapper.update(pkg);
    }

    private void saveItems(Long packageId, List<ServicePackageItemDTO> items) {
        if (items == null) {
            return;
        }
        for (ServicePackageItemDTO d : items) {
            ServicePackageItem pi = new ServicePackageItem();
            pi.setPackageId(packageId);
            pi.setItemId(d.getItemId());
            pi.setQuantity(d.getQuantity() == null ? 1 : d.getQuantity());
            pi.setTimes(d.getTimes() == null ? 1 : d.getTimes());
            packageItemMapper.insert(pi);
        }
    }

    private List<ServicePackageItemDTO> toDTOList(List<ServicePackageItem> list) {
        List<ServicePackageItemDTO> result = new ArrayList<>();
        for (ServicePackageItem pi : list) {
            ServicePackageItemDTO d = new ServicePackageItemDTO();
            BeanUtils.copyProperties(pi, d);
            result.add(d);
        }
        return result;
    }
}
