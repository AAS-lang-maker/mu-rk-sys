package com.music;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 核心注解：一键开启自动配置，扫描根包下所有注解类（controller/service/mapper/config都能扫到）
@SpringBootApplication
@MapperScan("com.music.Mapper")
public class MusicApplication {
    // 项目启动主方法，固定写法
    public static void main(String[] args) {
        SpringApplication.run(MusicApplication.class, args);
    }
}
