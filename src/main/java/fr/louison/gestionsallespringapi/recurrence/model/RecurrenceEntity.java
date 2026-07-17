package fr.louison.gestionsallespringapi.recurrence.model;

import fr.louison.gestionsallespringapi.recurrence.model.enums.RecurrenceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
