package com.hestia.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hestia.constant.MessageConstant;
import com.hestia.dto.PasswordEditDTO;
import com.hestia.dto.StaffDTO;
import com.hestia.dto.StaffLoginDTO;
import com.hestia.dto.StaffPageQueryDTO;
import com.hestia.entity.Staff;
import com.hestia.enumeration.StaffRole;
import com.hestia.exception.AccountLockedException;
import com.hestia.exception.AccountNotFoundException;
import com.hestia.exception.PasswordEditFailedException;
import com.hestia.exception.PasswordErrorException;
import com.hestia.mapper.StaffMapper;
import com.hestia.result.PageResult;
import com.hestia.service.StaffService;
import com.hestia.vo.StaffVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffMapper staffMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Staff login(StaffLoginDTO dto) {
        Staff staff = staffMapper.selectByUsername(dto.getUsername());
        if (staff == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getPassword(), staff.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (staff.getStatus() != null && staff.getStatus() == 0) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        return staff;
    }

    @Override
    public PageResult pageQuery(StaffPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<StaffVO> page = (Page<StaffVO>) staffMapper.pageQuery(query);
        List<StaffVO> records = page.getResult();
        records.forEach(vo -> vo.setRoleName(StaffRole.descOf(vo.getRole())));
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public StaffVO detail(Long id) {
        Staff staff = staffMapper.selectById(id);
        if (staff == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        StaffVO vo = new StaffVO();
        vo.setId(staff.getId());
        vo.setStaffNo(staff.getStaffNo());
        vo.setName(staff.getName());
        vo.setUsername(staff.getUsername());
        vo.setPhone(staff.getPhone());
        vo.setRole(staff.getRole());
        vo.setRoleName(StaffRole.descOf(staff.getRole()));
        vo.setSkillTags(staff.getSkillTags());
        vo.setWorkloadLimit(staff.getWorkloadLimit());
        vo.setStatus(staff.getStatus());
        vo.setActiveCount(staffMapper.countActiveOrders(staff.getId()));
        return vo;
    }

    @Override
    public void save(StaffDTO dto) {
        if (staffMapper.selectByUsername(dto.getUsername()) != null) {
            throw new PasswordEditFailedException("登录账号已存在");
        }
        Staff staff = new Staff();
        staff.setStaffNo(dto.getStaffNo() != null && !dto.getStaffNo().isEmpty()
                ? dto.getStaffNo()
                : String.format("S%03d", staffMapper.countAll() + 1));
        staff.setName(dto.getName());
        staff.setUsername(dto.getUsername());
        staff.setPassword(passwordEncoder.encode(
                dto.getPassword() == null || dto.getPassword().isEmpty() ? "123456" : dto.getPassword()));
        staff.setPhone(dto.getPhone());
        staff.setRole(dto.getRole() == null ? StaffRole.ENGINEER.getCode() : dto.getRole());
        staff.setSkillTags(dto.getSkillTags());
        staff.setWorkloadLimit(dto.getWorkloadLimit() == null ? 5 : dto.getWorkloadLimit());
        staff.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        staff.setCreateTime(LocalDateTime.now());
        staff.setUpdateTime(LocalDateTime.now());
        staffMapper.insert(staff);
        log.info("新增员工 {} ({})", staff.getName(), staff.getStaffNo());
    }

    @Override
    public void update(StaffDTO dto) {
        Staff staff = new Staff();
        staff.setId(dto.getId());
        staff.setStaffNo(dto.getStaffNo());
        staff.setName(dto.getName());
        staff.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            staff.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        staff.setPhone(dto.getPhone());
        staff.setRole(dto.getRole());
        staff.setSkillTags(dto.getSkillTags());
        staff.setWorkloadLimit(dto.getWorkloadLimit());
        staff.setStatus(dto.getStatus());
        staff.setUpdateTime(LocalDateTime.now());
        staffMapper.update(staff);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setStatus(status);
        staff.setUpdateTime(LocalDateTime.now());
        staffMapper.update(staff);
    }

    @Override
    public List<Staff> listEngineers(String skill) {
        return staffMapper.selectEngineers(skill);
    }

    @Override
    public void editPassword(Long staffId, PasswordEditDTO dto) {
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), staff.getPassword())) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
        Staff update = new Staff();
        update.setId(staffId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        update.setUpdateTime(LocalDateTime.now());
        staffMapper.update(update);
    }
}
