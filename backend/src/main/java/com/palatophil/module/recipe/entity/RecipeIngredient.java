package com.palatophil.module.recipe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recipe_ingredient")
public class RecipeIngredient {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recipeId;
    private Long blockId;
    private Long ingredientId;
    private BigDecimal amountG;
    private Integer isAnchor;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
