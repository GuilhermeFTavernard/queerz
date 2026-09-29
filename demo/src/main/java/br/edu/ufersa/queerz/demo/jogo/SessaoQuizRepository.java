package br.edu.ufersa.queerz.demo.jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessaoQuizRepository extends JpaRepository<SessaoQuiz, Long> {
    Optional<SessaoQuiz> findByCodigo(String codigo);
}