package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosEdicaoSenha(
        @NotBlank String senhaAtual,
        @NotBlank String novaSenha,
        @NotBlank String novaSenhaConfirmacao
) {
}