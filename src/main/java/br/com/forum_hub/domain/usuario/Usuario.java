package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private boolean ativo;

    //Como cada usuario pode ter mais de um perfil é necessário criar uma lista
    @ManyToMany (fetch = FetchType.EAGER) // Carregar os dados do perfil sempre que usuario for buscado no banco de dados
    //Cardinalidade many to many precisa da tabela intermediária. Cada usuario pode ter varios perfis e cada perfil pode ter vários usuarios

    //IMPORTANTE: NOTE QUE NÃO É NECESSÁRIO FAZER NENHUMA ANOTAÇÃO NA TABELA PERFIL !!!!!!!!!!!

    @JoinTable(
            name = "usuarios_perfis",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private List<Perfil> perfis = new ArrayList<>();

    /*
        Atributos para implementacao do refresh token opaco (persiste no DB)
     */
    private String refreshToken;
    private LocalDateTime expiracaoRefreshToken;

    public Usuario() {
    }

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada, Perfil perfil) {
        this.nomeCompleto = dados.nomeCompleto();
        this.email = dados.email();
        this.senha = senhaCriptografada;
        this.nomeUsuario = dados.nomeUsuario();
        this.biografia = dados.biografia();
        this.miniBiografia = dados.miniBiografia();
        this.verificado = false;
        this.token = UUID.randomUUID().toString();
        this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
        this.ativo = true;
        this.perfis.add(perfil);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return perfis;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
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

    public String getSenha() {
        return senha;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senhaCriptografada) {
        this.senha = senhaCriptografada;
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

    public void deletarUsuario(){
        this.ativo = false;
        this.token = null;
        this.expiracaoToken = null;
        this.verificado = false;
        this.refreshToken = null;
        this.expiracaoRefreshToken = null;
    }

    public Usuario alterarDados(DadosEdicaoUsuario dados) {
        if(dados.nomeCompleto() != null){
            this.nomeCompleto = dados.nomeCompleto();
        }
        if(dados.miniBiografia() != null){
            this.miniBiografia = dados.miniBiografia();
        }
        if(dados.biografia() != null){
            this.biografia = dados.biografia();
        }
        return this;
    }

    public void adicionarPerfil(Perfil perfil) {
        if (!perfis.contains(perfil)) {
            this.perfis.add(perfil);
        } else {
            throw new RegraDeNegocioException("Usuário já possui esse perfil adicionado!");
        }
    }
}