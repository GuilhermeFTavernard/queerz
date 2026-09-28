package br.edu.ufersa.queerz.demo.quiz;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TentativaQuiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pontuacaoFinal;

    private int tempoTotalGasto;

    private LocalDateTime dataFinalizacao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    public TentativaQuiz() {
    }

    public Long getId() {
        return id;
    }

    public int getPontuacaoFinal() {
        return pontuacaoFinal;
    }

    public void setPontuacaoFinal(int pontuacaoFinal) {
        this.pontuacaoFinal = pontuacaoFinal;
    }

    public int getTempoTotalGasto() {
        return tempoTotalGasto;
    }

    public void setTempoTotalGasto(int tempoTotalGasto) {
        this.tempoTotalGasto = tempoTotalGasto;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
