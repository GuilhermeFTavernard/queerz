package br.edu.ufersa.queerz.demo.jogo;
import br.edu.ufersa.queerz.demo.quiz.Quiz;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_jogo")
public class Jogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //pega o id do quiz, é uma chave estrangeira resumindo
    @ManyToOne //aqui é aquelas N -> 1, muitos jogos podem estar relacionados a um único quiz
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private boolean ativo;

    public Jogo() {
    }

    public Long getId() {return id;}
    public Quiz getQuiz() {return quiz;}
    public String getCodigo() {return codigo;}
    public boolean isAtivo() {return ativo;}
}