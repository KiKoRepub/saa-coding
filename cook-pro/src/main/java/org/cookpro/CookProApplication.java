package org.cookpro;

import org.cookpro.config.properties.ToolEnvProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ToolEnvProperties.class)
public class CookProApplication {
    public static void main(String[] args) {
        SpringApplication.run(CookProApplication.class, args);
    }
}