package br.edu.ufersa.queerz.demo.usuario.mapper;

import br.edu.ufersa.queerz.demo.usuario.UserRole;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioRequest;
import org.springframework.stereotype.Component;

/** Converte UsuarioRequest -> entidade. A saída está em UsuarioResponse.from(...). */
@Component
public class UsuarioMapper {

    /** Recebe a senha JÁ criptografada (BCrypt) pelo Service. Todo cadastro público nasce como USER. */
    public Usuario toEntity(UsuarioRequest request, String senhaCriptografada) {
        return new Usuario(request.nome(), request.email(), senhaCriptografada, UserRole.USER);
    }
}
