package com.hoursofza.personalutility;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PersonalUtilityApplication {

	public static void main(String[] args) {
		if (System.getProperty("os.name").contains("Mac")) {
			System.setProperty("apple.awt.UIElement", "true");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.awt.application.appearance", "system");
			
		}
		SpringApplicationBuilder builder = new SpringApplicationBuilder(PersonalUtilityApplication.class);
		builder.headless(false);
		builder.run(args);
	}

}
