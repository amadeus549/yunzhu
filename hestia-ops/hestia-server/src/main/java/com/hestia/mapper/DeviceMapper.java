package com.hestia.mapper;

import com.hestia.dto.DevicePageQueryDTO;
import com.hestia.entity.Device;
import com.hestia.vo.DeviceVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 设备台账 Mapper
 */
@Mapper
public interface DeviceMapper {

    @Select("select * from dev_device where id = #{id}")
    Device selectById(Long id);

    @Select("select * from dev_device where device_no = #{deviceNo}")
    Device selectByDeviceNo(String deviceNo);

    @Insert("insert into dev_device (device_no, name, category, brand, model, premise_id, location, install_date, warranty_end, " +
            "maintain_cycle_days, last_maintain_date, next_maintain_date, status, qr_code, remark, create_time, update_time) " +
            "values (#{deviceNo}, #{name}, #{category}, #{brand}, #{model}, #{premiseId}, #{location}, #{installDate}, #{warrantyEnd}, " +
            "#{maintainCycleDays}, #{lastMaintainDate}, #{nextMaintainDate}, #{status}, #{qrCode}, #{remark}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Device device);

    /** 动态更新，见 DeviceMapper.xml */
    void update(Device device);

    @Delete("delete from dev_device where id = #{id}")
    void deleteById(Long id);

    List<DeviceVO> pageQuery(DevicePageQueryDTO query);

    @Select("select * from dev_device where category = #{category} and status = 1 order by id")
    List<Device> selectByCategory(String category);

    @Select("select * from dev_device where status = 1 order by id")
    List<Device> selectEnabled();

    /** 到期待维保设备（next_maintain_date <= 基准日） */
    @Select("select * from dev_device where status = 1 and next_maintain_date is not null and next_maintain_date <= #{date} order by next_maintain_date")
    List<Device> selectMaintainDue(LocalDate date);

    @Select("select count(*) from dev_device where status = 1 and next_maintain_date is not null and next_maintain_date <= #{date}")
    Long countMaintainDue(LocalDate date);

    @Select("select count(*) from dev_device where status = #{status}")
    Long countByStatus(Integer status);

    @Select("select count(*) from dev_device")
    Long countAll();

    /** 设备类别列表 */
    @Select("select distinct category from dev_device order by category")
    List<String> selectCategories();
}
