package br.edu.ufersa.queerz.demo.usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_user")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false, length = 150)
    private String senha;

    //impedição de criação de metodos vazios
    protected Usuario() {
    }

    public Usuario(String nome, String email, String senha) {
        if(email == null || !email.contains("@")){
            throw new IllegalArgumentException("O email é invalido!");
        }
        if(senha == null || senha.matches("\\d{8}")){
            throw new IllegalArgumentException("A senha não pode ser nula ou inferior a 8 caracteres");
        }

        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // retorno de dados
    public Long getId(){return id;}
    public String getEmail(){return email;}
    public String getSenha(){return senha;}
    public String getNome(){return nome;}
}