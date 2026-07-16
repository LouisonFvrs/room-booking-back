package fr.louison.gestionsallespringapi.employee.model;

import fr.louison.gestionsallespringapi.department.model.DepartmentEntity;
import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity(name = "employee")
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmployee;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @NotNull(message = "Le département est obligatoire")
    @ManyToOne
    @JoinColumn(name = "id_department")
    private DepartmentEntity department;
}
