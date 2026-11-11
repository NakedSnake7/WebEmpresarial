package com.webempresarial.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AdminPasswordResetRequestExecutorConfig {

    @Bean(name = "adminPasswordResetRequestExecutor")
    public ThreadPoolTaskExecutor adminPasswordResetRequestExecutor() {

        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix(
                "admin-password-reset-request-"
        );

        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.AbortPolicy()
        );

        executor.setWaitForTasksToCompleteOnShutdown(false);

        executor.initialize();

        return executor;
    }
}
