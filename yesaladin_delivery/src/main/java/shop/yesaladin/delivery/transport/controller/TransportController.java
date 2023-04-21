package shop.yesaladin.delivery.transport.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import shop.yesaladin.common.dto.ResponseDto;
import shop.yesaladin.delivery.transport.dto.TransportResponseDto;
import shop.yesaladin.delivery.transport.service.inter.TransportService;

/**
 * 배송 등록 및 조회를 위한 RestController 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/delivery")
public class TransportController {

    private final TransportService transportService;

    /**
     * 배송 등록을 위한 POST API 입니다. POST: /api/delivery/{orderId}
     *
     * @param orderId 주문 번호(주문의 PK) 입니다.
     * @return 등록 완료 된 배송 정보를 담은 DTO를 반환합니다.
     * @author 송학현
     * @since 1.0
     */
    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<TransportResponseDto> register(@PathVariable Long orderId) {
        TransportResponseDto response = transportService.registerTransport(orderId);
        return ResponseDto.<TransportResponseDto>builder()
                .status(HttpStatus.CREATED)
                .success(true)
                .data(response)
                .build();
    }

    /**
     * 배송 전체 조회를 위한 GET API 입니다. GET: /api/delivery
     *
     * @return 등록된 배송의 전체 리스트를 반환 합니다.
     * @author 송학현
     * @since 1.0
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<List<TransportResponseDto>> findAll() {
        List<TransportResponseDto> response = transportService.findAll();
        return ResponseDto.<List<TransportResponseDto>>builder()
                .status(HttpStatus.OK)
                .success(true)
                .data(response)
                .build();
    }

    /**
     * 배송 단건 조회를 위한 GET API 입니다. GET: /api/delivery/{transportId}
     *
     * @param transportId 배송의 PK 입니다.
     * @return 배송 단건 조회 결과 입니다.
     * @author 송학현
     * @since 1.0
     */
    @GetMapping("/{transportId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<TransportResponseDto> findByDeliveryId(@PathVariable Long transportId) {
        TransportResponseDto response = transportService.findById(transportId);
        return ResponseDto.<TransportResponseDto>builder()
                .status(HttpStatus.OK)
                .success(true)
                .data(response)
                .build();
    }
}
