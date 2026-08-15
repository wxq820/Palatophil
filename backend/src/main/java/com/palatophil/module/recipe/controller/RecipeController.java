package com.palatophil.module.recipe.controller;

import com.palatophil.common.PageResult;
import com.palatophil.common.Result;
import com.palatophil.module.recipe.dto.RecipeSaveRequest;
import com.palatophil.module.recipe.dto.RecipeSummaryVO;
import com.palatophil.module.recipe.dto.RecipeVO;
import com.palatophil.module.recipe.service.RecipeService;
import com.palatophil.security.JwtAuthFilter;
import com.palatophil.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "食谱")
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService service;

    @Operation(summary = "食谱列表（PUBLIC 全部可见 + 自己的 PRIVATE）")
    @GetMapping
    public Result<PageResult<RecipeSummaryVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "PUBLIC / PRIVATE / 全部") @RequestParam(required = false) String visibility,
            @Parameter(description = "审核状态：0=待审核 1=已通过 2=已拒绝 4=全部（管理员用）")
            @RequestParam(required = false) Integer auditStatus,
            HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        Long ownerId = u == null ? 0L : u.getUserId();
        boolean isAdmin = u != null && u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole());
        return Result.ok(service.page(page, size, keyword, visibility, auditStatus, ownerId, isAdmin));
    }

    @Operation(summary = "食谱详情（含食材块、明细、标签）")
    @GetMapping("/{id}")
    public Result<RecipeVO> detail(@PathVariable Long id, HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        Long viewerId = u == null ? null : u.getUserId();
        return Result.ok(service.detail(id, viewerId));
    }

    @Operation(summary = "新建食谱（私有）")
    @PostMapping
    public Result<RecipeVO> create(@Valid @RequestBody RecipeSaveRequest req,
                                   HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (u == null) {
            return Result.fail(com.palatophil.common.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return Result.ok(service.create(req, u.getUserId()));
    }

    @Operation(summary = "更新食谱（仅自己）")
    @PutMapping("/{id}")
    public Result<RecipeVO> update(@PathVariable Long id,
                                   @Valid @RequestBody RecipeSaveRequest req,
                                   HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (u == null) {
            return Result.fail(com.palatophil.common.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        boolean isAdmin = u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole());
        return Result.ok(service.update(id, req, u.getUserId(), isAdmin));
    }

    @Operation(summary = "删除食谱（仅自己，逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (u == null) {
            return Result.fail(com.palatophil.common.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        boolean isAdmin = u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole());
        service.delete(id, u.getUserId(), isAdmin);
        return Result.ok();
    }

    @Operation(summary = "复制食谱到自己（派生）")
    @PostMapping("/{id}/copy")
    public Result<RecipeVO> copy(@PathVariable Long id, HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (u == null) {
            return Result.fail(com.palatophil.common.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return Result.ok(service.copy(id, u.getUserId()));
    }

    @Operation(summary = "审核食谱（仅管理员）：0=待审核 1=通过 2=拒绝")
    @PostMapping("/{id}/audit")
    public Result<RecipeVO> audit(@PathVariable Long id,
                                  @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                          description = "{\"auditStatus\":1}")
                                  @RequestBody java.util.Map<String, Integer> body,
                                  HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (u == null) {
            return Result.fail(com.palatophil.common.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        boolean isAdmin = u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole());
        if (!isAdmin) {
            return Result.fail(com.palatophil.common.ErrorCode.FORBIDDEN, "仅管理员可审核");
        }
        Integer status = body == null ? null : body.get("auditStatus");
        return Result.ok(service.audit(id, status));
    }
}
