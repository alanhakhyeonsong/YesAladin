package shop.yesaladin.delivery.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shop.yesaladin.delivery.common.dto.ErrorResponseDto;
import shop.yesaladin.delivery.transport.exception.TransportAlreadyCompletedException;
import shop.yesaladin.delivery.transport.exception.TransportNotFoundByOrderIdException;
import shop.yesaladin.delivery.transport.exception.TransportNotFoundException;

/**
 * 예외 처리를 위한 RestController Advice 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler({TransportNotFoundException.class, TransportNotFoundByOrderIdException.class})
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(Exception ex) {
        log.error("[NOT_FOUND] handleNotFoundException", ex);
        ErrorResponseDto error = new ErrorResponseDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TransportAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadyCompletedException(Exception ex) {
        log.error("[CONFLICT] handleAlreadyCompletedException", ex);
        ErrorResponseDto error = new ErrorResponseDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
        log.error("[INTERNAL_SERVER_ERROR] handleException", ex);
        ErrorResponseDto error = new ErrorResponseDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
