package br.edu.ufersa.queerz.demo.usuario.dto;
import br.edu.ufersa.queerz.demo.usuario.UserRole;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A senha NUNCA faz parte da resposta. */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        UserRole role
) {
    private static final Logger log = LoggerFactory.getLogger(UsuarioApplicationService.class);
    public static UsuarioResponse from(Usuario usuario) {
        log.info("Chegou aqui no inferno!");
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getRole());
    }
}
