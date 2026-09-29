package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.shared.exception.InvalidCredentialsException;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioDomainService;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Regra de autenticação: confere e-mail e senha (BCrypt) sem revelar qual dos dois falhou. */
@Service
public class AuthDomainService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioDomainService usuarioDomainService;
    private final PasswordEncoder passwordEncoder;

    public AuthDomainService(UsuarioRepository usuarioRepository,
                             UsuarioDomainService usuarioDomainService,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioDomainService = usuarioDomainService;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticar(String email, String senhaPura) {
        Usuario usuario = usuarioRepository.findByEmail(usuarioDomainService.normalizarEmail(email)).orElse(null);

        // Mesma mensagem para "e-mail inexistente" e "senha errada" (evita enumeração de usuários)
        if (usuario == null || !passwordEncoder.matches(senhaPura, usuario.getSenha())) {
            throw new InvalidCredentialsException();
        }
        return usuario;
    }
}
