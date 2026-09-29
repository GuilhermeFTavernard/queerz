package br.edu.ufersa.queerz.demo.usuario;

import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.DuplicateResourceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Regras de negócio de Usuário que não cabem na entidade:
 * unicidade do e-mail (chave de negócio), criptografia e regras de troca de senha.
 */
@Service
public class UsuarioDomainService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDomainService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** E-mail é chave de negócio: comparamos sempre aparado e em minúsculas. */
    public String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Cadastro: o e-mail (normalizado) não pode existir. */
    public void garantirEmailDisponivel(String emailNormalizado) {
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new DuplicateResourceException("O e-mail '%s' já está cadastrado".formatted(emailNormalizado));
        }
    }

    /** Edição: o e-mail pode continuar sendo do próprio usuário, mas não de outro. */
    public void garantirEmailDisponivel(String emailNormalizado, Long usuarioId) {
        if (usuarioRepository.existsByEmailAndIdNot(emailNormalizado, usuarioId)) {
            throw new DuplicateResourceException("O e-mail '%s' já está em uso por outro usuário".formatted(emailNormalizado));
        }
    }

    public String criptografarSenha(String senhaPura) {
        return passwordEncoder.encode(senhaPura);
    }

    /** Troca de senha: a atual precisa estar correta e a nova precisa ser diferente. */
    public void validarTrocaDeSenha(Usuario usuario, String senhaAtual, String novaSenha) {
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new BusinessRuleException("A senha atual está incorreta");
        }
        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            throw new BusinessRuleException("A nova senha deve ser diferente da senha atual");
        }
    }
}
