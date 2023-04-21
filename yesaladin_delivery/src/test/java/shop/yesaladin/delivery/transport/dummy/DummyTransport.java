package shop.yesaladin.delivery.transport.dummy;

import java.time.Clock;
import java.time.LocalDate;
import shop.yesaladin.delivery.transport.domain.model.Transport;
import shop.yesaladin.delivery.transport.domain.model.TransportStatusCode;

public class DummyTransport {

    public static Transport dummy(Clock clock, String trackingNo) {
        long orderId = 1L;

        return Transport.builder()
                .receptionDatetime(LocalDate.now(clock))
                .orderId(orderId)
                .trackingNo(trackingNo)
                .transportStatusCode(TransportStatusCode.INPROGRESS)
                .build();
    }

    public static Transport dummyWithId(Clock clock, String trackingNo) {
        long transportId = 1L;
        long orderId = 1L;

        return Transport.builder()
                .id(transportId)
                .receptionDatetime(LocalDate.now(clock))
                .orderId(orderId)
                .trackingNo(trackingNo)
                .transportStatusCode(TransportStatusCode.INPROGRESS)
                .build();
    }

    public static Transport dummyAlreadyComplete(Clock clock, String trackingNo) {
        long transportId = 1L;
        long orderId = 1L;

        return Transport.builder()
                .id(transportId)
                .receptionDatetime(LocalDate.now(clock))
                .orderId(orderId)
                .trackingNo(trackingNo)
                .transportStatusCode(TransportStatusCode.COMPLETE)
                .completionDatetime(LocalDate.now(clock))
                .build();
    }
}
