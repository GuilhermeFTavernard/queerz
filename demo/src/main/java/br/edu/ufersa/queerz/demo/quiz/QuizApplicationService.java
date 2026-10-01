package br.edu.ufersa.queerz.demo.quiz;

import br.edu.ufersa.queerz.demo.alternativa.Alternativa;
import br.edu.ufersa.queerz.demo.alternativa.AlternativaRepository;
import br.edu.ufersa.queerz.demo.alternativa.dto.AlternativaRequest;
import br.edu.ufersa.queerz.demo.pergunta.Pergunta;
import br.edu.ufersa.queerz.demo.pergunta.PerguntaRepository;
import br.edu.ufersa.queerz.demo.pergunta.dto.PerguntaRequest;
import br.edu.ufersa.queerz.demo.pergunta.dto.PerguntaResponse;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizRequest;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizResponse;
import br.edu.ufersa.queerz.demo.quiz.mapper.QuizMapper;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Casos de uso de Quiz em arquitetura monolítica stateful (sessão HTTP no servidor).
 */
@Service
public class QuizApplicationService {

    public static final String ATTR_USUARIO_LOGADO = "USUARIO_LOGADO";

    private final QuizRepository quizRepository;
    private final UsuarioRepository usuarioRepository;
    private final QuizDomainService quizDomainService;
    private final QuizMapper quizMapper;
    private final HttpServletRequest httpRequest;
    private final PerguntaRepository perguntaRepository;
    private final AlternativaRepository alternativaRepository;

    public QuizApplicationService(QuizRepository quizRepository,
                                  UsuarioRepository usuarioRepository,
                                  QuizDomainService quizDomainService,
                                  QuizMapper quizMapper,
                                  HttpServletRequest httpRequest, PerguntaRepository perguntaRepository, AlternativaRepository alternativaRepository) {
        this.quizRepository = quizRepository;
        this.usuarioRepository = usuarioRepository;
        this.quizDomainService = quizDomainService;
        this.quizMapper = quizMapper;
        this.httpRequest = httpRequest;
        this.perguntaRepository = perguntaRepository;
        this.alternativaRepository = alternativaRepository;
    }

    // ---------------- Operações CRUD do Quiz ----------------

