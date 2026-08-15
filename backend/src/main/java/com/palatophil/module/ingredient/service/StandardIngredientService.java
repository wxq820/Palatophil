package com.palatophil.module.ingredient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palatophil.common.BizException;
import com.palatophil.common.ErrorCode;
import com.palatophil.common.PageResult;
import com.palatophil.module.ingredient.dto.IngredientCreateRequest;
import com.palatophil.module.ingredient.dto.IngredientUpdateRequest;
import com.palatophil.module.ingredient.dto.IngredientVO;
import com.palatophil.module.ingredient.entity.StandardIngredient;
import com.palatophil.module.ingredient.mapper.StandardIngredientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardIngredientService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> CATEGORIES = Set.of(
            "VEGETABLE", "MEAT", "AQUATIC", "GRAIN", "SEASONING", "DAIRY", "FRUIT", "OTHER");

    private final StandardIngredientMapper ingredientMapper;

    public PageResult<IngredientVO> page(long page, long size, String keyword, String category,
                                         Integer systemOnly, Integer auditStatus, Long ownerId) {
        LambdaQueryWrapper<StandardIngredient> q = new LambdaQueryWrapper<>();
        // 审核状态筛选：默认仅已通过；管理员可显式传 0=待审核 2=已拒绝 4=全部
        if (auditStatus != null && auditStatus == 0) {
            q.eq(StandardIngredient::getAuditStatus, 0);
        } else if (auditStatus != null && auditStatus == 2) {
            q.eq(StandardIngredient::getAuditStatus, 2);
        } else if (auditStatus != null && auditStatus == 4) {
            // 全部，不加 audit_status 条件
        } else {
            q.eq(StandardIngredient::getAuditStatus, 1);
        }
        if (ownerId != null) {
            q.and(w -> w.eq(StandardIngredient::getIsSystem, 1).or().eq(StandardIngredient::getOwnerId, ownerId));
        } else {
            q.eq(StandardIngredient::getIsSystem, 1);
        }
        if (systemOnly != null && systemOnly == 1) {
            q.eq(StandardIngredient::getIsSystem, 1);
        }
        if (StringUtils.hasText(category)) {
            q.eq(StandardIngredient::getCategory, category.toUpperCase());
        }
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(StandardIngredient::getName, keyword)
                    .or().like(StandardIngredient::getAliases, keyword));
        }
        q.orderByDesc(StandardIngredient::getIsSystem).orderByAsc(StandardIngredient::getName);

        Page<StandardIngredient> pg = ingredientMapper.selectPage(Page.of(page, size), q);
        List<IngredientVO> list = pg.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(pg.getTotal(), pg.getCurrent(), pg.getSize(), list);
    }

    public IngredientVO detail(Long id) {
        StandardIngredient e = ingredientMapper.selectById(id);
        if (e == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食材不存在");
        }
        return toVO(e);
    }

    @Transactional
    public IngredientVO create(IngredientCreateRequest req, Long ownerId) {
        validateCategory(req.getCategory());
        // 查重时排除软删除历史记录
        StandardIngredient exist = ingredientMapper.selectOne(
                new LambdaQueryWrapper<StandardIngredient>()
                        .eq(StandardIngredient::getName, req.getName().trim())
                        .eq(StandardIngredient::getOwnerId, ownerId)
                        .eq(StandardIngredient::getDeleted, 0));
        if (exist != null) {
            throw new BizException(ErrorCode.CONFLICT, "同名食材已存在");
        }

        StandardIngredient e = new StandardIngredient();
        e.setName(req.getName().trim());
        e.setCategory(req.getCategory().toUpperCase());
        e.setAliases(toJson(req.getAliases()));
        e.setUnitDensity(req.getUnitDensity());
        e.setIsSystem(0);
        e.setAuditStatus(0);
        e.setOwnerId(ownerId);
        ingredientMapper.insert(e);
        return toVO(e);
    }

    @Transactional
    public IngredientVO update(Long id, IngredientUpdateRequest req, Long operatorId, boolean isAdmin) {
        StandardIngredient e = ingredientMapper.selectById(id);
        if (e == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食材不存在");
        }
        if (e.getIsSystem() != null && e.getIsSystem() == 1) {
            if (!isAdmin) {
                throw new BizException(ErrorCode.FORBIDDEN, "系统食材不可修改");
            }
        } else if (operatorId == null || !operatorId.equals(e.getOwnerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能修改自己的食材");
        }
        validateCategory(req.getCategory());

        // 重命名查重
        if (!e.getName().equals(req.getName().trim())) {
            Long owner = e.getIsSystem() == 1 ? null : e.getOwnerId();
            StandardIngredient dup = ingredientMapper.selectOne(
                    new LambdaQueryWrapper<StandardIngredient>()
                            .eq(StandardIngredient::getName, req.getName().trim())
                            .eq(owner == null, StandardIngredient::getOwnerId, owner)
                            .ne(StandardIngredient::getId, id));
            if (dup != null) {
                throw new BizException(ErrorCode.CONFLICT, "同名食材已存在");
            }
        }

        e.setName(req.getName().trim());
        e.setCategory(req.getCategory().toUpperCase());
        e.setAliases(toJson(req.getAliases()));
        e.setUnitDensity(req.getUnitDensity());
        e.setAuditStatus(e.getIsSystem() == 1 ? e.getAuditStatus() : 0);
        ingredientMapper.updateById(e);
        return toVO(e);
    }

    @Transactional
    public void delete(Long id, Long operatorId, boolean isAdmin) {
        StandardIngredient e = ingredientMapper.selectById(id);
        if (e == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食材不存在");
        }
        if (e.getIsSystem() != null && e.getIsSystem() == 1) {
            if (!isAdmin) {
                throw new BizException(ErrorCode.FORBIDDEN, "系统食材不可删除");
            }
        } else if (operatorId == null || !operatorId.equals(e.getOwnerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的食材");
        }
        ingredientMapper.deleteById(id);
    }

    /**
     * 审核食材（管理员）：0=待审核 1=通过 2=拒绝
     */
    @Transactional
    public IngredientVO audit(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "auditStatus 必须是 0/1/2");
        }
        StandardIngredient e = ingredientMapper.selectById(id);
        if (e == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食材不存在");
        }
        e.setAuditStatus(status);
        ingredientMapper.updateById(e);
        return toVO(e);
    }

    public IngredientVO toVO(StandardIngredient e) {
        IngredientVO v = new IngredientVO();
        v.setId(e.getId());
        v.setName(e.getName());
        v.setCategory(e.getCategory());
        v.setUnitDensity(e.getUnitDensity());
        v.setIsSystem(e.getIsSystem());
        v.setAuditStatus(e.getAuditStatus());
        v.setOwnerId(e.getOwnerId());
        v.setCreatedAt(e.getCreatedAt() == null ? null : e.getCreatedAt().toString());
        v.setUpdatedAt(e.getUpdatedAt() == null ? null : e.getUpdatedAt().toString());
        v.setAliases(parseAliases(e.getAliases()));
        return v;
    }

    private void validateCategory(String cat) {
        if (cat == null || !CATEGORIES.contains(cat.toUpperCase())) {
            throw new BizException(ErrorCode.BAD_REQUEST,
                    "category 必须是 " + CATEGORIES + " 之一");
        }
    }

    private String toJson(List<String> aliases) {
        if (aliases == null || aliases.isEmpty()) {
            return JSON_NULL;
        }
        try {
            return JSON.writeValueAsString(aliases);
        } catch (JsonProcessingException ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, "aliases JSON 序列化失败");
        }
    }

    private static final String JSON_NULL = "[]";

    private List<String> parseAliases(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return JSON.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            log.warn("aliases 解析失败: {}", json);
            return List.of();
        }
    }
}
