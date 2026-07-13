package fr.louison.gestionsallespringapi.equipment.repository;

import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Long> {
}
