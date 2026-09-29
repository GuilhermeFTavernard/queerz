package br.edu.ufersa.queerz.demo.tentativaQuiz;

import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.RespostaEscolhida;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.Resultado;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.quiz.QuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.QuizRepository;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.shared.security.UsuarioLogado;
import br.edu.ufersa.queerz.demo.tentativaquiz.dto.FinalizarTentativaRequest;
import br.edu.ufersa.queerz.demo.tentativaquiz.dto.TentativaResponse;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Casos de uso do jogo individual (usuário logado): registrar tentativa, histórico e ranking. */
@Service
public class TentativaQuizApplicationService {

    private final TentativaQuizRepository tentativaQuizRepository;
    private final QuizRepository quizRepository;
    private final UsuarioRepository usuarioRepository;
    private final QuizDomainService quizDomainService;
    private final CorrecaoQuizDomainService correcaoQuizDomainService;

    public TentativaQuizApplicationService(TentativaQuizRepository tentativaQuizRepository,
                                           QuizRepository quizRepository,
                                           UsuarioRepository usuarioRepository,
                                           QuizDomainService quizDomainService,
                                           CorrecaoQuizDomainService correcaoQuizDomainService) {
        this.tentativaQuizRepository = tentativaQuizRepository;
        this.quizRepository = quizRepository;
        this.usuarioRepository = usuarioRepository;
        this.quizDomainService = quizDomainService;
        this.correcaoQuizDomainService = correcaoQuizDomainService;
    }

    /** A pontuação é calculada aqui, pelo servidor, a partir das respostas. */
    @Transactional
    public TentativaResponse finalizar(FinalizarTentativaRequest request, UsuarioLogado logado) {
        Quiz quiz = buscarQuiz(request.quizId());
        quizDomainService.garantirPodeVisualizar(quiz, logado);
        quizDomainService.garantirJogavel(quiz);

        List<RespostaEscolhida> respostas = request.respostas().stream()
                .map(r -> new RespostaEscolhida(r.perguntaId(), r.alternativaId()))
                .toList();
        Resultado resultado = correcaoQuizDomainService.corrigir(quiz, respostas);

        Usuario usuario = usuarioRepository.findById(logado.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", logado.id()));

        TentativaQuiz tentativa = new TentativaQuiz();
        tentativa.setUsuario(usuario);
        tentativa.setQuiz(quiz);
        tentativa.setPontuacaoFinal(resultado.pontuacao());
        tentativa.setTempoTotalGasto(request.tempoTotalGasto());
        tentativa.setDataFinalizacao(LocalDateTime.now());

        return TentativaResponse.from(tentativaQuizRepository.save(tentativa));
    }

    @Transactional(readOnly = true)
    public List<TentativaResponse> listarMinhas(UsuarioLogado logado) {
        return tentativaQuizRepository.findByUsuarioIdOrderByDataFinalizacaoDesc(logado.id())
                .stream().map(TentativaResponse::from).toList();
    }

    /** Top 10: maior pontuação, desempate pelo menor tempo. */
    @Transactional(readOnly = true)
    public List<TentativaResponse> ranking(Long quizId, UsuarioLogado logado) {
        Quiz quiz = buscarQuiz(quizId);
        quizDomainService.garantirPodeVisualizar(quiz, logado);
        return tentativaQuizRepository.findTop10ByQuizIdOrderByPontuacaoFinalDescTempoTotalGastoAsc(quizId)
                .stream().map(TentativaResponse::from).toList();
    }

    private Quiz buscarQuiz(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }
}
