package com.kd.ci.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kd.ci.infrastructure.config.properties.FrontendProperties;

/** 提供自訂義所需要的Bean */
@Configuration
public class RegistrationConfig {
	
	@Bean
	@ConfigurationProperties(prefix = "boot4-uaa.frontend")
	FrontendProperties frontendProperties() {
		return new FrontendProperties();
	}
	
}
