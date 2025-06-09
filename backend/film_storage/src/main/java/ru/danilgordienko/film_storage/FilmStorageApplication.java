package ru.danilgordienko.film_storage;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmStorageApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("EXPR_TIME", dotenv.get("EXPR_TIME"));
		System.setProperty("SECRET", dotenv.get("SECRET"));
		System.setProperty("API_KEY", dotenv.get("API_KEY"));
		System.setProperty("PROXY_HOST", dotenv.get("PROXY_HOST"));
		System.setProperty("PROXY_PORT", dotenv.get("PROXY_PORT"));
		System.setProperty("PROXY_USERNAME", dotenv.get("PROXY_USERNAME"));
		System.setProperty("PROXY_PASSWORD", dotenv.get("PROXY_PASSWORD"));
		SpringApplication.run(FilmStorageApplication.class, args);
	}

}
