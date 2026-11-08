package com.hestia.service;

import com.hestia.dto.ServicePackageDTO;
import com.hestia.dto.ServicePackagePageQueryDTO;
import com.hestia.result.PageResult;
import com.hestia.vo.ServicePackageVO;

/**
 * 服务包服务
 */
public interface ServicePackageService {

    PageResult pageQuery(ServicePackagePageQueryDTO query);

    ServicePackageVO detail(Long id);

    void save(ServicePackageDTO dto);

    void update(ServicePackageDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
