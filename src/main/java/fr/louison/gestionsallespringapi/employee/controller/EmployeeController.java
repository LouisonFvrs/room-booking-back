package fr.louison.gestionsallespringapi.employee.controller;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import fr.louison.gestionsallespringapi.auth.CustomUserDetails;
import fr.louison.gestionsallespringapi.employee.dto.EmployeeDto;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<EmployeeDto.GetEmployeeDto>> getAllEmployees(
            @PageableDefault(page = 0, size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<EmployeeDto.GetEmployeeDto> employees = employeeService.findAll(pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<EmployeeDto.GetEmployeeDto> getEmployeeByEmail(@PathVariable String email) {
        EmployeeDto.GetEmployeeDto employee = employeeService.findByEmail(email);
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<EmployeeDto.PostOutputDto> createEmployee(
            @Valid @RequestBody EmployeeDto.PostInputDto postInputDto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        EmployeeDto.PostOutputDto createdEmployee =
                employeeService.createEmployee(postInputDto, currentUser.getEmployee().getIdEmployee());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        employeeService.deleteEmployee(id, currentUser.getEmployee().getIdEmployee());
        return ResponseEntity.noContent().build();
    }
}
