package fr.louison.gestionsallespringapi.employee.service;

import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.dto.EmployeeDto;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.exception.BadRequestException;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import fr.louison.gestionsallespringapi.invitation.repository.InvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeServiceTest {

    private static final long DEPARTMENT_ID = 7L;

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private final EmployeeEntity admin = employee(1L, EmployeeRole.ROLE_ADMIN);
    private final EmployeeEntity manager = employee(2L, EmployeeRole.ROLE_MANAGER);
    private final EmployeeEntity otherManager = employee(3L, EmployeeRole.ROLE_MANAGER);
    private final EmployeeEntity employee = employee(4L, EmployeeRole.ROLE_EMPLOYEE);

    @BeforeEach
    void setUp() {
        List.of(admin, manager, otherManager, employee).forEach(person ->
                when(employeeRepository.findById(person.getIdEmployee())).thenReturn(Optional.of(person)));
        when(departmentRepository.findById(DEPARTMENT_ID))
                .thenReturn(Optional.of(DepartmentEntity.builder().idDepartment(DEPARTMENT_ID).name("Produit").build()));
        when(employeeRepository.save(any(EmployeeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
    }

    @Test
    void aManagerCreatesAnEmployeeAccount() {
        EmployeeDto.PostOutputDto created = employeeService.createEmployee(input(EmployeeRole.ROLE_EMPLOYEE), manager.getIdEmployee());

        assertThat(created.getRole()).isEqualTo(EmployeeRole.ROLE_EMPLOYEE);
    }

    @Test
    void aManagerCannotCreateAManagerOrAnAdministrator() {
        assertThatThrownBy(() -> employeeService.createEmployee(input(EmployeeRole.ROLE_MANAGER), manager.getIdEmployee()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> employeeService.createEmployee(input(EmployeeRole.ROLE_ADMIN), manager.getIdEmployee()))
                .isInstanceOf(AccessDeniedException.class);

        verify(employeeRepository, never()).save(any(EmployeeEntity.class));
    }

    @Test
    void anAdministratorCreatesAnyRole() {
        EmployeeDto.PostOutputDto created = employeeService.createEmployee(input(EmployeeRole.ROLE_ADMIN), admin.getIdEmployee());

        assertThat(created.getRole()).isEqualTo(EmployeeRole.ROLE_ADMIN);
    }

    @Test
    void nobodyDeletesTheirOwnAccount() {
        assertThatThrownBy(() -> employeeService.deleteEmployee(admin.getIdEmployee(), admin.getIdEmployee()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void aManagerCannotDeleteAnotherManager() {
        assertThatThrownBy(() -> employeeService.deleteEmployee(otherManager.getIdEmployee(), manager.getIdEmployee()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void anEmployeeWhoOrganizesMeetingsCannotBeDeleted() {
        when(bookingRepository.existsByEmployeeOrganizer_IdEmployee(manager.getIdEmployee())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.deleteEmployee(manager.getIdEmployee(), admin.getIdEmployee()))
                .isInstanceOf(RessourceConflictException.class);
        verify(employeeRepository, never()).delete(any(EmployeeEntity.class));
    }

    @Test
    void deletingAnEmployeeRemovesTheirInvitations() {
        employeeService.deleteEmployee(employee.getIdEmployee(), manager.getIdEmployee());

        verify(invitationRepository).deleteByEmployeeAttendee_IdEmployee(employee.getIdEmployee());
        verify(employeeRepository).delete(employee);
    }

    private EmployeeDto.PostInputDto input(EmployeeRole role) {
        return EmployeeDto.PostInputDto.builder()
                .firstName("Nora")
                .lastName("Lambert")
                .email("nora.lambert@test.local")
                .role(role)
                .password("un-mot-de-passe")
                .departmentId(DEPARTMENT_ID)
                .build();
    }

    private static EmployeeEntity employee(Long id, EmployeeRole role) {
        return EmployeeEntity.builder()
                .idEmployee(id)
                .firstName("Prénom" + id)
                .lastName("Nom" + id)
                .email("employee" + id + "@test.local")
                .role(role)
                .build();
    }
}
