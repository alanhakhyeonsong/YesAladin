package shop.yesaladin.delivery.transport.domain.model;

import java.util.Arrays;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 배송의 배송 상태 코드를 변환 하기 위한 컨버터 클래스 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Converter(autoApply = true)
public class TransportStatusCodeConverter implements
        AttributeConverter<TransportStatusCode, Integer> {

    /**
     * 배송상태코드를 Integer 타입으로 변환합니다.
     *
     * @param transportStatusCode 배송상태코드
     * @return 배송상태코드의 Id
     * @author 송학현
     * @since 1.0
     */
    @Override
    public Integer convertToDatabaseColumn(TransportStatusCode transportStatusCode) {
        return transportStatusCode.getStatusCode();
    }

    /**
     * pk를 배송상태코드로 변환합니다.
     *
     * @param integer 배송상태코드의 Id
     * @return 배송상태코드
     * @author 송학현
     * @since 1.0
     */
    @Override
    public TransportStatusCode convertToEntityAttribute(Integer integer) {
        return Arrays.stream(TransportStatusCode.values())
                .filter(code -> integer.equals(code.getStatusCode()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
