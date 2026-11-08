package com.hestia.mapper;

import com.hestia.dto.ResidentPageQueryDTO;
import com.hestia.entity.Resident;
import com.hestia.vo.ResidentVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住户 Mapper
 */
@Mapper
public interface ResidentMapper {

    @Select("select * from sys_resident where phone = #{phone}")
    Resident selectByPhone(String phone);

    @Select("select * from sys_resident where id = #{id}")
    Resident selectById(Long id);

    @Insert("insert into sys_resident (openid, name, phone, avatar, status, create_time, update_time) " +
            "values (#{openid}, #{name}, #{phone}, #{avatar}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resident resident);

    /** 动态更新，见 ResidentMapper.xml */
    void update(Resident resident);

    List<ResidentVO> pageQuery(ResidentPageQueryDTO query);

    @Select("select count(*) from sys_resident")
    Long countAll();
}
