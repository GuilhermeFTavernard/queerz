package br.edu.ufersa.queerz.demo.usuario;
import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tb_user")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    //impedição de criação de metodos vazios
    protected Usuario() {
    }

    public Usuario(Long id){
        this.id = id;
    }

    public Usuario(String nome, String email, String senha, UserRole role) {
        if(email == null || !email.contains("@")){
            throw new IllegalArgumentException("O email é invalido!");
        }
        if(senha == null || senha.matches("\\d{8}")){
            throw new IllegalArgumentException("A senha não pode ser nula ou inferior a 8 caracteres");
        }

        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    // retorno de dados
    public Long getId(){return id;}
    public String getEmail(){return email;}
    public String getSenha(){return senha;}
    public String getNome(){return nome;}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        if(this.role == UserRole.ADMIN){
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}