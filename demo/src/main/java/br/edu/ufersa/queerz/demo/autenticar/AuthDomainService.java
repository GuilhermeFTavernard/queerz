package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.shared.exception.InvalidCredentialsException;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Regra de autenticação: confere se o usuário existe e se a senha bate, sem revelar qual falhou. */
@Service
public class AuthDomainService {

    private final PasswordEncoder passwordEncoder;

    public AuthDomainService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void validarCredenciais(Usuario usuario, String senhaPura) {
        // Mesma mensagem para "usuário nulo" e "senha errada" evita enumeração de usuários
        if (usuario == null || !passwordEncoder.matches(senhaPura, usuario.getSenha())) {
            throw new InvalidCredentialsException("E-mail ou senha inválidos");
        }
    }
}