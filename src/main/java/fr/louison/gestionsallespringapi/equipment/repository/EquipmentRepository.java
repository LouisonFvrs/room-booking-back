package fr.louison.gestionsallespringapi.equipment.repository;

import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Long> {

}
