package com.storex.storex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StorexApplication {

	public static void main(String[] args) {
		SpringApplication.run(StorexApplication.class, args);
	}

}

