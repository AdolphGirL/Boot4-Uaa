package com.kd.ci.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kd.ci.infrastructure.filter.CSPNonceFilter;
import com.kd.ci.security.authentication.PersonnelUserDetailsService;
import com.kd.ci.security.authorization.MenuPrefixAuthorizationManager;
import com.kd.ci.security.handler.CustomAccessDeniedHandler;
import com.kd.ci.security.handler.CustomAuthenticationFailureHandler;
import com.kd.ci.security.handler.CustomLogoutSuccessHandler;

/*** pring Boot 3.x + Spring Security 6 / 7，不建議再寫 @EnableWebSecurity
@EnableWebSecurity***/
@Configuration
public class SecurityConfig {
	
	private final MenuPrefixAuthorizationManager prefixManager;
	private final PersonnelUserDetailsService personnelUserDetailsService;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	public SecurityConfig(MenuPrefixAuthorizationManager prefixManager,
			PersonnelUserDetailsService personnelUserDetailsService, 
			CustomAccessDeniedHandler accessDeniedHandler) {
		this.prefixManager = prefixManager;
		this.personnelUserDetailsService = personnelUserDetailsService;
		this.accessDeniedHandler = accessDeniedHandler;
	}
    
	/***
	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		
		return new ProviderManager(provider);
	} */
	
