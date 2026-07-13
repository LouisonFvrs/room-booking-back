package fr.louison.gestionsallespringapi.employee.model;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity(name = "employee")
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmployee;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @ManyToOne
    @JoinColumn(name = "id_department")
    private DepartmentEntity department;
}
