package br.edu.ufersa.queerz.demo.pergunta;

import br.edu.ufersa.queerz.demo.alternativa.Alternativa;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String enunciado;

    private int tempoResposta;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Alternativa> alternativas = new ArrayList<>();

    @ManyToOne
    private Quiz quiz;

    public Pergunta() {
    }

    public Long getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public int getTempoResposta() {
        return tempoResposta;
    }

    public void setTempoResposta(int tempoResposta) {
        this.tempoResposta = tempoResposta;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public List<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<Alternativa> alternativas) {
        this.alternativas = alternativas;
    }

    public Alternativa localizarAlternativa(Long alternativaId) {
        return this.alternativas.stream()
                .filter(a -> Objects.equals(a.getId(), alternativaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", alternativaId));
    }
}
