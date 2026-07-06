package compass.transferencia_bancaria_api.repository;

import compass.transferencia_bancaria_api.domain.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByOrigemIdOrDestinoId(Long origemId, Long destinoId);

}