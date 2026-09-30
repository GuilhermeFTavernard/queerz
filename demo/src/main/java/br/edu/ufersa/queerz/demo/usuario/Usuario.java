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

    @Column(name = "user_name", nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // A classe UsuarioMapper pediu e fez sozinho isso (VOU CONFIAR NE)
    public Usuario(@NotBlank(message = "O nome é obrigatório") @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres") String nome, @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail em formato inválido") @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres") String email, String senhaCriptografada, UserRole userRole) {
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