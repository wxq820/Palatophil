package com.palatophil.module.algorithm.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 配方换算系数 K 计算器
 * 公式：K = Σ实际食材总量 / Σ标准食材总量
 * 实际分配量 = round(标准量 × K, 1)
 */
public final class KCalculator {

    private KCalculator() {}

    public static BigDecimal computeK(Map<Long, BigDecimal> standardAmounts, Map<Long, BigDecimal> actualAmounts) {
        BigDecimal standardSum = sum(standardAmounts);
        BigDecimal actualSum = sum(actualAmounts);
        if (standardSum == null || standardSum.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return actualSum.divide(standardSum, 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal apply(BigDecimal amount, BigDecimal k) {
        if (amount == null || k == null) return BigDecimal.ZERO;
        return AmountFormatter.display(amount.multiply(k));
    }

    private static BigDecimal sum(Map<Long, BigDecimal> map) {
        if (map == null || map.isEmpty()) return BigDecimal.ZERO;
        BigDecimal s = BigDecimal.ZERO;
        for (BigDecimal v : map.values()) {
            if (v != null) s = s.add(v);
        }
        return s;
    }
}
