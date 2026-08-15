package com.palatophil;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.palatophil.module.**.mapper")
@EnableAsync
@EnableScheduling
public class PalatophilApplication {

    public static void main(String[] args) {
        SpringApplication.run(PalatophilApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "  无定珍 (Palatophil) 启动成功\n" +
                "  Knife4j:  http://localhost:8080/doc.html\n" +
                "  Health:   http://localhost:8080/api/health\n" +
                "========================================\n");
    }
}
