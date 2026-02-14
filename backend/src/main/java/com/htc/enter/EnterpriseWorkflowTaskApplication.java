package com.htc.enter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@EnableCaching
public class EnterpriseWorkflowTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseWorkflowTaskApplication.class, args);
    }
}
