package com.kd.ci.security.authentication;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kd.ci.domain.first.Act;
import com.kd.ci.domain.first.OrgunitWithBelong;
import com.kd.ci.domain.first.Personnel;
import com.kd.ci.domain.main.PersonnelUserDetails;
import com.kd.ci.domain.main.adapter.ActAuthRoleAdapter;
import com.kd.ci.domain.main.adapter.MenuitemAuthPermissionAdapter;
import com.kd.ci.domain.main.roles.AuthRole;
import com.kd.ci.infrastructure.persistence.mapper.first.MenuitemMapper;
import com.kd.ci.infrastructure.persistence.mapper.first.OrgunitMapper;
import com.kd.ci.infrastructure.persistence.mapper.first.PersonnelMapper;
import com.kd.ci.shared.util.core.BigDecimals;
import com.kd.ci.shared.util.core.PredicateUtils;
import com.kd.ci.shared.util.core.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PersonnelUserDetailsService implements UserDetailsService {

	private final PersonnelMapper personnelMapper;
	private final OrgunitMapper orgunitMapper;
	private final MenuitemMapper menuitemMapper;

	public PersonnelUserDetailsService(PersonnelMapper personnelMapper, 
			OrgunitMapper orgunitMapper,
			MenuitemMapper menuitemMapper) {
		this.personnelMapper = personnelMapper;
		this.orgunitMapper = orgunitMapper;
		this.menuitemMapper = menuitemMapper;
	}

	@Override
	public UserDetails loadUserByUsername(String idNum) throws UsernameNotFoundException {
		Personnel personnel = this.personnelMapper.findPersonnelByidNum(idNum);
		if (personnel == null) {
			throw new UsernameNotFoundException("Can't find member: " + idNum);
		}
		
		OrgunitWithBelong orgunit = Optional.ofNullable(this.orgunitMapper.findOrgunitWithBelongByPersonnelIdNum(personnel.getIdnum()))
				.orElse(Collections.emptyList())
				.stream()
				.filter(Objects::nonNull)
				.filter(PredicateUtils.notNull(OrgunitWithBelong::getId))
				.findFirst().orElse(null);
		
		Set<AuthRole> auths = loadRolesByIdNum(personnel.getIdnum());
		return new PersonnelUserDetails(personnel, orgunit, auths);
	}
	
	private Set<AuthRole> loadRolesByIdNum(String idNum) {
		if (Strings.isBlank(idNum)) {
			return Collections.emptySet();
		}
		
		try {
			return Optional.ofNullable(this.personnelMapper.findActsIdsByPersonnelIdNum(idNum))
				.orElse(Collections.emptyList()).stream()
				.filter(Objects::nonNull)
				/** 目前 ACT 丟進來的只有 ACT id */
				.map(Act::of)
				.map(x -> new ActAuthRoleAdapter(x, 
							Optional.ofNullable(this.menuitemMapper.findMenuItemsByActId(BigDecimals.defaultIfNull(x.getId())))
								.orElse(Collections.emptyList()).stream()
								.filter(Objects::nonNull)
								.map(MenuitemAuthPermissionAdapter::new)
								.collect(Collectors.toSet()))
				)
				.collect(Collectors.toSet());
		} catch (Exception x) {
			log.error("[-] loadRolesByIdNum，idNum: {}，occur error: ", idNum, x);
			return Collections.emptySet();
		}
	}

}
