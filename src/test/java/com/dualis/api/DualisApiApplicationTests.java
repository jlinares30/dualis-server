package com.dualis.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.jwt.secret=test-only-secret-for-context-load-0123456789abcdef")
class DualisApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
