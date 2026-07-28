package fr.louison.gestionsallespringapi.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenResponseDto {

    private String token;
    private String tokenType;

    public TokenResponseDto(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }
}
