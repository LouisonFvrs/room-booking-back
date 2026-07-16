package fr.louison.gestionsallespringapi.equipment.dto;

import fr.louison.gestionsallespringapi.room.dto.RoomDto;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class EquipmentDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleDto {
        private Long idEquipment;
        private String name;
        private String serialNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostInputDto {

        @NotNull
        @NotBlank(message = "Un nom doit être assigner à l'équipement")
        private String name;

        @NotNull
        @NotBlank(message = "Un numéro de série doit être fourni")
        private String serialNumber;

        private List<Long> roomIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostOutputDto {

        private Long idEquipment;
        private String name;
        private String serialNumber;
        private List<RoomDto.SimpleDto> rooms;
    }
}
