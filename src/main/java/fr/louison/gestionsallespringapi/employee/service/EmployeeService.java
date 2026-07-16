package fr.louison.gestionsallespringapi.employee.service;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.employee.dto.EmployeeDto;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    public List<EmployeeDto.GetEmployeeDto> findAll() throws Exception {
        List<EmployeeEntity> employees = employeeRepository.findAll();

        if (employees.isEmpty()) {
            throw new Exception("Aucun employé trouvé");
        }

        return employees.stream()
                .map(emp -> EmployeeDto.GetEmployeeDto.builder()
                        .id(emp.getIdEmployee())
                        .firstName(emp.getFirstName())
                        .lastName(emp.getLastName())
                        .email(emp.getEmail())
                        .role(emp.getRole())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public EmployeeDto.GetEmployeeDto findByEmail(String email) throws Exception {
        EmployeeEntity employee = employeeRepository.findByEmail(email);

        if (employee == null) {
            throw new Exception("Aucun employé trouvé");
        }

        return EmployeeDto.GetEmployeeDto.builder()
                .id(employee.getIdEmployee())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .build();
    }

    public EmployeeEntity createEmployee(EmployeeDto.PostInputDto input) {

        DepartmentEntity department = departmentRepository.findById(input.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Erreur création"));

        // TODO: check exceptions and hash password

        EmployeeEntity employee = EmployeeEntity.builder()
                .firstName(input.getFirstName())
                .lastName(input.getLastName())
                .email(input.getEmail())
                .role(input.getRole())
                .password(input.getPassword())
                .department(department)
                .build();

        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employé non trouvé !"));

        employeeRepository.deleteById(id);
    }
}
