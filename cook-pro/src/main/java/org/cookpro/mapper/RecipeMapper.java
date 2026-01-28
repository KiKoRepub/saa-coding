package org.cookpro.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.cookpro.entity.Recipe;
@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {
}
