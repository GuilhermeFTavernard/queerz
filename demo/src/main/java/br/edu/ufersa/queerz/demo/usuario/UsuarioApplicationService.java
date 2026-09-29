package br.edu.ufersa.queerz.demo.usuario;

import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.ResourceNotFoundException;
import br.edu.ufersa.queerz.demo.shared.security.UsuarioLogado;
import br.edu.ufersa.queerz.demo.usuario.dto.AlterarSenhaRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.AtualizarPerfilRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioResponse;
import br.edu.ufersa.queerz.demo.usuario.mapper.UsuarioMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Casos de uso de Usuário: coordena transação, repositório, regras de domínio e conversão para DTO. */
@Service
public class UsuarioApplicationService {

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
        String email = usuarioDomainService.normalizarEmail(request.email());
        usuarioDomainService.garantirEmailDisponivel(email);

        String senhaCriptografada = usuarioDomainService.criptografarSenha(request.senha());
        Usuario usuario = usuarioMapper.toEntity(request, email, senhaCriptografada);

        // saveAndFlush: se outra requisição cadastrou o mesmo e-mail ao mesmo tempo, o conflito estoura aqui (409)
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

    @Transactional
    public UsuarioResponse atualizarPerfil(Long usuarioId, AtualizarPerfilRequest request) {
        Usuario usuario = buscarEntidade(usuarioId);

        String email = usuarioDomainService.normalizarEmail(request.email());
        usuarioDomainService.garantirEmailDisponivel(email, usuarioId);

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        return UsuarioResponse.from(usuarioRepository.saveAndFlush(usuario));
    }

    @Transactional
    public void alterarSenha(Long usuarioId, AlterarSenhaRequest request) {
        Usuario usuario = buscarEntidade(usuarioId);

        usuarioDomainService.validarTrocaDeSenha(usuario, request.senhaAtual(), request.novaSenha());
        usuario.setSenha(usuarioDomainService.criptografarSenha(request.novaSenha()));
        usuarioRepository.save(usuario);
    }

    /** Operação de ADMIN. Usuário que possui quizzes/tentativas não é removido (violação de FK vira 409). */
    @Transactional
    public void remover(Long id, UsuarioLogado solicitante) {
        if (solicitante.id().equals(id)) {
            throw new BusinessRuleException("Um administrador não pode excluir a própria conta");
        }
        Usuario usuario = buscarEntidade(id);
        usuarioRepository.delete(usuario);
        usuarioRepository.flush();
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}
