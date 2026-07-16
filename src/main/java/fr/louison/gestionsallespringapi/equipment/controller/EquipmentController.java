package fr.louison.gestionsallespringapi.equipment.controller;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.equipment.service.EquipmentService;
import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping
    public List<EquipmentDto.PostOutputDto> get() {
        return equipmentService.findAll();
    }

    @GetMapping("/{id}")
    public EquipmentDto.PostOutputDto get(@PathVariable Long id) throws Exception {
        return equipmentService.findById(id);
    }


    @PostMapping("/create")
    public EquipmentDto.PostOutputDto create(@RequestBody EquipmentDto.PostInputDto inputDto) {
        EquipmentEntity equipment = equipmentService.createEquipment(inputDto);
        return EquipmentDto.PostOutputDto.builder()
                .idEquipment(equipment.getIdEquipment())
                .name(equipment.getName())
                .serialNumber(equipment.getSerialNumber())
                .rooms(equipment.getRooms().stream()
                        .map(room -> RoomDto.SimpleDto.builder()
                                .idRoom(room.getIdRoom())
                                .name(room.getName())
                                .capacity(room.getCapacity())
                                .isAvailable(room.isAvailable())
                                .build()
                        )
                        .toList()
                )
                .build();
    }
}
