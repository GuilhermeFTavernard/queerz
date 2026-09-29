package br.edu.ufersa.queerz.demo.sessaoQuiz;

import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.ResultadoResponse;
import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.SubmeterRespostasRequest;
import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.SessaoResponse;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.RespostaEscolhida;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.Resultado;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.quiz.QuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.QuizRepository;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizResponse;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Casos de uso da sessão por PIN (Arquitetura Stateful via HttpSession).
 */
@Service
public class SessaoQuizApplicationService {

    public static final String ATTR_USUARIO_LOGADO = "USUARIO_LOGADO";
    public static final String ATTR_SESSAO_CODIGO = "SESSAO_JOGO_CODIGO";
    public static final String ATTR_JOGADOR_APELIDO = "JOGADOR_APELIDO";

    private final SessaoQuizRepository sessaoQuizRepository;
    private final QuizRepository quizRepository;
    private final SessaoQuizDomainService sessaoDomainService;
    private final QuizDomainService quizDomainService;
    private final CorrecaoQuizDomainService correcaoQuizDomainService;

    public SessaoQuizApplicationService(SessaoQuizRepository sessaoQuizRepository,
                                        QuizRepository quizRepository,
                                        SessaoQuizDomainService sessaoDomainService,
                                        QuizDomainService quizDomainService,
                                        CorrecaoQuizDomainService correcaoQuizDomainService) {
        this.sessaoQuizRepository = sessaoQuizRepository;
        this.quizRepository = quizRepository;
        this.sessaoDomainService = sessaoDomainService;
        this.quizDomainService = quizDomainService;
        this.correcaoQuizDomainService = correcaoQuizDomainService;
    }

    // ---------------- Dono / Criador do Quiz ----------------

    @Transactional
    public SessaoResponse abrir(Long quizId, HttpServletRequest httpRequest) {
        UsuarioResponse logado = obterUsuarioLogado(httpRequest);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirJogavel(quiz);

        SessaoQuiz sessao = new SessaoQuiz(quiz, sessaoDomainService.gerarCodigoUnico());
        sessaoQuizRepository.saveAndFlush(sessao);

        return SessaoResponse.from(sessao);
    }

    @Transactional
    public void encerrar(String codigo, HttpServletRequest httpRequest) {
        UsuarioResponse logado = obterUsuarioLogado(httpRequest);

        SessaoQuiz sessao = buscarSessao(codigo);
        quizDomainService.garantirPodeEditar(sessao.getQuiz(), logado);
        sessaoDomainService.garantirAtiva(sessao);

        sessao.encerrar();
        sessaoQuizRepository.save(sessao);
    }

    // ---------------- Jogadores / Participantes ----------------

    @Transactional
    public SessaoResponse entrar(SessaoResponse request, HttpServletRequest httpRequest) {
        SessaoQuiz sessao = buscarSessaoAtiva(request.codigoPin());
        String apelido = request.apelido() != null ? request.apelido().trim() : "";

        sessaoDomainService.garantirApelidoValidoEDisponivel(sessao, apelido);

        // Adiciona o participante diretamente no Agregado SessaoQuiz
        sessao.adicionarParticipante(apelido);
        sessaoQuizRepository.save(sessao);

        // Salva o estado da partida na HttpSession do jogador
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(ATTR_SESSAO_CODIGO, sessao.getCodigo());
        session.setAttribute(ATTR_JOGADOR_APELIDO, apelido);

        return SessaoResponse.from(sessao);
    }

    @Transactional(readOnly = true)
    public QuizResponse buscarQuiz(HttpServletRequest httpRequest) {
        SessaoQuiz sessao = recuperarSessaoDaRequisicao(httpRequest);
        quizDomainService.garantirJogavel(sessao.getQuiz());

        return QuizResponse.from(sessao.getQuiz());
    }

    @Transactional(readOnly = true)
    public ResultadoResponse submeter(SubmeterRespostasRequest request, HttpServletRequest httpRequest) {
        SessaoQuiz sessao = recuperarSessaoDaRequisicao(httpRequest);
        Quiz quiz = sessao.getQuiz();
        quizDomainService.garantirJogavel(quiz);

        List<RespostaEscolhida> respostas = request.respostas().stream()
                .map(r -> new RespostaEscolhida(r.perguntaId(), r.alternativaId()))
                .toList();

        Resultado resultado = correcaoQuizDomainService.corrigir(quiz, respostas);

        return new ResultadoResponse(quiz.getId(), resultado.pontuacao(), resultado.acertos(), resultado.totalPerguntas());
    }

    // ---------------- Helpers de Estado e Busca ----------------

    private UsuarioResponse obterUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(ATTR_USUARIO_LOGADO) == null) {
            throw new ForbiddenOperationException("Usuário não autenticado.");
        }
        return (UsuarioResponse) session.getAttribute(ATTR_USUARIO_LOGADO);
    }

    private SessaoQuiz recuperarSessaoDaRequisicao(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute(ATTR_SESSAO_CODIGO) == null) {
            throw new ForbiddenOperationException("Você não possui uma sessão de jogo ativa no momento.");
        }

        String codigo = (String) session.getAttribute(ATTR_SESSAO_CODIGO);
        return buscarSessaoAtiva(codigo);
    }

    private SessaoQuiz buscarSessao(String codigo) {
        String pin = sessaoDomainService.normalizarCodigo(codigo);
        return sessaoQuizRepository.findByCodigo(pin)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão com código " + pin + " não encontrada"));
    }

    private SessaoQuiz buscarSessaoAtiva(String codigo) {
        SessaoQuiz sessao = buscarSessao(codigo);
        sessaoDomainService.garantirAtiva(sessao);
        return sessao;
    }
}