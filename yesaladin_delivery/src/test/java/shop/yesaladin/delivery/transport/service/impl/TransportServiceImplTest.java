package shop.yesaladin.delivery.transport.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import shop.yesaladin.delivery.transport.domain.model.Transport;
import shop.yesaladin.delivery.transport.domain.model.TransportStatusCode;
import shop.yesaladin.delivery.transport.domain.repository.TransportRepository;
import shop.yesaladin.delivery.transport.dto.TransportResponseDto;
import shop.yesaladin.delivery.transport.dummy.DummyTransport;
import shop.yesaladin.delivery.transport.exception.TransportAlreadyCompletedException;
import shop.yesaladin.delivery.transport.exception.TransportNotFoundByOrderIdException;
import shop.yesaladin.delivery.transport.exception.TransportNotFoundException;
import shop.yesaladin.delivery.transport.service.inter.TransportService;

class TransportServiceImplTest {

    private TransportService service;
    private TransportRepository repository;
    private ApplicationEventPublisher applicationEventPublisher;

    private final Clock clock = Clock.fixed(
            Instant.parse("2023-01-19T00:00:00.000Z"),
            ZoneId.of("UTC")
    );

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(TransportRepository.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        service = new TransportServiceImpl(repository, applicationEventPublisher);
    }

