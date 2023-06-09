package shop.yesaladin.shop.order.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.yesaladin.common.code.ErrorCode;
import shop.yesaladin.common.exception.ClientException;
import shop.yesaladin.shop.coupon.service.inter.QueryMemberCouponService;
import shop.yesaladin.shop.coupon.service.inter.UseCouponService;
import shop.yesaladin.shop.member.service.inter.QueryMemberAddressService;
import shop.yesaladin.shop.member.service.inter.QueryMemberService;
import shop.yesaladin.shop.order.domain.model.MemberOrder;
import shop.yesaladin.shop.order.domain.model.NonMemberOrder;
import shop.yesaladin.shop.order.domain.model.Order;
import shop.yesaladin.shop.order.domain.model.OrderProduct;
import shop.yesaladin.shop.order.domain.model.OrderStatusChangeLog;
import shop.yesaladin.shop.order.domain.model.OrderStatusCode;
import shop.yesaladin.shop.order.domain.model.Subscribe;
import shop.yesaladin.shop.order.domain.repository.CommandOrderProductRepository;
import shop.yesaladin.shop.order.domain.repository.CommandOrderRepository;
import shop.yesaladin.shop.order.domain.repository.CommandOrderStatusChangeLogRepository;
import shop.yesaladin.shop.order.domain.repository.QueryOrderRepository;
import shop.yesaladin.shop.order.dto.OrderCreateRequestDto;
import shop.yesaladin.shop.order.dto.OrderCreateResponseDto;
import shop.yesaladin.shop.order.dto.OrderMemberCreateRequestDto;
import shop.yesaladin.shop.order.dto.OrderNonMemberCreateRequestDto;
import shop.yesaladin.shop.order.dto.OrderSubscribeCreateRequestDto;
import shop.yesaladin.shop.order.dto.OrderUpdateResponseDto;
import shop.yesaladin.shop.order.service.inter.CommandOrderCouponService;
import shop.yesaladin.shop.order.service.inter.CommandOrderService;
import shop.yesaladin.shop.point.domain.model.PointReasonCode;
import shop.yesaladin.shop.point.dto.PointHistoryRequestDto;
import shop.yesaladin.shop.point.service.inter.CommandPointHistoryService;
import shop.yesaladin.shop.product.domain.model.Product;
import shop.yesaladin.shop.product.dto.SubscribeProductOrderResponseDto;
import shop.yesaladin.shop.product.service.inter.CommandProductService;
import shop.yesaladin.shop.product.service.inter.QueryProductService;

/**
 * 주문 생성/수정/삭제와 관련한 서비스 구현체 입니다.
 *
 * @author 최예린
 * @since 1.0
 */
@RequiredArgsConstructor
@Service
public class CommandOrderServiceImpl implements CommandOrderService {

    private final CommandOrderRepository<NonMemberOrder> nonMemberOrderCommandOrderRepository;
    private final CommandOrderRepository<MemberOrder> memberOrderCommandOrderRepository;
    private final CommandOrderRepository<Subscribe> subscribeCommandOrderRepository;
    private final QueryOrderRepository queryOrderRepository;

