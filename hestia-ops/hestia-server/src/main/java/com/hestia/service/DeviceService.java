package com.hestia.service;

import com.hestia.dto.DeviceDTO;
import com.hestia.dto.DevicePageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.vo.DeviceVO;

import java.util.List;

/**
 * 设备台账服务
 */
public interface DeviceService {

    PageResult pageQuery(DevicePageQueryDTO query);

    DeviceVO detail(Long id);

    void save(DeviceDTO dto);

    void update(DeviceDTO dto);

    void delete(Long id);

    List<String> listCategories();

    /** 到期待维保设备数量 */
    Long countMaintainDue();

    /** 到期待维保设备列表 */
    List<DeviceVO> listMaintainDue();
}
