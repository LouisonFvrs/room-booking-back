package fr.louison.gestionsallespringapi.demo.service;

import fr.louison.gestionsallespringapi.demo.dto.DemoAccountDto;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DemoAccountServiceTest {

    @Test
    void noAccountIsServedWhenTheDemoModeIsOff() {
        assertThat(new DemoAccountService(false, "un-mot-de-passe").getAccounts()).isEmpty();
    }

    @Test
    void theDemoModeStaysOffWithoutAPassword() {
        DemoAccountService service = new DemoAccountService(true, "  ");

        assertThat(service.isEnabled()).isFalse();
        assertThat(service.getAccounts()).isEmpty();
    }

    @Test
    void oneAccountPerRoleIsServedWhenTheDemoModeIsOn() {
        DemoAccountService service = new DemoAccountService(true, "un-mot-de-passe");

        assertThat(service.getAccounts())
                .extracting(DemoAccountDto::role)
                .containsExactly(EmployeeRole.ROLE_ADMIN, EmployeeRole.ROLE_MANAGER, EmployeeRole.ROLE_EMPLOYEE);
        assertThat(service.getAccounts()).allSatisfy(account -> assertThat(account.password()).isEqualTo("un-mot-de-passe"));
    }
}
