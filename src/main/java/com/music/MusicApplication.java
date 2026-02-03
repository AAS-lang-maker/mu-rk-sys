package com.music;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

// 核心注解：一键开启自动配置，扫描根包下所有注解类（controller/service/mapper/config都能扫到）
@SpringBootApplication
@MapperScan("com.music.Mapper")
@EnableTransactionManagement(proxyTargetClass=true)
public class MusicApplication {
    // 项目启动主方法，固定写法
    public static void main(String[] args) {
        SpringApplication.run(MusicApplication.class, args);
    }
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        // 绑定项目默认的数据源，创建事务管理器
        return new DataSourceTransactionManager(dataSource);
    }
}
