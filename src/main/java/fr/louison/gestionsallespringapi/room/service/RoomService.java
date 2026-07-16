package fr.louison.gestionsallespringapi.room.service;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import fr.louison.gestionsallespringapi.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    public List<RoomDto.PostOutputDto> findAll() {
        List<RoomEntity> rooms = roomRepository.findAll();

        return rooms.stream()
                .map(room -> RoomDto.PostOutputDto.builder()
                        .idRoom(room.getIdRoom())
                        .name(room.getName())
                        .capacity(room.getCapacity())
                        .isAvailable(room.isAvailable())
                        .equipments(room.getEquipments().stream()
                                .map(equipment -> EquipmentDto.SimpleDto.builder()
                                        .idEquipment(equipment.getIdEquipment())
                                        .name(equipment.getName())
                                        .serialNumber(equipment.getSerialNumber())
                                        .build()
                                )
                                .toList()
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<RoomDto.PostOutputDto> findAllByIsAvailableTrue() {
        List<RoomEntity> rooms = roomRepository.findByIsAvailableTrue();

        return rooms.stream()
                .map(room ->  RoomDto.PostOutputDto.builder()
                        .idRoom(room.getIdRoom())
                        .name(room.getName())
                        .capacity(room.getCapacity())
                        .isAvailable(room.isAvailable())
                        .equipments(room.getEquipments().stream()
                                .map(equipment -> EquipmentDto.SimpleDto.builder()
                                        .idEquipment(equipment.getIdEquipment())
                                        .name(equipment.getName())
                                        .serialNumber(equipment.getSerialNumber())
                                        .build()
                                )
                                .toList()
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    public RoomDto.PostOutputDto findByIdRoom(Long idRoom) {

        RoomEntity room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new EntityNotFoundException("La salle n'existe pas"));

        return RoomDto.PostOutputDto.builder()
                .idRoom(room.getIdRoom())
                .name(room.getName())
                .capacity(room.getCapacity())
                .isAvailable(room.isAvailable())
                .equipments(room.getEquipments().stream()
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

    public RoomEntity createRoom(RoomDto.PostInputDto postInputDto) {

        List<EquipmentEntity> equipments = new ArrayList<EquipmentEntity>();

        if (postInputDto.getEquipmentIds() != null && !postInputDto.getEquipmentIds().isEmpty()) {
            equipments = equipmentRepository.findAllById(postInputDto.getEquipmentIds());
        }

        RoomEntity roomEntity = RoomEntity.builder()
                .name(postInputDto.getName()).
                capacity(postInputDto.getCapacity())
                .isAvailable(true)
                .equipments(equipments)
                .build();

        return roomRepository.save(roomEntity);
    }

    public void deleteById(Long id) {
        roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Salle non trouvé !"));

        roomRepository.deleteById(id);
    }

    public void assignEquipmentToRoom(Long idRoom, Long idEquipment) throws Exception {
        EquipmentEntity equipment = equipmentRepository.findById(idEquipment)
                .orElseThrow(() -> new Exception("Equipement non trouvé"));

        RoomEntity room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new Exception("Salle non trouvé"));

        room.addEquipment(equipment);
    }

    public void removeEquipmentFromRoom(Long idRoom, Long idEquipment) throws Exception {
        EquipmentEntity equipment = equipmentRepository.findById(idEquipment)
                .orElseThrow(() -> new Exception("Equipement non trouvé"));

        RoomEntity room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new Exception("Salle non trouvé"));

        room.removeEquipment(equipment);
    }
}
