package com.kd.ci.infrastructure.config.properties;

import lombok.Data;

/** 系統前端需要參數設定 */
@Data
public class FrontendProperties {
	
	/** 倒數計時時間，單位分 */
	private int countBackwards;
	
	/** session重新計算時間 - 盡量與timeout一致 */
	private int resetMin;
	
}
