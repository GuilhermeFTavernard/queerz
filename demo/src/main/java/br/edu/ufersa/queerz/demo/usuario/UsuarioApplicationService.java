package br.edu.ufersa.queerz.demo.usuario;

import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.usuario.dto.AlterarSenhaRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.AtualizarPerfilRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioResponse;
import br.edu.ufersa.queerz.demo.usuario.mapper.UsuarioMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Casos de uso de Usuário: coordena transação, repositório, regras de domínio,
 * conversão para DTO e sincronização do estado da sessão HTTP (Stateful Monolith).
 */
@Service
public class UsuarioApplicationService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioApplicationService.class);

    public static final String ATTR_USUARIO_LOGADO = "USUARIO_LOGADO";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioDomainService usuarioDomainService;
    private final UsuarioMapper usuarioMapper;

    public UsuarioApplicationService(UsuarioRepository usuarioRepository,
                                     UsuarioDomainService usuarioDomainService,
                                     UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioDomainService = usuarioDomainService;
        this.usuarioMapper = usuarioMapper;
    }

    /** Cadastro público: sempre nasce com o perfil USER. */
    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {

        log.info("Chegou aqui!");
        String email = usuarioDomainService.normalizarEmail(request.email());
        usuarioDomainService.garantirEmailDisponivel(email);

        String senhaCriptografada = usuarioDomainService.criptografarSenha(request.senha());
        Usuario usuario = usuarioMapper.toEntity(request, senhaCriptografada);
        return UsuarioResponse.from(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    /** Atualiza os dados do usuário autenticado na requisição e renova o estado na HttpSession. */
    @Transactional
    public UsuarioResponse atualizarPerfil(AtualizarPerfilRequest request, HttpServletRequest httpRequest) {
        UsuarioResponse logado = obterUsuarioLogado(httpRequest);
        Usuario usuario = buscarEntidade(logado.id());

        String email = usuarioDomainService.normalizarEmail(request.email());
        usuarioDomainService.garantirEmailDisponivel(email, usuario.getId());

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);

        UsuarioResponse atualizado = UsuarioResponse.from(usuarioRepository.saveAndFlush(usuario));

        // Atualiza a HttpSession com os novos dados cadastrais
        httpRequest.getSession().setAttribute(ATTR_USUARIO_LOGADO, atualizado);

        return atualizado;
    }

    @Transactional
    public void alterarSenha(AlterarSenhaRequest request, HttpServletRequest httpRequest) {
        UsuarioResponse logado = obterUsuarioLogado(httpRequest);
        Usuario usuario = buscarEntidade(logado.id());

        usuarioDomainService.validarTrocaDeSenha(usuario, request.senhaAtual(), request.novaSenha());
        usuario.setSenha(usuarioDomainService.criptografarSenha(request.novaSenha()));
        usuarioRepository.save(usuario);
    }

    /** Operação de ADMIN: exige usuário autenticado na sessão HTTP e impede autoexclusão. */
    @Transactional
    public void remover(Long id, HttpServletRequest httpRequest) {
        UsuarioResponse solicitante = obterUsuarioLogado(httpRequest);

        usuarioDomainService.garantirNaoEhPropriaConta(solicitante.id(), id);

        Usuario usuario = buscarEntidade(id);
        usuarioRepository.delete(usuario);
        usuarioRepository.flush();
    }

    // ---------------- Helpers ----------------

    private UsuarioResponse obterUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(ATTR_USUARIO_LOGADO) == null) {
            throw new ForbiddenOperationException("Usuário não autenticado. Faça login para continuar.");
        }
        return (UsuarioResponse) session.getAttribute(ATTR_USUARIO_LOGADO);
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}