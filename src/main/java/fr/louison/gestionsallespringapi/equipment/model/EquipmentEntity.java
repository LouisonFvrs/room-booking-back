package fr.louison.gestionsallespringapi.equipment.model;

import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity(name = "equipment")
public class EquipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipment;

    private String name;

    private String serialNumber;

    @ManyToMany(mappedBy = "equipments")
    private List<RoomEntity> rooms = new ArrayList<>();
}
