package br.edu.ufersa.queerz.demo.quiz;

import br.edu.ufersa.queerz.demo.quiz.dto.QuizDetalheResponse;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizJogoResponse;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizRequest;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizResponse;
import br.edu.ufersa.queerz.demo.quiz.mapper.QuizMapper;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.shared.security.UsuarioLogado;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Casos de uso de Quiz. */
@Service
public class QuizApplicationService {

    private final QuizRepository quizRepository;
    private final UsuarioRepository usuarioRepository;
    private final QuizDomainService quizDomainService;
    private final QuizMapper quizMapper;

    public QuizApplicationService(QuizRepository quizRepository,
                                  UsuarioRepository usuarioRepository,
                                  QuizDomainService quizDomainService,
                                  QuizMapper quizMapper) {
        this.quizRepository = quizRepository;
        this.usuarioRepository = usuarioRepository;
        this.quizDomainService = quizDomainService;
        this.quizMapper = quizMapper;
    }

    @Transactional
    public QuizResponse criar(QuizRequest request, UsuarioLogado logado) {
        quizDomainService.garantirTituloDisponivel(logado.id(), request.titulo().trim(), null);

        Usuario criador = usuarioRepository.findById(logado.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", logado.id()));

        Quiz quiz = quizMapper.toEntity(request);
        quiz.setCriador(criador);
        return QuizResponse.from(quizRepository.save(quiz));
    }

    @Transactional
    public QuizResponse atualizar(Long id, QuizRequest request, UsuarioLogado logado) {
        Quiz quiz = buscarEntidade(id);
        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirTituloDisponivel(quiz.getCriador().getId(), request.titulo().trim(), id);

        quizMapper.updateEntity(request, quiz);
        return QuizResponse.from(quizRepository.save(quiz));
    }

    @Transactional
    public void remover(Long id, UsuarioLogado logado) {
        Quiz quiz = buscarEntidade(id);
        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirPodeExcluir(quiz);

        quizRepository.delete(quiz);
        quizRepository.flush();
    }

    /** Painel do dono: inclui o gabarito. */
    @Transactional(readOnly = true)
    public QuizDetalheResponse buscarDetalhe(Long id, UsuarioLogado logado) {
        Quiz quiz = buscarEntidade(id);
        quizDomainService.garantirPodeEditar(quiz, logado);
        return QuizDetalheResponse.from(quiz);
    }

    /** Tela de jogo individual: sem o gabarito, e só se o quiz estiver visível e jogável. */
    @Transactional(readOnly = true)
    public QuizJogoResponse buscarParaJogo(Long id, UsuarioLogado logado) {
        Quiz quiz = buscarEntidade(id);
        quizDomainService.garantirPodeVisualizar(quiz, logado);
        quizDomainService.garantirJogavel(quiz);
        return QuizJogoResponse.from(quiz);
    }

    /** Consulta pública (não exige login). */
    @Transactional(readOnly = true)
    public List<QuizResponse> listarPublicos(String busca) {
        return quizRepository
                .findByPrivacidadeAndTituloContainingIgnoreCaseOrderByTituloAsc(Privacidade.PUBLICO, termo(busca))
                .stream().map(QuizResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> listarMeus(UsuarioLogado logado, String busca) {
        return quizRepository
                .findByCriadorIdAndTituloContainingIgnoreCaseOrderByTituloAsc(logado.id(), termo(busca))
                .stream().map(QuizResponse::from).toList();
    }

    private Quiz buscarEntidade(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }

    private String termo(String busca) {
        return busca == null ? "" : busca.trim();
    }
}
