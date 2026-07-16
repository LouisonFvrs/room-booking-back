package fr.louison.gestionsallespringapi.employee.controller;
import fr.louison.gestionsallespringapi.employee.dto.EmployeeDto;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.employee.repository.EmployeeRepository;
import fr.louison.gestionsallespringapi.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDto.GetEmployeeDto> get() throws Exception {
        return employeeService.findAll();
    }

    @GetMapping("/{email}")
    public EmployeeDto.GetEmployeeDto get(@PathVariable String email) throws Exception {
        return employeeService.findByEmail(email);
    }

    @PostMapping("/create")
    public EmployeeDto.PostOutputDto post(@Valid @RequestBody EmployeeDto.PostInputDto postInputDto) throws Exception {
        EmployeeEntity employeeEntity = employeeService.createEmployee(postInputDto);

        return EmployeeDto.PostOutputDto.builder()
                .id(employeeEntity.getIdEmployee())
                .firstName(employeeEntity.getFirstName())
                .lastName(employeeEntity.getLastName())
                .email(employeeEntity.getEmail())
                .role(employeeEntity.getRole())
                .department(employeeEntity.getDepartment())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) throws Exception {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("L'employé à été supprimé avec succès");
    }
}
