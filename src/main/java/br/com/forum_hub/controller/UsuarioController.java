package br.com.forum_hub.controller;

import br.com.forum_hub.domain.usuario.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DadosListagemUsuario> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados,
                                                          UriComponentsBuilder uriBuilder){

        var usuario = usuarioService.cadastrar(dados);
        var uri = uriBuilder.path("/{nomeUsuario}").buildAndExpand(usuario.getNomeUsuario()).toUri();

        return ResponseEntity.created(uri).body(new DadosListagemUsuario(usuario));

    }

    @GetMapping("/verificar-conta")
    public ResponseEntity<String> verificarEmail (@RequestParam String codigo){
        usuarioService.verificarEmail(codigo);
        return ResponseEntity.ok("Conta verificada com sucesso!");
    }

    @GetMapping("/{nomeUsuario}")
    public ResponseEntity<DadosListagemUsuario> obterUsuario(@PathVariable String nomeUsuario){

        var usuario = usuarioService.obterUsuario(nomeUsuario);

        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }

    @PutMapping("/editar-perfil")
    public ResponseEntity<DadosListagemUsuario> editarUsuario(@RequestBody DadosEdicaoUsuario dados,
                                                              @AuthenticationPrincipal Usuario autor){

        var usuario = usuarioService.editarUsuario(dados, autor);

        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }
}
