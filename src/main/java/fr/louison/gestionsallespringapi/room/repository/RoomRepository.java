package fr.louison.gestionsallespringapi.room.repository;

import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    public Page<RoomEntity> findByIsAvailableTrue(Pageable pageable);

    public List<RoomEntity> findByEquipmentsContaining(EquipmentEntity equipmentEntity);

    boolean existsByName(@NotBlank(message = "Le nom de la salle ne peut pas être vide") String name);
}
