package com.hestia.service.impl;

import com.hestia.dto.PremiseDTO;
import com.hestia.entity.Premise;
import com.hestia.entity.Resident;
import com.hestia.exception.PremiseBusinessException;
import com.hestia.mapper.PremiseMapper;
import com.hestia.mapper.ResidentMapper;
import com.hestia.service.PremiseService;
import com.hestia.vo.PremiseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 房屋/点位服务实现
 */
@Service
@RequiredArgsConstructor
public class PremiseServiceImpl implements PremiseService {

    private final PremiseMapper premiseMapper;
    private final ResidentMapper residentMapper;

    @Override
    public List<PremiseVO> listByResident(Long residentId) {
        return premiseMapper.selectByResidentId(residentId).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<PremiseVO> listAll() {
        return premiseMapper.selectAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public PremiseVO detail(Long id) {
        Premise premise = premiseMapper.selectById(id);
        if (premise == null) {
            throw new PremiseBusinessException("房屋点位不存在");
        }
        return toVO(premise);
    }

    @Override
    public void save(PremiseDTO dto) {
        Premise premise = new Premise();
        BeanUtils.copyProperties(dto, premise);
        premise.setCreateTime(LocalDateTime.now());
        premise.setUpdateTime(LocalDateTime.now());
        premiseMapper.insert(premise);
    }

    @Override
    public void update(PremiseDTO dto) {
        Premise premise = new Premise();
        BeanUtils.copyProperties(dto, premise);
        premise.setUpdateTime(LocalDateTime.now());
        premiseMapper.update(premise);
    }

    @Override
    public void delete(Long id) {
        if (premiseMapper.selectById(id) == null) {
            throw new PremiseBusinessException("房屋点位不存在");
        }
        premiseMapper.deleteById(id);
    }

    private PremiseVO toVO(Premise p) {
        PremiseVO vo = new PremiseVO();
        BeanUtils.copyProperties(p, vo);
        if (p.getResidentId() != null) {
            Resident r = residentMapper.selectById(p.getResidentId());
            if (r != null) {
                vo.setResidentName(r.getName());
            }
        }
        vo.setFullAddress(p.getCommunity() + p.getBuilding()
                + (p.getUnit() == null ? "" : p.getUnit()) + p.getRoomNo());
        return vo;
    }
}
