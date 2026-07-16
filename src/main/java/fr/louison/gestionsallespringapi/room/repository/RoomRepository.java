package fr.louison.gestionsallespringapi.room.repository;

import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    public List<RoomEntity> findByIsAvailableTrue();
}
