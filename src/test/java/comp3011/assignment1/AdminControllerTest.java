package comp3011.assignment1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import comp3011.assignment1.AdminController.TokenInOut;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// https://medium.com/@deepjashan2020/types-of-testing-you-need-to-know-in-java-spring-boot-763b231853a2
// https://www.baeldung.com/spring-boot-testing#integration-testing-with-springboottest
// https://docs.spring.io/spring-framework/reference/testing/webtestclient.html#webtestclient-tests

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {
	
	@Autowired private MockMvc mvc;
	@Autowired private TokenInOut tokenInOut;
	
	@MockitoBean
	private ConfigurableApplicationContext context;
	
	/*
	 * Assessing if the /api/v1/admin/uptime
	 * endpoint returns an expected 200 status and the
	 * expected JSON components.
	 */
	@Test
	void uptimeTest() throws Exception {
		mvc.perform(get("/api/v1/admin/uptime"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.utcServerStart").exists())
		.andExpect(jsonPath("$.utcNow").exists())
		.andExpect(jsonPath("$.serverUptimeSeconds").exists());
	}	
	
	@Test 
	void shutdownTest() throws Exception {
		mvc.perform(post("/api/v1/admin/shutdown"))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.message").value("Graceful shutdown requested."));
		
		mvc.perform(post("/api/v1/admin/shutdown"))
			.andExpect(status().isConflict())
	        .andExpect(jsonPath("$.status").value(409))
	        .andExpect(jsonPath("$.error").value("Conflict"))
	        .andExpect(jsonPath("$.message").value("Graceful shutdown is already in progress."))
	        .andExpect(jsonPath("$.path").value("/api/v1/admin/shutdown"));
	}
	
	@Test 
	void statsTest() throws Exception {
		// mock tokens in and out
		long in = tokenInOut.getTokensIn();
		long out = tokenInOut.getTokensOut();
		
		// Update the current token count
		tokenInOut.updateTokens(50,50);
		
		// check the token counts and that the values were updated correctly
	    mvc.perform(get("/api/v1/global/stats"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.inputTokens").value(in + 50))
	        .andExpect(jsonPath("$.outputTokens").value(out + 50));
	}
	
}