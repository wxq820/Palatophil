package com.palatophil.module.recipe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecipeVO {

    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Long sourceRecipeId;
    private Long ownerId;
    private String ownerNickname;
    private String visibility;
    private Integer auditStatus;
    private Integer version;
    private Integer servings;
    private String createdAt;
    private String updatedAt;
    private List<BlockVO> blocks;
    private List<TagVO> tags;

    @Data
    public static class BlockVO {
        private Long id;
        private String name;
        private String blockType;
        private Integer sortOrder;
        private List<IngredientVO> ingredients;
    }

    @Data
    public static class IngredientVO {
        private Long id;
        private Long ingredientId;
        private String ingredientName;
        private String category;
        private BigDecimal amountG;
        private Integer isAnchor;
        private String note;
    }

    @Data
    public static class TagVO {
        private Long id;
        private String name;
    }
}
