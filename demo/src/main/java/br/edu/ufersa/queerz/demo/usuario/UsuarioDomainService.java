package br.edu.ufersa.queerz.demo.usuario;

import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.DuplicateResourceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Regras de negócio de Usuário puras: unicidade do e-mail, criptografia,
 * validação de troca de senha e restrições de exclusão.
 */
@Service
public class UsuarioDomainService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioDomainService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void garantirEmailDisponivel(String emailNormalizado) {
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new DuplicateResourceException("O e-mail '%s' já está cadastrado".formatted(emailNormalizado));
        }
    }

    public void garantirEmailDisponivel(String emailNormalizado, Long usuarioId) {
        if (usuarioRepository.existsByEmailAndIdNot(emailNormalizado, usuarioId)) {
            throw new DuplicateResourceException("O e-mail '%s' já está em uso por outro usuário".formatted(emailNormalizado));
        }
    }

    public String criptografarSenha(String senhaPura) {
        return passwordEncoder.encode(senhaPura);
    }

    public void validarTrocaDeSenha(Usuario usuario, String senhaAtual, String novaSenha) {
        if (!passwordEncoder.matches(senhaAtual, usuario.getPassword())) {
            throw new BusinessRuleException("A senha atual está incorreta");
        }
        if (passwordEncoder.matches(novaSenha, usuario.getPassword())) {
            throw new BusinessRuleException("A nova senha deve ser diferente da senha atual");
        }
    }

    /** Regra de segurança/domínio: impede que um administrador exclua o próprio usuário. */
    public void garantirNaoEhPropriaConta(Long solicitanteId, Long alvoId) {
        if (solicitanteId.equals(alvoId)) {
            throw new BusinessRuleException("Um administrador não pode excluir a própria conta");
        }
    }
}