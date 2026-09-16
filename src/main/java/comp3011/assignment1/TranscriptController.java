package comp3011.assignment1;

import comp3011.assignment1.AdminController.TokenInOut;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import java.nio.file.Path;

import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Value;

@RestController 
public class TranscriptController {
	
	private final OpenAIClient client;
	private final TokenInOut tokenInOut;
	
    public TranscriptController(@Value("${OPENAI_API_KEY}") String apiKey, TokenInOut tokenInOut) {  
    	this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    	
    	this.tokenInOut = tokenInOut;
    }
	
	@PostMapping("/transcribe")
	public String transcribe(@RequestParam("audio") MultipartFile audio) throws Exception {
//		https://developers.openai.com/api/reference/java/resources/audio/subresources/transcriptions
		
		Path temp = Files.createTempFile("audio", ".webm");
		audio.transferTo(temp);
		
		var model_response = client.audio().transcriptions().create(
            TranscriptionCreateParams.builder()
	            .file(temp)
	            .model("gpt-4o-mini-transcribe")
	            .build());
		
		Files.deleteIfExists(temp);
		
		var transcript = model_response.asTranscription();
		transcript.usage().ifPresent(usage -> {
			if(usage.isTokens()) {
				long tokensIn = usage.asTokens().inputTokens();
				long tokensOut = usage.asTokens().outputTokens();
				tokenInOut.updateTokens(tokensIn, tokensOut);
			}
		});
		
		return transcript.text();
	}
}