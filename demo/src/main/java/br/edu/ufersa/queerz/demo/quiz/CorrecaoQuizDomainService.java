package br.edu.ufersa.queerz.demo.quiz;

import br.edu.ufersa.queerz.demo.alternativa.Alternativa;
import br.edu.ufersa.queerz.demo.pergunta.Pergunta;
import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Regra de pontuação, compartilhada pelo jogo individual (TentativaQuiz) e pela sessão por PIN (SessaoQuiz).
 * O servidor SEMPRE calcula a nota: o cliente só informa o que marcou.
 *
 * Pontuação = percentual de acertos (0 a 100) sobre o total de perguntas do quiz;
 * pergunta não respondida conta como erro.
 */
@Service
public class CorrecaoQuizDomainService {

    /** Resposta em termos de domínio (independente de DTO da camada web). */
    public record RespostaEscolhida(Long perguntaId, Long alternativaId) {}

    public record Resultado(int acertos, int totalPerguntas, int pontuacao) {}

    public Resultado corrigir(Quiz quiz, List<RespostaEscolhida> respostas) {
        Map<Long, Pergunta> perguntas = quiz.getPerguntas().stream()
                .collect(Collectors.toMap(Pergunta::getId, Function.identity()));

        Set<Long> jaRespondidas = new HashSet<>();
        int acertos = 0;

        for (RespostaEscolhida resposta : respostas) {
            Pergunta pergunta = perguntas.get(resposta.perguntaId());
            if (pergunta == null) {
                throw new BusinessRuleException(
                        "A pergunta #%d não pertence a este quiz".formatted(resposta.perguntaId()));
            }
            if (!jaRespondidas.add(resposta.perguntaId())) {
                throw new BusinessRuleException(
                        "A pergunta #%d foi respondida mais de uma vez".formatted(resposta.perguntaId()));
            }

            Alternativa escolhida = pergunta.getAlternativas().stream()
                    .filter(a -> a.getId().equals(resposta.alternativaId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "A alternativa #%d não pertence à pergunta #%d"
                                    .formatted(resposta.alternativaId(), resposta.perguntaId())));

            if (escolhida.isCorreta()) {
                acertos++;
            }
        }

        int total = perguntas.size();
        int pontuacao = total == 0 ? 0 : (acertos * 100) / total;
        return new Resultado(acertos, total, pontuacao);
    }
}
