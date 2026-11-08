package com.hestia.mapper;

import com.hestia.entity.ServicePackageItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务包明细 Mapper
 */
@Mapper
public interface ServicePackageItemMapper {

    @Select("select * from svc_package_item where package_id = #{packageId}")
    List<ServicePackageItem> selectByPackageId(Long packageId);

    @Insert("insert into svc_package_item (package_id, item_id, quantity, times) values (#{packageId}, #{itemId}, #{quantity}, #{times})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServicePackageItem item);

    @Delete("delete from svc_package_item where package_id = #{packageId}")
    void deleteByPackageId(Long packageId);
}
