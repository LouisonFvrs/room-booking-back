package fr.louison.gestionsallespringapi.department.service;

import fr.louison.gestionsallespringapi.department.dto.DepartmentDto;
import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.department.repository.DepartmentRepository;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import fr.louison.gestionsallespringapi.exception.RessourceNotFoundException;
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
                .map(this::mapToPostOutputDto)
                .toList();
    }

    public DepartmentDto.PostOutputDto createDepartment(DepartmentDto.PostInputDto departmentDto) {

        if (departmentRepository.existsByName(departmentDto.getName())) {
            throw new RessourceConflictException("Un département nommé '" + departmentDto.getName() + "' existe déjà.");
        }

        DepartmentEntity department = DepartmentEntity.builder()
                .name(departmentDto.getName())
                .build();

        DepartmentEntity savedDepartment = departmentRepository.save(department);

        return mapToPostOutputDto(savedDepartment);
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RessourceNotFoundException("Département introuvable avec l'ID : " + id);
        }

        departmentRepository.deleteById(id);
    }


    // Méthode helper privée pour garder le code lisible
    private DepartmentDto.PostOutputDto mapToPostOutputDto(DepartmentEntity department) {
        return DepartmentDto.PostOutputDto.builder()
                .id(department.getIdDepartment())
                .name(department.getName())
                .build();
    }
}
