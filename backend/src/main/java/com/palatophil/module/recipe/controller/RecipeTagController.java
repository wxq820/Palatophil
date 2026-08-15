package com.palatophil.module.recipe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.palatophil.common.Result;
import com.palatophil.module.recipe.entity.RecipeTag;
import com.palatophil.module.recipe.mapper.RecipeTagMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "食谱标签")
@RestController
@RequestMapping("/api/recipe-tags")
@RequiredArgsConstructor
public class RecipeTagController {

    private final RecipeTagMapper tagMapper;

    @Operation(summary = "全部食谱标签")
    @GetMapping
    public Result<List<RecipeTag>> all() {
        return Result.ok(tagMapper.selectList(new LambdaQueryWrapper<RecipeTag>()
                .orderByAsc(RecipeTag::getIsSystem).orderByAsc(RecipeTag::getId)));
    }
}
