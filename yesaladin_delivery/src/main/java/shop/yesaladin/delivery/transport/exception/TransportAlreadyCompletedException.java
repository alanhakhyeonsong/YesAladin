package shop.yesaladin.delivery.transport.exception;

/**
 * 배송 상태가 이미 완료된 경우 발생하는 예외 입니다.
 *
 * @author 송학현
 * @since 1.0
 */
public class TransportAlreadyCompletedException extends RuntimeException {

    private static final String MESSAGE = "Transport already completed, order id: ";

    public TransportAlreadyCompletedException(Long id) {
        super(MESSAGE + id);
    }
}
