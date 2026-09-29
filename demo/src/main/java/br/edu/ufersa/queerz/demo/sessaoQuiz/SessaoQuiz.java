package br.edu.ufersa.queerz.demo.sessaoQuiz;

import br.edu.ufersa.queerz.demo.quiz.Quiz;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "sessao_quiz")
public class SessaoQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    /**
     * Mapeamento de Value Objects sem criar uma entidade @Entity 'Participante'.
     * O JPA gera uma tabela auxiliar simples vinculando os apelidos ao id da sessão.
     */
    @ElementCollection
    @CollectionTable(name = "sessao_quiz_participantes", joinColumns = @JoinColumn(name = "sessao_quiz_id"))
    @Column(name = "apelido")
    private List<String> participantes = new ArrayList<>();

    public SessaoQuiz() {
    }

    public SessaoQuiz(Quiz quiz, String codigo) {
        this.quiz = quiz;
        this.codigo = codigo;
        this.ativo = true;
        this.criadoEm = LocalDateTime.now();
    }

    // ---------------- Métodos de Domínio (DDD) ----------------

    public void encerrar() {
        this.ativo = false;
    }

    public void adicionarParticipante(String apelido) {
        if (apelido != null && !apelido.isBlank()) {
            String apelidoFormatado = apelido.trim();
            boolean jaExiste = this.participantes.stream()
                    .anyMatch(p -> p.equalsIgnoreCase(apelidoFormatado));
            if (!jaExiste) {
                this.participantes.add(apelidoFormatado);
            }
        }
    }

    // ---------------- Getters ----------------

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public String getCodigo() {
        return codigo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public List<String> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }
}