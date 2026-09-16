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
//	OpenAI SDK client
	private final OpenAIClient client;
//	Token counter for input and output token usage
	private final TokenInOut tokenInOut;
	
//	Reference to the application context so that the /shutdown endpoint can trigger application shutdown
	private final ConfigurableApplicationContext context;
	
	// This is the server start reference point
	private final Instant serverStart = Instant.now();
	
	// We need a thread-safe check for if a shutdown is in progress
	private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
	
//	Constructor getting API key, application context, and the token usage count.
    public AdminController(@Value("${OPENAI_API_KEY}") String apiKey, ConfigurableApplicationContext context, TokenInOut tokenInOut) {  
    	this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    	
    	this.context = context;
    	this.tokenInOut = tokenInOut;
    }
    
    /*
     * GET Request endpoint to access the uptime of the server.
     * Returns the number of seconds the server has been running for.
     */
    @GetMapping("/api/v1/admin/uptime")
    public ResponseEntity<Map<String, Object>> uptime() throws Exception {
//    	Current utc time now
    	Instant utcNow = Instant.now();
//    	Calculate the difference between the serverStart time and the current time in seconds
    	double serverUptimeSeconds = Duration.between(serverStart, utcNow).toSeconds();
    	try {
//    		This is the 200 response case. If successful, return the start time, current time, and uptime.
    		
        	//Map response body to simulate JSON return format
        	Map<String, Object> responseBody = new LinkedHashMap<>();
        	responseBody.put("utcServerStart", serverStart.toString());
        	responseBody.put("utcNow", utcNow.toString());
        	responseBody.put("serverUptimeSeconds", serverUptimeSeconds);
        	
        	return ResponseEntity.ok(responseBody);
    	} 
    	catch (Exception e) {
//    		This is the 500 response code case. 
//    		On failure, return a status of 500 and that an internal error occurred.
    		
    		Map<String, Object> errorResponseBody = new LinkedHashMap<>();
    		errorResponseBody.put("timestamp", Instant.now().toString());
    		errorResponseBody.put("status", 500);
    		errorResponseBody.put("error", "Internal Server Error");
    		errorResponseBody.put("message", "Server uptime unavailable. An error occurred.");
    		errorResponseBody.put("path", "/api/v1/admin/uptime");
    		
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseBody);
    	}
    }
    
    
    /*
     * POST Request endpoint for initiating server shutdown.
     * Will only allow for shutdown to initiate once.
     */
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
//    	If the server is already shutting down, a conflict response is returned.
    	
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
    	
//    	Delay the shutdown by a second so the response can be sent to the caller,
//    	and then the server shuts down.
    	try { // 202 case
        	new Thread(() -> {
        		try {
        			Thread.sleep(1000);
        		} catch (InterruptedException e){}
        		
        		context.close();
        		
        	}).start();
        	
        	Map<String, Object> responseBody = new LinkedHashMap<>();
        	

        	responseBody.put("message", "Graceful shutdown requested.");
        	return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseBody);
        }
    	
    	catch (Exception e) {
//    		If anything goes wrong, a 500 response will be returned to the caller and the 
//    		server shut down is not initiated.
    		shuttingDown.set(false);
    		Map<String, Object> errorResponseBody = new LinkedHashMap<>();
    		errorResponseBody.put("timestamp", Instant.now().toString());
    		errorResponseBody.put("status", 500);
    		errorResponseBody.put("error", "Internal Server Error");
    		errorResponseBody.put("message", "Server error, graceful shutdown failed.");
    		errorResponseBody.put("path", "/api/v1/admin/shutdown");
    		
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseBody);
    	}
  
    }
    
    
    /* GET request endpoint which returns the current token usage count. 
     * This will return the number of input and output tokens used.
     */
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

    /*
     * TokenInOut bean used for tracking token input and output usage. 
     * The purpose of this is to keep track of token counts across the applications 
     * lifecycle.
     */
    @Component
    public static class TokenInOut {
    	/* AtomicLong is used here because it is thread safe
    	 * when there are many HTTP requests happening at the same time. 
    	 * This avoids problems such as race conditions. By default, 
    	 * the token usage returns a long cast count.
    	 */
    	private final AtomicLong tokens_in = new AtomicLong(0);
    	private final AtomicLong tokens_out = new AtomicLong(0);
    	
//    	To add more tokens 
    	public void updateTokens(long tokens_used_in, long tokens_used_out) {
//    		Ensure that token counts cannot be subtracted
    		if (tokens_used_in < 0 ||  tokens_used_out < 0) {
    			return;
    		}
//    		Updated the token counts
    		tokens_in.getAndAdd(tokens_used_in);
    		tokens_out.getAndAdd(tokens_used_out);
    	}
    	
//    	getter function for input token count
    	public long getTokensIn() {
    		return tokens_in.get();
    	}
//    	getter function for output token count
    	public long getTokensOut() {
    		return tokens_out.get();
    	}
    }
}