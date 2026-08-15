package com.palatophil.module.ingredient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("standard_ingredient")
public class StandardIngredient {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /**
     * 别名数组 JSON: ["番茄", "洋柿子"]
     */
    private String aliases;
    /**
     * VEGETABLE / MEAT / AQUATIC / GRAIN / SEASONING / DAIRY / FRUIT / OTHER
     */
    private String category;
    /**
     * 密度 (kg/L)，液态食材使用
     */
    private BigDecimal unitDensity;
    /**
     * 0=用户自定义 1=系统预置
     */
    private Integer isSystem;
    /**
     * 0=待审核 1=已通过 2=已拒绝
     */
    private Integer auditStatus;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