	/** 使用 PersonnelUserDetailsService，避免 Spring 自動掃描到其他 UserDetailsService */
	@Bean
	AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(this.personnelUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		
		return new ProviderManager(provider);
	}
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, CSPNonceFilter cspNonceFilter) throws Exception {
		
		http
			/** 讓 CORS Filter 在 Security Filter 之前執行（preflight OPTIONS 不會被擋） */
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			
			/** SameSite 屬性用來控制 Cookie 在跨站請求中的行為，spring boot 可全局設定，application.yml 已經設定 */
			.headers(headers -> headers
					/** 強制瀏覽器只用 HTTPS 與網站通訊，防止被降級成 HTTP 攻擊， */
					/** 在 HSTS header 中加入 preload 這個字串，它本身不會讓瀏覽器自動把你加入 preload 清單。
					 * 一旦真的被 preload：
					 * 不能再回到 HTTP
					 * 不能只讓部分子網域 HTTPS
					 * 移除 preload 需要數月，這是「不可逆」決策；測試階段最好關閉
					.httpStrictTransportSecurity(hsts -> hsts
							.includeSubDomains(true)
							.maxAgeInSeconds(31536000 * 2)
							.preload(true) --> 這邊要注意，小心使用
					)*/
					/** 沒有開啟HTTPS，一併關閉；開發階段先關閉，正式環境再開啟
					 * 正式環境如果有nginx在前，則
					 * ssl: enabled: false   # 因為 TLS 在 nginx
					 * forward-headers-strategy: framework 
					 * */
					.httpStrictTransportSecurity(hsts -> hsts.disable())	// 
					/** 取代舊的 xssProtection（現代瀏覽器大多已棄用或忽略），建議直接禁用或移除，改靠 CSP 防 XSS
					.xssProtection(null)**/
					.contentSecurityPolicy(csp -> csp
						.policyDirectives(
							"default-src 'self'; " +
							"script-src 'self' 'nonce-{nonce}' 'strict-dynamic' 'unsafe-eval'; " +	// 關鍵：nonce + strict-dynamic；strict-dynamic 存在時所有 host allowlist（例如 self、domain）會被忽略，
																									// 只有帶 nonce 或 hash 的 script 才會被允許執行。
							"style-src 'self' 'unsafe-inline'; " +									// inline style 也用 nonce
							"style-src-elem 'self' https://fonts.googleapis.com; " +				// 添加GOOGLE FONTS，Google Fonts 的流程：fonts.googleapis.com   (CSS) -> fonts.gstatic.com      (font file)
							"font-src 'self' https://fonts.gstatic.com data:; " +					// 添加
//							"font-src 'self' data:; " +
							"img-src 'self' data: blob: https: http://api.tgos.tw; " +				// 常見圖片來源(允許 TGOS 圖片與標記)
							"connect-src 'self' https: wss: http://api.tgos.tw; " +					// API、WebSocket(允許 TGOS API 連線)
							"frame-src 'self'; " +
							"object-src 'none'; " +
							"base-uri 'self'; " +
							"form-action 'self'; " +
							
//							"frame-ancestors 'self'; " +						// 取代舊 X-Frame-Options
//							"upgrade-insecure-requests; " +						// 資源有 HTTPS 版本，升級成 HTTPS。自動 http → https，只是建議，

																				// 可以先關閉，因為目前系統非未開啟TLS
//							"block-all-mixed-content;"							// 防混合內容，網站是HTTPS的話，禁止載入 HTTP 資源（圖片、腳本、樣式表等），強制所有資源都必須使用 HTTPS
							""
						)
					)
					/**Spring Security 7 支援，防 clickjacking；或直接用 CSP 的 frame-ancestors 取代 frameOptions
					 * */
					.frameOptions(frame -> frame.deny())
					/** default X-Content-Type-Options: nosniff (不要猜測 MIME type，只能信任 Content-Type)
					.contentTypeOptions(withDefaults()) */
					
					/** 同源請求（same-origin）：完整的 URL 會被傳送作為 Referer */
					/** 跨源請求（cross-origin）：只有來源（scheme + host + port）會被傳送作為 Referer，路徑和查詢參數會被省略 */
					/** 無 Referer：完全不會傳送 Referer header，這在某些情況下（如從 HTTPS 網站跳轉到 HTTP 網站）可能發生
					 * 從 HTTPS 到 HTTP：不會傳送 Referer（防止敏感資訊洩漏）。
					 *  */
					.referrerPolicy(referrer -> 
						referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
					)
			)
			/** 單體系統 CSRF 策略，使用 Cookie 儲存 CSRF token，便於 JS 讀取 */
			/** CSRF token 儲存在名為 XSRF-TOKEN 的 Cookie 中，並允許 JS 讀取（HttpOnly=false）。這對 AJAX 請求很重要，因為 jQuery 可以從 Cookie 讀取 token。
			.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) */
			/** 如果不需 JS 讀取，可保持預設的 HttpOnly=true，但 AJAX 需從 HTML meta tag 或隱藏欄位獲取 token。 */
			.csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository()))
			/** 從最寬寫到最嚴 */
			.authorizeHttpRequests(
				auth -> auth
					/** 靜態資源放行，靜態資源優先（最常被請求，放在最前面性能最好）
					.requestMatchers("/webjars/**", "/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll() **/
					.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
					
					.requestMatchers("/", "/error", "/access-denied").permitAll()
					/** 需要登入 */
					.requestMatchers("/dashboard", "/map/**", "/api/common/**", "/rest/common/**").authenticated()
					/** 登入後，所有前面沒有明確匹配的請求，都交給 MenuPrefixAuthorizationManager 來判斷是否有權限。*/
					.anyRequest().access(this.prefixManager)
			)
			.exceptionHandling(ex -> ex.accessDeniedHandler(this.accessDeniedHandler))
			/** 自訂登入頁面 **/
			.formLogin(form -> form
					.loginPage("/login")
					.loginProcessingUrl("/login")
					/** 登入成功後是否導到原先請求頁面，true: 是；false: 導回預設頁 **/
					.defaultSuccessUrl("/dashboard", true)
					.failureHandler(customAuthenticationFailureHandler())
					.permitAll())
			.logout((logout) -> logout
					.logoutUrl("/logout")
					.logoutSuccessHandler(customLogoutSuccessHandler())
					.deleteCookies("JSESSIONID")
					.permitAll())
			.rememberMe(remember -> remember
					/** 密鑰，防止篡改 */
					.key("mySecretKey123")
					/** 14 天 */
					.tokenValiditySeconds(60 * 60 * 24 * 14)
					/** 對應前端 checkbox name */
					.rememberMeParameter("remember-me")
					/** 認證用戶訊息 */
					.userDetailsService(this.personnelUserDetailsService)
					/** 可自訂 Cookie */
					.rememberMeCookieName("my-remember-me")
					/*** 安全性考量，建議在生產環境中啟用 secure flag，確保 Cookie 只能在 HTTPS 連線中傳送，測試時設定 false，或者關閉不設定
					.useSecureCookie(true) */
					)
			/** Nonce Filter 放在 HeaderWriterFilter 之前，確保 */
			.addFilterBefore(cspNonceFilter, HeaderWriterFilter.class);
		
		return http.build();
	}
	
	/*** 跨域資源共享（CORS）配置，允許來自特定來源的請求，適用於前後端分離架構，
	 * 如果有需要可改由透過環境變數進行動態讀取Environment；CORS規範處理的是RESPONSE **/
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		/**
		 * 使用 allowedOriginPatterns + allowCredentials 來支援 wildcard，因為瀏覽器不允許 allowedOrigins 設為 * 且 allowCredentials 為 true 的組合。*/
		configuration.setAllowedOriginPatterns(List.of(
				"http://localhost:*",						// 開發環境各種 port
				"https://your-frontend.com",				// 正式環境
				"https://*.your-domain.com"					// 子域名也允許（可選）
		));

		/** GET, POST, PUT, DELETE, OPTIONS ... */
		configuration.setAllowedMethods(List.of("*"));
		
		/** 請求近來允許的 header，Authorization, Content-Type 等 */
		configuration.setAllowedHeaders(List.of("*"));
		
		/** 回應出去時前端 JS 可以讀到哪些 header，
		 * CORS 規範，如果非預設的瀏覽器預設不會暴露給前端 JavaScript（fetch、axios、jQuery.ajax 的 .getResponseHeader() 會拿到 null 或空字串） */
		configuration.setExposedHeaders(List.of(
			"X-Trace-Id",
			/** 以下，只是先放置 */
			"Authorization",
			"X-Request-Id",
			"Location"
		));
		
		/** 是否允許帶憑證（cookie、Authorization 等）
		 * 若使用 cookie / session / withCredentials 必須 true */
		configuration.setAllowCredentials(true);
		
		/** preflight 快取 1 小時 */
		configuration.setMaxAge(3600L);

		/** 註冊 CORS 配置，全域套用，對所有路徑生效 */
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	@Bean
	AuthenticationFailureHandler customAuthenticationFailureHandler() {
		return new CustomAuthenticationFailureHandler();
	}
	
	@Bean
	LogoutSuccessHandler customLogoutSuccessHandler() {
		return new CustomLogoutSuccessHandler();
	}
}
