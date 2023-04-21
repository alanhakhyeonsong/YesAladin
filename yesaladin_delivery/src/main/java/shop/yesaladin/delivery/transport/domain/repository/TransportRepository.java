package shop.yesaladin.delivery.transport.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.yesaladin.delivery.transport.domain.model.Transport;

/**
 * Transport 테이블에 JPA로 접근 하는 Repository 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
public interface TransportRepository extends JpaRepository<Transport, Long>,
        TransportRepositoryCustom {

}
