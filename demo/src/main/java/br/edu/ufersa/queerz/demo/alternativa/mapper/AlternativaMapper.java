package br.edu.ufersa.queerz.demo.alternativa.mapper;

import br.edu.ufersa.queerz.demo.alternativa.Alternativa;
import br.edu.ufersa.queerz.demo.alternativa.dto.AlternativaRequest;
import org.springframework.stereotype.Component;

/** Converte AlternativaRequest -> entidade. A saída (entidade -> DTO) está em AlternativaResponse.from(...). */
@Component
public class AlternativaMapper {

    public Alternativa toEntity(AlternativaRequest request) {
        Alternativa alternativa = new Alternativa();
        updateEntity(request, alternativa);
        return alternativa;
    }

    public void updateEntity(AlternativaRequest request, Alternativa alternativa) {
        alternativa.setTexto(request.texto());
        alternativa.setCorreta(request.correta());
    }
}