    @Transactional
    public QuizResponse criar(QuizRequest request, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        quizDomainService.garantirTituloDisponivel(logado.id(), request.titulo().trim(), null);

        Usuario criador = usuarioRepository.findById(logado.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", logado.id()));


        Quiz quiz = quizMapper.toEntity(request, criador);
        return QuizResponse.from(quizRepository.save(quiz));
    }

    @Transactional
    public QuizResponse atualizar(Long id, QuizRequest request, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        Quiz quiz = buscarEntidade(id);

        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirTituloDisponivel(quiz.getCriador().getId(), request.titulo().trim(), id);

        quizMapper.updateEntity(request, quiz);
        return QuizResponse.from(quizRepository.save(quiz));
    }

    @Transactional
    public QuizResponse atualizar(Long id, QuizRequest request) {
        return atualizar(id, request, this.httpRequest);
    }

    @Transactional
    public void remover(Long id, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        Quiz quiz = buscarEntidade(id);

        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirPodeExcluir(quiz);

        quizRepository.delete(quiz);
        quizRepository.flush();
    }

    @Transactional
    public void remover(Long id) {
        remover(id, this.httpRequest);
    }

    @Transactional(readOnly = true)
    public QuizResponse buscarPorId(Long id, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        Quiz quiz = buscarEntidade(id);

        quizDomainService.garantirPodeEditar(quiz, logado);
        return QuizResponse.from(quiz);
    }

    @Transactional(readOnly = true)
    public QuizResponse buscarPorId(Long id) {
        return buscarPorId(id, this.httpRequest);
    }

    @Transactional(readOnly = true)
    public QuizResponse buscarPorIdComPerguntas(Long id, HttpServletRequest req) {
        return buscarPorId(id, req);
    }

    @Transactional(readOnly = true)
    public QuizResponse buscarPorIdComPerguntas(Long id) {
        return buscarPorIdComPerguntas(id, this.httpRequest);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> listarPublicos(String busca) {
        return quizRepository
                .findByPrivacidadeAndTituloContainingIgnoreCaseOrderByTituloAsc(Privacidade.PUBLICO, termo(busca))
                .stream().map(QuizResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> listarMeus(String busca, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        return quizRepository
                .findByCriadorIdAndTituloContainingIgnoreCaseOrderByTituloAsc(logado.id(), termo(busca))
                .stream().map(QuizResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> listarMeus(String busca) {
        return listarMeus(busca, this.httpRequest);
    }

    // ---------------- Gerenciamento de Perguntas ----------------

    @Transactional
    public void adicionarPergunta(Long quizId, PerguntaRequest request, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        Quiz quiz = buscarEntidade(quizId);
        quizDomainService.garantirPodeEditar(quiz, logado);

        Pergunta pergunta = new Pergunta();
        pergunta.setEnunciado(request.enunciado().trim());
        pergunta.setTempoResposta(request.tempoResposta());
        pergunta.setQuiz(quiz);

        perguntaRepository.save(pergunta);
    }

    @Transactional(readOnly = true)
    public PerguntaResponse buscarPergunta(Long quizId, Long perguntaId, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        Quiz quiz = buscarEntidade(quizId);
        quizDomainService.garantirPodeEditar(quiz, logado);

        Pergunta pergunta = localizarPergunta(quiz, perguntaId);
        return PerguntaResponse.from(pergunta);
    }

    @Transactional
    public void atualizarPergunta(Long quizId, Long perguntaId, PerguntaRequest request, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        Quiz quiz = buscarEntidade(quizId);
        quizDomainService.garantirPodeEditar(quiz, logado);

        Pergunta pergunta = localizarPergunta(quiz, perguntaId);
        pergunta.setEnunciado(request.enunciado().trim());
        pergunta.setTempoResposta(request.tempoResposta());

        quizRepository.save(quiz);
    }

    // ---------------- Gerenciamento de Alternativas ----------------

    @Transactional
    public void adicionarAlternativa(Long perguntaId, AlternativaRequest request, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);

        Pergunta pergunta = perguntaRepository.findById(perguntaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", perguntaId));

        quizDomainService.garantirPodeEditar(pergunta.getQuiz(), logado);

        Alternativa alternativa = new Alternativa();
        alternativa.setTexto(request.texto().trim());
        alternativa.setCorreta(request.correta());

        alternativaRepository.save(alternativa);
    }

    @Transactional
    public void atualizarAlternativa(Long quizId, Long perguntaId, Long alternativaId, AlternativaRequest request, HttpServletRequest req) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao(req);
        Quiz quiz = buscarEntidade(quizId);
        quizDomainService.garantirPodeEditar(quiz, logado);

        Pergunta pergunta = localizarPergunta(quiz, perguntaId);
        Alternativa alternativa = pergunta.localizarAlternativa(alternativaId);

        alternativa.setTexto(request.texto().trim());
        alternativa.setCorreta(request.correta());

        quizRepository.save(quiz);
    }

    // ---------------- Helpers Internos ----------------

    private UsuarioResponse obterUsuarioResponseDaSessao(HttpServletRequest req) {
        HttpServletRequest requestEfetiva = req != null ? req : this.httpRequest;
        HttpSession session = requestEfetiva.getSession(false);

        if (session == null) {
            throw new ForbiddenOperationException("Sessão inexistente ou expirada. Realize o login novamente.");
        }

        Object usuarioObj = session.getAttribute(ATTR_USUARIO_LOGADO);
        if (usuarioObj instanceof UsuarioResponse response) {
            return response;
        }

        Long usuarioId = (Long) session.getAttribute("USUARIO_LOGADO_ID");
        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
            return UsuarioResponse.from(usuario);
        }

        throw new ForbiddenOperationException("Usuário não autenticado. Realize o login para continuar.");
    }

    private Quiz buscarEntidade(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }

    private Pergunta localizarPergunta(Quiz quiz, Long perguntaId) {
        return quiz.getPerguntas().stream()
                .filter(p -> p.getId() != null && p.getId().equals(perguntaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", perguntaId));
    }

    private String termo(String busca) {
        return busca == null ? "" : busca.trim();
    }
}