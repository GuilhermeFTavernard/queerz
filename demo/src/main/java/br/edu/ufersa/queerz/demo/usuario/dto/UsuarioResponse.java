package br.edu.ufersa.queerz.demo.usuario.dto;
import br.edu.ufersa.queerz.demo.usuario.UserRole;
import br.edu.ufersa.queerz.demo.usuario.Usuario;


/** A senha NUNCA faz parte da resposta. */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        UserRole role
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole());
    }
}
