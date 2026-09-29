package br.edu.ufersa.queerz.demo.autenticar.dto;

public record TokenResponse(
        String token,
        String tipo,
        long expiraEmSegundos
) {
    public static TokenResponse bearer(String token, long expiraEmSegundos) {
        return new TokenResponse(token, "Bearer", expiraEmSegundos);
    }
}
