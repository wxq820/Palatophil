package com.palatophil.module.recipe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.palatophil.common.BizException;
import com.palatophil.common.ErrorCode;
import com.palatophil.common.PageResult;
import com.palatophil.module.ingredient.entity.StandardIngredient;
import com.palatophil.module.ingredient.mapper.StandardIngredientMapper;
import com.palatophil.module.recipe.dto.RecipeSaveRequest;
import com.palatophil.module.recipe.dto.RecipeSummaryVO;
import com.palatophil.module.recipe.dto.RecipeVO;
import com.palatophil.module.recipe.entity.Recipe;
import com.palatophil.module.recipe.entity.RecipeBlock;
import com.palatophil.module.recipe.entity.RecipeIngredient;
import com.palatophil.module.recipe.entity.RecipeTag;
import com.palatophil.module.recipe.entity.RecipeTagRel;
import com.palatophil.module.recipe.mapper.RecipeBlockMapper;
import com.palatophil.module.recipe.mapper.RecipeIngredientMapper;
import com.palatophil.module.recipe.mapper.RecipeMapper;
import com.palatophil.module.recipe.mapper.RecipeTagMapper;
import com.palatophil.module.recipe.mapper.RecipeTagRelMapper;
import com.palatophil.module.user.entity.SysUser;
import com.palatophil.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private static final Set<String> VIS = Set.of("PRIVATE", "PUBLIC");
    private static final Set<String> BTYPES = Set.of("SINGLE", "MULTI");

    private final RecipeMapper recipeMapper;
    private final RecipeBlockMapper blockMapper;
    private final RecipeIngredientMapper ingredientMapper;
    private final RecipeTagMapper tagMapper;
    private final RecipeTagRelMapper tagRelMapper;
    private final StandardIngredientMapper standardIngredientMapper;
    private final SysUserMapper userMapper;

    public PageResult<RecipeSummaryVO> page(long page, long size, String keyword, String visibility,
                                            Integer auditStatus, Long ownerId, boolean isAdmin) {
        LambdaQueryWrapper<Recipe> q = new LambdaQueryWrapper<>();
        // 审核状态：默认 1=已通过；管理员可传 0=待审核 2=已拒绝 4=全部
        if (auditStatus != null && auditStatus == 0) {
            q.eq(Recipe::getAuditStatus, 0);
        } else if (auditStatus != null && auditStatus == 2) {
            q.eq(Recipe::getAuditStatus, 2);
        } else if (auditStatus != null && auditStatus == 4) {
            // 全部
        } else {
            q.eq(Recipe::getAuditStatus, 1);
        }
        if ("PUBLIC".equalsIgnoreCase(visibility)) {
            q.eq(Recipe::getVisibility, "PUBLIC");
        } else if ("PRIVATE".equalsIgnoreCase(visibility)) {
            q.eq(Recipe::getVisibility, "PRIVATE")
                    .eq(Recipe::getOwnerId, ownerId);
        } else {
            // 默认可见性规则：PUBLIC + (PRIVATE 且 owner = me)；管理员查看待审核时忽略此规则
            if (!(isAdmin && auditStatus != null && auditStatus == 0)) {
                q.and(w -> w.eq(Recipe::getVisibility, "PUBLIC")
                        .or(w2 -> w2.eq(Recipe::getVisibility, "PRIVATE").eq(Recipe::getOwnerId, ownerId)));
            }
        }
        if (StringUtils.hasText(keyword)) {
            q.like(Recipe::getName, keyword.trim())
                    .or().like(Recipe::getDescription, keyword.trim());
        }
        q.orderByDesc(Recipe::getUpdatedAt);

        Page<Recipe> pg = recipeMapper.selectPage(Page.of(page, size), q);

        // 食材数
        Map<Long, Integer> ingCount = new HashMap<>();
        if (!pg.getRecords().isEmpty()) {
            List<Long> ids = pg.getRecords().stream().map(Recipe::getId).toList();
            LambdaQueryWrapper<RecipeIngredient> iq = new LambdaQueryWrapper<>();
            iq.in(RecipeIngredient::getRecipeId, ids);
            for (RecipeIngredient ri : ingredientMapper.selectList(iq)) {
                ingCount.merge(ri.getRecipeId(), 1, Integer::sum);
            }
        }
        // 创建者昵称
        Map<Long, String> nicknameByOwner = loadOwnerNickname(
                pg.getRecords().stream().map(Recipe::getOwnerId).filter(java.util.Objects::nonNull).toList());

        List<RecipeSummaryVO> list = pg.getRecords().stream().map(r -> {
            RecipeSummaryVO v = new RecipeSummaryVO();
            v.setId(r.getId());
            v.setName(r.getName());
            v.setDescription(r.getDescription());
            v.setCoverImage(r.getCoverImage());
            v.setVisibility(r.getVisibility());
            v.setAuditStatus(r.getAuditStatus());
            v.setVersion(r.getVersion());
            v.setServings(r.getServings());
            v.setOwnerId(r.getOwnerId());
            v.setOwnerNickname(nicknameByOwner.get(r.getOwnerId()));
            v.setIngredientCount(ingCount.getOrDefault(r.getId(), 0));
            v.setCreatedAt(r.getCreatedAt());
            v.setUpdatedAt(r.getUpdatedAt());
            return v;
        }).toList();

        return PageResult.of(pg.getTotal(), pg.getCurrent(), pg.getSize(), list);
    }

    public RecipeVO detail(Long id, Long viewerId) {
        Recipe r = recipeMapper.selectById(id);
        if (r == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食谱不存在");
        }
        // 私有食谱：仅本人可见
        if ("PRIVATE".equalsIgnoreCase(r.getVisibility())) {
            if (viewerId == null || !viewerId.equals(r.getOwnerId())) {
                throw new BizException(ErrorCode.FORBIDDEN, "该食谱为私有");
            }
        }
        RecipeVO vo = toDetail(r);

        // 食材块
        List<RecipeBlock> blocks = blockMapper.selectList(
                new LambdaQueryWrapper<RecipeBlock>()
                        .eq(RecipeBlock::getRecipeId, id)
                        .orderByAsc(RecipeBlock::getSortOrder));
        // 食材明细
        List<RecipeIngredient> details = ingredientMapper.selectList(
                new LambdaQueryWrapper<RecipeIngredient>()
                        .eq(RecipeIngredient::getRecipeId, id));
        // 标准食材信息（name + category）
        Map<Long, StandardIngredient> ingMap = new HashMap<>();
        if (!details.isEmpty()) {
            List<Long> ingIds = details.stream().map(RecipeIngredient::getIngredientId).toList();
            for (StandardIngredient si : standardIngredientMapper.selectBatchIds(ingIds)) {
                ingMap.put(si.getId(), si);
            }
        }
        // 分组
        Map<Long, List<RecipeIngredient>> byBlock = new HashMap<>();
        for (RecipeIngredient ri : details) {
            byBlock.computeIfAbsent(ri.getBlockId(), k -> new ArrayList<>()).add(ri);
        }

        List<RecipeVO.BlockVO> blockVos = new ArrayList<>();
        for (RecipeBlock b : blocks) {
            RecipeVO.BlockVO bv = new RecipeVO.BlockVO();
            bv.setId(b.getId());
            bv.setName(b.getName());
            bv.setBlockType(b.getBlockType());
            bv.setSortOrder(b.getSortOrder());
            bv.setIngredients(new ArrayList<>());
            for (RecipeIngredient ri : byBlock.getOrDefault(b.getId(), List.of())) {
                RecipeVO.IngredientVO iv = new RecipeVO.IngredientVO();
                iv.setId(ri.getId());
                iv.setIngredientId(ri.getIngredientId());
                iv.setAmountG(ri.getAmountG());
                iv.setIsAnchor(ri.getIsAnchor());
                iv.setNote(ri.getNote());
                StandardIngredient si = ingMap.get(ri.getIngredientId());
                if (si != null) {
                    iv.setIngredientName(si.getName());
                    iv.setCategory(si.getCategory());
                }
                bv.getIngredients().add(iv);
            }
            blockVos.add(bv);
        }
        vo.setBlocks(blockVos);

        // tags
        List<RecipeTagRel> rels = tagRelMapper.selectList(
                new LambdaQueryWrapper<RecipeTagRel>().eq(RecipeTagRel::getRecipeId, id));
        List<RecipeVO.TagVO> tags = new ArrayList<>();
        if (!rels.isEmpty()) {
            List<Long> tagIds = rels.stream().map(RecipeTagRel::getTagId).toList();
            Map<Long, String> nameById = new HashMap<>();
            for (RecipeTag t : tagMapper.selectBatchIds(tagIds)) {
                nameById.put(t.getId(), t.getName());
            }
            for (Long tid : tagIds) {
                RecipeVO.TagVO tv = new RecipeVO.TagVO();
                tv.setId(tid);
                tv.setName(nameById.get(tid));
                tags.add(tv);
            }
        }
        vo.setTags(tags);
        return vo;
    }

    @Transactional
    public RecipeVO create(RecipeSaveRequest req, Long ownerId) {
        validate(req);
        verifyIngredients(req);

        Recipe r = new Recipe();
        r.setName(req.getName().trim());
        r.setDescription(req.getDescription());
        r.setCoverImage(req.getCoverImage());
        r.setVisibility(StringUtils.hasText(req.getVisibility()) ? req.getVisibility().toUpperCase() : "PRIVATE");
        r.setAuditStatus("PUBLIC".equalsIgnoreCase(r.getVisibility()) ? 0 : 1);
        r.setVersion(1);
        r.setServings(req.getServings());
        r.setOwnerId(ownerId);
        recipeMapper.insert(r);

        saveBlocks(r.getId(), req.getBlocks());
        saveTags(r.getId(), req.getTagIds());
        return detail(r.getId(), ownerId);
    }

    @Transactional
    public RecipeVO update(Long id, RecipeSaveRequest req, Long operatorId, boolean isAdmin) {
        Recipe r = recipeMapper.selectById(id);
        if (r == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食谱不存在");
        }
        if (r.getOwnerId() == null || !r.getOwnerId().equals(operatorId)) {
            if (!isAdmin) {
                throw new BizException(ErrorCode.FORBIDDEN, "只能修改自己的食谱");
            }
        }
        validate(req);
        verifyIngredients(req);

        r.setName(req.getName().trim());
        r.setDescription(req.getDescription());
        r.setCoverImage(req.getCoverImage());
        if (StringUtils.hasText(req.getVisibility())) {
            r.setVisibility(req.getVisibility().toUpperCase());
        }
        // 重新提交审核
        r.setAuditStatus("PUBLIC".equalsIgnoreCase(r.getVisibility()) ? 0 : 1);
        r.setVersion(r.getVersion() == null ? 1 : r.getVersion() + 1);
        r.setServings(req.getServings());
        recipeMapper.updateById(r);

        // 清旧块 / 食材
        blockMapper.delete(new LambdaQueryWrapper<RecipeBlock>().eq(RecipeBlock::getRecipeId, id));
        ingredientMapper.delete(new LambdaQueryWrapper<RecipeIngredient>().eq(RecipeIngredient::getRecipeId, id));
        tagRelMapper.delete(new LambdaQueryWrapper<RecipeTagRel>().eq(RecipeTagRel::getRecipeId, id));

        saveBlocks(id, req.getBlocks());
        saveTags(id, req.getTagIds());

        return detail(id, operatorId);
    }

    @Transactional
    public void delete(Long id, Long operatorId, boolean isAdmin) {
        Recipe r = recipeMapper.selectById(id);
        if (r == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食谱不存在");
        }
        if (r.getOwnerId() == null || !r.getOwnerId().equals(operatorId)) {
            if (!isAdmin) {
                throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的食谱");
            }
        }
        recipeMapper.deleteById(id);
        blockMapper.delete(new LambdaQueryWrapper<RecipeBlock>().eq(RecipeBlock::getRecipeId, id));
        ingredientMapper.delete(new LambdaQueryWrapper<RecipeIngredient>().eq(RecipeIngredient::getRecipeId, id));
        tagRelMapper.delete(new LambdaQueryWrapper<RecipeTagRel>().eq(RecipeTagRel::getRecipeId, id));
    }

    @Transactional
    public RecipeVO copy(Long sourceId, Long operatorId) {
        Recipe src = recipeMapper.selectById(sourceId);
        if (src == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "源食谱不存在");
        }
        // 拷贝主表
        Recipe copy = new Recipe();
        copy.setName(src.getName() + " (副本)");
        copy.setDescription(src.getDescription());
        copy.setCoverImage(src.getCoverImage());
        copy.setSourceRecipeId(src.getId());
        copy.setOwnerId(operatorId);
        copy.setVisibility("PRIVATE");
        copy.setAuditStatus(1);
        copy.setVersion(1);
        copy.setServings(src.getServings());
        recipeMapper.insert(copy);

        // 拷贝块 + 明细
        List<RecipeBlock> blocks = blockMapper.selectList(
                new LambdaQueryWrapper<RecipeBlock>().eq(RecipeBlock::getRecipeId, sourceId));
        Map<Long, Long> blockIdMap = new HashMap<>();
        for (RecipeBlock b : blocks) {
            RecipeBlock nb = new RecipeBlock();
            nb.setRecipeId(copy.getId());
            nb.setName(b.getName());
            nb.setBlockType(b.getBlockType());
            nb.setSortOrder(b.getSortOrder());
            blockMapper.insert(nb);
            blockIdMap.put(b.getId(), nb.getId());
        }
        List<RecipeIngredient> details = ingredientMapper.selectList(
                new LambdaQueryWrapper<RecipeIngredient>().eq(RecipeIngredient::getRecipeId, sourceId));
        for (RecipeIngredient ri : details) {
            RecipeIngredient ni = new RecipeIngredient();
            ni.setRecipeId(copy.getId());
            ni.setBlockId(blockIdMap.get(ri.getBlockId()));
            ni.setIngredientId(ri.getIngredientId());
            ni.setAmountG(ri.getAmountG());
            ni.setIsAnchor(ri.getIsAnchor());
            ni.setNote(ri.getNote());
            ingredientMapper.insert(ni);
        }
        // 标签
        List<RecipeTagRel> rels = tagRelMapper.selectList(
                new LambdaQueryWrapper<RecipeTagRel>().eq(RecipeTagRel::getRecipeId, sourceId));
        for (RecipeTagRel r : rels) {
            RecipeTagRel nr = new RecipeTagRel();
            nr.setRecipeId(copy.getId());
            nr.setTagId(r.getTagId());
            tagRelMapper.insert(nr);
        }
        return detail(copy.getId(), operatorId);
    }

    // -- helpers -----------------------------------------------------------------

    /**
     * 审核食谱（管理员）：0=待审核 1=通过 2=拒绝
     */
    @Transactional
    public RecipeVO audit(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "auditStatus 必须是 0/1/2");
        }
        Recipe r = recipeMapper.selectById(id);
        if (r == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "食谱不存在");
        }
        r.setAuditStatus(status);
        recipeMapper.updateById(r);
        return detail(id, null);
    }

    private void validate(RecipeSaveRequest req) {
        if (req.getBlocks() == null || req.getBlocks().isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "至少提交 1 个食材块");
        }
        if (StringUtils.hasText(req.getVisibility())
                && !VIS.contains(req.getVisibility().toUpperCase())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "visibility 必须是 PRIVATE 或 PUBLIC");
        }
        for (RecipeSaveRequest.BlockInput b : req.getBlocks()) {
            if (StringUtils.hasText(b.getBlockType()) && !BTYPES.contains(b.getBlockType().toUpperCase())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "blockType 必须是 SINGLE 或 MULTI");
            }
            if (b.getIngredients() == null || b.getIngredients().isEmpty()) {
                throw new BizException(ErrorCode.BAD_REQUEST, "块「" + b.getName() + "」至少 1 个食材");
            }
            boolean hasAnchor = false;
            for (RecipeSaveRequest.BlockInput.IngredientInput in : b.getIngredients()) {
                if (in.getIsAnchor() != null && in.getIsAnchor() == 1) {
                    if (hasAnchor) {
                        throw new BizException(ErrorCode.BAD_REQUEST, "每个块只能有 1 个锚点食材");
                    }
                    hasAnchor = true;
                }
            }
        }
    }

    private void verifyIngredients(RecipeSaveRequest req) {
        Set<Long> ids = new HashSet<>();
        for (RecipeSaveRequest.BlockInput b : req.getBlocks()) {
            for (RecipeSaveRequest.BlockInput.IngredientInput in : b.getIngredients()) {
                ids.add(in.getIngredientId());
            }
        }
        List<StandardIngredient> existing = standardIngredientMapper.selectBatchIds(ids);
        if (existing.size() != ids.size()) {
            throw new BizException(ErrorCode.NOT_FOUND, "部分食材不存在或已删除");
        }
    }

    private void saveBlocks(Long recipeId, List<RecipeSaveRequest.BlockInput> blocks) {
        for (RecipeSaveRequest.BlockInput b : blocks) {
            RecipeBlock block = new RecipeBlock();
            block.setRecipeId(recipeId);
            block.setName(b.getName() == null || b.getName().isBlank() ? "主料" : b.getName().trim());
            block.setBlockType(StringUtils.hasText(b.getBlockType()) ? b.getBlockType().toUpperCase() : "SINGLE");
            block.setSortOrder(b.getSortOrder() == null ? 0 : b.getSortOrder());
            blockMapper.insert(block);

            for (RecipeSaveRequest.BlockInput.IngredientInput in : b.getIngredients()) {
                RecipeIngredient ri = new RecipeIngredient();
                ri.setRecipeId(recipeId);
                ri.setBlockId(block.getId());
                ri.setIngredientId(in.getIngredientId());
                ri.setAmountG(in.getAmountG());
                ri.setIsAnchor(in.getIsAnchor() == null ? 0 : in.getIsAnchor());
                ri.setNote(in.getNote());
                ingredientMapper.insert(ri);
            }
        }
    }

    private void saveTags(Long recipeId, List<Long> tagIds) {
        if (tagIds == null) {
            return;
        }
        // 去重
        List<Long> distinct = tagIds.stream().distinct().toList();
        List<RecipeTag> tags = tagMapper.selectBatchIds(distinct);
        if (tags.size() != distinct.size()) {
            throw new BizException(ErrorCode.NOT_FOUND, "部分标签不存在");
        }
        for (Long tid : distinct) {
            RecipeTagRel rel = new RecipeTagRel();
            rel.setRecipeId(recipeId);
            rel.setTagId(tid);
            tagRelMapper.insert(rel);
        }
    }

    private Map<Long, String> loadOwnerNickname(List<Long> ownerIds) {
        if (ownerIds.isEmpty()) return Map.of();
        Map<Long, String> map = new HashMap<>();
        for (SysUser u : userMapper.selectBatchIds(ownerIds)) {
            map.put(u.getId(), u.getNickname());
        }
        return map;
    }

    private RecipeVO toDetail(Recipe r) {
        RecipeVO vo = new RecipeVO();
        vo.setId(r.getId());
        vo.setName(r.getName());
        vo.setDescription(r.getDescription());
        vo.setCoverImage(r.getCoverImage());
        vo.setSourceRecipeId(r.getSourceRecipeId());
        vo.setOwnerId(r.getOwnerId());
        vo.setVisibility(r.getVisibility());
        vo.setAuditStatus(r.getAuditStatus());
        vo.setVersion(r.getVersion());
        vo.setServings(r.getServings());
        vo.setCreatedAt(r.getCreatedAt() == null ? null : r.getCreatedAt().toString());
        vo.setUpdatedAt(r.getUpdatedAt() == null ? null : r.getUpdatedAt().toString());
        if (r.getOwnerId() != null) {
            SysUser u = userMapper.selectById(r.getOwnerId());
            if (u != null) vo.setOwnerNickname(u.getNickname());
        }
        return vo;
    }
}
