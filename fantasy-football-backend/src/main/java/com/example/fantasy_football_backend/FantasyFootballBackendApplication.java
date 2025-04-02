package com.example.fantasy_football_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FantasyFootballBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FantasyFootballBackendApplication.class, args);
	}
}