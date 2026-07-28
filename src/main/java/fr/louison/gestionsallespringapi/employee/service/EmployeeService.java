package fr.louison.gestionsallespringapi.employee.service;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.dto.EmployeeDto;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import fr.louison.gestionsallespringapi.exception.RessourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.PasswordAuthentication;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<EmployeeDto.GetEmployeeDto> findAll(Pageable pageable) {
        Page<EmployeeEntity> employeePage = employeeRepository.findAll(pageable);

        return employeePage.map(this::mapToGetEmployeeDto);
    }

    public EmployeeDto.GetEmployeeDto findByEmail(String email) {
        EmployeeEntity employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Employé introuvable avec l'email : " + email));

        return mapToGetEmployeeDto(employee);
    }

    public EmployeeDto.PostOutputDto createEmployee(EmployeeDto.PostInputDto input) {

        if (employeeRepository.existsByEmail(input.getEmail())) {
            throw new RessourceConflictException("Un employé avec l'email '" + input.getEmail() + "' existe déjà.");
        }

        DepartmentEntity department = departmentRepository.findById(input.getDepartmentId())
                .orElseThrow(() -> new RessourceNotFoundException("Département introuvable avec l'ID : " + input.getDepartmentId()));

        EmployeeEntity employee = EmployeeEntity.builder()
                .firstName(input.getFirstName())
                .lastName(input.getLastName())
                .email(input.getEmail())
                .role(input.getRole())
                .password(passwordEncoder.encode(input.getPassword()))
                .department(department)
                .build();

        EmployeeEntity savedEmployee = employeeRepository.save(employee);

        return mapToPostOutputDto(savedEmployee);
    }



    public void deleteEmployee(Long id) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Employé non trouvé !"));

        employeeRepository.delete(employee);
    }

    // Méthode helper réutilisable dans tout le service
    private EmployeeDto.GetEmployeeDto mapToGetEmployeeDto(EmployeeEntity emp) {
        return EmployeeDto.GetEmployeeDto.builder()
                .id(emp.getIdEmployee())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .email(emp.getEmail())
                .role(emp.getRole())
                .build();
    }

    // Méthode helper de mapping dans le service
    private EmployeeDto.PostOutputDto mapToPostOutputDto(EmployeeEntity employee) {
        return EmployeeDto.PostOutputDto.builder()
                .id(employee.getIdEmployee())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .build();
    }
}
