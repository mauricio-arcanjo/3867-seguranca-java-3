package br.com.forum_hub.infra.seguranca;

import br.com.forum_hub.domain.perfil.PerfilNome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfiguracoesSeguranca {

    private final FiltroTokenAcesso filtroTokenAcesso;

    public ConfiguracoesSeguranca(FiltroTokenAcesso filtroTokenAcesso) {
        this.filtroTokenAcesso = filtroTokenAcesso;
    }

    @Bean
    public SecurityFilterChain filtrosSeguranca(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(req -> {
                    req.requestMatchers(
                            "/login",
                            "/atualizar-token-jwt",
                            "/atualizar-token-opaco",
                            "/registrar",
                            "/verificar-conta")
                            .permitAll();
                    req.requestMatchers(HttpMethod.GET,"/cursos").permitAll();

                    req.requestMatchers(HttpMethod.GET,"/topicos/**").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/topicos").hasRole(PerfilNome.ESTUDANTE.toString());
                    req.requestMatchers(HttpMethod.PUT, "/topicos").hasRole(PerfilNome.ESTUDANTE.toString());
                    req.requestMatchers(HttpMethod.DELETE, "/topicos/**").hasRole(PerfilNome.ESTUDANTE.toString());
                    //Importante ter endpoint mais específico antes do mais genérico
                    req.requestMatchers(HttpMethod.PATCH, "/topicos/{idTopico}/respostas/**").hasAnyRole(PerfilNome.INSTRUTOR.toString(), PerfilNome.ESTUDANTE.toString());
                    req.requestMatchers(HttpMethod.PATCH, "/topicos/**").hasRole(PerfilNome.MODERADOR.toString());

                    req.requestMatchers(HttpMethod.PATCH, "/adicionar-perfil/**").hasRole(PerfilNome.ADMIN.toString());

                    req.requestMatchers(HttpMethod.PATCH, "/ativar/**").hasRole(PerfilNome.ADMIN.toString());

                    req.anyRequest().authenticated();
                })
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(filtroTokenAcesso, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy hierarquiaPerfis(){
        String hierarquia = "ROLE_" + PerfilNome.ADMIN + " > ROLE_" + PerfilNome.MODERADOR + "\n" +
                "ROLE_" + PerfilNome.MODERADOR + " > ROLE_" + PerfilNome.INSTRUTOR + "\n" +
                "ROLE_" + PerfilNome.MODERADOR + " > ROLE_" + PerfilNome.ESTUDANTE + "\n";

        return RoleHierarchyImpl.fromHierarchy(hierarquia);

        //outra implementação
//            return RoleHierarchyImpl.withDefaultRolePrefix()
//                    .role("ADMIN").implies("MODERADOR")
//                    .role("MODERADOR").implies("ESTUDANTE", "INSTRUTOR")
//                    .build();
    }

}
