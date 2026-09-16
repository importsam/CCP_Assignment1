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
	
//	OpenAI SDK client
	private final OpenAIClient client;
	
//	Token counter for input and output token usage
	private final TokenInOut tokenInOut;
	
	// Constructor, we import the API key securely and initialise the token stats.  
    public TranscriptController(@Value("${OPENAI_API_KEY}") String apiKey, TokenInOut tokenInOut) {  
    	this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    	
    	this.tokenInOut = tokenInOut;
    }
	
/*  Transcribe endpoint for sending microphone audio to the OpenAI model and receiving a transcript./
  * Additionally, this records the number of tokens used in input and output, recorded via the TokenInOut object.
  */
	@PostMapping("/transcribe")
	public String transcribe(@RequestParam("audio") MultipartFile audio) throws Exception {
//		https://developers.openai.com/api/reference/java/resources/audio/subresources/transcriptions
		
//		The OpenAI client requests a file from disk, so save the audio file to disk temporarily.
		Path temp = Files.createTempFile("audio", ".webm");
		audio.transferTo(temp);
		
//		Send the audio to the gpt-4o-mini-transcribe model
		var model_response = client.audio().transcriptions().create(
            TranscriptionCreateParams.builder()
	            .file(temp)
	            .model("gpt-4o-mini-transcribe")
	            .build());
		
//		Remove the temporary audio file from disk
		Files.deleteIfExists(temp);
		
//		Extract/unwrap the transcript from the client response object
		var transcript = model_response.asTranscription();
//		The response may be empty if nothing was returned, so check first and then update token count.
		model_response.asTranscription().usage().ifPresent(usage -> {
			if(usage.isTokens()) {
				long tokensIn = usage.asTokens().inputTokens();
				long tokensOut = usage.asTokens().outputTokens();
				tokenInOut.updateTokens(tokensIn, tokensOut);
			}
		});
		
		// return the transcribed text
		return transcript.text();
	}
}