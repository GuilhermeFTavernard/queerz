package br.edu.ufersa.queerz.demo.usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senhaCriptografada, UserRole userRole) {
        this.nome = nome;
        this.email = email;
        this.senha = senhaCriptografada;
        this.role = (role!= null) ? role : UserRole.USER;
    }

    // UserDetails
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; } // o login é pelo email
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String senha) { this.senha = senha; }
    public void setRole(UserRole role) { this.role = role; }
    public UserRole getRole(){return role;}
    public void setNome(String userName) { this.nome = nome; }
    //public String setEmail(String email){return email;}
    public String setSenha(String senha){return senha;}
}