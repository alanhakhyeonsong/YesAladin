package shop.yesaladin.delivery.transport.service.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import shop.yesaladin.delivery.transport.dto.TransportCompleteEventDto;
import shop.yesaladin.delivery.transport.dto.TransportResponseDto;
import shop.yesaladin.delivery.transport.service.inter.TransportService;

/**
 * 배송 등록 API 호출로 배송 entity가 commit 되는 시점에 배송 상태를 변경 하고 완료날짜를 지정하기 위한 event listener 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TransportCompleteEventListener {

    public static Queue<Long> orderIdQueue = new ConcurrentLinkedQueue<>();
    private final TransportService transportService;

    /**
     * 배송 등록 후 해당 배송 entity를 완료 상태로 변경하기 위한 이벤트 처리 메소드 입니다.
     *
     * @param dto 배송 entity를 완료 상태로 변경하기 위한 이벤트 메시지 클래스 입니다.
     * @author 송학현
     * @since 1.0
     */
    @TransactionalEventListener
    public void handleTransportStatus(TransportCompleteEventDto dto) {
        Long orderId = dto.getOrderId();
        TransportResponseDto transport = transportService.findByOrderId(orderId);
        transportService.completeTransport(transport.getOrderId());
        orderIdQueue.add(orderId);
        log.info("handleTransportStatus success");
    }
}
