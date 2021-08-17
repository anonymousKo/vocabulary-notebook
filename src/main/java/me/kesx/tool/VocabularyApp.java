package me.kesx.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "me.kesx.tool.dao")
@EnableTransactionManagement
@EntityScan(basePackages="me.kesx.tool.entity")
public class VocabularyApp {
    public static void main(String[] args) {
        SpringApplication.run(VocabularyApp.class, args);
    }

}
