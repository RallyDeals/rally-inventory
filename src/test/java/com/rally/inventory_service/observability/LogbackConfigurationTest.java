package com.rally.inventory_service.observability;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class LogbackConfigurationTest {

    @Test
    @DisplayName("Verify Logback initialized with CONSOLE, ROLLING_FILE and LOKI appenders via Spring Boot LoggingSystem")
    void testLogbackAppendersConfigured() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        LogbackLoggingSystem loggingSystem = new LogbackLoggingSystem(getClass().getClassLoader());
        loggingSystem.initialize(new LoggingInitializationContext(environment), "classpath:logback-spring.xml", null);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        Appender<ILoggingEvent> console = rootLogger.getAppender("CONSOLE");
        Appender<ILoggingEvent> rollingFile = rootLogger.getAppender("ROLLING_FILE");
        Appender<ILoggingEvent> loki = rootLogger.getAppender("LOKI");

        assertThat(console).as("CONSOLE appender should be attached to root logger").isNotNull();
        assertThat(rollingFile).as("ROLLING_FILE appender should be attached to root logger").isNotNull();
        assertThat(loki).as("LOKI appender should be attached to root logger").isNotNull();
    }
}
