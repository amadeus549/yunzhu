package com.hestia.service;

import com.hestia.dto.PremiseDTO;
import com.hestia.vo.PremiseVO;

import java.util.List;

/**
 * 房屋/点位服务
 */
public interface PremiseService {

    List<PremiseVO> listByResident(Long residentId);

    List<PremiseVO> listAll();

    PremiseVO detail(Long id);

    void save(PremiseDTO dto);

    void update(PremiseDTO dto);

    void delete(Long id);
}
