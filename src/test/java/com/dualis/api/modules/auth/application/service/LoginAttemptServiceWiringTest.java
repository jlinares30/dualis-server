package com.dualis.api.modules.auth.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/** Guards the Spring wiring of a bean with two constructors (caught only at startup otherwise). */
class LoginAttemptServiceWiringTest {

    @Test
    @DisplayName("Spring can instantiate LoginAttemptService")
    void springCreatesBean() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(LoginAttemptService.class);
            ctx.refresh();
            assertThat(ctx.getBean(LoginAttemptService.class)).isNotNull();
        }
    }
}
