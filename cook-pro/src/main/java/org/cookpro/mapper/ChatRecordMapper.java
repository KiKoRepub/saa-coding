package org.cookpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.cookpro.entity.ChatRecord;

import java.util.List;

@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {

    Integer batchInsert(@Param("list") List<ChatRecord> chatRecords);
}
