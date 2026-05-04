package com.music.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "rankTask") // 对应你 @Async("rankTask") 的名字
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // 核心线程数：平时保留5个
        executor.setMaxPoolSize(10);      // 最大线程数：忙的时候最多10个
        executor.setQueueCapacity(100);   // 队列容量：忙不过来时排队，最多排100个
        executor.setThreadNamePrefix("rank-war-report-"); // 线程名字（看日志方便）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 满了之后由调用线程（主线程）执行，防止任务丢失
        executor.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(60); // 关闭时最多等待60秒
        executor.initialize();
        return executor;
    }
}          