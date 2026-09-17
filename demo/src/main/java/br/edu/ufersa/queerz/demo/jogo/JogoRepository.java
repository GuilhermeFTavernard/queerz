package br.edu.ufersa.queerz.demo.jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JogoRepository extends JpaRepository<Jogo, Long> {
    Optional<Jogo> findByCodigo(String codigo);
}