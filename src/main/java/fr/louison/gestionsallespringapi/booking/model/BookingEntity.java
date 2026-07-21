package fr.louison.gestionsallespringapi.booking.model;

import fr.louison.gestionsallespringapi.booking.model.enums.BookingStatus;
import fr.louison.gestionsallespringapi.employee.model.EmployeeEntity;
import fr.louison.gestionsallespringapi.invitation.model.InvitationEntity;
import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "booking")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBooking;

    private String title;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "id_room")
    private RoomEntity room;

    @ManyToOne
    @JoinColumn(name = "id_organizer")
    private EmployeeEntity employeeOrganizer;

    @ManyToOne
    @JoinColumn(name = "id_recurrence")
    private RecurrenceEntity recurrence;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvitationEntity> invitations = new ArrayList<>();
}
