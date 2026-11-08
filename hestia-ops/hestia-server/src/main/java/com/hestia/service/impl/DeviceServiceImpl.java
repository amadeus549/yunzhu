package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.dto.DeviceDTO;
import com.hestia.dto.DevicePageQueryDTO;
import com.hestia.entity.Device;
import com.hestia.enumeration.DeviceStatus;
import com.hestia.exception.DeletionNotAllowedException;
import com.hestia.mapper.DeviceMapper;
import com.hestia.result.PageResult;
import com.hestia.service.DeviceService;
import com.hestia.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备台账服务实现
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceMapper deviceMapper;

    @Override
    public PageResult pageQuery(DevicePageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<DeviceVO> page = (Page<DeviceVO>) deviceMapper.pageQuery(query);
        page.getResult().forEach(vo -> vo.setStatusName(DeviceStatus.descOf(vo.getStatus())));
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public DeviceVO detail(Long id) {
        Device d = deviceMapper.selectById(id);
        if (d == null) {
            throw new DeletionNotAllowedException("设备不存在");
        }
        DeviceVO vo = new DeviceVO();
        BeanUtils.copyProperties(d, vo);
        vo.setStatusName(DeviceStatus.descOf(d.getStatus()));
        vo.setMaintainDue(d.getNextMaintainDate() != null && !d.getNextMaintainDate().isAfter(LocalDate.now()) ? 1 : 0);
        vo.setWarrantyExpired(d.getWarrantyEnd() != null && d.getWarrantyEnd().isBefore(LocalDate.now()) ? 1 : 0);
        return vo;
    }

    @Override
    public void save(DeviceDTO dto) {
        Device device = new Device();
        BeanUtils.copyProperties(dto, device);
        device.setStatus(dto.getStatus() == null ? DeviceStatus.NORMAL.getCode() : dto.getStatus());
        device.setMaintainCycleDays(dto.getMaintainCycleDays() == null ? 90 : dto.getMaintainCycleDays());
        // 根据安装日期 + 维保周期推算下次维保日期
        device.setLastMaintainDate(dto.getInstallDate());
        if (dto.getInstallDate() != null) {
            device.setNextMaintainDate(dto.getInstallDate().plusDays(device.getMaintainCycleDays()));
        }
        device.setCreateTime(LocalDateTime.now());
        device.setUpdateTime(LocalDateTime.now());
        deviceMapper.insert(device);
    }

    @Override
    public void update(DeviceDTO dto) {
        Device device = new Device();
        BeanUtils.copyProperties(dto, device);
        if (dto.getInstallDate() != null && dto.getMaintainCycleDays() != null) {
            device.setNextMaintainDate(dto.getInstallDate().plusDays(dto.getMaintainCycleDays()));
        }
        device.setUpdateTime(LocalDateTime.now());
        deviceMapper.update(device);
    }

    @Override
    public void delete(Long id) {
        deviceMapper.deleteById(id);
    }

    @Override
    public List<String> listCategories() {
        return deviceMapper.selectCategories();
    }

    @Override
    public Long countMaintainDue() {
        return deviceMapper.countMaintainDue(LocalDate.now());
    }

    @Override
    public List<DeviceVO> listMaintainDue() {
        DevicePageQueryDTO q = new DevicePageQueryDTO();
        q.setPage(1);
        q.setPageSize(50);
        q.setMaintainDue(1);
        PageHelper.startPage(1, 50);
        List<DeviceVO> list = deviceMapper.pageQuery(q);
        list.forEach(vo -> vo.setStatusName(DeviceStatus.descOf(vo.getStatus())));
        return list;
    }
}
