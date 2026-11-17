package fr.louison.gestionsallespringapi.configuration;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Creates the first administrator on an empty database. Without it nobody can log in,
 * since accounts can only be created by an authenticated administrator or manager.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private static final String DEFAULT_DEPARTMENT = "Administration";

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            return;
        }
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.warn("No employee exists and ADMIN_EMAIL / ADMIN_PASSWORD are not set: nobody can log in.");
            return;
        }

        DepartmentEntity department = departmentRepository.findByName(DEFAULT_DEPARTMENT)
                .orElseGet(() -> departmentRepository.save(DepartmentEntity.builder().name(DEFAULT_DEPARTMENT).build()));

        employeeRepository.save(EmployeeEntity.builder()
                .firstName("Admin")
                .lastName("System")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(EmployeeRole.ROLE_ADMIN)
                .department(department)
                .build());

        log.info("Initial administrator account created for {}", adminEmail);
    }
}
