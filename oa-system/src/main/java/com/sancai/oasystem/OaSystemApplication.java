package com.sancai.oasystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement
@ComponentScan(basePackages = {
		"com.sancai.oasystem.controller",
		"com.sancai.oasystem.bean",
		"com.sancai.oasystem.service"})
@MapperScan("com.sancai.oasystem.dao")
@SpringBootApplication
public class OaSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OaSystemApplication.class, args);
	}

}
