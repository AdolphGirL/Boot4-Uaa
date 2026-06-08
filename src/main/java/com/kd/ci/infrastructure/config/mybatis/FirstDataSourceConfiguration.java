package com.kd.ci.infrastructure.config.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** 客製化交易管理與資料源 */
@Configuration
@MapperScan(basePackages = "com.kd.ci.infrastructure.persistence.mapper.first", sqlSessionFactoryRef = "firstSqlSessionFactory")
public class FirstDataSourceConfiguration {
	
	/** 手動指定 mapper.xml 位置 **/
	public static final String MAPPER_LOCATION = "classpath*:mybatis/mapper/first/*.xml";

	public static final String TYPE_ALIASES_PACKAGE = "com.kd.ci.domain.first";
	
	/**
	 * 全域的mybatis session 設定
	 * MyBatis 的核心配置類，負責加載和存儲 MyBatis 的所有配置信息。
	 * 
	 * 配置多源資料庫 sql log，多數據來源列印sql執行日誌問題
	 * 
	 * @return
	 */
	@Bean
	@ConfigurationProperties(prefix = "mybatis.configuration")
	org.apache.ibatis.session.Configuration globalConfiguration() {
		return new org.apache.ibatis.session.Configuration();
	}
	
	@Primary
	@Bean("firstHikariConfig")
	@ConfigurationProperties(prefix = "spring.datasource.first")
	HikariConfig firstHikariConfig() {
		return new HikariConfig();
	}

	@Primary
	@Bean("firstDataSource")
	HikariDataSource firstDataSource(@Qualifier("firstHikariConfig") HikariConfig hikariConfig) {
		return new HikariDataSource(hikariConfig);
	}
	
	@Primary
	@Bean(name = "firstJdbcTemplate")
	JdbcTemplate jdbcTemplate(@Qualifier("firstDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	/**
	 * 交易管理
	 * @param hikariDataSource
	 * @return
	 */
	@Bean(name = "firstTransactionManager")
	@Primary
	PlatformTransactionManager firstTransactionManager(
			@Qualifier("firstDataSource") HikariDataSource hikariDataSource) {
		return new DataSourceTransactionManager(hikariDataSource);
	}
	
	/**
	 * sql session factory
	 * @param hikariDataSource
	 * @param configuration
	 * @return
	 * @throws Exception
	 */
	@Primary
	@Bean(name = "firstSqlSessionFactory")
	SqlSessionFactory firstSqlSessionFactory(@Qualifier("firstDataSource") HikariDataSource hikariDataSource,
			org.apache.ibatis.session.Configuration configuration) throws Exception {
		final SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
		sessionFactoryBean.setDataSource(hikariDataSource);
		
		/** 其中一個配置 globalConfiguration **/
		sessionFactoryBean.setConfiguration(configuration);
		
		sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_LOCATION));
		sessionFactoryBean.setTypeAliasesPackage(TYPE_ALIASES_PACKAGE);
		return sessionFactoryBean.getObject();
	}

	@Primary
	@Bean(name = "firstSqlSessionTemplate")
	SqlSessionTemplate sqlSessionTemplate(
			@Qualifier("firstSqlSessionFactory") SqlSessionFactory firstSqlSessionFactory) throws Exception {
		return new SqlSessionTemplate(firstSqlSessionFactory);
	}
	
}
