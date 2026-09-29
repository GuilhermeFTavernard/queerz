package br.edu.ufersa.queerz.demo.jogo;

import br.edu.ufersa.queerz.demo.jogo.dto.EntrarSessaoRequest;
import br.edu.ufersa.queerz.demo.jogo.dto.ResultadoResponse;
import br.edu.ufersa.queerz.demo.jogo.dto.SessaoResponse;
import br.edu.ufersa.queerz.demo.jogo.dto.SubmeterRespostasRequest;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.RespostaEscolhida;
import br.edu.ufersa.queerz.demo.quiz.CorrecaoQuizDomainService.Resultado;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.quiz.QuizDomainService;
import br.edu.ufersa.queerz.demo.quiz.QuizRepository;
import br.edu.ufersa.queerz.demo.quiz.dto.QuizJogoResponse;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.shared.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Casos de uso da sessão por PIN.
 * Dono do quiz: abrir/encerrar sessão. Jogadores (podem ser anônimos): entrar, ver o quiz e enviar respostas.
 */
@Service
public class SessaoQuizApplicationService {

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

    // ---------------- dono do quiz ----------------

    @Transactional
    public SessaoResponse abrir(Long quizId, UsuarioLogado logado) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));
        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirJogavel(quiz);

        SessaoQuiz sessao = new SessaoQuiz(quiz, sessaoDomainService.gerarCodigoUnico());
        sessaoQuizRepository.saveAndFlush(sessao);
        return SessaoResponse.from(sessao, null);
    }

    @Transactional
    public void encerrar(String codigo, UsuarioLogado logado) {
        SessaoQuiz sessao = buscarSessao(codigo);
        quizDomainService.garantirPodeEditar(sessao.getQuiz(), logado);
        sessaoDomainService.garantirAtiva(sessao);

        sessao.encerrar();
        sessaoQuizRepository.save(sessao);
    }

    // ---------------- jogadores ----------------

    @Transactional(readOnly = true)
    public SessaoResponse entrar(EntrarSessaoRequest request) {
        SessaoQuiz sessao = buscarSessaoAtiva(request.codigoPin());
        return SessaoResponse.from(sessao, request.apelido().trim());
    }

    /** Perguntas da sessão, sem o gabarito. */
    @Transactional(readOnly = true)
    public QuizJogoResponse buscarQuiz(String codigo) {
        SessaoQuiz sessao = buscarSessaoAtiva(codigo);
        quizDomainService.garantirJogavel(sessao.getQuiz());
        return QuizJogoResponse.from(sessao.getQuiz());
    }

    /** Corrige no servidor e devolve o resultado (o apelido do jogador não é persistido). */
    @Transactional(readOnly = true)
    public ResultadoResponse submeter(String codigo, SubmeterRespostasRequest request) {
        SessaoQuiz sessao = buscarSessaoAtiva(codigo);
        Quiz quiz = sessao.getQuiz();
        quizDomainService.garantirJogavel(quiz);

        List<RespostaEscolhida> respostas = request.respostas().stream()
                .map(r -> new RespostaEscolhida(r.perguntaId(), r.alternativaId()))
                .toList();
        Resultado resultado = correcaoQuizDomainService.corrigir(quiz, respostas);

        return new ResultadoResponse(quiz.getId(), resultado.pontuacao(), resultado.acertos(), resultado.totalPerguntas());
    }

    // ---------------- helpers ----------------

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
