package com.palatophil.module.ingredient.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class IngredientVO {

    private Long id;
    private String name;
    private List<String> aliases;
    private String category;
    private BigDecimal unitDensity;
    private Integer isSystem;
    private Integer auditStatus;
    private Long ownerId;
    private String createdAt;
    private String updatedAt;
}
