package com.palatophil.module.recipe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recipe_tag")
public class RecipeTag {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer isSystem;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
