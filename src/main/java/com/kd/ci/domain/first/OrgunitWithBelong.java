package com.kd.ci.domain.first;

import java.math.BigDecimal;

/** 擴充 Orgunit (需要科室資料) */
public class OrgunitWithBelong extends Orgunit {
	
	private BigDecimal suborgunitid;

	public BigDecimal getSuborgunitid() {
		return suborgunitid;
	}

	public void setSuborgunitid(BigDecimal suborgunitid) {
		this.suborgunitid = suborgunitid;
	}
	
}