    @Test
    @DisplayName("배송 등록 성공")
    void registerTransport() throws Exception {
        //given
        long transportId = 1L;
        long orderId = 1L;
        String trackingNo = UUID.randomUUID().toString();
        Transport savedTransport = DummyTransport.dummyWithId(clock, trackingNo);

        Mockito.when(repository.save(any())).thenReturn(savedTransport);

        //when
        TransportResponseDto response = service.registerTransport(orderId);

        //then
        assertThat(response.getId()).isEqualTo(transportId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getTrackingNo()).isEqualTo(trackingNo);
        assertThat(response.getTransportStatus()).isEqualTo(TransportStatusCode.INPROGRESS.name());
        assertThat(response.getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(response.getCompletionDatetime()).isNull();

        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("해당 배송이 없는 경우 예외가 발생 한다.")
    void completeTransport_fail_notFoundTransport() throws Exception {
        //given
        long orderId = 1L;

        Mockito.when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());

        //when, then
        assertThatThrownBy(() -> service.completeTransport(orderId))
                .isInstanceOf(TransportNotFoundByOrderIdException.class)
                .hasMessageContainingAll("Transport not founded, order id: " + orderId);

        verify(repository, times(1)).findByOrderId(orderId);
    }

    @Test
    @DisplayName("해당 배송이 이미 완료된 경우 예외가 발생 한다.")
    void completeTransport_fail_alreadyCompleted() throws Exception {
        //given
        long orderId = 1L;
        String trackingNo = UUID.randomUUID().toString();
        Transport transport = DummyTransport.dummyAlreadyComplete(clock, trackingNo);

        Mockito.when(repository.findByOrderId(orderId)).thenReturn(Optional.of(transport));

        //when, then
        assertThatThrownBy(() -> service.completeTransport(orderId))
                .isInstanceOf(TransportAlreadyCompletedException.class)
                .hasMessageContainingAll("Transport already completed, order id: " + orderId);

        verify(repository, times(1)).findByOrderId(orderId);
    }

    @Test
    @DisplayName("배송 완료 성공")
    void completeTransport() throws Exception {
        //given
        long transportId = 1L;
        long orderId = 1L;
        String trackingNo = UUID.randomUUID().toString();
        Transport transport = DummyTransport.dummyWithId(clock, trackingNo);

        Mockito.when(repository.findByOrderId(orderId)).thenReturn(Optional.of(transport));

        //when
        TransportResponseDto response = service.completeTransport(orderId);

        //then
        assertThat(response.getId()).isEqualTo(transportId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getTrackingNo()).isEqualTo(trackingNo);
        assertThat(response.getTransportStatus()).isEqualTo(TransportStatusCode.COMPLETE.name());
        assertThat(response.getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(response.getCompletionDatetime()).isEqualTo(LocalDate.now());

        verify(repository, times(1)).findByOrderId(orderId);
    }

    @Test
    @DisplayName("전체 조회 시 배송이 없는 경우 빈 리스트를 반환한다.")
    void findAll_returnEmptyList() throws Exception {
        //when
        List<TransportResponseDto> transports = service.findAll();

        //then
        assertThat(transports).isEmpty();

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("전체 조회 성공")
    void findAll() throws Exception {
        //given
        long transportId = 1L;
        long orderId = 1L;
        String trackingNo = UUID.randomUUID().toString();
        Transport transport = DummyTransport.dummyWithId(clock, trackingNo);

        Mockito.when(repository.findAll()).thenReturn(List.of(transport));

        //when
        List<TransportResponseDto> transports = service.findAll();

        //then
        int expectedSize = 1;
        assertThat(transports).hasSize(expectedSize);

        int index = 0;
        TransportResponseDto actual = transports.get(index);
        assertThat(actual.getId()).isEqualTo(transportId);
        assertThat(actual.getOrderId()).isEqualTo(orderId);
        assertThat(actual.getTrackingNo()).isEqualTo(trackingNo);
        assertThat(actual.getTransportStatus()).isEqualTo(TransportStatusCode.INPROGRESS.name());
        assertThat(actual.getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(actual.getCompletionDatetime()).isNull();

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("존재 하지 않는 배송의 경우 조회 불가")
    void findById_fail_whenNotExist() throws Exception {
        //given
        long transportId = 1L;

        Mockito.when(repository.findById(transportId)).thenReturn(Optional.empty());

        //when, then
        assertThatThrownBy(() -> service.findById(transportId))
                .isInstanceOf(TransportNotFoundException.class)
                .hasMessageContainingAll("Transport not founded, transport id: " + transportId);

        verify(repository, times(1)).findById(transportId);
    }

    @Test
    @DisplayName("조회 성공")
    void findById() throws Exception {
        //given
        long transportId = 1L;
        long orderId = 1L;
        String trackingNo = UUID.randomUUID().toString();
        Transport transport = DummyTransport.dummyWithId(clock, trackingNo);

        Mockito.when(repository.findById(transportId)).thenReturn(Optional.of(transport));

        //when
        TransportResponseDto response = service.findById(transportId);

        //then
        assertThat(response.getId()).isEqualTo(transportId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getTrackingNo()).isEqualTo(trackingNo);
        assertThat(response.getTransportStatus()).isEqualTo(TransportStatusCode.INPROGRESS.name());
        assertThat(response.getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(response.getCompletionDatetime()).isNull();

        verify(repository, times(1)).findById(transportId);
    }

    @Test
    @DisplayName("존재 하지 않는 배송의 경우 주문 번호 기준 조회 불가")
    void findByOrderId_fail_whenNotExist() throws Exception {
        //given
        long orderId = 1L;

        Mockito.when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());

        //when, then
        assertThatThrownBy(() -> service.findByOrderId(orderId))
                .isInstanceOf(TransportNotFoundByOrderIdException.class)
                .hasMessageContainingAll("Transport not founded, order id: " + orderId);

        verify(repository, times(1)).findByOrderId(orderId);
    }

    @Test
    @DisplayName("주문 번호 기준 배송 조회 성공")
    void findByOrderId() throws Exception {
        //given
        long transportId = 1L;
        long orderId = 1L;
        String trackingNo = UUID.randomUUID().toString();
        Transport transport = DummyTransport.dummyWithId(clock, trackingNo);

        Mockito.when(repository.findByOrderId(orderId)).thenReturn(Optional.of(transport));

        //when
        TransportResponseDto response = service.findByOrderId(orderId);

        //then
        assertThat(response.getId()).isEqualTo(transportId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getTrackingNo()).isEqualTo(trackingNo);
        assertThat(response.getTransportStatus()).isEqualTo(TransportStatusCode.INPROGRESS.name());
        assertThat(response.getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(response.getCompletionDatetime()).isNull();

        verify(repository, times(1)).findByOrderId(orderId);
    }

    @Test
    @DisplayName("가장 최근 배송 조회 시 존재 하지 않는 경우 null이 반환 된다.")
    void getLatestTransport_failed_notExist() throws Exception {
        //given
        long orderId = 1L;
        Mockito.when(repository.getLatestTransportByOrderId(anyLong())).thenReturn(Optional.empty());

        //when
        Transport latestTransport = service.getLatestTransport(orderId);

        //then
        assertThat(latestTransport).isNull();
    }

    @Test
    @DisplayName("가장 최근 배송 조회 성공")
    void getLatestTransport() throws Exception {
        //given
        long transportId = 1L;
        long orderId = 1L;
        String trackingNo = UUID.randomUUID().toString();
        Transport transport = DummyTransport.dummyAlreadyComplete(clock, trackingNo);

        Mockito.when(repository.getLatestTransportByOrderId(anyLong())).thenReturn(Optional.of(transport));

        //when
        Transport latestTransport = service.getLatestTransport(orderId);

        //then
        assertThat(latestTransport).isNotNull();
        assertThat(latestTransport.getId()).isEqualTo(transportId);
        assertThat(latestTransport.getOrderId()).isEqualTo(orderId);
        assertThat(latestTransport.getTrackingNo()).isEqualTo(trackingNo);
        assertThat(latestTransport.getTransportStatusCode()).isEqualTo(TransportStatusCode.COMPLETE);
        assertThat(latestTransport.getReceptionDatetime()).isEqualTo(LocalDate.now(clock));
        assertThat(latestTransport.getCompletionDatetime()).isEqualTo(LocalDate.now(clock));
    }
}
