package fr.louison.gestionsallespringapi.booking.dto;

import fr.louison.gestionsallespringapi.booking.model.enums.BookingStatus;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.invitation.model.enums.InvitationStatus;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class BookingDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PostInputDto {

        @NotNull
        @NotBlank(message = "Le titre est obligatoire")
        private String title;

        private String description;

        @NotNull(message = "La date de début est obligatoire")
        @FutureOrPresent(message = "La réservation ne peut pas débuter dans le passé")
        private LocalDateTime startDate;

        @NotNull(message = "La date de fin est obligatoire")
        private LocalDateTime endDate;

        @NotNull(message = "La salle est obligatoire")
        private Long idRoom;

        @NotNull(message = "L'organisateur est obligatoire")
        private Long idOrganizer;

        private Long idRecurrence;

        // Représente la liste des ids des utilisateurs que l'on souhaite invité, on crée plus tard l'invitation
        private List<Long> invitationIds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PostOutputDto {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private RoomSummaryDto room;
        private BookingStatus status;
        private EmployeeSummaryDto organizer;
        private RecurrenceEntity recurrence;
        private List<InvitationSummaryDto> invitations;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoomSummaryDto { private Long id; private String name; }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmployeeSummaryDto { private Long id; private String firstName; private String lastName; }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InvitationSummaryDto { private Long id; private InvitationStatus status; private Long attendeeId; }
}
