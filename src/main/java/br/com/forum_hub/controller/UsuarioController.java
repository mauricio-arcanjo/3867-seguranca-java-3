package br.com.forum_hub.controller;

import br.com.forum_hub.domain.perfil.DadosPerfil;
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
    public ResponseEntity<DadosListagemUsuario> editarUsuario(@RequestBody @Valid DadosEdicaoUsuario dados,
                                                              @AuthenticationPrincipal Usuario logado){

        var usuario = usuarioService.editarUsuario(dados, logado);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }

    @PatchMapping("/alterar-senha")
    public ResponseEntity<String> alterarSenha(@RequestBody @Valid DadosEdicaoSenha dados,
                                                              @AuthenticationPrincipal Usuario logado){

        usuarioService.alterarSenha(dados, logado);
        return ResponseEntity.ok("Senha alterada com sucesso!");
    }

    @PatchMapping("adicionar-perfil/{id}")
    public ResponseEntity<DadosListagemUsuario> adicionarPerfil(@PathVariable Long id, @RequestBody @Valid DadosPerfil dados){

        var usuario = usuarioService.adicionarPerfil(id, dados);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));

    }

    @PatchMapping("remover-perfil/{id}")
    public ResponseEntity<DadosListagemUsuario> removerPerfil(@PathVariable Long id, @RequestBody @Valid DadosPerfil dados){

        var usuario = usuarioService.removerPerfil(id, dados);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));

    }


    @DeleteMapping("/desativar")
    public ResponseEntity<String> desativar(@AuthenticationPrincipal Usuario logado){

        usuarioService.desativar(logado);

        return ResponseEntity.ok("Usuario desativado com sucesso!");
    }

    @DeleteMapping("/desativar/{id}")
    public ResponseEntity<String> desativarPorId(@PathVariable Long id, @AuthenticationPrincipal Usuario logado){

        usuarioService.desativar(id, logado);

        return ResponseEntity.ok("Usuario desativado com sucesso!");
    }

    @PatchMapping("/reativar-conta/{id}")
    public ResponseEntity<String> ativar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado){

        usuarioService.reativar(id, logado);

        return ResponseEntity.ok("Usuario ativado com sucesso!");
    }



}
