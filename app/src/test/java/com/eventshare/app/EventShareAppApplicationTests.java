package com.eventshare.app;

import com.eventshare.app.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class EventShareAppApplicationTests {

	@Test
	void contextLoads() {
		//これから設定
	}

}