package com.sporton.SportOn;

import com.sporton.SportOn.entity.AppUser;
import com.sporton.SportOn.entity.Role;
import com.sporton.SportOn.repository.AppUserRepository;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@SpringBootApplication
@EnableScheduling
public class SportOnApplication {

	public static void main(String[] args) {
		SpringApplication.run(SportOnApplication.class, args);
	}

	private static String urlEncode(String value) {
		// Implement URL encoding logic here
		// (Note: You may want to use a library like URLEncoder for this)
		// Example assumes simple encoding
		return value.replaceAll(" ", "%20");
	}
	@Scheduled(fixedRate = 100000) // Execute every 10 minutes (600,000 milliseconds)
	public static String getAccessToken() throws URISyntaxException, IOException, InterruptedException {
		String apiUrl = "https://smsapi.hormuud.com//token";
		String username = "mohamed200";
		String password = "9yvLL9LpmZIuhNTFARYCJw==";
		String grant_type = "password";

		// Encode request parameters using URLEncoder
		String encodedParams = URLEncoder.encode("username=" + username, "UTF-8") + "&" +
				URLEncoder.encode("password=" + password, "UTF-8") + "&" +
				URLEncoder.encode("grant_type=" + grant_type, "UTF-8");

		// Build the HTTP request

		String tokenEndpoint = "https://smsapi.hormuud.com//token";
		String requestBody = String.format("grant_type=password&username=mohamed200&password=9yvLL9LpmZIuhNTFARYCJw==",
				urlEncode(username), urlEncode(password));

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(tokenEndpoint))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
				.build();

		try {
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			String resultJSON = response.body();
			return extractAccessToken(resultJSON);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	private static String extractAccessToken(String resultJSON) {
		// Parse JSON to extract access token
		// (Note: You may want to use a JSON library like Jackson or Gson for this)
		// Example assumes using simple string manipulation
		return resultJSON.substring(resultJSON.indexOf("\"access_token\":\"") + 16, resultJSON.indexOf("\",\""));
	}

	@Bean
	CommandLineRunner initAdminUser(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			String adminPhoneNumber = "612518368";

			// Check if admin user already exists
			if (appUserRepository.findByPhoneNumber(adminPhoneNumber).isEmpty()) {
				AppUser adminUser = AppUser.builder()
						.fullName("Yusuf Ahmed Shire")
						.phoneNumber(adminPhoneNumber)
						.email("yusufshire58@gmail.com")
						.password(passwordEncoder.encode("12345678"))
						.role(Role.ADMIN)
						.joinedDate(java.time.LocalDate.now().toString())
						.approved(true)
						.build();

				// Save admin user to the database
				appUserRepository.save(adminUser);
				System.out.println("Admin user created");
			} else {
				System.out.println("Admin user already exists");
			}
		};
	}

}
