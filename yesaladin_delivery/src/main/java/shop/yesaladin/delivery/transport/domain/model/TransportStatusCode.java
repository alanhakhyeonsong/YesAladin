package shop.yesaladin.delivery.transport.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 배송 상태 코드에 대한 클래스 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum TransportStatusCode {
    INPROGRESS(1), COMPLETE(2);

    private final int statusCode;
}
