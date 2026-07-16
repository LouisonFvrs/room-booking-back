package fr.louison.gestionsallespringapi.room.dto;

import fr.louison.gestionsallespringapi.equipment.dto.EquipmentDto;
import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class RoomDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SimpleDto {
        private Long idRoom;
        private String name;
        private int capacity;
        private boolean isAvailable;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PostOutputDto {
        private Long idRoom;
        private String name;
        private int capacity;
        private boolean isAvailable;
        private List<EquipmentDto.SimpleDto> equipments;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PostInputDto {

        @NotBlank(message = "Le nom de la salle ne peut pas être vide")
        private String name;

        @Min(value = 5, message = "La capacité minimale est de 5 personnes")
        private int capacity;

        private List<Long> equipmentIds;

    }
}
