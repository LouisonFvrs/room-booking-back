package fr.louison.gestionsallespringapi.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns on the {@code @Scheduled} tasks, such as the nightly reset of a demonstration deployment.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
