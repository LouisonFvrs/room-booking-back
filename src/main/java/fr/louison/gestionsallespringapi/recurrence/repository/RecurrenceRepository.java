package fr.louison.gestionsallespringapi.recurrence.repository;

import fr.louison.gestionsallespringapi.recurrence.model.RecurrenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurrenceRepository extends JpaRepository<RecurrenceEntity, Long> {
}
