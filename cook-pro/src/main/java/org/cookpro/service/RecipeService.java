package org.cookpro.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.cookpro.entity.Recipe;
import org.cookpro.mapper.RecipeMapper;
import org.springframework.stereotype.Service;

@Service
public class RecipeService extends ServiceImpl<RecipeMapper, Recipe> {
}
