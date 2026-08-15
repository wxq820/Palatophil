package com.palatophil.module.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class RecipeSaveRequest {

    @NotBlank(message = "食谱名称不能为空")
    private String name;

    private String description;
    private String coverImage;

    /**
     * PRIVATE / PUBLIC，缺省 PRIVATE
     */
    private String visibility;

    @NotNull(message = "基准份数不能为空")
    @Positive(message = "份数必须 > 0")
    private Integer servings;

    /**
     * 食材块列表（顺序）
     */
    @NotNull(message = "blocks 不能为空")
    private List<BlockInput> blocks;

    /**
     * 标签 ID 数组（可选）
     */
    private List<Long> tagIds;

    @Data
    public static class BlockInput {

        private String name;

        /**
         * SINGLE / MULTI；可选，缺省 SINGLE
         */
        private String blockType;

        private Integer sortOrder;

        @NotNull(message = "食材明细不能为空")
        private List<IngredientInput> ingredients;

        @Data
        public static class IngredientInput {

            @NotNull(message = "ingredientId 不能为空")
            private Long ingredientId;

            @NotNull(message = "克数不能为空")
            @Positive(message = "克数必须 > 0")
            private java.math.BigDecimal amountG;

            private Integer isAnchor;

            private String note;
        }
    }
}
