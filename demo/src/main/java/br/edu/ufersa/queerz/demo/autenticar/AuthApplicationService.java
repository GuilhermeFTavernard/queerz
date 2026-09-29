package br.edu.ufersa.queerz.demo.autenticar;

import br.edu.ufersa.queerz.demo.autenticar.dto.LoginRequest;
import br.edu.ufersa.queerz.demo.autenticar.dto.TokenResponse;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso "login": valida as credenciais (domínio) e emite o JWT. */
@Service
public class AuthApplicationService {

    private final AuthDomainService authDomainService;
    private final TokenService tokenService;

    public AuthApplicationService(AuthDomainService authDomainService, TokenService tokenService) {
        this.authDomainService = authDomainService;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = authDomainService.autenticar(request.email(), request.senha());
        String token = tokenService.gerarToken(usuario);
        return TokenResponse.bearer(token, tokenService.getExpiracaoEmSegundos());
    }
}
