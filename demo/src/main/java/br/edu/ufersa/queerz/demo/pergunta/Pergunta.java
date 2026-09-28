package br.edu.ufersa.queerz.demo.pergunta;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String enunciado;

    private int tempoResposta;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Alternativa> alternativas = new ArrayList<>();

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

    public List<Alternativa> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<Alternativa> alternativas) {
        this.alternativas = alternativas;
    }
}
