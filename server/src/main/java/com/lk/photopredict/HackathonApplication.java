package com.lk.photopredict;

import com.lk.photopredict.repository.Bucket;
import com.lk.photopredict.repository.DataStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@SpringBootApplication
public class HackathonApplication {

	public static final String PROJECT_ID = "hackathon2025-458305";
	public static final String LOCATION = "europe-west1"; // Western Europe region


	private static final Logger logger = LoggerFactory.getLogger(HackathonApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(HackathonApplication.class, args);
	}

	@Bean
	public Credentials credentials() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream credentialsStream = classloader.getResourceAsStream("hackathon2025-458305-3dfaf8634507.json");
		try {
			assert credentialsStream != null;
			return GoogleCredentials.fromStream(credentialsStream)
					.createScoped(Arrays.asList(
							"https://www.googleapis.com/auth/cloud-platform",
							"https://www.googleapis.com/auth/cloud-vision",
							"https://www.googleapis.com/auth/cloud-platform.read-only"
					));
		} catch (IOException e) {
			logger.error("Failed to load credentials", e);
			throw new RuntimeException(e);
		}
	}

	@Bean
	public CredentialsProvider credentialsProvider(Credentials credentials) {
		return () -> credentials;
	}

	@Bean
	public Storage storage(Credentials credentials) {
		return StorageOptions.newBuilder()
				.setCredentials(credentials)
				.setProjectId("hackathon2025-458305")
				.build()
				.getService();
	}
	@Bean @Qualifier("image")
	public DataStore imageDataStore(Storage storage) {
		return new DataStore(storage, Bucket.Name.Image);
	}

	@Bean @Qualifier("recording")
	public DataStore recordingDataStore(Storage storage) {
		return new DataStore(storage, Bucket.Name.Recording);
	}
	@Bean @Qualifier("deviation")
	public DataStore deviationDataStore(Storage storage) {
		return new DataStore(storage, Bucket.Name.Deviation);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
