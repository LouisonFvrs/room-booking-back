package fr.louison.gestionsallespringapi.demo.service;

import fr.louison.gestionsallespringapi.demo.dto.DemoAccountDto;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * The accounts a visitor may use on a demonstration deployment. Their password is public by design:
 * the application hands it to anyone, so the demo mode must never be enabled on a real installation.
 */
@Service
public class DemoAccountService {

    public static final String ADMIN_EMAIL = "admin@demo.local";
    public static final String MANAGER_EMAIL = "manager@demo.local";
    public static final String EMPLOYEE_EMAIL = "employe@demo.local";

    private final boolean enabled;
    private final String password;

    public DemoAccountService(
            @Value("${app.demo.enabled:false}") boolean enabled,
            @Value("${app.demo.password:}") String password
    ) {
        this.enabled = enabled;
        this.password = password;
    }

    /** The demo mode needs a password to create its accounts: without one it stays off. */
    public boolean isEnabled() {
        return enabled && StringUtils.hasText(password);
    }

    public String getPassword() {
        return password;
    }

    public List<DemoAccountDto> getAccounts() {
        if (!isEnabled()) {
            return List.of();
        }

        return List.of(
                new DemoAccountDto(EmployeeRole.ROLE_ADMIN, ADMIN_EMAIL, password),
                new DemoAccountDto(EmployeeRole.ROLE_MANAGER, MANAGER_EMAIL, password),
                new DemoAccountDto(EmployeeRole.ROLE_EMPLOYEE, EMPLOYEE_EMAIL, password)
        );
    }
}
