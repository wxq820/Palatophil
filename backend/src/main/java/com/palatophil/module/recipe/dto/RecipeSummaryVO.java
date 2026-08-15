package com.palatophil.module.recipe.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecipeSummaryVO {

    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private String visibility;
    private Integer auditStatus;
    private Integer version;
    private Integer servings;
    private Long ownerId;
    private String ownerNickname;
    private Integer ingredientCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
