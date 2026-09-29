package br.edu.ufersa.queerz.demo.pergunta;

import br.edu.ufersa.queerz.demo.alternativa.Alternativa;
import br.edu.ufersa.queerz.demo.alternativa.dto.AlternativaRequest;
import br.edu.ufersa.queerz.demo.alternativa.dto.AlternativaResponse;
import br.edu.ufersa.queerz.demo.alternativa.mapper.AlternativaMapper;
import br.edu.ufersa.queerz.demo.pergunta.dto.PerguntaRequest;
import br.edu.ufersa.queerz.demo.pergunta.dto.PerguntaResponse;
import br.edu.ufersa.queerz.demo.pergunta.mapper.PerguntaMapper;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.quiz.QuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.QuizRepository;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso do painel "gerenciar quiz": perguntas e suas alternativas.
 * Aplicação baseada em sessão (stateful), obtendo o usuário autenticado na HttpSession.
 */
@Service
public class PerguntaApplicationService {

    private final QuizRepository quizRepository;
    private final UsuarioRepository usuarioRepository;
    private final QuizDomainService quizDomainService;
    private final PerguntaDomainService perguntaDomainService;
    private final PerguntaMapper perguntaMapper;
    private final AlternativaMapper alternativaMapper;
    private final HttpServletRequest httpRequest;

    public PerguntaApplicationService(QuizRepository quizRepository,
                                      UsuarioRepository usuarioRepository,
                                      QuizDomainService quizDomainService,
                                      PerguntaDomainService perguntaDomainService,
                                      PerguntaMapper perguntaMapper,
                                      AlternativaMapper alternativaMapper,
                                      HttpServletRequest httpRequest) {
        this.quizRepository = quizRepository;
        this.usuarioRepository = usuarioRepository;
        this.quizDomainService = quizDomainService;
        this.perguntaDomainService = perguntaDomainService;
        this.perguntaMapper = perguntaMapper;
        this.alternativaMapper = alternativaMapper;
        this.httpRequest = httpRequest;
    }

    // ---------------- Perguntas ----------------

    @Transactional
    public PerguntaResponse adicionar(Long quizId, PerguntaRequest request) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        prepararEdicao(quiz, logado);
        quizDomainService.garantirLimiteDePerguntas(quiz);
        perguntaDomainService.garantirEnunciadoDisponivel(quiz, request.enunciado(), null);

        Pergunta pergunta = perguntaMapper.toEntity(request);
        quiz.getPerguntas().add(pergunta);
        quizRepository.flush(); // cascade persiste a pergunta e gera o id

        return PerguntaResponse.from(pergunta);
    }

    @Transactional
    public PerguntaResponse atualizar(Long perguntaId, PerguntaRequest request) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);

        prepararEdicao(quiz, logado);
        perguntaDomainService.garantirEnunciadoDisponivel(quiz, request.enunciado(), pergunta);

        perguntaMapper.updateEntity(request, pergunta);
        return PerguntaResponse.from(pergunta);
    }

    @Transactional
    public void remover(Long perguntaId) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);

        prepararEdicao(quiz, logado);

        quiz.getPerguntas().remove(pergunta); // orphanRemoval apaga a pergunta e suas alternativas
        quizRepository.flush();
    }

    // ---------------- Alternativas ----------------

    @Transactional
    public AlternativaResponse adicionarAlternativa(Long perguntaId, AlternativaRequest request) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);

        prepararEdicao(quiz, logado);
        perguntaDomainService.validarAlternativa(pergunta, null, request);

        Alternativa alternativa = alternativaMapper.toEntity(request);
        pergunta.getAlternativas().add(alternativa);
        quizRepository.flush();

        return AlternativaResponse.from(alternativa);
    }

    @Transactional
    public AlternativaResponse atualizarAlternativa(Long perguntaId, Long alternativaId, AlternativaRequest request) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);

        prepararEdicao(quiz, logado);
        Alternativa alternativa = localizarAlternativa(pergunta, alternativaId);
        perguntaDomainService.validarAlternativa(pergunta, alternativa, request);

        alternativaMapper.updateEntity(request, alternativa);
        return AlternativaResponse.from(alternativa);
    }

    @Transactional
    public void removerAlternativa(Long perguntaId, Long alternativaId) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);

        prepararEdicao(quiz, logado);
        Alternativa alternativa = localizarAlternativa(pergunta, alternativaId);

        pergunta.getAlternativas().remove(alternativa);
        quizRepository.flush();
    }

    // ---------------- Helpers ----------------

    /**
     * Recupera o usuário autenticado armazenado na sessão do servidor.
     */
    private UsuarioResponse obterUsuarioResponseDaSessao() {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("USUARIO_LOGADO_ID") == null) {
            throw new BusinessRuleException("Sessão inexistente ou expirada. Realize o login novamente.");
        }

        Long usuarioId = (Long) session.getAttribute("USUARIO_LOGADO_ID");
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        return UsuarioResponse.from(usuario);
    }

    /** Só o dono/ADMIN mexe na estrutura, e só enquanto o quiz não tem tentativas. */
    private void prepararEdicao(Quiz quiz, UsuarioResponse logado) {
        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirEstruturaEditavel(quiz);
    }

    private Quiz buscarQuizDaPergunta(Long perguntaId) {
        return quizRepository.findByPerguntas_Id(perguntaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", perguntaId));
    }

    private Pergunta localizarPergunta(Quiz quiz, Long perguntaId) {
        return quiz.getPerguntas().stream()
                .filter(p -> p.getId().equals(perguntaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", perguntaId));
    }
}