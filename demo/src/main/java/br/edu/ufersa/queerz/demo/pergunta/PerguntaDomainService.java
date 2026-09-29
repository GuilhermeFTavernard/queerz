package br.edu.ufersa.queerz.demo.pergunta;

import br.edu.ufersa.queerz.demo.alternativa.Alternativa;
import br.edu.ufersa.queerz.demo.alternativa.dto.AlternativaRequest;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.DuplicateResourceException;
import org.springframework.stereotype.Service;

/**
 * Regras do agregado Pergunta (que é raiz das Alternativas):
 * limite de alternativas, uma única correta, sem textos repetidos e sem enunciados repetidos no quiz.
 */
@Service
public class PerguntaDomainService {

    public static final int MIN_ALTERNATIVAS = 2;
    public static final int MAX_ALTERNATIVAS = 5;

    /** Enunciado é chave de negócio dentro do quiz. {@code alvo} é a pergunta sendo editada (null no cadastro). */
    public void garantirEnunciadoDisponivel(Quiz quiz, String enunciado, Pergunta alvo) {
        String novo = enunciado.trim();
        boolean duplicado = quiz.getPerguntas().stream()
                .filter(p -> p != alvo)
                .anyMatch(p -> novo.equalsIgnoreCase(p.getEnunciado()));
        if (duplicado) {
            throw new DuplicateResourceException("Já existe uma pergunta com este enunciado no quiz");
        }
    }

    /**
     * Valida inclusão/edição de uma alternativa. {@code alvo} é a alternativa sendo editada (null na inclusão).
     */
    public void validarAlternativa(Pergunta pergunta, Alternativa alvo, AlternativaRequest request) {
        if (alvo == null && pergunta.getAlternativas().size() >= MAX_ALTERNATIVAS) {
            throw new BusinessRuleException(
                    "Uma pergunta pode ter no máximo %d alternativas".formatted(MAX_ALTERNATIVAS));
        }

        String texto = request.texto().trim();
        boolean textoRepetido = pergunta.getAlternativas().stream()
                .filter(a -> a != alvo)
                .anyMatch(a -> texto.equalsIgnoreCase(a.getTexto()));
        if (textoRepetido) {
            throw new DuplicateResourceException("Já existe uma alternativa com este texto na pergunta");
        }

        if (Boolean.TRUE.equals(request.correta())) {
            boolean jaExisteCorreta = pergunta.getAlternativas().stream()
                    .anyMatch(a -> a != alvo && a.isCorreta());
            if (jaExisteCorreta) {
                throw new BusinessRuleException(
                        "A pergunta já possui uma alternativa correta; desmarque-a antes de marcar outra");
            }
        }
    }

    /** Uma pergunta só pode ser jogada com 2 a 5 alternativas e exatamente uma correta. */
    public void garantirJogavel(Pergunta pergunta) {
        int total = pergunta.getAlternativas().size();
        if (total < MIN_ALTERNATIVAS || total > MAX_ALTERNATIVAS) {
            throw new BusinessRuleException(
                    "A pergunta #%d deve ter entre %d e %d alternativas"
                            .formatted(pergunta.getId(), MIN_ALTERNATIVAS, MAX_ALTERNATIVAS));
        }
        long corretas = pergunta.getAlternativas().stream().filter(Alternativa::isCorreta).count();
        if (corretas != 1) {
            throw new BusinessRuleException(
                    "A pergunta #%d deve ter exatamente uma alternativa correta".formatted(pergunta.getId()));
        }
    }
}
