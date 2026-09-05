package fr.louison.gestionsallespringapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "LoginRequestDto", description = "DTO pour la requête de connexion")
public class LoginRequestDto {

    @NotBlank(message = "L'email doit être renseigné")
    @Email(message = "Format invalide")
    private String email;

    @NotBlank(message = "Le mot de passe doit être renseigné")
    private String password;
}
