package com.kd.ci.infrastructure.config.encoder;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kd.ci.shared.util.password.Sha256PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

	/** 舊系統還有 sha256，所以採用多重判斷的處理 */
	@Bean
	PasswordEncoder passwordEncoder() {
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("bcrypt", new BCryptPasswordEncoder(12));							/** spring security 推薦使用 */
		encoders.put("sha256", new Sha256PasswordEncoder());							/** 自訂 sha256 編碼器 */
		
		DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder("bcrypt", encoders);
		delegating.setDefaultPasswordEncoderForMatches(new Sha256PasswordEncoder());	/** DB 沒有 {id} → 視為 SHA-256 */
		
		return delegating;
	}

}
