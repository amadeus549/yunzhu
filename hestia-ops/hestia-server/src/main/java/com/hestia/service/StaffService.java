package com.hestia.service;

import com.hestia.dto.PasswordEditDTO;
import com.hestia.dto.StaffDTO;
import com.hestia.dto.StaffLoginDTO;
import com.hestia.dto.StaffPageQueryDTO;
import com.hestia.entity.Staff;
import com.hestia.result.PageResult;
import com.hestia.vo.StaffVO;

import java.util.List;

/**
 * 员工服务
 */
public interface StaffService {

    /** 登录校验，返回员工实体 */
    Staff login(StaffLoginDTO dto);

    PageResult pageQuery(StaffPageQueryDTO query);

    StaffVO detail(Long id);

    void save(StaffDTO dto);

    void update(StaffDTO dto);

    void updateStatus(Long id, Integer status);

    /** 按技能筛选在岗工程师 */
    List<Staff> listEngineers(String skill);

    void editPassword(Long staffId, PasswordEditDTO dto);
}
