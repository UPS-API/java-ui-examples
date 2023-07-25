package com.ups.shippingWebApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.ups.shippingWebApp")
public class ShippingWebAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShippingWebAppApplication.class, args);
	}

}
