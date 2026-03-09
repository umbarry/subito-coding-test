package com.subito.subitocodingtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubitoCodingTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubitoCodingTestApplication.class, args);
	}

}
