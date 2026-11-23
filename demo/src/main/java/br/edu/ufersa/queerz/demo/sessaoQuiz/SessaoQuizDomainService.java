package br.edu.ufersa.queerz.demo.sessaoQuiz;

import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;

/** Regras da sessão por PIN: geração de código único, checagem de estado e regras de participante. */
@Service
public class SessaoQuizDomainService {

    // sem 0/O/1/I para não confundir na hora de digitar o PIN
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_PIN = 6;
    private static final int MAX_TENTATIVAS = 10;

    private final SessaoQuizRepository sessaoQuizRepository;
    private final SecureRandom random = new SecureRandom();

    public SessaoQuizDomainService(SessaoQuizRepository sessaoQuizRepository) {
        this.sessaoQuizRepository = sessaoQuizRepository;
    }

    public String normalizarCodigo(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase(Locale.ROOT);
    }

    public String gerarCodigoUnico() {
        for (int i = 0; i < MAX_TENTATIVAS; i++) {
            StringBuilder pin = new StringBuilder(TAMANHO_PIN);
            for (int j = 0; j < TAMANHO_PIN; j++) {
                pin.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
            }
            String codigo = pin.toString();
            if (!sessaoQuizRepository.existsByCodigo(codigo)) {
                return codigo;
            }
        }
        throw new BusinessRuleException("Não foi possível gerar um código único para a sessão; tente novamente");
    }

    public void garantirAtiva(SessaoQuiz sessao) {
        if (!sessao.isAtivo()) {
            throw new BusinessRuleException("A sessão '%s' já foi encerrada".formatted(sessao.getCodigo()));
        }
    }

    /**
     * Validações de entrada de Participante (Value Object).
     */
    public void garantirApelidoValidoEDisponivel(SessaoQuiz sessao, String apelido) {
        if (apelido == null || apelido.isBlank()) {
            throw new BusinessRuleException("O apelido não pode ser vazio.");
        }
        if (apelido.length() > 20) {
            throw new BusinessRuleException("O apelido deve ter no máximo 20 caracteres.");
        }

        // Se SessaoQuiz gerencia a lista internamente (ex: uma List<String> ou List<Participante>)
        boolean apelidoEmUso = sessao.getParticipantes().stream()
                .anyMatch(p -> p.equalsIgnoreCase(apelido));

        if (apelidoEmUso) {
            throw new BusinessRuleException("O apelido '%s' já está em uso nesta sessão. Escolha outro.".formatted(apelido));
        }
    }
}