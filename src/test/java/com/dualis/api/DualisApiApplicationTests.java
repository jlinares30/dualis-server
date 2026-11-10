package com.dualis.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the whole application against a real PostgreSQL: runs every Flyway migration and validates
 * the resulting schema against the JPA entities. Runs only when DB_URL points to a database
 * (CI provides one; locally: docker compose up -d postgres-db and export DB_URL/DB_USERNAME/DB_PASSWORD).
 */
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-for-context-load-0123456789abcdef",
        "app.exchange-rates.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class DualisApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
