package shop.yesaladin.shop.order.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentRequest;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.yesaladin.common.code.ErrorCode;
import shop.yesaladin.common.exception.ClientException;
import shop.yesaladin.shop.order.dto.OrderCreateResponseDto;
import shop.yesaladin.shop.order.dto.OrderMemberCreateRequestDto;
import shop.yesaladin.shop.order.dto.OrderNonMemberCreateRequestDto;
import shop.yesaladin.shop.order.dto.OrderSubscribeCreateRequestDto;
import shop.yesaladin.shop.order.service.inter.CommandOrderService;
import shop.yesaladin.shop.product.dto.ProductOrderRequestDto;

@AutoConfigureRestDocs
@WebMvcTest(CommandOrderController.class)
class CommandOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommandOrderService commandOrderService;

    @Autowired
    ObjectMapper objectMapper;

    String ordererName = "김몽머";
    String ordererPhoneNumber = "01012341234";
    String ordererAddress = "서울특별시 구로구 디지털로26길 72 (구로동, NHN KCP)";
    LocalDate expectedShippingDate = LocalDate.of(2023, 1, 5);
    String recipientName = "김몽대";
    String recipientPhoneNumber = "01029482743";
    List<ProductOrderRequestDto> orderProducts;
    List<ProductOrderRequestDto> subscribeOrderProducts;
    long nonMemberProductTotalAmount = 10000L;
    long productTotalAmount = 9000L;
    int shippingFee = 3000;
    int wrappingFee = 0;
    Long ordererAddressId = 1L;
    List<String> orderCoupons;
    long usePoint = 1000L;
    long savePoint = 500L;
    int expectedDay = 10;
    int intervalMonth = 6;

    long orderId = 1L;
    String orderNumber = "20230108-234f34fs";
    String orderName = "테스트 주문";

    @BeforeEach
    void setUp() {
        orderProducts = new ArrayList<>();
        subscribeOrderProducts = new ArrayList<>();

        String isbn = "152374859182";
        for (int i = 0; i < 5; i++) {
            int quantity = i + 1;
            orderProducts.add(new ProductOrderRequestDto(isbn + i, quantity));
        }
        subscribeOrderProducts.add(new ProductOrderRequestDto(isbn + 7, 2));
    }

    @Test
    @WithMockUser(username = "anonymous", authorities = "ROLE_ANONYMOUS")
    @DisplayName("비회원 주문 실패 - 요청 데이터 유효성 에러")
    void createNonMemberOrders_fail_validationError() throws Exception {
        //given
        OrderNonMemberCreateRequestDto request = getInvalidNonMemberOrderRequest();

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/non-member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.ORDER_BAD_REQUEST.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-non-member-order-fail-validation-error",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererName").type(JsonFieldType.STRING).description("주문자명"),
                        fieldWithPath("ordererPhoneNumber").type(JsonFieldType.STRING)
                                .description("주문자 연락처"),
                        fieldWithPath("ordererAddress").type(JsonFieldType.STRING)
                                .description("주문자 주소")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "anonymous", authorities = "ROLE_ANONYMOUS")
    @DisplayName("비회원 주문 실패 - [상품] 주문 상품이 존재하지 않거나 구매가 불가능한 경우")
    void createNonMemberOrders_fail_productNotAvailableToOrder() throws Exception {
        //given
        OrderNonMemberCreateRequestDto request = getNonMemberOrderRequest();

        Mockito.when(commandOrderService.createNonMemberOrders(any()))
                .thenThrow(new ClientException(ErrorCode.BAD_REQUEST, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/non-member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.BAD_REQUEST.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-non-member-order-fail-product-not-available-to-order",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererName").type(JsonFieldType.STRING).description("주문자명"),
                        fieldWithPath("ordererPhoneNumber").type(JsonFieldType.STRING)
                                .description("주문자 연락처"),
                        fieldWithPath("ordererAddress").type(JsonFieldType.STRING)
                                .description("주문자 주소")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "anonymous", authorities = "ROLE_ANONYMOUS")
    @DisplayName("비회원 주문 성공")
    void createNonMemberOrders_success() throws Exception {
        //given
        OrderNonMemberCreateRequestDto request = getNonMemberOrderRequest();
        OrderCreateResponseDto response = getNonMemberResponse();

        Mockito.when(commandOrderService.createNonMemberOrders(any())).thenReturn(response);

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/non-member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.data.orderId", equalTo((int) orderId)))
                .andExpect(jsonPath("$.data.orderNumber", equalTo(orderNumber)))
                .andExpect(jsonPath("$.data.orderName", equalTo(orderName)))
                .andExpect(jsonPath(
                        "$.data.totalAmount",
                        equalTo((int) nonMemberProductTotalAmount)
                ))
                .andExpect(jsonPath("$.data.shippingFee", equalTo(shippingFee)))
                .andExpect(jsonPath("$.data.wrappingFee", equalTo(wrappingFee)));

        //docs
        result.andDo(document(
                "create-non-member-order-success",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererName").type(JsonFieldType.STRING).description("주문자명"),
                        fieldWithPath("ordererPhoneNumber").type(JsonFieldType.STRING)
                                .description("주문자 연락처"),
                        fieldWithPath("ordererAddress").type(JsonFieldType.STRING)
                                .description("주문자 주소")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.orderId").type(JsonFieldType.NUMBER)
                                .description("주문 Pk"),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING)
                                .description("주문 번호"),
                        fieldWithPath("data.orderName").type(JsonFieldType.STRING)
                                .description("주문명"),
                        fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("data.shippingFee").type(JsonFieldType.NUMBER)
                                .description("배송비"),
                        fieldWithPath("data.wrappingFee").type(JsonFieldType.NUMBER)
                                .description("포장비"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("회원 주문 실패 - 요청 데이터 유효성 에러")
    void createMemberOrders_fail_validationError() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getInvalidMemberOrderRequest();

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.ORDER_BAD_REQUEST.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-member-order-fail-validation-error",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("회원 주문 실패 - [상품] 주문 상품이 존재하지 않거나 구매가 불가능한 경우")
    void createMemberOrders_fail_productNotFound() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getMemberOrderRequest();

        Mockito.when(commandOrderService.createMemberOrders(any(), any(), any()))
                .thenThrow(new ClientException(ErrorCode.BAD_REQUEST, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.BAD_REQUEST.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-member-order-fail-product-not-available-to-order",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("회원 주문 실패 - [회원] 존재하지 않는 회원인 경우")
    void createMemberOrders_fail_memberNotFound() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getMemberOrderRequest();

        Mockito.when(commandOrderService.createMemberOrders(any(), any(), any()))
                .thenThrow(new ClientException(ErrorCode.MEMBER_NOT_FOUND, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.MEMBER_NOT_FOUND.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-member-order-fail-member-not-found",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("회원 주문 실패 - [배송지] 존재하지 않는 배송지인 경우")
    void createMemberOrders_fail_memberAddressNotFound() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getMemberOrderRequest();

        Mockito.when(commandOrderService.createMemberOrders(any(), any(), any()))
                .thenThrow(new ClientException(ErrorCode.ADDRESS_NOT_FOUND, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.ADDRESS_NOT_FOUND.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-member-order-fail-address-not-found",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("회원 주문 실패 - [포인트] 소지한 포인트보다 더 많이 사용한 경우")
    void createMemberOrders_fail_pointOverUse() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getMemberOrderRequest();

        Mockito.when(commandOrderService.createMemberOrders(any(), any(), any()))
                .thenThrow(new ClientException(ErrorCode.POINT_OVER_USE, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.POINT_OVER_USE.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-member-order-fail-point-over-use",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("회원 주문 성공")
    void createMemberOrders_success() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getMemberOrderRequest();
        OrderCreateResponseDto response = getMemberResponse();

        Mockito.when(commandOrderService.createMemberOrders(any(), any(), any()))
                .thenReturn(response);

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/member")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.data.orderId", equalTo((int) orderId)))
                .andExpect(jsonPath("$.data.orderNumber", equalTo(orderNumber)))
                .andExpect(jsonPath("$.data.orderName", equalTo(orderName)))
                .andExpect(jsonPath("$.data.totalAmount", equalTo((int) productTotalAmount)))
                .andExpect(jsonPath("$.data.shippingFee", equalTo(shippingFee)))
                .andExpect(jsonPath("$.data.wrappingFee", equalTo(wrappingFee)));

        //docs
        result.andDo(document(
                "create-member-order-success",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.orderId").type(JsonFieldType.NUMBER)
                                .description("주문 Pk"),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING)
                                .description("주문 번호"),
                        fieldWithPath("data.orderName").type(JsonFieldType.STRING)
                                .description("주문명"),
                        fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("data.shippingFee").type(JsonFieldType.NUMBER)
                                .description("배송비"),
                        fieldWithPath("data.wrappingFee").type(JsonFieldType.NUMBER)
                                .description("포장비"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("정기구독 주문 실패 - 요청 데이터 유효성 에러")
    void createSubscribeOrders_fail_validationError() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getInvalidSubscribeRequest();

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/subscribe")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.ORDER_BAD_REQUEST.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-subscribe-order-fail-validation-error",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트"),
                        fieldWithPath("expectedDay").type(JsonFieldType.NUMBER)
                                .description("희망 정기 배송 일자"),
                        fieldWithPath("intervalMonth").type(JsonFieldType.NUMBER)
                                .description("구독 기간")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("정기구독 실패 - [상품] 존재하지 않는 상품인 경우")
    void createSubscribeOrders_fail_productNotFound() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getSubscribeRequest();

        Mockito.when(commandOrderService.createSubscribeOrders(any(), any()))
                .thenThrow(new ClientException(ErrorCode.PRODUCT_NOT_FOUND, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/subscribe")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.PRODUCT_NOT_FOUND.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-subscribe-order-fail-product-not-found",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트"),
                        fieldWithPath("expectedDay").type(JsonFieldType.NUMBER)
                                .description("희망 정기 배송 일자"),
                        fieldWithPath("intervalMonth").type(JsonFieldType.NUMBER)
                                .description("구독 기간")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("정기구독 실패 - [상품] 정기구독 상품이 아닌 경우")
    void createSubscribeOrders_fail_productIsNotSubscribeProduct() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getSubscribeRequest();

        Mockito.when(commandOrderService.createSubscribeOrders(any(), any()))
                .thenThrow(new ClientException(ErrorCode.PRODUCT_NOT_SUBSCRIBE_PRODUCT, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/subscribe")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.PRODUCT_NOT_SUBSCRIBE_PRODUCT.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-subscribe-order-fail-product-not-subscribe-product",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트"),
                        fieldWithPath("expectedDay").type(JsonFieldType.NUMBER)
                                .description("희망 정기 배송 일자"),
                        fieldWithPath("intervalMonth").type(JsonFieldType.NUMBER)
                                .description("구독 기간")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("정기구독 실패 - [회원] 존재하지 않는 회원인 경우")
    void createSubscribeOrders_fail_memberNotFound() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getSubscribeRequest();

        Mockito.when(commandOrderService.createSubscribeOrders(any(), any()))
                .thenThrow(new ClientException(ErrorCode.MEMBER_NOT_FOUND, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/subscribe")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.MEMBER_NOT_FOUND.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-subscribe-order-fail-member-not-found",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트"),
                        fieldWithPath("expectedDay").type(JsonFieldType.NUMBER)
                                .description("희망 정기 배송 일자"),
                        fieldWithPath("intervalMonth").type(JsonFieldType.NUMBER)
                                .description("구독 기간")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("정기구독 실패 - [배송지] 존재하지 않는 배송지인 경우")
    void createSubscribeOrders_fail_memberAddressNotFound() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getSubscribeRequest();

        Mockito.when(commandOrderService.createSubscribeOrders(any(), any()))
                .thenThrow(new ClientException(ErrorCode.ADDRESS_NOT_FOUND, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/subscribe")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.ADDRESS_NOT_FOUND.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-subscribe-order-fail-address-not-found",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트"),
                        fieldWithPath("expectedDay").type(JsonFieldType.NUMBER)
                                .description("희망 정기 배송 일자"),
                        fieldWithPath("intervalMonth").type(JsonFieldType.NUMBER)
                                .description("구독 기간")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("정기구독 실패 - [포인트] 소지한 포인트보다 더 많이 사용한 경우")
    void createSubscribeOrders_fail_pointOverUse() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getSubscribeRequest();

        Mockito.when(commandOrderService.createSubscribeOrders(any(), any()))
                .thenThrow(new ClientException(ErrorCode.POINT_OVER_USE, ""));

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/subscribe")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.POINT_OVER_USE.getDisplayName())
                ));

        //docs
        result.andDo(document(
                "create-subscribe-order-fail-point-over-use",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트"),
                        fieldWithPath("expectedDay").type(JsonFieldType.NUMBER)
                                .description("희망 정기 배송 일자"),
                        fieldWithPath("intervalMonth").type(JsonFieldType.NUMBER)
                                .description("구독 기간")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data").type(JsonFieldType.NUMBER)
                                .description("null")
                                .optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                )
        ));
    }

    @Test
    @WithMockUser(username = "user@1", authorities = "ROLE_USER")
    @DisplayName("정기구독 성공")
    void createSubscribeOrders_success() throws Exception {
        //given
        OrderMemberCreateRequestDto request = getSubscribeRequest();
        OrderCreateResponseDto response = getMemberResponse();

        Mockito.when(commandOrderService.createSubscribeOrders(any(), any()))
                .thenReturn(response);

        //when
        ResultActions result = mockMvc.perform(post("/v1/orders/subscribe")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.data.orderId", equalTo((int) orderId)))
                .andExpect(jsonPath("$.data.orderNumber", equalTo(orderNumber)))
                .andExpect(jsonPath("$.data.orderName", equalTo(orderName)))
                .andExpect(jsonPath("$.data.totalAmount", equalTo((int) productTotalAmount)))
                .andExpect(jsonPath("$.data.shippingFee", equalTo(shippingFee)))
                .andExpect(jsonPath("$.data.wrappingFee", equalTo(wrappingFee)));

        //docs
        result.andDo(document(
                "create-subscribe-order-success",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("expectedShippingDate").type(JsonFieldType.STRING)
                                .description("희망 배송 일자")
                                .optional(),
                        fieldWithPath("orderProducts").type(JsonFieldType.ARRAY)
                                .description("주문 상품 목록"),
                        fieldWithPath("orderProducts.[].isbn").type(JsonFieldType.STRING)
                                .description("주문 상품"),
                        fieldWithPath("orderProducts.[].quantity").type(JsonFieldType.NUMBER)
                                .description("주문 상품 수량"),
                        fieldWithPath("productTotalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("wrappingFee").type(JsonFieldType.NUMBER).description("포장비"),
                        fieldWithPath("recipientName").type(JsonFieldType.STRING)
                                .description("수령인명"),
                        fieldWithPath("recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 연락처"),
                        fieldWithPath("ordererAddressId").type(JsonFieldType.NUMBER)
                                .description("회원 배송지 Pk"),
                        fieldWithPath("orderCoupons").type(JsonFieldType.ARRAY)
                                .description("주문에 사용한 쿠폰")
                                .optional(),
                        fieldWithPath("usePoint").type(JsonFieldType.NUMBER)
                                .description("사용 포인트"),
                        fieldWithPath("savePoint").type(JsonFieldType.NUMBER)
                                .description("적립 포인트"),
                        fieldWithPath("expectedDay").type(JsonFieldType.NUMBER)
                                .description("희망 정기 배송 일자"),
                        fieldWithPath("intervalMonth").type(JsonFieldType.NUMBER)
                                .description("구독 기간")

                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.orderId").type(JsonFieldType.NUMBER)
                                .description("주문 Pk"),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING)
                                .description("주문 번호"),
                        fieldWithPath("data.orderName").type(JsonFieldType.STRING)
                                .description("주문명"),
                        fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER)
                                .description("총 상품 금액"),
                        fieldWithPath("data.shippingFee").type(JsonFieldType.NUMBER)
                                .description("배송비"),
                        fieldWithPath("data.wrappingFee").type(JsonFieldType.NUMBER)
                                .description("포장비"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }

    private OrderNonMemberCreateRequestDto getNonMemberOrderRequest() {
        return new OrderNonMemberCreateRequestDto(
                expectedShippingDate,
                orderProducts,
                nonMemberProductTotalAmount,
                shippingFee,
                wrappingFee,
                recipientName,
                recipientPhoneNumber,
                ordererName,
                ordererPhoneNumber,
                ordererAddress
        );
    }

    private OrderNonMemberCreateRequestDto getInvalidNonMemberOrderRequest() {
        return new OrderNonMemberCreateRequestDto(
                expectedShippingDate,
                orderProducts,
                nonMemberProductTotalAmount,
                shippingFee,
                wrappingFee,
                recipientName,
                recipientPhoneNumber,
                "  ",
                "675",
                "d"
        );
    }

    private OrderMemberCreateRequestDto getMemberOrderRequest() {
        return new OrderMemberCreateRequestDto(
                expectedShippingDate,
                orderProducts,
                productTotalAmount,
                shippingFee,
                wrappingFee,
                recipientName,
                recipientPhoneNumber,
                ordererAddressId,
                orderCoupons,
                usePoint,
                savePoint
        );
    }

    private OrderMemberCreateRequestDto getInvalidMemberOrderRequest() {
        return new OrderMemberCreateRequestDto(
                expectedShippingDate,
                orderProducts,
                productTotalAmount,
                shippingFee,
                wrappingFee,
                recipientName,
                recipientPhoneNumber,
                -7L,
                orderCoupons,
                -1000,
                -400
        );
    }

    private OrderSubscribeCreateRequestDto getSubscribeRequest() {
        return new OrderSubscribeCreateRequestDto(
                expectedShippingDate,
                subscribeOrderProducts,
                productTotalAmount,
                shippingFee,
                wrappingFee,
                recipientName,
                recipientPhoneNumber,
                ordererAddressId,
                orderCoupons,
                usePoint,
                savePoint,
                expectedDay,
                intervalMonth
        );
    }

    private OrderSubscribeCreateRequestDto getInvalidSubscribeRequest() {
        return new OrderSubscribeCreateRequestDto(
                expectedShippingDate,
                orderProducts,
                productTotalAmount,
                shippingFee,
                wrappingFee,
                recipientName,
                recipientPhoneNumber,
                ordererAddressId,
                orderCoupons,
                usePoint,
                savePoint,
                0,
                intervalMonth
        );
    }

    private OrderCreateResponseDto getNonMemberResponse() {

        return ReflectionUtils.newInstance(
                OrderCreateResponseDto.class,
                orderId,
                orderNumber,
                orderName,
                nonMemberProductTotalAmount,
                shippingFee,
                wrappingFee
        );
    }

    private OrderCreateResponseDto getMemberResponse() {
        return ReflectionUtils.newInstance(
                OrderCreateResponseDto.class,
                orderId,
                orderNumber,
                orderName,
                productTotalAmount,
                shippingFee,
                wrappingFee
        );
    }
}
