package com.palatophil.module.algorithm.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 食材去重合并器
 * 业务规则：按 ingredient_id 全局合并，
 * 全员食材总量 = Σ(标准量 × K)
 */
public final class IngredientMerger {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private Long ingredientId;
        private BigDecimal amount;
    }

    private IngredientMerger() {}

    public static Map<Long, BigDecimal> merge(List<Item> items) {
        Map<Long, BigDecimal> map = new HashMap<>();
        if (items == null) return map;
        for (Item it : items) {
            if (it == null || it.getIngredientId() == null || it.getAmount() == null) continue;
            map.merge(it.getIngredientId(), it.getAmount(), BigDecimal::add);
        }
        return map;
    }
}
