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
		System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
		System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("RABBITMQ_HOST", dotenv.get("RABBITMQ_HOST"));
		System.setProperty("RABBITMQ_PORT", dotenv.get("RABBITMQ_PORT"));
		System.setProperty("RABBITMQ_USERNAME", dotenv.get("RABBITMQ_USERNAME"));
		System.setProperty("RABBITMQ_PASSWORD", dotenv.get("RABBITMQ_PASSWORD"));
		System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));
		System.setProperty("REDIS_PORT", dotenv.get("REDIS_PORT"));
		System.setProperty("ES_HOST", dotenv.get("ES_HOST"));
		System.setProperty("ES_PORT", dotenv.get("ES_PORT"));
		System.setProperty("APP_SECURITY_REFRESH_EXPIRATION", dotenv.get("APP_SECURITY_REFRESH_EXPIRATION"));
		System.setProperty("APP_SECURITY_ACCESS_EXPIRATION", dotenv.get("APP_SECURITY_ACCESS_EXPIRATION"));
		System.setProperty("APP_SECURITY_SECRET", dotenv.get("APP_SECURITY_SECRET"));
		SpringApplication.run(FilmStorageApplication.class, args);
	}

}
