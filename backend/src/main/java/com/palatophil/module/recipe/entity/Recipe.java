package com.palatophil.module.recipe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recipe")
public class Recipe {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Long sourceRecipeId;
    private Long ownerId;
    /**
     * PRIVATE / PUBLIC
     */
    private String visibility;
    /**
     * 0=待审核 1=已通过 2=已拒绝
     */
    private Integer auditStatus;
    private Integer version;
    private Integer servings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
