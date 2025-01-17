package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosLogin;
import br.com.forum_hub.domain.autenticacao.DadosRefreshToken;
import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.TokenService;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public AutenticacaoController(AuthenticationManager authenticationManager, TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }
    @PostMapping("/login")
    public ResponseEntity<DadosToken> efetuarLogin(@Valid @RequestBody DadosLogin dados){
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        var authentication = authenticationManager.authenticate(authenticationToken);

        var usuario = (Usuario) authentication.getPrincipal();
        String tokenAcesso = tokenService.gerarToken(usuario);

        //Geração de token jwt
//        String refreshToken = tokenService.gerarRefreshToken(usuario);

        //Geração de token opaco
        String refreshToken = usuario.novoRefreshToken();
        //Esse método é necessario apenas se houver o uso do token opaco que precisa ser salvo no banco de dados
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken));
    }

    //Método para JWT TOKEN
    @PostMapping("/atualizar-token-jwt")
    public ResponseEntity<DadosToken> atualizarTokenJwt(@Valid @RequestBody DadosRefreshToken dados){
        var refreshToken = dados.refreshToken();
        Long idUsuario = Long.valueOf(tokenService.verificarToken(refreshToken)); //Mesmo método de verificação do token principal pode ser usado nesse aqui. A diferença é que o subject será id e não userName
        var usuario = usuarioRepository.getReferenceById(idUsuario);

        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshTokenAtualizado = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshTokenAtualizado));
    }

    //Método para Token Opaco
    @PostMapping("/atualizar-token-opaco")
    public ResponseEntity<DadosToken> atualizarTokenOpaco(@Valid @RequestBody DadosRefreshToken dados){
        var refreshToken = dados.refreshToken();
        var usuario = usuarioRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RegraDeNegocioException("Refresh token inválido!"));

        if(usuario.refreshTokenExpirado())
            throw new RegraDeNegocioException("Refresh token expirado!");

        String tokenAcesso = tokenService.gerarToken(usuario);
        String novoRefreshToken = usuario.novoRefreshToken();

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, novoRefreshToken));
    }
}
