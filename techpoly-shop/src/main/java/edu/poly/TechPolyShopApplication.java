package edu.poly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TechPolyShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechPolyShopApplication.class, args);
	}

}
