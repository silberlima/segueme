package com.silberlima.segueme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SeguemeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeguemeApplication.class, args);
	}

}
