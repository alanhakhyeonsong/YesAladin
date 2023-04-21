package shop.yesaladin.delivery.transport.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import shop.yesaladin.delivery.transport.domain.model.Transport;
import shop.yesaladin.delivery.transport.domain.model.TransportStatusCode;
import shop.yesaladin.delivery.transport.domain.repository.TransportRepository;
import shop.yesaladin.delivery.transport.dto.TransportCompleteEventDto;
import shop.yesaladin.delivery.transport.dto.TransportResponseDto;
import shop.yesaladin.delivery.transport.exception.TransportNotFoundByOrderIdException;
import shop.yesaladin.delivery.transport.exception.TransportNotFoundException;
import shop.yesaladin.delivery.transport.service.inter.TransportService;

/**
 * 배송 도메인의 Service 구현체 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TransportServiceImpl implements TransportService {

    private final TransportRepository transportRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TransportResponseDto registerTransport(Long orderId) {
        String trackingNo = UUID.randomUUID().toString();

        Transport transport = Transport.builder()
                .receptionDatetime(LocalDate.now())
                .orderId(orderId)
                .trackingNo(trackingNo)
                .transportStatusCode(TransportStatusCode.INPROGRESS)
                .build();
        Transport savedTransport = transportRepository.save(transport);

        applicationEventPublisher.publishEvent(new TransportCompleteEventDto(transport.getOrderId()));

        return TransportResponseDto.fromEntity(savedTransport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransportResponseDto completeTransport(Long orderId) {
        Transport transport = getTransport(orderId);
        transport.completeTransport();
        transportRepository.save(transport);
        return TransportResponseDto.fromEntity(transport);
    }

    private Transport getTransport(Long orderId) {
        return transportRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TransportNotFoundByOrderIdException(orderId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransportResponseDto> findAll() {
        return transportRepository.findAll().stream()
                .map(TransportResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TransportResponseDto findById(Long transportId) {
        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new TransportNotFoundException(String.valueOf(transportId)));

        return TransportResponseDto.fromEntity(transport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TransportResponseDto findByOrderId(Long orderId) {
        Transport transport = transportRepository.findByOrderId(orderId)
                .orElseThrow(() -> new TransportNotFoundByOrderIdException(orderId));

        return TransportResponseDto.fromEntity(transport);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Transport getLatestTransport(Long orderId) {
        return transportRepository.getLatestTransportByOrderId(orderId).orElse(null);
    }
}
