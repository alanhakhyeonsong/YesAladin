package shop.yesaladin.delivery.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 메세지 반환 dto 클래스입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public class ErrorResponseDto {

    private final String message;
}
