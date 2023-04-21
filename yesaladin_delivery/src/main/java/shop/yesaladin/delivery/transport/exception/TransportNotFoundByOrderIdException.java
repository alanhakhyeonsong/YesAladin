package shop.yesaladin.delivery.transport.exception;

/**
 * 배송 정보가 존재 하지 않는 경우 발생 하는 예외
 *
 * @author 송학현
 * @since 1.0
 */
public class TransportNotFoundByOrderIdException extends RuntimeException {

    private static final String MESSAGE = "Transport not founded, order id: ";

    public TransportNotFoundByOrderIdException(Long id) {
        super(MESSAGE + id);
    }
}
