package com.music.Config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MyBatis分页插件配置类，实现分页查询
 */
@Configuration
public class MyBatisPageConfig {
    @Bean // 手动将分页插件注入Spring容器
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties props = new Properties();
        // 分页参数合理化：页码小于1查第1页，大于总页数查最后一页
        props.setProperty("reasonable", "true");
        pageInterceptor.setProperties(props);
        return pageInterceptor;
    }
}
