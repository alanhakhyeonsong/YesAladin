package shop.yesaladin.delivery.transport.service.inter;

import java.util.List;
import shop.yesaladin.delivery.transport.domain.model.Transport;
import shop.yesaladin.delivery.transport.dto.TransportResponseDto;

/**
 * 배송 도메인의 Service Interface 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
public interface TransportService {

    /**
     * 배송 등록을 위한 기능 입니다.
     *
     * @param orderId 주문의 PK 입니다.
     * @return 배송 등록 시 해당 배송에 대한 정보를 담은 DTO 입니다.
     * @author 송학현
     * @since 1.0
     */
    TransportResponseDto registerTransport(Long orderId);

    /**
     * 배송 상태를 완료로 변경 하기 위한 기능 입니다.
     *
     * @param orderId 배송 entity에 들어 있는 주문의 PK 입니다.
     * @return 배송 상태 변경 시 해당 배송에 대한 정보를 담은 DTO 입니다.
     * @author 송학현
     * @since 1.0
     */
    TransportResponseDto completeTransport(Long orderId);

    /**
     * 전체 배송 조회를 위한 기능 입니다.
     *
     * @return 배송 정보를 담은 DTO의 List를 반환 합니다.
     * @author 송학현
     * @since 1.0
     */
    List<TransportResponseDto> findAll();

    /**
     * 배송 단건 조회를 위한 기능 입니다.
     *
     * @param transportId 조회 대상 배송의 PK 입니다.
     * @return 배송에 대한 정보를 담은 DTO 입니다.
     * @author 송학현
     * @since 1.0
     */
    TransportResponseDto findById(Long transportId);

    /**
     * 주문 번호 기준 배송 단건 조회를 위한 기능 입니다.
     *
     * @param orderId 조회 대상 배송에 들어 있는 주문 번호 입니다.
     * @return 배송에 대한 정보를 담은 DTO 입니다.
     * @author 송학현
     * @since 1.0
     */
    TransportResponseDto findByOrderId(Long orderId);

    /**
     * 완료 상태의 배송 중 가장 최신의 것에 담긴 orderId를 조회 하기 위한 기능 입니다.
     *
     * @return 완료 상태의 배송 중 가장 최신의 것에 담긴 orderId
     * @author 송학현
     * @since 1.0
     */
    Transport getLatestTransport(Long orderId);
}
