package com.palatophil.module.ingredient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class IngredientCreateRequest {

    @NotBlank(message = "名称不能为空")
    @Size(max = 64, message = "名称最长 64 字符")
    private String name;

    @NotBlank(message = "分类不能为空")
    private String category;

    private List<String> aliases;

    private BigDecimal unitDensity;
}
