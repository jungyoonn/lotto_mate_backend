package com.eeerrorcode.lottomate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LottomateApplication {

	public static void main(String[] args) {
		SpringApplication.run(LottomateApplication.class, args);
	}

}