    private final CommandOrderStatusChangeLogRepository commandOrderStatusChangeLogRepository;
    private final CommandOrderProductRepository commandOrderProductRepository;
    private final CommandPointHistoryService commandPointHistoryService;
    private final CommandOrderCouponService commandOrderCouponService;
    private final CommandProductService commandProductService;
    private final QueryMemberAddressService queryMemberAddressService;
    private final QueryMemberCouponService queryMemberCouponService;
    private final QueryProductService queryProductService;
    private final QueryMemberService queryMemberService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final UseCouponService useCouponService;
    private final Clock clock;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderCreateResponseDto createNonMemberOrders(
            OrderNonMemberCreateRequestDto request
    ) {
        LocalDateTime orderDateTime = LocalDateTime.now(clock);
        Map<String, Product> products = commandProductService.orderProducts(request.getOrderProducts());

        Order savedOrder = createNonMemberOrder(request, products, orderDateTime);

        createOrderProduct(request, products, savedOrder);
        createOrderStatusChangeLog(orderDateTime, savedOrder);

        return OrderCreateResponseDto.fromEntity(savedOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderCreateResponseDto createMemberOrders(
            OrderMemberCreateRequestDto request,
            String loginId,
            String type
    ) {
        if (!request.getOrderCoupons().isEmpty()) {
            queryMemberCouponService.getValidMemberCouponSummaryListByCouponCodes(
                    loginId,
                    request.getOrderCoupons()
            );
        }


        LocalDateTime orderDateTime = LocalDateTime.now(clock);
        Map<String, Product> products = commandProductService.orderProducts(request.getOrderProducts());

        Order savedOrder = createMemberOrder(request, orderDateTime, products, loginId);

        if (!request.getOrderCoupons().isEmpty()) {
            try {
                requestUseCoupon(request, loginId, savedOrder);

                createOrderCoupon(request, savedOrder);
            } catch (Exception e) {
                useCouponService.cancelCouponUse(request.getOrderCoupons());
            }
        }

        createOrderProduct(request, products, savedOrder);

        createUsePointHistory(request.getUsePoint(), loginId);

        createOrderStatusChangeLog(orderDateTime, savedOrder);


        deleteOrderProductInCart(loginId, type, products);

        return OrderCreateResponseDto.fromEntity(savedOrder);
    }

    /**
     * 장바구니에서 주문한 상품이라면 Redis 에서 삭제합니다.
     *
     * @param loginId  회원 아이디
     * @param type     장바구니에서 주문했다면 null, 바로 주문이라면 "one"
     * @param products 주문한 상품 Map
     * @author 이수정
     * @since 1.0
     */
    private void deleteOrderProductInCart(
            String loginId,
            String type,
            Map<String, Product> products
    ) {
        if (Objects.isNull(type)) {
            products.keySet().forEach(key ->
                    redisTemplate.opsForHash().delete(loginId, products.get(key).getId().toString())
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderCreateResponseDto createSubscribeOrders(
            OrderSubscribeCreateRequestDto request,
            String loginId
    ) {
        queryMemberCouponService.getValidMemberCouponSummaryListByCouponCodes(
                loginId,
                request.getOrderCoupons()
        );

        LocalDateTime orderDateTime = LocalDateTime.now(clock);

        Order savedOrder = creatSubscribe(request, orderDateTime, loginId);

        createUsePointHistory(request.getUsePoint(), loginId);

        createOrderStatusChangeLog(orderDateTime, savedOrder);

        requestUseCoupon(request, loginId, savedOrder);

        return OrderCreateResponseDto.fromEntity(savedOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderUpdateResponseDto hideOnOrder(String loginId, Long orderId, boolean hide) {
        MemberOrder order = tryGetMemberOrderById(orderId);

        checkUserIsOwnerOfOrder(loginId, orderId, order);

        if (hide) {
            order.hiddenOn();
        } else {
            order.hiddenOff();
        }

        return OrderUpdateResponseDto.fromEntity(order);
    }

    private MemberOrder tryGetMemberOrderById(Long orderId) {
        return (MemberOrder) queryOrderRepository.findById(orderId)
                .orElseThrow(() -> new ClientException(
                        ErrorCode.ORDER_NOT_FOUND,
                        "Order not found with id : " + orderId
                ));
    }

    private void checkUserIsOwnerOfOrder(String loginId, Long orderId, MemberOrder order) {
        if (!Objects.equals(order.getMember().getLoginId(), (loginId))) {
            throw new ClientException(
                    ErrorCode.ORDER_BAD_REQUEST,
                    loginId + " is not a owner of order(" + orderId + ")."
            );
        }
    }

    private Order createNonMemberOrder(
            OrderNonMemberCreateRequestDto request,
            Map<String, Product> products,
            LocalDateTime orderDateTime
    ) {
        NonMemberOrder nonMemberOrder = request.toEntity(
                generateOrderName(List.copyOf(products.values())),
                generateOrderNumber(orderDateTime),
                orderDateTime
        );

        return nonMemberOrderCommandOrderRepository.save(nonMemberOrder);
    }

    private Order createMemberOrder(
            OrderMemberCreateRequestDto request,
            LocalDateTime orderDateTime,
            Map<String, Product> products,
            String loginId
    ) {
        MemberOrder memberOrder = request.toEntity(
                generateOrderName(List.copyOf(products.values())),
                generateOrderNumber(orderDateTime),
                orderDateTime,
                queryMemberService.findByLoginId(loginId),
                queryMemberAddressService.findById(request.getOrdererAddressId())
        );

        return memberOrderCommandOrderRepository.save(memberOrder);
    }

    private Order creatSubscribe(
            OrderSubscribeCreateRequestDto request,
            LocalDateTime orderDateTime,
            String loginId
    ) {
        SubscribeProductOrderResponseDto subscribeProductOrder = queryProductService.getIssnByOrderProduct(
                request.getOrderProducts().get(0));

        Subscribe subscribe = request.toEntity(
                subscribeProductOrder.getTitle()
                        .replace("((\\d|1[012])(월호|월))|(no.*(\\d|1[012]))", ""),
                generateOrderNumber(orderDateTime),
                orderDateTime,
                queryMemberService.findByLoginId(loginId),
                queryMemberAddressService.findById(request.getOrdererAddressId()),
                generateNextRenewalDate(request.getExpectedDay(), orderDateTime),
                subscribeProductOrder.getSubscribeProduct()
        );
        return subscribeCommandOrderRepository.save(subscribe);
    }

    private void createOrderProduct(
            OrderCreateRequestDto request,
            Map<String, Product> products,
            Order order
    ) {
        request.getOrderProducts()
                .stream()
                .map(orderProduct -> OrderProduct.builder()
                        .quantity(orderProduct.getQuantity())
                        .product(products.get(orderProduct.getIsbn()))
                        .order(order)
                        .build())
                .forEach(commandOrderProductRepository::save);
    }

    private void createUsePointHistory(long usePoint, String loginId) {
        if (usePoint != 0) {
            commandPointHistoryService.use(new PointHistoryRequestDto(
                    loginId,
                    usePoint,
                    PointReasonCode.USE_ORDER
            ));
        }
    }

    private void createOrderCoupon(OrderMemberCreateRequestDto request, Order savedOrder) {
        if (!request.getOrderCoupons().isEmpty()) {
            commandOrderCouponService.createOrderCoupons(
                    savedOrder.getId(),
                    request.getOrderCoupons()
            );
        }
    }

    private void createOrderStatusChangeLog(LocalDateTime orderDateTime, Order savedOrder) {
        OrderStatusChangeLog orderStatusChangeLog = OrderStatusChangeLog.create(
                savedOrder,
                orderDateTime,
                OrderStatusCode.ORDER
        );
        commandOrderStatusChangeLogRepository.save(orderStatusChangeLog);
    }

    private void requestUseCoupon(
            OrderMemberCreateRequestDto request,
            String loginId,
            Order savedOrder
    ) {
        String requestId = useCouponService.sendCouponUseRequest(loginId, request.getOrderCoupons())
                .getRequestId();
        putRequestIdForCouponsToRedis(savedOrder.getOrderNumber(), requestId);
    }

    private void putRequestIdForCouponsToRedis(String orderNumber, String requestId) {
        redisTemplate.opsForHash().put("USE_COUPON_REQ_ID", orderNumber, requestId);
    }

    private LocalDate generateNextRenewalDate(
            Integer expectedDay,
            LocalDateTime orderDateTime
    ) {
        LocalDate nextRenewalDate = orderDateTime.withDayOfMonth(expectedDay)
                .toLocalDate();
        if (nextRenewalDate.isAfter(ChronoLocalDate.from(orderDateTime))) {
            nextRenewalDate = nextRenewalDate.plusMonths(1);
        }
        return nextRenewalDate;
    }

    private String generateOrderName(List<Product> products) {
        String title = products.get(0).getTitle();
        if (products.size() == 1) {
            return getValidOrderName(title);
        }
        return getValidOrderName(title) + "외 " + (products.size() - 1) + "권";
    }

    private String getValidOrderName(String title) {
        if (title.length() > 90) {
            return title.substring(0, 87) + "...";
        }
        return title;
    }

    private String generateOrderNumber(LocalDateTime orderDateTime) {
        return orderDateTime.toLocalDate().toString().replace("-", "") + "-" + UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }
}
