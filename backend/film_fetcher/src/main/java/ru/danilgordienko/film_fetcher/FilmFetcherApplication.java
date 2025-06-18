package ru.danilgordienko.film_fetcher;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmFetcherApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("API_KEY", dotenv.get("API_KEY"));
		System.setProperty("PROXY_HOST", dotenv.get("PROXY_HOST"));
		System.setProperty("PROXY_PORT", dotenv.get("PROXY_PORT"));
		System.setProperty("PROXY_USERNAME", dotenv.get("PROXY_USERNAME"));
		System.setProperty("PROXY_PASSWORD", dotenv.get("PROXY_PASSWORD"));
		SpringApplication.run(FilmFetcherApplication.class, args);
	}

}
