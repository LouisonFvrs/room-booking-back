package fr.louison.gestionsallespringapi.room.service;

import fr.louison.gestionsallespringapi.booking.repository.BookingRepository;
import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.equipment.repository.EquipmentRepository;
import fr.louison.gestionsallespringapi.exception.BadRequestException;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Page<RoomDto.PostOutputDto> findAll(Pageable pageable) {
        Page<RoomEntity> rooms = roomRepository.findAll(pageable);

        return rooms.map(this::mapToPostOutputDto);
    }

    public Page<RoomDto.PostOutputDto> findAllByIsAvailableTrue(Pageable pageable) {
        Page<RoomEntity> availableRooms = roomRepository.findByIsAvailableTrue(pageable);

        return availableRooms.map(this::mapToPostOutputDto);
    }

    public RoomDto.PostOutputDto findByIdRoom(Long idRoom) {
        RoomEntity room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new RessourceNotFoundException("La salle est introuvable avec l'ID : " + idRoom));

        return mapToPostOutputDto(room);
    }

    public RoomDto.PostOutputDto createRoom(RoomDto.PostInputDto postInputDto) {

        if (roomRepository.existsByName(postInputDto.getName())) {
            throw new RessourceConflictException("Une salle nommée '" + postInputDto.getName() + "' existe déjà.");
        }

        List<EquipmentEntity> equipments = new ArrayList<>();
        if (postInputDto.getEquipmentIds() != null && !postInputDto.getEquipmentIds().isEmpty()) {
            equipments = equipmentRepository.findAllById(postInputDto.getEquipmentIds());

            if (equipments.size() != postInputDto.getEquipmentIds().size()) {
                throw new RessourceNotFoundException("Un ou plusieurs équipements spécifiés sont introuvables.");
            }
        }

        RoomEntity roomEntity = RoomEntity.builder()
                .name(postInputDto.getName())
                .capacity(postInputDto.getCapacity())
                .isAvailable(true)
                .equipments(equipments)
                .build();

        RoomEntity savedRoom = roomRepository.save(roomEntity);

        return mapToPostOutputDto(savedRoom);
    }

    public void deleteById(Long id) {
        RoomEntity room = roomRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("La salle est introuvable avec l'ID : " + id));

        // Bookings, cancelled ones included, keep pointing at their room as history.
        if (bookingRepository.existsByRoom_IdRoom(id)) {
            throw new RessourceConflictException(
                    "La salle " + room.getName() + " a des réservations et ne peut pas être supprimée."
            );
        }

        roomRepository.delete(room);
    }

    @Transactional
    public RoomDto.PostOutputDto assignEquipmentToRoom(Long idRoom, Long idEquipment) {

        RoomEntity room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new RessourceNotFoundException("La salle est introuvable avec l'ID : " + idRoom));

        EquipmentEntity equipment = equipmentRepository.findById(idEquipment)
                .orElseThrow(() -> new RessourceNotFoundException("L'équipement est introuvable avec l'ID : " + idEquipment));

        if (room.getEquipments().contains(equipment)) {
            throw new RessourceConflictException("L'équipement '" + equipment.getName() + "' est déjà associé à cette salle.");
        }

        room.getEquipments().add(equipment);
        RoomEntity updatedRoom = roomRepository.save(room);

        return mapToPostOutputDto(updatedRoom);
    }

    @Transactional
    public RoomDto.PostOutputDto removeEquipmentFromRoom(Long idRoom, Long idEquipment) {

        RoomEntity room = roomRepository.findById(idRoom)
                .orElseThrow(() -> new RessourceNotFoundException("La salle est introuvable avec l'ID : " + idRoom));

        EquipmentEntity equipment = equipmentRepository.findById(idEquipment)
                .orElseThrow(() -> new RessourceNotFoundException("L'équipement est introuvable avec l'ID : " + idEquipment));

        if (!room.getEquipments().contains(equipment)) {
            throw new BadRequestException("L'équipement '" + equipment.getName() + "' n'est pas associé à cette salle.");
        }

        room.getEquipments().remove(equipment);
        RoomEntity updatedRoom = roomRepository.save(room);

        return mapToPostOutputDto(updatedRoom);
    }

    // Méthode helper privée réutilisable dans tout le service
    private RoomDto.PostOutputDto mapToPostOutputDto(RoomEntity room) {
        List<EquipmentDto.SimpleDto> equipmentsDto = room.getEquipments() != null
                ? room.getEquipments().stream()
                  .map(equipment -> EquipmentDto.SimpleDto.builder()
                                    .idEquipment(equipment.getIdEquipment())
                                    .name(equipment.getName())
                                    .serialNumber(equipment.getSerialNumber())
                                    .build())
                  .toList()
                : Collections.emptyList();

        return RoomDto.PostOutputDto.builder()
                .idRoom(room.getIdRoom())
                .name(room.getName())
                .capacity(room.getCapacity())
                .isAvailable(room.isAvailable())
                .equipments(equipmentsDto)
                .build();
    }
}
