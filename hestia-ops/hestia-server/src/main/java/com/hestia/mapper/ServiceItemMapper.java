package com.hestia.mapper;

import com.hestia.dto.ServiceItemPageQueryDTO;
import com.hestia.entity.ServiceItem;
import com.hestia.vo.ServiceItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 服务项 Mapper
 */
@Mapper
public interface ServiceItemMapper {

    @Select("select * from svc_item where id = #{id}")
    ServiceItem selectById(Long id);

    @Select("select * from svc_item where category_id = #{categoryId} and status = 1 order by sort")
    List<ServiceItem> selectByCategoryId(Long categoryId);

    @Select("select * from svc_item where status = 1 order by category_id, sort")
    List<ServiceItem> selectEnabled();

    @Insert("insert into svc_item (category_id, code, name, description, image, price, unit, duration_min, required_skill, status, sort, create_time, update_time) " +
            "values (#{categoryId}, #{code}, #{name}, #{description}, #{image}, #{price}, #{unit}, #{durationMin}, #{requiredSkill}, #{status}, #{sort}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ServiceItem item);

    /** 动态更新，见 ServiceItemMapper.xml */
    void update(ServiceItem item);

    @Delete("delete from svc_item where id = #{id}")
    void deleteById(Long id);

    List<ServiceItemVO> pageQuery(ServiceItemPageQueryDTO query);
}
