package com.hestia.mapper;

import com.hestia.entity.ServiceCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 住户服务清单 Mapper
 */
@Mapper
public interface ServiceCartMapper {

    @Select("select * from svc_cart where resident_id = #{residentId} order by id")
    List<ServiceCart> selectByResidentId(Long residentId);

    @Select("select * from svc_cart where resident_id = #{residentId} and item_id = #{itemId} and ((spec_id is null and #{specId} is null) or spec_id = #{specId}) limit 1")
    ServiceCart selectOne(@Param("residentId") Long residentId, @Param("itemId") Long itemId, @Param("specId") Long specId);

    @Insert("insert into svc_cart (resident_id, item_id, spec_id, quantity, create_time) values (#{residentId}, #{itemId}, #{specId}, #{quantity}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceCart cart);

    @Update("update svc_cart set quantity = #{quantity} where id = #{id}")
    void updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Delete("delete from svc_cart where id = #{id}")
    void deleteById(Long id);

    @Delete("delete from svc_cart where resident_id = #{residentId}")
    void deleteByResidentId(Long residentId);
}
