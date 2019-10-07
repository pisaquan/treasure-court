package com.sancai.oa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.sancai.oa.*.mapper")
@SpringBootApplication
@EnableCaching
@EnableScheduling
@ServletComponentScan
@ImportResource(locations = { "classpath:druid-bean.xml" })
public class Application extends SpringBootServletInitializer {


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


//	private CorsConfiguration buildConfig() {
//		//TODO 允许跨域 测试用
//		CorsConfiguration corsConfiguration = new CorsConfiguration();
//		corsConfiguration.addAllowedOrigin("*"); // 1
//		corsConfiguration.addAllowedHeader("*"); // 2
//		corsConfiguration.addAllowedMethod("*"); // 3
//		corsConfiguration.setAllowCredentials(true);
//		return corsConfiguration;
//	}


//	@Bean
//	public CorsFilter corsFilter() {
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		source.registerCorsConfiguration("/**", buildConfig()); // 4
//		return new CorsFilter(source);
//	}

}
