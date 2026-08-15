package com.palatophil.module.ingredient.controller;

import com.palatophil.common.PageResult;
import com.palatophil.common.Result;
import com.palatophil.module.ingredient.dto.IngredientCreateRequest;
import com.palatophil.module.ingredient.dto.IngredientUpdateRequest;
import com.palatophil.module.ingredient.dto.IngredientVO;
import com.palatophil.module.ingredient.service.StandardIngredientService;
import com.palatophil.security.JwtAuthFilter;
import com.palatophil.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "食材库")
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final StandardIngredientService service;

    @Operation(summary = "食材分页")
    @GetMapping
    public Result<PageResult<IngredientVO>> page(
            @Parameter(description = "页码（默认 1）") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页大小（默认 20）") @RequestParam(defaultValue = "20") long size,
            @Parameter(description = "关键字：名称/别名") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类：VEGETABLE/MEAT/...") @RequestParam(required = false) String category,
            @Parameter(description = "只看系统食材 1=是") @RequestParam(required = false) Integer systemOnly,
            @Parameter(description = "审核状态：0=待审核 1=已通过 2=已拒绝 4=全部（管理员用）")
            @RequestParam(required = false) Integer auditStatus,
            HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        Long ownerId = u == null ? null : u.getUserId();
        return Result.ok(service.page(page, size, keyword, category, systemOnly, auditStatus, ownerId));
    }

    @Operation(summary = "食材详情")
    @GetMapping("/{id}")
    public Result<IngredientVO> detail(@PathVariable Long id) {
        return Result.ok(service.detail(id));
    }

    @Operation(summary = "新建自定义食材（待审核）")
    @PostMapping
    public Result<IngredientVO> create(@Valid @RequestBody IngredientCreateRequest req,
                                       HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (u == null) {
            return Result.fail(com.palatophil.common.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return Result.ok(service.create(req, u.getUserId()));
    }

    @Operation(summary = "更新食材（私有食材=本人；系统食材=仅管理员）")
    @PutMapping("/{id}")
    public Result<IngredientVO> update(@PathVariable Long id,
                                       @Valid @RequestBody IngredientUpdateRequest req,
                                       HttpServletRequest request) {
        LoginUser u = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (u == null) {
            return Result.fail(com.palatophil.common.ErrorCode.UNAUTHORIZED, "请先登录");
        }
        boolean isAdmin = u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole());
        return Result.ok(service.update(id, req, u.getUserId(), isAdmin));
    }

    @Operation(summary = "删除食材（私有=本人；系统=仅管理员；逻辑删除）")
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

    @Operation(summary = "审核食材（仅管理员）")
    @PostMapping("/{id}/audit")
    public Result<IngredientVO> audit(@PathVariable Long id,
                                      @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                              description = "{\"auditStatus\":1} 1=通过 2=拒绝")
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
