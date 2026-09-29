package br.edu.ufersa.queerz.demo.quiz;

import br.edu.ufersa.queerz.demo.jogo.SessaoQuizRepository;
import br.edu.ufersa.queerz.demo.pergunta.Pergunta;
import br.edu.ufersa.queerz.demo.pergunta.PerguntaDomainService;
import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.DuplicateResourceException;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.shared.security.UsuarioLogado;
import br.edu.ufersa.queerz.demo.tentativaquiz.TentativaQuizRepository;
import org.springframework.stereotype.Service;

/**
 * Regras de negócio de Quiz: posse (dono/ADMIN), visibilidade, unicidade de título por autor,
 * estrutura imutável após haver tentativas e critérios para um quiz ser "jogável".
 */
@Service
public class QuizDomainService {

    public static final int MAX_PERGUNTAS = 50;

    private final QuizRepository quizRepository;
    private final TentativaQuizRepository tentativaQuizRepository;
    private final SessaoQuizRepository sessaoQuizRepository;
    private final PerguntaDomainService perguntaDomainService;

    public QuizDomainService(QuizRepository quizRepository,
                             TentativaQuizRepository tentativaQuizRepository,
                             SessaoQuizRepository sessaoQuizRepository,
                             PerguntaDomainService perguntaDomainService) {
        this.quizRepository = quizRepository;
        this.tentativaQuizRepository = tentativaQuizRepository;
        this.sessaoQuizRepository = sessaoQuizRepository;
        this.perguntaDomainService = perguntaDomainService;
    }

    /** Título é chave de negócio por autor. {@code quizIdIgnorado} é o quiz sendo editado (null no cadastro). */
    public void garantirTituloDisponivel(Long criadorId, String titulo, Long quizIdIgnorado) {
        boolean existe = (quizIdIgnorado == null)
                ? quizRepository.existsByCriadorIdAndTituloIgnoreCase(criadorId, titulo)
                : quizRepository.existsByCriadorIdAndTituloIgnoreCaseAndIdNot(criadorId, titulo, quizIdIgnorado);
        if (existe) {
            throw new DuplicateResourceException("Você já possui um quiz com o título '%s'".formatted(titulo));
        }
    }

    public boolean ehDonoOuAdmin(Quiz quiz, UsuarioLogado usuario) {
        return usuario != null
                && (usuario.admin() || quiz.getCriador().getId().equals(usuario.id()));
    }

    /** Alterar/excluir/gerenciar: só o dono ou um ADMIN. */
    public void garantirPodeEditar(Quiz quiz, UsuarioLogado usuario) {
        if (!ehDonoOuAdmin(quiz, usuario)) {
            throw new ForbiddenOperationException("Você não tem permissão para gerenciar este quiz");
        }
    }

    /**
     * Quiz PUBLICO é visível a todos; PRIVADO só ao dono/ADMIN.
     * Para os demais respondemos 404 (não revelamos que o quiz privado existe).
     */
    public void garantirPodeVisualizar(Quiz quiz, UsuarioLogado usuario) {
        if (quiz.getPrivacidade() == Privacidade.PUBLICO || ehDonoOuAdmin(quiz, usuario)) {
            return;
        }
        throw new ResourceNotFoundException("Quiz", quiz.getId());
    }

    /** Quiz com tentativas ou sessões registradas não pode ser removido (preserva o histórico). */
    public void garantirPodeExcluir(Quiz quiz) {
        if (tentativaQuizRepository.existsByQuizId(quiz.getId())) {
            throw new BusinessRuleException("O quiz já possui tentativas registradas e não pode ser excluído");
        }
        if (sessaoQuizRepository.existsByQuizId(quiz.getId())) {
            throw new BusinessRuleException("O quiz já possui sessões criadas e não pode ser excluído");
        }
    }

    /** Alterar perguntas/alternativas depois de haver notas registradas invalidaria os resultados. */
    public void garantirEstruturaEditavel(Quiz quiz) {
        if (tentativaQuizRepository.existsByQuizId(quiz.getId())) {
            throw new BusinessRuleException(
                    "O quiz já possui tentativas registradas; suas perguntas e alternativas não podem mais ser alteradas");
        }
    }

    public void garantirLimiteDePerguntas(Quiz quiz) {
        if (quiz.getPerguntas().size() >= MAX_PERGUNTAS) {
            throw new BusinessRuleException("Um quiz pode ter no máximo %d perguntas".formatted(MAX_PERGUNTAS));
        }
    }

    /** Para jogar: precisa ter perguntas e todas devem estar bem formadas. */
    public void garantirJogavel(Quiz quiz) {
        if (quiz.getPerguntas().isEmpty()) {
            throw new BusinessRuleException("O quiz ainda não possui perguntas");
        }
        for (Pergunta pergunta : quiz.getPerguntas()) {
            perguntaDomainService.garantirJogavel(pergunta);
        }
    }
}
