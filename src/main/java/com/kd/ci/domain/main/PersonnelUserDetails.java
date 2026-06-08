package com.kd.ci.domain.main;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.kd.ci.domain.first.OrgunitWithBelong;
import com.kd.ci.domain.first.Personnel;
import com.kd.ci.domain.main.permission.AuthPermission;
import com.kd.ci.domain.main.roles.AuthRole;
import com.kd.ci.shared.util.core.Strings;

import lombok.extern.slf4j.Slf4j;

/** 封裝 Personnel to UserDetails */
@Slf4j
public class PersonnelUserDetails implements UserDetails {

	private static final long serialVersionUID = -196611514925254696L;

	private final Personnel personnel;
	private final OrgunitWithBelong orgunit;
	private final Set<AuthRole> roles;
	
	private final Collection<? extends GrantedAuthority> authorities;

	public PersonnelUserDetails(Personnel personnel, OrgunitWithBelong orgunit, 
			Set<AuthRole> roles) {
		this.personnel = personnel;
		this.orgunit = orgunit;
		this.roles = roles != null ? roles : Collections.emptySet();
		
		/**
		this.authorities = this.roles.stream()
				.flatMap(role -> role.getGrantedPermissions().stream())
				.map(AuthPermission::getPermissionCode)
				.filter(Strings::isNotBlank)
				.distinct().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());*/
		
		/** 優化寫法 */
		this.authorities = roles.stream()
				.flatMap(r -> r.getGrantedPermissions().stream())
				.map(AuthPermission::toAuthority)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		String username = Optional.ofNullable(personnel)
				.map(Personnel::getIdnum)
				.orElse("unknown");
		
		log.info("[+] 登入權限載入完成，使用者: {} | 共 {} 個權限", username, this.authorities.size());
		
		/** 詳細列出每一個 authority（方便除錯） */
		this.authorities.stream().map(GrantedAuthority::getAuthority)
			/** 排序方便閱讀 */
			.sorted()
			.forEach(auth -> log.info("   → Authority: {}", auth));
	}
	
	/** 將所有角色的權限攤平，轉成 Spring Security 認識的 GrantedAuthority(角色也包進去（ROLE_ 前綴）)，有需要擴充到角色 + URL定位可以使用 */
	private Collection<? extends GrantedAuthority> buildAuthorities(Set<AuthRole> roles) {
		/** 保持順序可讀性較好 */
		Set<GrantedAuthority> authorities = new LinkedHashSet<>();

		/** 角色本身（建議用 roleCode，如果沒有用有意義的字串，清除非A-Z0-9_的資料） */
		for (AuthRole role : roles) {
			String roleStr = Strings.defaultIfBlank(role.getRoleCode(), String.valueOf(role.getRoleId()));
			String normalized = roleStr.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
			authorities.add(new SimpleGrantedAuthority("ROLE_" + normalized));
			
			/** 權限（只加有 location 的） */
			Set<AuthPermission> perms = role.getGrantedPermissions();
			if (perms != null) {
				perms.stream()
					.filter(perm -> Strings.isNotBlank(perm.getPermissionCode()))
					.map(perm -> new SimpleGrantedAuthority(perm.getPermissionCode().trim()))
					.forEach(authorities::add);
			}
		}

		return authorities;
	}
	
	public Personnel getPersonnel() {
		return personnel;
	}
	
	public OrgunitWithBelong getOrgunit() {
		return orgunit;
	}
	
	public Set<AuthRole> getRoles() {
		return roles;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public @Nullable String getPassword() {
		return personnel.getPassword();
	}

	@Override
	public String getUsername() {
		return personnel.getIdnum();
	}
	
	public @Nullable String getOrgunitName() {
		return orgunit.getShortname();
	}

	public @Nullable String getOrgCode() {
		return orgunit.getOrgcode();
	}
	
}
