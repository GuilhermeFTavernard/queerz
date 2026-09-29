package br.edu.ufersa.queerz.demo.sessaoQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessaoQuizRepository extends JpaRepository<SessaoQuiz, Long> {
    Optional<SessaoQuiz> findByCodigo(String codigo);
    boolean existsByQuizId(Long quizId);
    boolean existsByCodigo(String codigo);
}