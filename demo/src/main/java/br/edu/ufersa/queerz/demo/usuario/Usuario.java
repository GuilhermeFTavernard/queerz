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

    @Column(nullable = false, length = 150)
    private String email;

    protected Usuario() {
    }

}
