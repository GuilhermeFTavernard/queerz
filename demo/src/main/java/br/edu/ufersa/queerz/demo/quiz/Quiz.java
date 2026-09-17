package br.edu.ufersa.queerz.demo.quiz;

@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private Privacidade privacidade;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Pergunta> perguntas = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    private List<TentativaQuiz> tentativas = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    private List<SessaoQuiz> sessoes = new ArrayList<>();

    public Quiz() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Privacidade getPrivacidade() {
        return privacidade;
    }

    public void setPrivacidade(Privacidade privacidade) {
        this.privacidade = privacidade;
    }

    public List<Pergunta> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<Pergunta> perguntas) {
        this.perguntas = perguntas;
    }

    public List<TentativaQuiz> getTentativas() {
        return tentativas;
    }

    public void setTentativas(List<TentativaQuiz> tentativas) {
        this.tentativas = tentativas;
    }

    public List<SessaoQuiz> getSessoes() {
        return sessoes;
    }

    public void setSessoes(List<SessaoQuiz> sessoes) {
        this.sessoes = sessoes;
    }
}
