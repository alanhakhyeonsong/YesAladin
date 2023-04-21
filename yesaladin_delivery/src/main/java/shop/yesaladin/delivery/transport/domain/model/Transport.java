package shop.yesaladin.delivery.transport.domain.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.yesaladin.delivery.transport.exception.TransportAlreadyCompletedException;

/**
 * 배송의 엔티티 클래스 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "transports")
@Entity
public class Transport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reception_datetime", nullable = false)
    private LocalDate receptionDatetime;

    @Column(name = "completion_datetime")
    private LocalDate completionDatetime;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "tracking_no", nullable = false)
    private String trackingNo;

    @Column(name = "transport_status_code_id")
    @Convert(converter = TransportStatusCodeConverter.class)
    private TransportStatusCode transportStatusCode;

    /**
     * 배송이 완료 되었을 때 상태를 변경 하기 위한 기능 입니다.
     *
     * @author 송학현
     * @since 1.0
     */
    public void completeTransport() {
        if (this.transportStatusCode.equals(TransportStatusCode.COMPLETE)) {
            throw new TransportAlreadyCompletedException(this.id);
        }
        this.completionDatetime = LocalDate.now();
        this.transportStatusCode = TransportStatusCode.COMPLETE;
    }
}
