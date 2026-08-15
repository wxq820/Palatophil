package com.palatophil.module.algorithm.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 食材克数格式化工具
 * 业务规则（来自 V1.2 规格）：
 *  - 统一保留 1 位小数
 *  - < 0.1g 在前端直接过滤不展示
 *  - > 0 但被舍入后为 0 时，按 0.1g 最小展示
 */
public final class AmountFormatter {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.1");

    private AmountFormatter() {}

    public static BigDecimal roundToTenth(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.setScale(1, RoundingMode.HALF_UP);
    }

    public static BigDecimal display(BigDecimal amount) {
        BigDecimal rounded = roundToTenth(amount);
        if (rounded.compareTo(BigDecimal.ZERO) == 0 && amount.signum() > 0) {
            return MIN_AMOUNT;
        }
        return rounded;
    }

    public static boolean isVisible(BigDecimal amount) {
        return amount != null && amount.compareTo(MIN_AMOUNT) >= 0;
    }
}
