package br.edu.ufersa.queerz.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class UsuarioController {

    // Exibir informações do usuário
    @GetMapping
    public String exibirPerfil(Model model) {
        System.out.println("Carregando página de perfil do usuário");
        return "usuario/perfil";
    }

    // Formulário de edição das informações cadastrais
    @GetMapping("/editar")
    public String formEditarPerfil(Model model) {
        System.out.println("Exibindo formulário de edição de perfil");
        return "usuario/form-perfil";
    }

    // Editar informações do usuário
    @PostMapping("/editar")
    public String editarPerfil(@RequestParam("nome") String nome,
                               @RequestParam("email") String email,
                               RedirectAttributes redirectAttributes) {
        System.out.println("Atualizando perfil | Nome: " + nome + " | Email: " + email);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Perfil atualizado com sucesso!");
        return "redirect:/perfil";
    }

    // Formulário de alteração de senha
    @GetMapping("/alterar-senha")
    public String formAlterarSenha(Model model) {
        System.out.println("Exibindo formulário de alteração de senha");
        return "usuario/form-senha";
    }

    // Alterar a senha
    @PostMapping("/alterar-senha")
    public String alterarSenha(@RequestParam("senhaAtual") String senhaAtual,
                               @RequestParam("novaSenha") String novaSenha,
                               RedirectAttributes redirectAttributes) {
        System.out.println("Alterando senha | Senha atual: " + senhaAtual + " | Nova senha: " + novaSenha);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Senha alterada com sucesso!");
        return "redirect:/perfil";
    }
}