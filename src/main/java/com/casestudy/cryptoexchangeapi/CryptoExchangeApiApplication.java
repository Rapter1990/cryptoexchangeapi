package com.casestudy.cryptoexchangeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CryptoExchangeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoExchangeApiApplication.class, args);
	}

}
