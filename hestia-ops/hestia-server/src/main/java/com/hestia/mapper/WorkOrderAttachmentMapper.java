package com.hestia.mapper;

import com.hestia.entity.WorkOrderAttachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工单附件 Mapper
 */
@Mapper
public interface WorkOrderAttachmentMapper {

    @Insert("insert into wo_attachment (order_id, file_type, url, uploader_id, uploader_type, create_time) " +
            "values (#{orderId}, #{fileType}, #{url}, #{uploaderId}, #{uploaderType}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WorkOrderAttachment attachment);

    @Select("select url from wo_attachment where order_id = #{orderId} order by id")
    List<String> selectUrlsByOrderId(Long orderId);
}
