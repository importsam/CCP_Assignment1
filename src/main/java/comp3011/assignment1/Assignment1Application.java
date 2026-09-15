package comp3011.assignment1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

@SpringBootApplication
public class Assignment1Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Assignment1Application.class, args);
	}
}
