package fr.louison.gestionsallespringapi.equipment.controller;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.equipment.service.EquipmentService;
import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.hibernate.engine.jdbc.Size;
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
@RequestMapping("/api/equipments")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<EquipmentDto.PostOutputDto>> getAllEquipments(
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<EquipmentDto.PostOutputDto> equipments = equipmentService.findAll(pageable);
        return ResponseEntity.ok(equipments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<EquipmentDto.PostOutputDto> getEquipmentById(@PathVariable Long id) {
        EquipmentDto.PostOutputDto equipment = equipmentService.findById(id);
        return ResponseEntity.ok(equipment);
    }


    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<EquipmentDto.PostOutputDto> createEquipment(
            @Valid @RequestBody EquipmentDto.PostInputDto inputDto
    ) {
        EquipmentDto.PostOutputDto createdEquipment = equipmentService.createEquipment(inputDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEquipment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
