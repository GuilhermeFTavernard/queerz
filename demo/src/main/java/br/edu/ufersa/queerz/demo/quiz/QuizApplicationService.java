package br.edu.ufersa.queerz.demo.quiz;

import br.edu.ufersa.queerz.demo.quiz.dto.QuizResponse;
import br.edu.ufersa.queerz.demo.sessaoQuiz.dto.SessaoResponse;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizRequest;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizResponse;
import br.edu.ufersa.queerz.demo.quiz.mapper.QuizMapper;
import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
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

    private final QuizRepository quizRepository;
    private final UsuarioRepository usuarioRepository;
    private final QuizDomainService quizDomainService;
    private final QuizMapper quizMapper;
    private final HttpServletRequest httpRequest;

    public QuizApplicationService(QuizRepository quizRepository,
                                  UsuarioRepository usuarioRepository,
                                  QuizDomainService quizDomainService,
                                  QuizMapper quizMapper,
                                  HttpServletRequest httpRequest) {
        this.quizRepository = quizRepository;
        this.usuarioRepository = usuarioRepository;
        this.quizDomainService = quizDomainService;
        this.quizMapper = quizMapper;
        this.httpRequest = httpRequest;
    }

    @Transactional
    public QuizResponse criar(QuizRequest request) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        quizDomainService.garantirTituloDisponivel(logado.id(), request.titulo().trim(), null);

        Usuario criador = usuarioRepository.findById(logado.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", logado.id()));

        Quiz quiz = quizMapper.toEntity(request);
        quiz.setCriador(criador);
        return QuizResponse.from(quizRepository.save(quiz));
    }

    @Transactional
    public QuizResponse atualizar(Long id, QuizRequest request) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarEntidade(id);

        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirTituloDisponivel(quiz.getCriador().getId(), request.titulo().trim(), id);

        quizMapper.updateEntity(request, quiz);
        return QuizResponse.from(quizRepository.save(quiz));
    }

    @Transactional
    public void remover(Long id) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarEntidade(id);

        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirPodeExcluir(quiz);

        quizRepository.delete(quiz);
        quizRepository.flush();
    }

    /** Painel do dono: inclui o gabarito. */
    @Transactional(readOnly = true)
    public QuizResponse buscarDetalhe(Long id) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        Quiz quiz = buscarEntidade(id);

        quizDomainService.garantirPodeEditar(quiz, logado);
        return QuizResponse.from(quiz);
    }

    /** Consulta pública (não exige login). */
    @Transactional(readOnly = true)
    public List<QuizResponse> listarPublicos(String busca) {
        return quizRepository
                .findByPrivacidadeAndTituloContainingIgnoreCaseOrderByTituloAsc(Privacidade.PUBLICO, termo(busca))
                .stream().map(QuizResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> listarMeus(String busca) {
        UsuarioResponse logado = obterUsuarioResponseDaSessao();
        return quizRepository
                .findByCriadorIdAndTituloContainingIgnoreCaseOrderByTituloAsc(logado.id(), termo(busca))
                .stream().map(QuizResponse::from).toList();
    }

    // ---------------- Helpers ----------------

    /**
     * Recupera obrigatoriamente o usuário autenticado armazenado na sessão do servidor.
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

    /**
     * Tenta recuperar o usuário logado da sessão, retornando null caso não haja sessão ativa
     * (útil para operações com acesso público parcial).
     */
    private UsuarioResponse obterUsuarioResponseDaSessaoOpcional() {
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("USUARIO_LOGADO_ID") == null) {
            return null;
        }

        Long usuarioId = (Long) session.getAttribute("USUARIO_LOGADO_ID");
        return usuarioRepository.findById(usuarioId)
                .map(UsuarioResponse::from)
                .orElse(null);
    }

    private Quiz buscarEntidade(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }

    private String termo(String busca) {
        return busca == null ? "" : busca.trim();
    }
}