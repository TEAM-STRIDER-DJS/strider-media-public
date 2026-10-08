package com.strider.strider_media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.strider.strider_media",
		"com.strider.strider_common_lib"
})
public class StriderMediaApplication {

	public static void main(String[] args) {
		SpringApplication.run(StriderMediaApplication.class, args);
	}

}
