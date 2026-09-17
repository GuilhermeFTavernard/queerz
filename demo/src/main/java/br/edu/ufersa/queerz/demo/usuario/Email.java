package br.edu.ufersa.queerz.demo.usuario;

public record Email(String email) {
    public Email{
        if(email == null || !email.contains("@")){
            throw new IllegalArgumentException("O Email é invalido!");
        }
    }
}
