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
		SpringApplication.run(FilmStorageApplication.class, args);
	}

}
