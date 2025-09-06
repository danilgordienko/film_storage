package ru.danilgordienko.film_fetcher;

//import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FilmFetcherApplication {

	public static void main(String[] args) {
//		Dotenv dotenv = Dotenv.load();
//		System.setProperty("API_KEY", dotenv.get("API_KEY"));
//		System.setProperty("PROXY_HOST", dotenv.get("PROXY_HOST"));
//		System.setProperty("PROXY_PORT", dotenv.get("PROXY_PORT"));
//		System.setProperty("PROXY_USERNAME", dotenv.get("PROXY_USERNAME"));
//		System.setProperty("PROXY_PASSWORD", dotenv.get("PROXY_PASSWORD"));
//		System.setProperty("RABBITMQ_HOST", dotenv.get("RABBITMQ_HOST"));
//		System.setProperty("RABBITMQ_PORT", dotenv.get("RABBITMQ_PORT"));
//		System.setProperty("RABBITMQ_USERNAME", dotenv.get("RABBITMQ_USERNAME"));
//		System.setProperty("RABBITMQ_PASSWORD", dotenv.get("RABBITMQ_PASSWORD"));
//		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
//		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
//		System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
//		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
//		System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
		SpringApplication.run(FilmFetcherApplication.class, args);
	}

}
