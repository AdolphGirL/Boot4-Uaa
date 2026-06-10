package com.kd.ci.security.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kd.ci.infrastructure.filter.CSPNonceFilter;
import com.kd.ci.security.authentication.PersonnelUserDetailsService;
import com.kd.ci.security.authorization.MenuPrefixAuthorizationManager;
import com.kd.ci.security.handler.CustomAccessDeniedHandler;
import com.kd.ci.security.handler.CustomAuthenticationFailureHandler;
import com.kd.ci.security.handler.CustomLogoutSuccessHandler;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/*** pring Boot 3.x + Spring Security 6 / 7，不建議再寫 @EnableWebSecurity
@EnableWebSecurity***/
@Configuration
public class SecurityConfig {
	
	private final MenuPrefixAuthorizationManager prefixManager;
	private final PersonnelUserDetailsService personnelUserDetailsService;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final CSPNonceFilter cspNonceFilter;

	public SecurityConfig(MenuPrefixAuthorizationManager prefixManager,
			PersonnelUserDetailsService personnelUserDetailsService, 
			CustomAccessDeniedHandler accessDeniedHandler, CSPNonceFilter cspNonceFilter) {
		this.prefixManager = prefixManager;
		this.personnelUserDetailsService = personnelUserDetailsService;
		this.accessDeniedHandler = accessDeniedHandler;
		this.cspNonceFilter = cspNonceFilter;		
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
	
	/** ==========================================	**/
	/** 1. 授權伺服器專用 Filter Chain (高優先權)		**/
	/** ==========================================	**/
	@Bean
	@Order(1)
	SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		/** Spring Security 7 在啟動時進行了嚴格檢查：如果排在前面的 Filter Chain（@Order(1)）匹配了 any request，
		 * 那麼排在後面的 Filter Chain（@Order(2)）就永遠不會被觸發，因此直接拋出異常阻止系統啟動。 */
		
		/** 💡 關鍵修正：透過這個配置，讓這個 Filter Chain 只處理 OAuth2 / OIDC 相關的 URL */
		/** 其他網頁、自訂登入頁的請求就會自動「滑落」到第二層去處理！ */
		/** 💡 透過 Spring Security 7 內建的 Endpoints 匹配器，自動篩選出 OAuth2 相關端點 */
		
		/*** 在 Spring Security 7 中，如果您直接透過 new OAuth2AuthorizationServerConfigurer() 實例化一個 Configurer 物件，它的內部狀態（包括端點路徑、預設參數、自訂設定等）此時尚未被初始化（未調用 init 方法）。
		 * 因此直接調用 authServerConfigurer.getEndpointsMatcher() 就會抓到一個 null 物件，導致請求進來（訪問 /dashboard）在比對 Filter Chain 時直接發生空指標異常。
		org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer authServerConfigurer =
				new org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer();***/
		
		/***
		💡 關鍵修正：不從物件中取，直接手動明確定義 OAuth2 伺服器必須攔截的標準端點路徑
		如此一來，這個 Filter Chain「只會」吃這兩個路徑下的請求，絕對不會和後面的 anyRequest 衝突！***/
		
		http
			.securityMatchers(matchers -> matchers
				.requestMatchers("/oauth2/**", "/.well-known/**")
			)
			// 或者是寫成精簡版：.securityMatcher(new org.springframework.security.web.util.matcher.OrRequestMatcher(
			//     new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/oauth2/**"),
			//     new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/.well-known/**")
			// ))
			/** 2. 套用 Spring Security 7 內建的 OAuth2 授權伺服器配置 */
			.oauth2AuthorizationServer(authServer -> authServer
				.oidc(Customizer.withDefaults()) // 啟用 OpenID Connect 1.0 (支援 yml 中的 openid scope)
			)
			/** 3. 當未認證使用者觸發 OAuth2 流程時，重導向至您現有的自訂登入頁面 `/login` */
			.exceptionHandling(exceptions -> exceptions
				.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"),
					new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
				)
			)
			/** 4. 確保 OIDC 用戶資訊端點 (User Info / OIDC 登出) 能夠接受來自前端應用的 Bearer Access Token */
			.oauth2ResourceServer(resourceServer -> resourceServer
				.jwt(Customizer.withDefaults())
			)
			/** 5. 共享原先的 CORS 跨域設定，允許外來 Client 應用的請求 */
			.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		return http.build();
	}
	
	/** ==========================================	**/
	/** 2. 原有單體系統 Filter Chain 
	 * (必須在 Order(1) 之後，攔截不屬於 OAuth2 協議的所有其餘請求)		**/
	/** ==========================================	**/
	@Bean
	@Order(2)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
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
	
	/** ==========================================	**/
	/** 3. Getting Started 指南必須定義的必要組件		**/
	/** ==========================================	**/
	
	/**
	 * 配置用於簽署 JWT (Access Token / ID Token) 的 JWK (Json Web Key) 來源
	 * 範例採用自動生成的非對稱 RSA 密鑰（生產環境建議改由 KeyStore 讀取固定證書）
	 * 
	 * 將 JWKSource<SecurityContext> 改為 JWKSource<com.nimbusds.jose.proc.SecurityContext>
	 */
	@Bean 
	JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		
		/** Nimbus 的 RSAKey */
		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	/**
	 * 用於解密與驗證 JWT Token 的 Decoder (由官方提供標準化建立方式)
	 */
	@Bean
	JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	/**
	 * 配置授權伺服器的基礎端點設定。 它會自動識別並繼承您 yml 中設定的 `server.servlet.context-path: /boot4-uaa`
	 */
	@Bean
	AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}
	
	/** ==========================================	**/
	/** 4. 原有的 CORS 與自訂 Handler 配置			**/
	/** ==========================================	**/
	
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
