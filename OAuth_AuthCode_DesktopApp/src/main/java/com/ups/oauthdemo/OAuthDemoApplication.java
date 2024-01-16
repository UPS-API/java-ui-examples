package com.ups.oauthdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.ups.oauthdemo.view.GUI;


@SpringBootApplication
public class OAuthDemoApplication {
	
	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(OAuthDemoApplication.class);
		builder.headless(false); //App uses a GUI
		builder.run(args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx, GUI gui) {
		return args -> {
			gui.inputClientCredentials();
		};
	}
}
