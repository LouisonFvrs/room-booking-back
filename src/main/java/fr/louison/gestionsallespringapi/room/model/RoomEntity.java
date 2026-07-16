package fr.louison.gestionsallespringapi.room.model;

import fr.louison.gestionsallespringapi.equipment.model.EquipmentEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "room")
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRoom;

    private String name;

    private int capacity;

    private boolean isAvailable;

    @ManyToMany
    @JoinTable(
            name = "room_equipment",
            joinColumns = @JoinColumn(name = "id_room"),
            inverseJoinColumns = @JoinColumn(name = "id_equipment")
    )
    private List<EquipmentEntity> equipments = new ArrayList<>();

    public void addEquipment(EquipmentEntity equipment) {
        equipments.add(equipment);
        equipment.getRooms().add(this);
    }

    public void removeEquipment(EquipmentEntity equipment) {
        equipments.remove(equipment);
        equipment.getRooms().remove(this);
    }
}
