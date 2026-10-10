package com.example.keebmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KeebMallApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeebMallApplication.class, args);
	}

}
