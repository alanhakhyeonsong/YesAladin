package shop.yesaladin.delivery.transport.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import shop.yesaladin.delivery.transport.domain.model.Transport;
import shop.yesaladin.delivery.transport.domain.model.TransportStatusCode;
import shop.yesaladin.delivery.transport.dummy.DummyTransport;

@DataJpaTest
class TransportRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private TransportRepository repository;

    private Transport transport;

    private final Clock clock = Clock.fixed(
            Instant.parse("2023-01-19T00:00:00.000Z"),
            ZoneId.of("UTC")
    );

    private String trackingNo;

    @BeforeEach
    void setUp() {
        trackingNo = UUID.randomUUID().toString();
        transport = DummyTransport.dummy(clock, trackingNo);
        entityManager.persist(transport);
    }

    @Test
    void save() throws Exception {
        //given
        long orderId = 1L;

        //when
        Transport savedTransport = repository.save(transport);

        //then
        assertThat(savedTransport.getOrderId()).isEqualTo(orderId);
        assertThat(savedTransport.getTrackingNo()).isEqualTo(trackingNo);
        assertThat(savedTransport.getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(savedTransport.getTransportStatusCode()).isEqualTo(TransportStatusCode.INPROGRESS);
    }

    @Test
    void findById() throws Exception {
        //given
        long transportId = 1L;
        long orderId = 1L;

        //when
        Optional<Transport> optionalTransport = repository.findById(transportId);

        //then
        assertThat(optionalTransport).isPresent();
        assertThat(optionalTransport.get().getOrderId()).isEqualTo(orderId);
        assertThat(optionalTransport.get().getTrackingNo()).isEqualTo(trackingNo);
        assertThat(optionalTransport.get().getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(optionalTransport.get().getTransportStatusCode()).isEqualTo(TransportStatusCode.INPROGRESS);
    }

    @Test
    void findByOrderId() throws Exception {
        //given
        long orderId = 1L;
//        entityManager.persist(transport);

        //when
        Optional<Transport> optionalTransport = repository.findByOrderId(orderId);

        //then
        assertThat(optionalTransport).isPresent();
        assertThat(optionalTransport.get().getOrderId()).isEqualTo(orderId);
        assertThat(optionalTransport.get().getTrackingNo()).isEqualTo(trackingNo);
        assertThat(optionalTransport.get().getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(optionalTransport.get().getTransportStatusCode()).isEqualTo(TransportStatusCode.INPROGRESS);
    }
    
    @Test
    void getLatestTransportBy_failedNotFound() throws Exception {
        // given
        long orderId = 11L;

        //when
        Optional<Transport> latestTransport = repository.getLatestTransportByOrderId(orderId);

        //then
        assertThat(latestTransport).isEmpty();
    }

    @Test
    void getLatestTransportBy() throws Exception {
        //given
        long orderId = 1L;
//        entityManager.persist(transport);

        //when
        Optional<Transport> latestTransport = repository.getLatestTransportByOrderId(orderId);

        //then
        assertThat(latestTransport).isPresent();
    }
}
