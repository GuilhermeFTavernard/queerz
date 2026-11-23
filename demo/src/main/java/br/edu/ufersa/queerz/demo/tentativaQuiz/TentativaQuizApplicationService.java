package br.edu.ufersa.queerz.demo.tentativaQuiz;

import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.RespostaEscolhida;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.Resultado;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.quiz.QuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.QuizRepository;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.tentativaQuiz.dto.FinalizarTentativaRequest;
import br.edu.ufersa.queerz.demo.tentativaQuiz.dto.TentativaResponse;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Casos de uso do jogo individual (usuário logado via HttpSession): registrar tentativa, histórico e ranking.
 */
@Service
public class TentativaQuizApplicationService {

    public static final String ATTR_USUARIO_LOGADO = "USUARIO_LOGADO";

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

    /** A pontuação é calculada pelo servidor a partir das respostas e vinculada ao usuário da sessão HTTP. */
    @Transactional
    public TentativaResponse finalizar(FinalizarTentativaRequest request, HttpServletRequest httpRequest) {
        UsuarioResponse logado = obterUsuarioLogado(httpRequest);

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
    public List<TentativaResponse> listarMinhas(HttpServletRequest httpRequest) {
        UsuarioResponse logado = obterUsuarioLogado(httpRequest);

        return tentativaQuizRepository.findByUsuarioIdOrderByDataFinalizacaoDesc(logado.id())
                .stream()
                .map(TentativaResponse::from)
                .toList();
    }

    /** Top 10: maior pontuação, desempate pelo menor tempo. */
    @Transactional(readOnly = true)
    public List<TentativaResponse> ranking(Long quizId, HttpServletRequest httpRequest) {
        UsuarioResponse logado = obterUsuarioLogado(httpRequest);

        Quiz quiz = buscarQuiz(quizId);
        quizDomainService.garantirPodeVisualizar(quiz, logado);

        return tentativaQuizRepository.findTop10ByQuizIdOrderByPontuacaoFinalDescTempoTotalGastoAsc(quizId)
                .stream()
                .map(TentativaResponse::from)
                .toList();
    }

    // ---------------- Helpers ----------------

    private UsuarioResponse obterUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(ATTR_USUARIO_LOGADO) == null) {
            throw new ForbiddenOperationException("Usuário não autenticado. Faça login para continuar.");
        }
        return (UsuarioResponse) session.getAttribute(ATTR_USUARIO_LOGADO);
    }

    private Quiz buscarQuiz(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }
}