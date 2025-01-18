package br.com.forum_hub.domain.usuario;

import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(username) //Username nesse caso se refere ao email do usuario, é usado esse nome devido a classe UserDetails
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));
    }

    @Transactional
    public Usuario cadastrar(DadosCadastroUsuario dados) {

       var optionalUsuario = usuarioRepository.findByEmailIgnoreCaseOrNomeUsuarioIgnoreCase(dados.email(), dados.nomeUsuario());

        if(optionalUsuario.isPresent()){
            throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse email ou nome de usuário!");
        }

        if (!dados.senha().equals(dados.confirmacaoSenha())) {
            throw new RegraDeNegocioException("Senha não bate com a confirmação!");
        }

        var senhaCriptografada = passwordEncoder.encode(dados.senha());

        var usuario = new Usuario(dados, senhaCriptografada);

        emailService.enviarEmailVerificacao(usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void verificarEmail(String codigo) {
        var usuario = usuarioRepository.findByToken(codigo).orElseThrow();
        usuario.verificar();
    }

    public Usuario obterUsuario(String nomeUsuario) {
        return usuarioRepository.findByNomeUsuarioIgnoreCaseAndVerificadoTrueAndAtivoTrue(nomeUsuario)
                .orElseThrow(
                        () -> new RegraDeNegocioException("Usuário não encontrado!")
                );
    }

    @Transactional
    public Usuario editarUsuario(DadosEdicaoUsuario dados, Usuario usuario) {

        if (!usuario.getEmail().equalsIgnoreCase(dados.email())){
            var optionalUsuario = usuarioRepository.findByEmailIgnoreCase(dados.email());

            if(optionalUsuario.isPresent()){
                throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse email!");
            }

            usuario.alterarEmail(dados.email());
            emailService.enviarEmailVerificacao(usuario);
        }
        if (!usuario.getNomeUsuario().equalsIgnoreCase(dados.nomeUsuario())){
            var optionalUsuario = usuarioRepository.findByNomeUsuarioIgnoreCase(dados.nomeUsuario());

            if(optionalUsuario.isPresent()){
                throw new RegraDeNegocioException("Já existe uma conta cadastrada com esse nome de usuário!");
            }

            usuario.setNomeUsuario(dados.nomeUsuario());
        }

        return usuario.alterarDados(dados);
    }

    @Transactional
    public void alterarSenha(DadosEdicaoSenha dados, Usuario logado) {

        if(!passwordEncoder.matches(dados.senhaAtual(), logado.getPassword())){
            throw new RegraDeNegocioException("Senha digitada não confere com senha atual!");
        }

        if (!dados.novaSenha().equals(dados.novaSenhaConfirmacao())) {
            throw new RegraDeNegocioException("Senha não bate com a confirmação!");
        }

        if (passwordEncoder.matches(dados.novaSenha(), logado.getSenha())) {
            throw new RegraDeNegocioException("Senha nova e atual precisam ser diferentes!");
        }

        var senhaCriptografada = passwordEncoder.encode(dados.novaSenha());
        logado.setSenha(senhaCriptografada);
    }

    @Transactional
    public void desativar(Usuario logado) {
        logado.deletarUsuario();
    }
}