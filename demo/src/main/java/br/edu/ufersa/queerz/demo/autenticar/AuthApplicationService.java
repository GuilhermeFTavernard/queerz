package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.autenticar.dto.LoginRequest;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import br.edu.ufersa.queerz.demo.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService {

    private final AuthDomainService authDomainService;
    private final UsuarioRepository usuarioRepository;

    public AuthApplicationService(AuthDomainService authDomainService, UsuarioRepository usuarioRepository) {
        this.authDomainService = authDomainService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public void login(LoginRequest request, HttpServletRequest httpRequest) {
        //Normalização de dados na entrada do caso de uso
        String emailNormalizado = request.email().toLowerCase().trim();

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);

        //Domínio: aplica a regra de negócio
        authDomainService.validarCredenciais(usuario, request.senha());

        //Renovação da sessão para evitar Session Fixation
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = httpRequest.getSession(true);
        newSession.setAttribute("USUARIO_LOGADO_ID", usuario.getId());
    }
}