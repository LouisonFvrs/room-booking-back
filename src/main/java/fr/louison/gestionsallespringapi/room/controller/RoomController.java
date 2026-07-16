package fr.louison.gestionsallespringapi.room.controller;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public List<RoomDto.PostOutputDto> get() {
        return roomService.findAll();
    }

    @GetMapping("/available")
    public List<RoomDto.PostOutputDto> getAvailable() {
        return roomService.findAllByIsAvailableTrue();
    }

    @GetMapping("/{id}")
    public RoomDto.PostOutputDto get(@PathVariable Long id) {
        return roomService.findByIdRoom(id);
    }

    @PostMapping("/create")
    public RoomDto.PostOutputDto create(@Valid @RequestBody RoomDto.PostInputDto postInputDto) {
        RoomEntity roomEntity = roomService.createRoom(postInputDto);

        return RoomDto.PostOutputDto.builder()
                .idRoom(roomEntity.getIdRoom())
                .name(roomEntity.getName())
                .capacity(roomEntity.getCapacity())
                .isAvailable(roomEntity.isAvailable())
                .equipments(roomEntity.getEquipments().stream()
                        .map(equipment -> EquipmentDto.SimpleDto.builder()
                                .idEquipment(equipment.getIdEquipment())
                                .name(equipment.getName())
                                .serialNumber(equipment.getSerialNumber())
                                .build()
                        )
                        .toList()
                )
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        roomService.deleteById(id);
        return ResponseEntity.ok("La salle à bien été supprimé");
    }

    @PutMapping("/{idRoom}/equipments/{idEquipment}")
    public ResponseEntity<String> assignEquipment(@PathVariable Long idRoom, @PathVariable Long idEquipment) throws Exception {
        roomService.assignEquipmentToRoom(idRoom, idEquipment);
        return ResponseEntity.ok("Équipement ajouté");
    }

    @DeleteMapping("/{idRoom}/equipments/{idEquipment}")
    public ResponseEntity<String> removeEquipmentFromRoom(@PathVariable Long idRoom, @PathVariable Long idEquipment) throws Exception {
        roomService.removeEquipmentFromRoom(idRoom, idEquipment);
        return ResponseEntity.ok("Équipement retiré");
    }
}
