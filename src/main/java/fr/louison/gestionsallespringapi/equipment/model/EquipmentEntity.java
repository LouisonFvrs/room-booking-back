package fr.louison.gestionsallespringapi.equipment.model;

import fr.louison.gestionsallespringapi.room.model.RoomEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "equipment")
public class EquipmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipment;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    @Column(unique = true)
    private String serialNumber;

    @ManyToMany(mappedBy = "equipments")
    private List<RoomEntity> rooms = new ArrayList<>();
}
