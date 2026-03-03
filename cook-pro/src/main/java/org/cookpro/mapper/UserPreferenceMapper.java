package org.cookpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.cookpro.dto.UserPreferenceDTO;
import org.cookpro.entity.UserPreference;

@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {


    UserPreferenceDTO getOneByUserId(@Param("userId") Long userId);

}
