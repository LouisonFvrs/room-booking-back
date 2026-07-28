package fr.louison.gestionsallespringapi.equipment.repository;

import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Long> {

    boolean existsBySerialNumber(@NotNull @NotBlank(message = "Un numéro de série doit être fourni") String serialNumber);
}
