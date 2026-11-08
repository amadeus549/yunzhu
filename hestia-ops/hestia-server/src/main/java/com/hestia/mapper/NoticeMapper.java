package com.hestia.mapper;

import com.hestia.entity.Notice;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 站内通知 Mapper
 */
@Mapper
public interface NoticeMapper {

    @Insert("insert into sys_notice (receiver_id, receiver_type, title, content, biz_type, biz_id, is_read, create_time) " +
            "values (#{receiverId}, #{receiverType}, #{title}, #{content}, #{bizType}, #{bizId}, #{isRead}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Notice notice);

    @Select("select * from sys_notice where receiver_id = #{receiverId} and receiver_type = #{receiverType} order by id desc limit 50")
    List<Notice> selectByReceiver(@Param("receiverId") Long receiverId, @Param("receiverType") Integer receiverType);

    @Update("update sys_notice set is_read = 1 where id = #{id}")
    void markRead(Long id);

    @Update("update sys_notice set is_read = 1 where receiver_id = #{receiverId} and receiver_type = #{receiverType}")
    void markAllRead(@Param("receiverId") Long receiverId, @Param("receiverType") Integer receiverType);

    @Select("select count(*) from sys_notice where receiver_id = #{receiverId} and receiver_type = #{receiverType} and is_read = 0")
    Long countUnread(@Param("receiverId") Long receiverId, @Param("receiverType") Integer receiverType);
}
