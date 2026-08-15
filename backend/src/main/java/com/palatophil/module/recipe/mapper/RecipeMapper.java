package com.palatophil.module.recipe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.palatophil.module.recipe.entity.Recipe;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecipeMapper extends BaseMapper<Recipe> {
}
