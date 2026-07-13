package fr.louison.gestionsallespringapi.recurrence.model;

import fr.louison.gestionsallespringapi.recurrence.model.enums.RecurrenceType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Entity(name = "recurrence")
public class RecurrenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecurrence;

    @Enumerated(EnumType.STRING)
    private RecurrenceType type;

    private int intervalValue;

    private LocalDate endDate;
}
