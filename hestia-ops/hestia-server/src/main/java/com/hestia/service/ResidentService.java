package com.hestia.service;

import com.hestia.dto.ResidentDTO;
import com.hestia.dto.ResidentPageQueryDTO;
import com.hestia.entity.Resident;
import com.hestia.result.PageResult;
import com.hestia.vo.ResidentVO;

import java.util.List;

/**
 * 住户服务
 */
public interface ResidentService {

    /** 手机号 + 验证码登录（演示环境验证码固定 123456） */
    Resident login(String phone, String code);

    PageResult pageQuery(ResidentPageQueryDTO query);

    List<ResidentVO> listAll();

    void save(ResidentDTO dto);

    void update(ResidentDTO dto);
}
