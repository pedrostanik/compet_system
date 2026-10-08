package com.petshop.api;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PetshopApiApplicationTests {

	// AnthropicAutoConfiguration is excluded in src/test/resources/application.properties,
	// so nothing provides the ChatModel that AiConfig#chatClient requires.
	@MockitoBean
	private ChatModel chatModel;

	@Test
	void contextLoads() {
	}

}
