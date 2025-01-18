package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name="usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeCompleto;
    private String email;
    private String senha;
    private String nomeUsuario;
    private String biografia;
    private String miniBiografia;
    private Boolean verificado;
    private String token;
    private LocalDateTime expiracaoToken;
    /*
        Atributos para implementacao do refresh token opaco (persiste no DB)
     */
    private String refreshToken;
    private LocalDateTime expiracaoRefreshToken;

    public Usuario() {
    }

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada) {
        this.nomeCompleto = dados.nomeCompleto();
        this.email = dados.email();
        this.senha = senhaCriptografada;
        this.nomeUsuario = dados.nomeUsuario();
        this.biografia = dados.biografia();
        this.miniBiografia = dados.miniBiografia();
        this.verificado = false;
        this.token = UUID.randomUUID().toString();
        this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getNome() {
        return nomeCompleto;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getBiografia() {
        return biografia;
    }

    public String getMiniBiografia() {
        return miniBiografia;
    }

    public Long getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public boolean refreshTokenExpirado() {
        return expiracaoRefreshToken.isBefore(LocalDateTime.now());
    }

    public String novoRefreshToken() {
        this.refreshToken = UUID.randomUUID().toString();
        this.expiracaoRefreshToken = LocalDateTime.now().plusMinutes(120);
        return refreshToken;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }
    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setMiniBiografia(String miniBiografia) {
        this.miniBiografia = miniBiografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public void verificar() {
        if(expiracaoToken.isBefore(LocalDateTime.now())){
            throw new RegraDeNegocioException("Link de verificação expirado!");
        }
        this.verificado = true;
        this.token = null;
        this.expiracaoToken = null;
    }

    public void alterarEmail(String email) {
        this.email = email;
        this.verificado = false;
        this.token = UUID.randomUUID().toString();
        this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
    }
}