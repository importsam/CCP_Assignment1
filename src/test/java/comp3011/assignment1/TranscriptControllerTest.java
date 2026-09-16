package comp3011.assignment1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.openai.client.OpenAIClient;
import com.openai.models.audio.transcriptions.Transcription;
import com.openai.models.audio.transcriptions.TranscriptionCreateResponse;

// https://medium.com/@deepjashan2020/types-of-testing-you-need-to-know-in-java-spring-boot-763b231853a2
// https://www.baeldung.com/spring-boot-testing#integration-testing-with-springboottest
// https://site.mockito.org/javadoc/current/org/mockito/Mock.html
@SpringBootTest
@AutoConfigureMockMvc
class TranscriptControllerTest {
	
	@Autowired private MockMvc mvc;
	
	@MockitoBean private OpenAIClient client;
	
	@Test
	void transcribeRequestTest() throws Exception {
		// build an empty shell of a transcript
		Transcription testTranscript = mock(Transcription.class);
		// When .text() is called on testTranscript, return "Test transcript"
		when(testTranscript.text()).thenReturn("Test transcript");
		
		// Create a mock response
		TranscriptionCreateResponse testResponse = mock(TranscriptionCreateResponse.class);
		// when .asTranscription() called on the testResponse, return the test transcript
		when(testResponse.asTranscription()).thenReturn(testTranscript);
		
		// create a fake audio transcript
		when(client.audio().transcriptions().create(any())).thenReturn(testResponse);
		
		// Create a mock audio file
		MockMultipartFile fakeAudio = new MockMultipartFile("audio", "test.webm", "audio/webm", "fake audio bytes".getBytes());
		
		// Call the endpoint with the fake audio
		mvc.perform(multipart("/transcribe").file(fakeAudio))
	    .andExpect(status().isOk())
	    .andExpect(content().string("Test transcript"));
	}
}