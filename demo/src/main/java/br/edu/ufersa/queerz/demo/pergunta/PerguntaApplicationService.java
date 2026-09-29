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
import br.edu.ufersa.queerz.demo.shared.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso do painel "gerenciar quiz": perguntas e suas alternativas.
 * Pergunta é raiz do agregado, então as alternativas são tratadas aqui.
 */
@Service
public class PerguntaApplicationService {

    private final QuizRepository quizRepository;
    private final QuizDomainService quizDomainService;
    private final PerguntaDomainService perguntaDomainService;
    private final PerguntaMapper perguntaMapper;
    private final AlternativaMapper alternativaMapper;

    public PerguntaApplicationService(QuizRepository quizRepository,
                                      QuizDomainService quizDomainService,
                                      PerguntaDomainService perguntaDomainService,
                                      PerguntaMapper perguntaMapper,
                                      AlternativaMapper alternativaMapper) {
        this.quizRepository = quizRepository;
        this.quizDomainService = quizDomainService;
        this.perguntaDomainService = perguntaDomainService;
        this.perguntaMapper = perguntaMapper;
        this.alternativaMapper = alternativaMapper;
    }

    // ---------------- Perguntas ----------------

    @Transactional
    public PerguntaResponse adicionar(Long quizId, PerguntaRequest request, UsuarioLogado logado) {
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
    public PerguntaResponse atualizar(Long perguntaId, PerguntaRequest request, UsuarioLogado logado) {
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);
        prepararEdicao(quiz, logado);
        perguntaDomainService.garantirEnunciadoDisponivel(quiz, request.enunciado(), pergunta);

        perguntaMapper.updateEntity(request, pergunta);
        return PerguntaResponse.from(pergunta);
    }

    @Transactional
    public void remover(Long perguntaId, UsuarioLogado logado) {
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);
        prepararEdicao(quiz, logado);

        quiz.getPerguntas().remove(pergunta); // orphanRemoval apaga a pergunta e suas alternativas
        quizRepository.flush();
    }

    // ---------------- Alternativas ----------------

    @Transactional
    public AlternativaResponse adicionarAlternativa(Long perguntaId, AlternativaRequest request, UsuarioLogado logado) {
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
    public AlternativaResponse atualizarAlternativa(Long perguntaId, Long alternativaId,
                                                    AlternativaRequest request, UsuarioLogado logado) {
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);
        prepararEdicao(quiz, logado);
        Alternativa alternativa = localizarAlternativa(pergunta, alternativaId);
        perguntaDomainService.validarAlternativa(pergunta, alternativa, request);

        alternativaMapper.updateEntity(request, alternativa);
        return AlternativaResponse.from(alternativa);
    }

    @Transactional
    public void removerAlternativa(Long perguntaId, Long alternativaId, UsuarioLogado logado) {
        Quiz quiz = buscarQuizDaPergunta(perguntaId);
        Pergunta pergunta = localizarPergunta(quiz, perguntaId);
        prepararEdicao(quiz, logado);
        Alternativa alternativa = localizarAlternativa(pergunta, alternativaId);

        pergunta.getAlternativas().remove(alternativa);
        quizRepository.flush();
    }

    // ---------------- helpers ----------------

    /** Só o dono/ADMIN mexe na estrutura, e só enquanto o quiz não tem tentativas. */
    private void prepararEdicao(Quiz quiz, UsuarioLogado logado) {
        quizDomainService.garantirPodeEditar(quiz, logado);
        quizDomainService.garantirEstruturaEditavel(quiz);
    }

    private Quiz buscarQuizDaPergunta(Long perguntaId) {
        return quizRepository.findByPerguntasId(perguntaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", perguntaId));
    }

    private Pergunta localizarPergunta(Quiz quiz, Long perguntaId) {
        return quiz.getPerguntas().stream()
                .filter(p -> p.getId().equals(perguntaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Pergunta", perguntaId));
    }

    private Alternativa localizarAlternativa(Pergunta pergunta, Long alternativaId) {
        return pergunta.getAlternativas().stream()
                .filter(a -> a.getId().equals(alternativaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", alternativaId));
    }
}
