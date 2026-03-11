package org.cookpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.cookpro.dto.ToolChatDTO;
import org.cookpro.entity.ToolEntity;

import java.util.List;

@Mapper
public interface ToolMapper extends BaseMapper<ToolEntity> {


    List<ToolChatDTO> selectChatDTOByIds(@Param("list") List<Long> toolIdList);


    ToolChatDTO selectChatDTOByName(String toolName);


    int batchSaveOrUpdate(List<ToolEntity> toolEntityList);

}
