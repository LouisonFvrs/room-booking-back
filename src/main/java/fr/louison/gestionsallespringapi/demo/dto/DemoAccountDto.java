package fr.louison.gestionsallespringapi.demo.dto;

import fr.louison.gestionsallespringapi.employee.model.enums.EmployeeRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DemoAccountDto", description = "Compte de démonstration proposé sur la page de connexion")
public record DemoAccountDto(EmployeeRole role, String email, String password) {
}
