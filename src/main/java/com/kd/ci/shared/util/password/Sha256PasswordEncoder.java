package com.kd.ci.shared.util.password;

import org.springframework.security.crypto.password.PasswordEncoder;

public class Sha256PasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return PasswordUtils.sha256Hex(rawPassword.toString());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		String hash = PasswordUtils.sha256Hex(rawPassword.toString());
		return hash.equalsIgnoreCase(encodedPassword);
	}
}
