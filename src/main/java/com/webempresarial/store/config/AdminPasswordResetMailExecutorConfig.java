package com.webempresarial.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AdminPasswordResetMailExecutorConfig {

    @Bean(name = "adminPasswordResetMailExecutor")
    public ThreadPoolTaskExecutor adminPasswordResetMailExecutor() {

        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);

        executor.setThreadNamePrefix(
                "admin-password-reset-mail-"
        );

        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.AbortPolicy()
        );

        executor.setWaitForTasksToCompleteOnShutdown(false);

        executor.initialize();

        return executor;
    }
}
