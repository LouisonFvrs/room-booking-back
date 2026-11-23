package fr.louison.gestionsallespringapi.demo;

import fr.louison.gestionsallespringapi.demo.service.DemoResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Resets a demonstration deployment every night (RG-5-02). The task always runs; outside the demo
 * mode the service itself does nothing, so a real installation is never touched.
 */
@Component
@RequiredArgsConstructor
public class DemoResetScheduler {

    private final DemoResetService demoResetService;

    // Paris time whatever the server's zone: hosting platforms usually run in UTC
    @Scheduled(cron = "${app.demo.reset-cron}", zone = "Europe/Paris")
    public void resetNightly() {
        demoResetService.reset();
    }
}
