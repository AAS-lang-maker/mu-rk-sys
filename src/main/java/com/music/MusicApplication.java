package com.music;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import dev.langchain4j.service.spring.AiServiceScan;
//@AiServiceScan("com.music.Service")
@MapperScan("com.music.Mapper")
@EnableAsync
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})

public class MusicApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(MusicApplication.class, args);
    }


    // 修正后的静态资源映射配置
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 修复favicon.ico映射（如果图标在static根目录，就写classpath:/static/）
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/") // 图标文件实际存放路径
                .setCachePeriod(0);

        // 2. 核心修复：静态资源映射规则
        // 场景1：如果你的项目上下文路径是 /music（application.properties里配了server.servlet.context-path=/music）
        // 不需要额外配置这个映射，Spring Boot默认会把static下的资源映射到 /music/**
        // 场景2：如果是控制器想加/music前缀，静态资源仍用默认映射即可
        // 【删除错误的/music/**映射，改用Spring Boot默认规则】
    }
}