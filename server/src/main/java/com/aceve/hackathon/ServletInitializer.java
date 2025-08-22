package com.aceve.hackathon;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(HackathonApplication.class);
	}

}
