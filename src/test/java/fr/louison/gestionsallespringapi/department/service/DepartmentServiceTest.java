package fr.louison.gestionsallespringapi.department.service;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    private static final long DEPARTMENT_ID = 7L;

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private final DepartmentEntity department = DepartmentEntity.builder().idDepartment(DEPARTMENT_ID).name("Produit").build();

    @BeforeEach
    void setUp() {
        when(departmentRepository.findById(DEPARTMENT_ID)).thenReturn(Optional.of(department));
    }

    @Test
    void aDepartmentWithEmployeesCannotBeDeleted() {
        when(employeeRepository.existsByDepartment_IdDepartment(DEPARTMENT_ID)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.deleteDepartment(DEPARTMENT_ID))
                .isInstanceOf(RessourceConflictException.class)
                .hasMessageContaining("Produit");
        verify(departmentRepository, never()).delete(any(DepartmentEntity.class));
    }

    @Test
    void anEmptyDepartmentIsDeleted() {
        when(employeeRepository.existsByDepartment_IdDepartment(DEPARTMENT_ID)).thenReturn(false);

        departmentService.deleteDepartment(DEPARTMENT_ID);

        verify(departmentRepository).delete(department);
    }
}
