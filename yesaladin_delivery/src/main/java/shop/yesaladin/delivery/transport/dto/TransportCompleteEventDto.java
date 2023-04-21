package shop.yesaladin.delivery.transport.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 배송을 완료 상태로 변경 하기 위한 event dto
 *
 * @author 송학현
 * @since 1.0
 */
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TransportCompleteEventDto {

    private Long orderId;
}
