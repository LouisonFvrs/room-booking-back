package fr.louison.gestionsallespringapi.room.controller;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.service.RoomService;
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
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<RoomDto.PostOutputDto>> getAllRooms(
            @PageableDefault(size = 20, page = 0, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<RoomDto.PostOutputDto> pages = roomService.findAll(pageable);
        return ResponseEntity.ok(pages);
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<RoomDto.PostOutputDto>> getAvailableRooms(
            @PageableDefault(size = 20, page = 0, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<RoomDto.PostOutputDto> rooms = roomService.findAllByIsAvailableTrue(pageable);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<RoomDto.PostOutputDto> getRoomById(@PathVariable Long id) {
        RoomDto.PostOutputDto room = roomService.findByIdRoom(id);
        return ResponseEntity.ok(room);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoomDto.PostOutputDto> createRoom(
            @Valid @RequestBody RoomDto.PostInputDto postInputDto
    ) {
        RoomDto.PostOutputDto createdRoom = roomService.createRoom(postInputDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{idRoom}/equipments/{idEquipment}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoomDto.PostOutputDto> assignEquipment(
            @PathVariable Long idRoom,
            @PathVariable Long idEquipment
    ) {
        RoomDto.PostOutputDto updatedRoom = roomService.assignEquipmentToRoom(idRoom, idEquipment);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/{idRoom}/equipments/{idEquipment}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoomDto.PostOutputDto> removeEquipmentFromRoom(
            @PathVariable Long idRoom,
            @PathVariable Long idEquipment
    ) {
        RoomDto.PostOutputDto updatedRoom = roomService.removeEquipmentFromRoom(idRoom, idEquipment);
        return ResponseEntity.ok(updatedRoom);
    }
}
