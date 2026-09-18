package br.edu.ufersa.queerz.demo.participante;


import br.edu.ufersa.queerz.demo.sessaoQuiz.SessaoQuiz;
import jakarta.persistence.*;

@Entity
public class Participante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String apelido;

    private int pontuacaoAtual;

    @ManyToOne
    @JoinColumn(name = "sessao_quiz_id")
    private SessaoQuiz sessaoQuiz;

    public Participante() {
    }

    public Long getId() {
        return id;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public int getPontuacaoAtual() {
        return pontuacaoAtual;
    }

    public void setPontuacaoAtual(int pontuacaoAtual) {
        this.pontuacaoAtual = pontuacaoAtual;
    }

    public SessaoQuiz getSessaoQuiz() {
        return sessaoQuiz;
    }

    public void setSessaoQuiz(SessaoQuiz sessaoQuiz) {
        this.sessaoQuiz = sessaoQuiz;
    }
}
