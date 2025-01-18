package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosEdicaoUsuario(
        @NotBlank String email,
        @NotBlank String nomeCompleto,
        @NotBlank String nomeUsuario,
        String miniBiografia,
        String biografia

) {
}