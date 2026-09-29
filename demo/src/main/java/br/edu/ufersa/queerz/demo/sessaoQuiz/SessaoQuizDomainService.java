package br.edu.ufersa.queerz.demo.jogo;

import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;

/** Regras da sessão por PIN: geração de código único e exigência de sessão ativa. */
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

    /** O PIN digitado pelo jogador pode vir em minúsculas ou com espaços. */
    public String normalizarCodigo(String codigo) {
        return codigo == null ? null : codigo.trim().toUpperCase(Locale.ROOT);
    }

    /** O código é chave de negócio única: sorteia até achar um livre. */
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
}
