package com.palatophil.module.recipe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("recipe_tag_rel")
public class RecipeTagRel {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recipeId;
    private Long tagId;
}
