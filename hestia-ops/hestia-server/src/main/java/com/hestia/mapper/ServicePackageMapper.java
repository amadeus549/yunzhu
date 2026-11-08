package com.hestia.mapper;

import com.hestia.dto.ServicePackagePageQueryDTO;
import com.hestia.entity.ServicePackage;
import com.hestia.vo.ServicePackageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务包 Mapper
 */
@Mapper
public interface ServicePackageMapper {

    @Select("select * from svc_package where id = #{id}")
    ServicePackage selectById(Long id);

    @Insert("insert into svc_package (category_id, code, name, description, image, price, original_price, valid_days, status, create_time, update_time) " +
            "values (#{categoryId}, #{code}, #{name}, #{description}, #{image}, #{price}, #{originalPrice}, #{validDays}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServicePackage pkg);

    /** 动态更新，见 ServicePackageMapper.xml */
    void update(ServicePackage pkg);

    @Delete("delete from svc_package where id = #{id}")
    void deleteById(Long id);

    List<ServicePackageVO> pageQuery(ServicePackagePageQueryDTO query);
}
