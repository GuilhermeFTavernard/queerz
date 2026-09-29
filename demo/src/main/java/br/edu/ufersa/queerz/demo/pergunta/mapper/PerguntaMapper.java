package br.edu.ufersa.queerz.demo.pergunta.mapper;

import br.edu.ufersa.queerz.demo.pergunta.Pergunta;
import br.edu.ufersa.queerz.demo.pergunta.dto.PerguntaRequest;
import org.springframework.stereotype.Component;

/** Converte PerguntaRequest -> entidade. A saída está em PerguntaResponse.from(...). */
@Component
public class PerguntaMapper {

    public Pergunta toEntity(PerguntaRequest request) {
        Pergunta pergunta = new Pergunta();
        updateEntity(request, pergunta);
        return pergunta;
    }

    public void updateEntity(PerguntaRequest request, Pergunta pergunta) {
        pergunta.setEnunciado(request.enunciado());
        pergunta.setTempoResposta(request.tempoResposta());
    }
}
