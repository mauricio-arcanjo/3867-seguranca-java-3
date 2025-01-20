package br.com.forum_hub.domain.perfil;

import br.com.forum_hub.domain.usuario.Usuario;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HierarquiaService {

    private final RoleHierarchy roleHierarchy;

    public HierarquiaService(RoleHierarchy roleHierarchy) {
        this.roleHierarchy = roleHierarchy;
    }

    public boolean usuarioNaoTemPermissoes(Usuario logado, Usuario autor, String perfilDesejado) {

        //For each da lista de perfis (authorities) do usuario logado, visto que cada usuario pode ter mais de um perfil
        for(GrantedAuthority autoridade : logado.getAuthorities()){
            var autoridadesAlcancaveis = roleHierarchy.getReachableGrantedAuthorities(List.of(autoridade));

            //for each gerado para cada authority do usuario, considerando a hierarquia criada no metodo hierarquia perfis de ConfiguracoesSeguranca,
            //se o role for adm ou moderador, eles tem hierarquia maior que instrutor
            for (GrantedAuthority perfil : autoridadesAlcancaveis){
                if(perfil.getAuthority().equals("ROLE_" + perfilDesejado)
                        || logado.getId().equals(autor.getId())) {

                    return false;
                }
            }
        }
        return true;

        //OUTRA FORMA DE FAZER (metodo  mais simples e precisa apenas do return):
//        return logado.getAuthorities().stream()
//                .flatMap(autoridade -> roleHierarchy.getReachableGrantedAuthorities(List.of(autoridade))
//                        .stream())
//                            .noneMatch(
//                                    perfil -> perfil.getAuthority().equals(perfilDesejado) || logado.getId().equals(autor.getId())
//                            );

    }

}
