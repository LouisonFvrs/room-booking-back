package fr.louison.gestionsallespringapi.department.service;

import fr.louison.gestionsallespringapi.department.dto.DepartmentDto;
import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<DepartmentDto.PostOutputDto> getDepartments() {
        List<DepartmentEntity> departments = departmentRepository.findAll();
        return departments.stream()
                .map(department -> DepartmentDto.PostOutputDto.builder()
                        .id(department.getIdDepartment())
                        .name(department.getName())
                        .build()
                )
                .toList();
    }

    public DepartmentEntity createDepartment(DepartmentDto.PostInputDto departmentDto) {
        DepartmentEntity department =  DepartmentEntity.builder()
                .name(departmentDto.getName())
                .build();
        return departmentRepository.save(department);
    }

    public ResponseEntity<String> deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok("Fonction supprimé avec succès");
    }
}
