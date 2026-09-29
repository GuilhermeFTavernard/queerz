package br.edu.ufersa.queerz.demo.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz , Long> {
    Optional<Quiz> findByPerguntas_Id(Long perguntaId);
    List<Quiz> findByPrivacidadeAndTituloContainingIgnoreCaseOrderByTituloAsc(Privacidade privacidade, String titulo);
    List<Quiz> findByCriadorIdAndTituloContainingIgnoreCaseOrderByTituloAsc(Long id, String titulo);
    boolean existsByCriadorIdAndTituloIgnoreCase(Long id, String titulo);
    boolean existsByCriadorIdAndTituloIgnoreCaseAndIdNot(Long criadorId, String titulo, Long quizIdIgnorado);
}
