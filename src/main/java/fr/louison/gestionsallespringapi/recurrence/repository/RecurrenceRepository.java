package fr.louison.gestionsallespringapi.recurrence.repository;

import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurrenceRepository extends JpaRepository<EquipmentEntity, Long> {
}
