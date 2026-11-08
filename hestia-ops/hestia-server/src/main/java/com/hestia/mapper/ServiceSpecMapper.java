package com.hestia.mapper;

import com.hestia.entity.ServiceSpec;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务规格 Mapper
 */
@Mapper
public interface ServiceSpecMapper {

    @Select("select * from svc_spec where item_id = #{itemId} order by sort")
    List<ServiceSpec> selectByItemId(Long itemId);

    @Insert("insert into svc_spec (item_id, name, price, duration_min, sort, create_time, update_time) " +
            "values (#{itemId}, #{name}, #{price}, #{durationMin}, #{sort}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceSpec spec);

    @Delete("delete from svc_spec where item_id = #{itemId}")
    void deleteByItemId(Long itemId);

    @Select("select * from svc_spec where id = #{id}")
    ServiceSpec selectById(Long id);
}
