package fr.louison.gestionsallespringapi.equipment.service;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.exception.RessourceConflictException;
import fr.louison.gestionsallespringapi.exception.RessourceNotFoundException;
import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private RoomRepository roomRepository;

    public Page<EquipmentDto.PostOutputDto> findAll(Pageable pageable) {
        Page<EquipmentEntity> equipmentPage = equipmentRepository.findAll(pageable);

        return equipmentPage.map(this::mapToPostOutputDto);
    }

    public EquipmentDto.PostOutputDto findById(Long id) {
        EquipmentEntity equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Équipement introuvable avec l'ID : " + id));

        return mapToPostOutputDto(equipment);
    }

    public EquipmentDto.PostOutputDto createEquipment(EquipmentDto.PostInputDto inputDto) {

        if (inputDto.getSerialNumber() != null && equipmentRepository.existsBySerialNumber(inputDto.getSerialNumber())) {
            throw new RessourceConflictException("Un équipement avec le numéro de série '" + inputDto.getSerialNumber() + "' existe déjà.");
        }

        List<RoomEntity> rooms = new ArrayList<>();
        if (inputDto.getRoomIds() != null && !inputDto.getRoomIds().isEmpty()) {
            rooms = roomRepository.findAllById(inputDto.getRoomIds());

            if (rooms.size() != inputDto.getRoomIds().size()) {
                throw new RessourceNotFoundException("Une ou plusieurs salles spécifiées sont introuvables.");
            }
        }

        EquipmentEntity equipment = EquipmentEntity.builder()
                .name(inputDto.getName())
                .serialNumber(inputDto.getSerialNumber())
                .rooms(rooms)
                .build();

        EquipmentEntity savedEquipment = equipmentRepository.save(equipment);

        return mapToPostOutputDto(savedEquipment);
    }

    @Transactional
    public void deleteEquipment(Long id) {

        EquipmentEntity equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Équipement introuvable avec l'ID : " + id));

        if (equipment.getRooms() != null) {
            for (RoomEntity room : equipment.getRooms()) {
                room.getEquipments().remove(equipment);
            }
        }
        equipmentRepository.delete(equipment);
    }

    // Méthode helper privée réutilisable
    private EquipmentDto.PostOutputDto mapToPostOutputDto(EquipmentEntity equipment) {
        return EquipmentDto.PostOutputDto.builder()
                .idEquipment(equipment.getIdEquipment())
                .name(equipment.getName())
                .serialNumber(equipment.getSerialNumber())
                .rooms(equipment.getRooms() != null ? equipment.getRooms().stream()
                                                      .map(room -> RoomDto.SimpleDto.builder()
                                                                   .idRoom(room.getIdRoom())
                                                                   .name(room.getName())
                                                                   .capacity(room.getCapacity())
                                                                   .isAvailable(room.isAvailable())
                                                                   .build())
                                                      .toList() : Collections.emptyList())
                .build();
    }
}
