package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCaseAndVerificadoTrue(String username); //Username nesse caso se refere ao email do usuario, é usado esse nome devido a classe UserDetails
    Optional<Usuario> findByRefreshToken(String refreshToken);
    Optional<Usuario> findByEmailIgnoreCaseOrNomeUsuarioIgnoreCase(String email, String nomeUsuario);
    Optional<Usuario> findByToken(String codigo);
    Optional<Usuario> findByNomeUsuarioIgnoreCase(String nomeUsuario);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    Optional<Usuario> findByNomeUsuarioIgnoreCaseAndVerificadoTrueAndAtivoTrue(String nomeUsuario);
}
