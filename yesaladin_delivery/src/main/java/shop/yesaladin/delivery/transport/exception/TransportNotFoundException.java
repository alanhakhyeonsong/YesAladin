package shop.yesaladin.delivery.transport.exception;

/**
 * 배송 정보가 존재 하지 않는 경우 발생 하는 예외
 *
 * @author 송학현
 * @since 1.0
 */
public class TransportNotFoundException extends RuntimeException {

    private static final String MESSAGE = "Transport not founded, transport id: ";

    public TransportNotFoundException(String id) {
        super(MESSAGE + id);
    }
}
