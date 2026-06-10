package com.kd.ci.infrastructure.config.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kd.ci.infrastructure.interceptor.MenuModelInterceptor;
import com.kd.ci.infrastructure.interceptor.NonceModelInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	private final NonceModelInterceptor nonceInterceptor;
	private final MenuModelInterceptor menuModelInterceptor;
	
	public WebMvcConfig(NonceModelInterceptor nonceInterceptor, MenuModelInterceptor menuModelInterceptor) {
		this.nonceInterceptor = nonceInterceptor;
		this.menuModelInterceptor = menuModelInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(nonceInterceptor);
		registry.addInterceptor(menuModelInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns("/webjars/**", 
					"/static/**", 
					"/favicon.ico", "/error", "/login", "/login/**",
					"/logout");
	}
	
	/** 手動指定一下靜態資源對應位置(預設也包含，此處限縮一下) */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
	}
}
