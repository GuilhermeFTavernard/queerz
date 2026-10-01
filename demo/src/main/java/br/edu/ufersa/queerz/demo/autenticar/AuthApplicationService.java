package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.autenticar.dto.LoginRequest;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import java.util.List;

@Service
public class AuthApplicationService {

    private final AuthDomainService authDomainService;
    private final UsuarioRepository usuarioRepository;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthApplicationService(AuthDomainService authDomainService, UsuarioRepository usuarioRepository) {
        this.authDomainService = authDomainService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public void login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String emailNormalizado = request.email().toLowerCase().trim();
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);

        authDomainService.validarCredenciais(usuario, request.senha());

        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = httpRequest.getSession(true);
        newSession.setAttribute("USUARIO_LOGADO_ID", usuario.getId());

        // Avisa o Spring Security que este usuário está autenticado
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
        var authentication = UsernamePasswordAuthenticationToken
                .authenticated(usuario.getEmail(), null, authorities);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }
}