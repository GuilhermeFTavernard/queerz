package br.edu.ufersa.queerz.demo.usuario;

import br.edu.ufersa.queerz.demo.shared.exception.BusinessRuleException;
import br.edu.ufersa.queerz.demo.shared.exception.DuplicateResourceException;
import br.edu.ufersa.queerz.demo.shared.exception.ForbiddenOperationException;
import br.edu.ufersa.queerz.demo.usuario.dto.AlterarSenhaRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.AtualizarPerfilRequest;
import br.edu.ufersa.queerz.demo.usuario.dto.UsuarioResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class UsuarioController {

    private final UsuarioApplicationService usuarioApplicationService;

    public UsuarioController(UsuarioApplicationService usuarioApplicationService) {
        this.usuarioApplicationService = usuarioApplicationService;
    }

    // Exibir informações do usuário
    @GetMapping
    public String exibirPerfil(HttpServletRequest request, Model model) {
        UsuarioResponse logado = obterUsuarioLogado(request);
        if (logado == null) {
            return "redirect:/login";
        }

        UsuarioResponse usuario = usuarioApplicationService.buscarPorId(logado.id());
        model.addAttribute("usuario", usuario);
        return "usuario/perfil";
    }

    // Formulário de edição das informações cadastrais
    @GetMapping("/editar")
    public String formEditarPerfil(HttpServletRequest request, Model model) {
        UsuarioResponse logado = obterUsuarioLogado(request);
        if (logado == null) {
            return "redirect:/login";
        }

        UsuarioResponse usuario = usuarioApplicationService.buscarPorId(logado.id());
        model.addAttribute("atualizarPerfilRequest", new AtualizarPerfilRequest(usuario.nome(), usuario.email()));
        return "usuario/form-perfil";
    }

    // Editar informações do usuário
    @PostMapping("/editar")
    public String editarPerfil(@ModelAttribute AtualizarPerfilRequest request,
                               HttpServletRequest httpRequest,
                               RedirectAttributes redirectAttributes) {
        try {
            usuarioApplicationService.atualizarPerfil(request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Perfil atualizado com sucesso!");
            return "redirect:/perfil";
        } catch (DuplicateResourceException | BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/perfil/editar";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Formulário de alteração de senha
    @GetMapping("/alterar-senha")
    public String formAlterarSenha(HttpServletRequest request, Model model) {
        UsuarioResponse logado = obterUsuarioLogado(request);
        if (logado == null) {
            return "redirect:/login";
        }

        model.addAttribute("alterarSenhaRequest", new AlterarSenhaRequest("", ""));
        return "usuario/form-senha";
    }

    // Alterar a senha
    @PostMapping("/alterar-senha")
    public String alterarSenha(@ModelAttribute AlterarSenhaRequest request,
                               HttpServletRequest httpRequest,
                               RedirectAttributes redirectAttributes) {
        try {
            usuarioApplicationService.alterarSenha(request, httpRequest);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso!");
            return "redirect:/perfil";
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            return "redirect:/perfil/alterar-senha";
        } catch (ForbiddenOperationException e) {
            return "redirect:/login";
        }
    }

    // Helper privado para obter o usuário logado diretamente da HttpSession
    private UsuarioResponse obterUsuarioLogado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UsuarioResponse) session.getAttribute(UsuarioApplicationService.ATTR_USUARIO_LOGADO);
    }
}