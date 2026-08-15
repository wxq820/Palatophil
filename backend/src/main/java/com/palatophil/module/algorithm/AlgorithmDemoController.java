package com.palatophil.module.algorithm;

import com.palatophil.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "算法演示")
@RestController
@RequestMapping("/api/algorithm")
public class AlgorithmDemoController {

    @Operation(summary = "计算 K 系数")
    @PostMapping("/compute-k")
    public Result<Map<String, Object>> computeK(@RequestBody KRequest req) {
        Map<Long, BigDecimal> std = new HashMap<>();
        Map<Long, BigDecimal> act = new HashMap<>();
        if (req.standardAmounts != null) req.standardAmounts.forEach((k, v) -> std.put(k, new BigDecimal(v)));
        if (req.actualAmounts != null) req.actualAmounts.forEach((k, v) -> act.put(k, new BigDecimal(v)));

        BigDecimal k = com.palatophil.module.algorithm.core.KCalculator.computeK(std, act);
        Map<String, Object> res = new HashMap<>();
        res.put("k", k);
        res.put("kStr", k.toPlainString());
        return Result.ok(res);
    }

    public static class KRequest {
        public Map<Long, String> standardAmounts;
        public Map<Long, String> actualAmounts;
    }
}
