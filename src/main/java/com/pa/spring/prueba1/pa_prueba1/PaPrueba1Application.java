package com.pa.spring.prueba1.pa_prueba1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.TimeZone;

@SpringBootApplication

@EnableMongoRepositories("com.pa.spring.prueba1.pa_prueba1.repository")

@ServletComponentScan
public class PaPrueba1Application {

	public static void main(String[] args) {
        // Establecer la zona horaria por defecto de la JVM a la zona local de la barbería.
        // Esto ayuda a que las conversiones entre LocalDateTime y Date/BSON sean consistentes
        // con la zona America/Bogota (UTC-5) y evita desajustes al consultar rangos.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        System.out.println("Zona por defecto establecida a: " + TimeZone.getDefault().getID());
		SpringApplication.run(PaPrueba1Application.class, args);

		System.out.println("Aplicación iniciada correctamente con MongoDB como base de datos");
	}
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() { 
			@Override
			public void addCorsMappings(@NonNull CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("*")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*");
			}
		};
	}
}
