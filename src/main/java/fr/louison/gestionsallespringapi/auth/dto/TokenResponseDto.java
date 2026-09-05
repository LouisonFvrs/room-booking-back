package fr.louison.gestionsallespringapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(name = "TokenResponseDto", description = "DTO pour la réponse du token")
public class TokenResponseDto {

    private String token;
    private String tokenType;

    public TokenResponseDto(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
}
