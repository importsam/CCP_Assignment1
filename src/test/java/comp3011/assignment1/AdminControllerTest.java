package comp3011.assignment1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

// https://medium.com/@deepjashan2020/types-of-testing-you-need-to-know-in-java-spring-boot-763b231853a2
// https://www.baeldung.com/spring-boot-testing#integration-testing-with-springboottest
// https://docs.spring.io/spring-framework/reference/testing/webtestclient.html#webtestclient-tests

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {
	
	@Autowired private MockMvc mvc;
	
	
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
}