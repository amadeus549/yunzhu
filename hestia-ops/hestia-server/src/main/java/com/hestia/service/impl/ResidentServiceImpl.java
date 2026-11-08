package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.constant.MessageConstant;
import com.hestia.dto.ResidentDTO;
import com.hestia.dto.ResidentPageQueryDTO;
import com.hestia.entity.Resident;
import com.hestia.exception.AccountNotFoundException;
import com.hestia.mapper.ResidentMapper;
import com.hestia.result.PageResult;
import com.hestia.service.ResidentService;
import com.hestia.vo.ResidentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 住户服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResidentServiceImpl implements ResidentService {

    /** 演示环境固定验证码 */
    private static final String DEMO_CODE = "123456";

    private final ResidentMapper residentMapper;

    @Override
    public Resident login(String phone, String code) {
        if (!DEMO_CODE.equals(code)) {
            throw new AccountNotFoundException("验证码错误");
        }
        Resident resident = residentMapper.selectByPhone(phone);
        if (resident == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (resident.getStatus() != null && resident.getStatus() == 0) {
            throw new AccountNotFoundException("账号已被禁用");
        }
        return resident;
    }

    @Override
    public PageResult pageQuery(ResidentPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<ResidentVO> page = (Page<ResidentVO>) residentMapper.pageQuery(query);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<ResidentVO> listAll() {
        ResidentPageQueryDTO q = new ResidentPageQueryDTO();
        q.setPage(1);
        q.setPageSize(1000);
        return residentMapper.pageQuery(q);
    }

    @Override
    public void save(ResidentDTO dto) {
        if (residentMapper.selectByPhone(dto.getPhone()) != null) {
            throw new AccountNotFoundException("手机号已存在");
        }
        Resident resident = new Resident();
        resident.setName(dto.getName());
        resident.setPhone(dto.getPhone());
        resident.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        resident.setCreateTime(LocalDateTime.now());
        resident.setUpdateTime(LocalDateTime.now());
        residentMapper.insert(resident);
    }

    @Override
    public void update(ResidentDTO dto) {
        Resident resident = new Resident();
        resident.setId(dto.getId());
        resident.setName(dto.getName());
        resident.setPhone(dto.getPhone());
        resident.setStatus(dto.getStatus());
        resident.setUpdateTime(LocalDateTime.now());
        residentMapper.update(resident);
    }
}
