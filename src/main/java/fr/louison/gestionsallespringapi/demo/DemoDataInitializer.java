package fr.louison.gestionsallespringapi.demo;

import fr.louison.gestionsallespringapi.demo.service.DemoAccountService;
import fr.louison.gestionsallespringapi.demo.service.DemoDataSeeder;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fills a demonstration deployment with accounts, rooms and a few meetings so that a visitor
 * has something to look at. It runs once: an existing demo administrator means the data is there.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final DemoAccountService demoAccountService;
    private final EmployeeRepository employeeRepository;
    private final DemoDataSeeder demoDataSeeder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoAccountService.isEnabled() || employeeRepository.existsByEmail(DemoAccountService.ADMIN_EMAIL)) {
            return;
        }

        demoDataSeeder.seed();

        log.warn("Demo mode is ON: demo accounts exist and their password is served by /api/auth/demo-accounts.");
    }
}
