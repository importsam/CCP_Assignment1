package comp3011.assignment1;

import org.springframework.web.bind.annotation.RestController;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import com.openai.models.responses.inputtokens.InputTokenCountParams;
import com.openai.models.responses.inputtokens.InputTokenCountResponse;

import comp3011.assignment1.AdminController.TokenInOut;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;



@RestController 
public class AdminController {
	
	private final OpenAIClient client;
	private final TokenInOut tokenInOut;
	

	private final ConfigurableApplicationContext context;
	
	// This is the server start reference point
	private final Instant serverStart = Instant.now();
	
	// We need a thread-safe check for if a shutdown is in progress
	private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
	
    public AdminController(@Value("${OPENAI_API_KEY}") String apiKey, ConfigurableApplicationContext context, TokenInOut tokenInOut) {  
    	this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    	
    	this.context = context;
    	this.tokenInOut = tokenInOut;
    }
    
    @GetMapping("/api/v1/admin/uptime")
    public ResponseEntity<Map<String, Object>> uptime() throws Exception {
    	
    	Instant utcNow = Instant.now();
    	double serverUptimeSeconds = Duration.between(serverStart, utcNow).toSeconds();
    	
    	//Map response body to simulate JSON return format
    	Map<String, Object> responseBody = new LinkedHashMap<>();
    	responseBody.put("utcServerStart", serverStart.toString());
    	responseBody.put("utcNow", utcNow.toString());
    	responseBody.put("serverUptimeSeconds", serverUptimeSeconds);
    	
    	return ResponseEntity.ok(responseBody);
    }
    
    @PostMapping("/api/v1/admin/shutdown")
    public ResponseEntity<Map<String, Object>> shutdown() throws Exception {
    	
    	/* We need to match:
    	 * value:
            timestamp: "2026-07-14T03:45:30Z"
            status: 409
            error: "Conflict"
            message: "Graceful shutdown is already in progress."
            path: "/api/v1/admin/shutdown"
    	 */
    	
    	if(!shuttingDown.compareAndSet(false, true)) {
    		Map<String, Object> conflictResponseBody = new LinkedHashMap<>();
    		conflictResponseBody.put("timestamp", Instant.now().toString());
    		conflictResponseBody.put("status", 409);
    		conflictResponseBody.put("error", "Conflict");
    		conflictResponseBody.put("message", "Graceful shutdown is already in progress.");
    		conflictResponseBody.put("path", "/api/v1/admin/shutdown");
    	    
    		// https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/HttpStatus.html
    	    return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictResponseBody);
    	}
    	
    	try { // 202 case
        	new Thread(() -> {
        		try {
        			Thread.sleep(500);
        		}catch (InterruptedException e){}
        		
        		context.close();
        		
        	}).start();
        	
        	Map<String, Object> responseBody = new LinkedHashMap<>();
        	
        	responseBody.put("message", "Graceful shutdown requested.");
        	return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseBody);
        }
    	
    	catch (Exception e) {
    		shuttingDown.set(false);
    		Map<String, Object> errorResponseBody = new LinkedHashMap<>();
    		errorResponseBody.put("timestamp", Instant.now().toString());
    		errorResponseBody.put("status", 500);
    		errorResponseBody.put("error", "InternalServerError");
    		errorResponseBody.put("message", "Server error, graceful shutdown failed.");
    		errorResponseBody.put("path", "/api/v1/admin/shutdown");
    		
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseBody);
    	}
  
    }
        
    @GetMapping("/api/v1/global/stats")
    public ResponseEntity<Map<String, Object>> stats() throws Exception {
    	/* This endpoint will read from the TokenInOut count and
    	 * will send a response with the current input and output token count.
    	 */
	   	long token_count_in = tokenInOut.getTokensIn();
    	long token_count_out = tokenInOut.getTokensOut();
    	
    	Map<String, Object> responseBody = new LinkedHashMap<>();
    	
    	responseBody.put("inputTokens", token_count_in);
    	responseBody.put("outputTokens", token_count_out);
    	
    	return ResponseEntity.ok(responseBody);	
    	
	}

    @Component
    public static class TokenInOut {
    	/* AtomicInteger is used here because it is thread safe
    	 * when there are many HTTP requests happening at the same time. 
    	 * This avoids problems such as race conditions. By default, 
    	 * the token usage returns a long cast count.
    	 */
    	private final AtomicLong tokens_in = new AtomicLong(0);
    	private final AtomicLong tokens_out = new AtomicLong(0);
    	
    	public void updateTokens(long tokens_used_in, long tokens_used_out) {
    		tokens_in.getAndAdd(tokens_used_in);
    		tokens_out.getAndAdd(tokens_used_out);
    	}
    	
    	public long getTokensIn() {
    		return tokens_in.get();
    	}
    	
    	public long getTokensOut() {
    		return tokens_out.get();
    	}
    }
}



