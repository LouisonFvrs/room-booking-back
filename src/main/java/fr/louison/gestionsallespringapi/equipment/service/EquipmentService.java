package fr.louison.gestionsallespringapi.equipment.service;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<EquipmentDto.PostOutputDto> findAll() {
        List<EquipmentEntity> equipments = equipmentRepository.findAll();

        return equipments.stream()
                .map(equipment -> EquipmentDto.PostOutputDto.builder()
                        .idEquipment(equipment.getIdEquipment())
                        .name(equipment.getName())
                        .serialNumber(equipment.getSerialNumber())
                        .rooms(equipment.getRooms().stream()
                                // On map chaque RoomEntity en RoomDto.SimpleDto
                                .map(room -> RoomDto.SimpleDto.builder()
                                        .idRoom(room.getIdRoom())
                                        .name(room.getName())
                                        .capacity(room.getCapacity())
                                        .isAvailable(room.isAvailable())
                                        .build()
                                )
                                .toList()
                        )
                        .build()
                )
                .toList();
    }

    public EquipmentDto.PostOutputDto findById(Long id) throws Exception {

        EquipmentEntity equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new Exception("Équipement non trouvé"));

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

    public EquipmentEntity createEquipment(EquipmentDto.PostInputDto inputDto) {

        List<RoomEntity> rooms = new ArrayList<RoomEntity>();

        if(inputDto.getRoomIds() != null && !inputDto.getRoomIds().isEmpty()) {
            rooms = roomRepository.findAllById(inputDto.getRoomIds());
        }

        EquipmentEntity equipment = EquipmentEntity.builder()
                .name(inputDto.getName())
                .serialNumber(inputDto.getSerialNumber())
                .rooms(rooms)
                .build();

        return equipmentRepository.save(equipment);
    }

    public ResponseEntity<String> deleteEquipment(Long id) {

        EquipmentEntity equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipement introuvable"));

        List<RoomEntity> rooms = roomRepository.findByEquipmentsContaining(equipment);

        for  (RoomEntity room : rooms) {
            room.getEquipments().remove(equipment);
            roomRepository.save(room);
        }

        equipmentRepository.delete(equipment);

        return ResponseEntity.ok("Équipement supprimé");
    }
}
