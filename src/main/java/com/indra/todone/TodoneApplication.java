package com.indra.todone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TodoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoneApplication.class, args);
	}

}
