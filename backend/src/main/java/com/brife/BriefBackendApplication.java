package com.brife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class BriefBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BriefBackendApplication.class, args);
	}

}