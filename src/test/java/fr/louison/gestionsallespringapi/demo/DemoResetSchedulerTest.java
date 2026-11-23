package fr.louison.gestionsallespringapi.demo;

import fr.louison.gestionsallespringapi.demo.service.DemoResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DemoResetSchedulerTest {

    @Mock private DemoResetService demoResetService;

    // RG-5-02: the nightly task hands over to the reset service
    @Test
    void theNightlyTaskResetsTheDemoData() {
        new DemoResetScheduler(demoResetService).resetNightly();

        verify(demoResetService).reset();
    }

    // RG-5-02: the schedule comes from the configuration and runs on Paris time
    @Test
    void theTaskIsScheduledFromTheConfigurationOnParisTime() throws NoSuchMethodException {
        Scheduled scheduled = DemoResetScheduler.class.getMethod("resetNightly").getAnnotation(Scheduled.class);

        assertThat(scheduled.cron()).isEqualTo("${app.demo.reset-cron}");
        assertThat(scheduled.zone()).isEqualTo("Europe/Paris");
    }
}
