package ru.danilgordienko.film_storage;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FilmStorageApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		//System.setProperty("EXPR_TIME", dotenv.get("EXPR_TIME"));
		System.setProperty("RABBITMQ_HOST", dotenv.get("RABBITMQ_HOST"));
		System.setProperty("RABBITMQ_PORT", dotenv.get("RABBITMQ_PORT"));
		System.setProperty("RABBITMQ_USERNAME", dotenv.get("RABBITMQ_USERNAME"));
		System.setProperty("RABBITMQ_PASSWORD", dotenv.get("RABBITMQ_PASSWORD"));
		System.setProperty("APP_SECURITY_REFRESH_EXPIRATION", dotenv.get("APP_SECURITY_REFRESH_EXPIRATION"));
		System.setProperty("APP_SECURITY_ACCESS_EXPIRATION", dotenv.get("APP_SECURITY_ACCESS_EXPIRATION"));
		System.setProperty("APP_SECURITY_SECRET", dotenv.get("APP_SECURITY_SECRET"));
		SpringApplication.run(FilmStorageApplication.class, args);
	}

}
