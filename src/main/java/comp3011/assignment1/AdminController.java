package comp3011.assignment1;

import org.springframework.web.bind.annotation.RestController;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestParam;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;



@RestController 
public class AdminController {
	
	private final OpenAIClient client;
	
	// This is the start reference point
	private final Instant serverStart = Instant.now();
	
    public AdminController(@Value("${OPENAI_API_KEY}") String apiKey) {  
    	this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    }
    
    @GetMapping("/api/v1/admin/uptime")
    public Map<String, Object> uptime() throws Exception {
    	
    	Instant utcNow = Instant.now();
    	double serverUptimeSeconds = Duration.between(utcNow, serverStart).toSeconds();
    	
    	//Map response body to simulate JSON return format
    	Map<String, Object> responseBody = new LinkedHashMap<>();
    	responseBody.put("utcServerStart", serverStart.toString());
    	responseBody.put("utcNow", utcNow.toString());
    	responseBody.put("serverUptimeSeconds", serverUptimeSeconds);
    	
    	return responseBody;
    }
    
    @GetMapping("/api/v1/admin/shutdown")
    public String shutdown() throws Exception {
    	
    }
    
    @GetMapping("/api/v1/global/stats")
    public String stats() throws Exception {
    	
    }
}