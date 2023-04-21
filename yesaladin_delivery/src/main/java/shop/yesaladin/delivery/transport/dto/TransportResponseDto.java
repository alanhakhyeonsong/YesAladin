package shop.yesaladin.delivery.transport.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import shop.yesaladin.delivery.transport.domain.model.Transport;

/**
 * 배송 정보를 담은 DTO 클래스 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TransportResponseDto {

    private Long id;
    private LocalDate receptionDatetime;
    private LocalDate completionDatetime;
    private Long orderId;
    private String trackingNo;
    private String transportStatus;

    public static TransportResponseDto fromEntity(Transport transport) {
        return new TransportResponseDto(
                transport.getId(),
                transport.getReceptionDatetime(),
                transport.getCompletionDatetime(),
                transport.getOrderId(),
                transport.getTrackingNo(),
                transport.getTransportStatusCode().name()
        );
    }
}
