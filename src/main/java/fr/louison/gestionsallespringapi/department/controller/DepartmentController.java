package fr.louison.gestionsallespringapi.department.controller;

import fr.louison.gestionsallespringapi.department.dto.DepartmentDto;
import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public List<DepartmentDto.PostOutputDto> get() {
        return departmentService.getDepartments();
    }

    @PostMapping("/create")
    public DepartmentDto.PostOutputDto create(@Valid @RequestBody DepartmentDto.PostInputDto departmentDto) {
        DepartmentEntity department = departmentService.createDepartment(departmentDto);

        return DepartmentDto.PostOutputDto.builder()
                .id(department.getIdDepartment())
                .name(department.getName())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        return departmentService.deleteDepartment(id);
    }
}
