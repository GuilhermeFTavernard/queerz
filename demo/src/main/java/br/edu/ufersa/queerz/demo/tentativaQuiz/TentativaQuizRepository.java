package br.edu.ufersa.queerz.demo.tentativaQuiz;

import br.edu.ufersa.queerz.demo.quiz.Quiz;
import br.edu.ufersa.queerz.demo.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TentativaQuizRepository extends JpaRepository<TentativaQuiz , Long> {
    List<TentativaQuiz> findByQuiz(Quiz quiz);
    List<TentativaQuiz> findByUsuario(Usuario usuario);
    boolean existsByQuizId(Long quizId);
    List <TentativaQuiz> findByUsuarioIdOrderByDataFinalizacaoDesc(Long userId);
    List <TentativaQuiz> findTop10ByQuizIdOrderByPontuacaoFinalDescTempoTotalGastoAsc(Long quizId);
}
