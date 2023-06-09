package shop.yesaladin.shop.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentRequest;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import shop.yesaladin.shop.member.domain.model.Member;
import shop.yesaladin.shop.member.domain.model.MemberAddress;
import shop.yesaladin.shop.order.domain.model.MemberOrder;
import shop.yesaladin.shop.order.domain.model.OrderCode;
import shop.yesaladin.shop.order.dto.OrderPaymentResponseDto;
import shop.yesaladin.shop.order.persistence.dummy.DummyMember;
import shop.yesaladin.shop.order.persistence.dummy.DummyMemberAddress;
import shop.yesaladin.shop.payment.domain.model.Payment;
import shop.yesaladin.shop.payment.domain.model.PaymentCard;
import shop.yesaladin.shop.payment.dto.PaymentCompleteSimpleResponseDto;
import shop.yesaladin.shop.payment.dto.PaymentRequestDto;
import shop.yesaladin.shop.payment.dummy.DummyPayment;
import shop.yesaladin.shop.payment.dummy.DummyPaymentCard;
import shop.yesaladin.shop.payment.exception.PaymentFailException;
import shop.yesaladin.shop.payment.service.inter.CommandPaymentService;


@AutoConfigureRestDocs
@WebMvcTest(CommandPaymentController.class)
class CommandPaymentControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommandPaymentService paymentService;

    private MemberOrder memberOrder;
    private Payment payment;
    private Long orderId = 1L;
    private String orderNumber = "zaIkFmBcYW4k_J9rOl0M2b7";
    private String paymentId = "dJv2eBNjG0Poxy1XQL8RJqpXm9gkOk87nO5Wmlg96RKwZz4Y";

    @BeforeEach
    void setUp() {
        Member member = DummyMember.member();
        MemberAddress memberAddress = DummyMemberAddress.address(member);
        memberOrder = MemberOrder.builder()
                .id(orderId)
                .orderNumber(orderNumber)
                .name("memberOrder")
                .orderDateTime(LocalDateTime.now())
                .expectedTransportDate(LocalDate.now())
                .isHidden(false)
                .usedPoint(0)
                .shippingFee(0)
                .wrappingFee(0)
                .orderCode(OrderCode.MEMBER_ORDER)
                .memberAddress(memberAddress)
                .member(member)
                .build();

        payment = DummyPayment.payment(paymentId, memberOrder);

        PaymentCard paymentCard = DummyPaymentCard.paymentCard(payment);
        payment.setPaymentCard(paymentCard);
    }

    @WithMockUser
    @Test
    @DisplayName("결제 승인 성공")
    void confirmPayment() throws Exception {
        // given
        ArgumentCaptor<PaymentRequestDto> dtoArgumentCaptor = ArgumentCaptor.forClass(
                PaymentRequestDto.class);

        PaymentRequestDto requestDto = new PaymentRequestDto(
                paymentId,
                orderNumber,
                15000L
        );

        PaymentCompleteSimpleResponseDto responseDto = PaymentCompleteSimpleResponseDto.fromEntityByCard(
                payment);
        OrderPaymentResponseDto nameAndAddress = new OrderPaymentResponseDto(memberOrder.getMember()
                .getName(), memberOrder.getMemberAddress().getAddress());
        responseDto.setUserInfo(
                nameAndAddress.getOrdererName(),
                nameAndAddress.getAddress(),
                memberOrder.getRecipientName(),
                memberOrder.getRecipientPhoneNumber()
        );
        when(paymentService.confirmTossRequest(any())).thenReturn(responseDto);

        // when
        ResultActions perform = mockMvc.perform(post("/v1/payments/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        perform.andExpect(status().isOk())
                .andExpect(header().stringValues("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.data.orderNumber", equalTo(responseDto.getOrderNumber())))
                .andExpect(jsonPath("$.data.paymentId", equalTo(responseDto.getPaymentId())))
                .andExpect(jsonPath("$.data.cardNumber", equalTo(responseDto.getCardNumber())));

        verify(paymentService, times(1)).confirmTossRequest(dtoArgumentCaptor.capture());
        assertThat(dtoArgumentCaptor.getValue()
                .getPaymentKey()).isEqualTo(requestDto.getPaymentKey());

        perform.andDo(document(
                "confirm-payment-to-Toss",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING)
                                .description("토스 페이먼츠에서 제공하는 결제 id"),
                        fieldWithPath("orderId").type(JsonFieldType.STRING)
                                .description("결제하고자 하는 주문의 번호 - id 아님"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                .description("결제하고자하는 총 금액")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.paymentId").type(JsonFieldType.STRING)
                                .description("결제정보 아이디").optional(),
                        fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER)
                                .description("결제 총 금액").optional(),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING)
                                .description("주문 번호").optional(),
                        fieldWithPath("data.method").type(JsonFieldType.STRING)
                                .description("결제 방법").optional(),
                        fieldWithPath("data.currency").type(JsonFieldType.STRING)
                                .description("결제 통화").optional(),
                        fieldWithPath("data.approvedDateTime").type(JsonFieldType.STRING)
                                .description("결제 승인 일시").optional(),
                        fieldWithPath("data.ordererName").type(JsonFieldType.STRING)
                                .description("주문자 이름").optional(),
                        fieldWithPath("data.recipientName").type(JsonFieldType.STRING)
                                .description("수령인 이름").optional(),
                        fieldWithPath("data.recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 전화번호").optional(),
                        fieldWithPath("data.orderAddress").type(JsonFieldType.STRING)
                                .description("주문 배송지").optional(),
                        fieldWithPath("data.orderName").type(JsonFieldType.STRING)
                                .description("주문명").optional(),
                        fieldWithPath("data.cardCode").type(JsonFieldType.STRING)
                                .description("카드 종류").optional(),
                        fieldWithPath("data.cardOwnerCode").type(JsonFieldType.STRING)
                                .description("카드 소유 구분").optional(),
                        fieldWithPath("data.cardNumber").type(JsonFieldType.STRING)
                                .description("카드 번호").optional(),
                        fieldWithPath("data.cardInstallmentPlanMonths").type(JsonFieldType.NUMBER)
                                .description("할부 개월 수").optional(),
                        fieldWithPath("data.cardApproveNumber").type(JsonFieldType.STRING)
                                .description("카드 결제 승인 번호").optional(),
                        fieldWithPath("data.cardAcquirerCode").type(JsonFieldType.STRING)
                                .description("카드 매입사").optional(),
                        fieldWithPath("data.easyPayProvider").type(JsonFieldType.STRING)
                                .description("간편결제 제공").optional(),
                        fieldWithPath("data.easyPayAmount").type(JsonFieldType.NUMBER)
                                .description("간편결제 결제금액").optional(),
                        fieldWithPath("data.easyPayDiscountAmount").type(JsonFieldType.NUMBER)
                                .description("간편결제 즉시 할인 금액").optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지").optional()
                )
        ));
    }

    @WithMockUser
    @Test
    @DisplayName("결제 승인 실패 - 토스에서 실패처리")
    void confirmPayment_fail() throws Exception {
        // given
        ArgumentCaptor<PaymentRequestDto> dtoArgumentCaptor = ArgumentCaptor.forClass(
                PaymentRequestDto.class);

        PaymentRequestDto requestDto = new PaymentRequestDto(
                paymentId,
                orderNumber,
                15000L
        );

        PaymentCompleteSimpleResponseDto responseDto = PaymentCompleteSimpleResponseDto.fromEntityByCard(
                payment);
        OrderPaymentResponseDto nameAndAddress = new OrderPaymentResponseDto(memberOrder.getMember()
                .getName(), memberOrder.getMemberAddress().getAddress());
        responseDto.setUserInfo(
                nameAndAddress.getOrdererName(),
                nameAndAddress.getAddress(),
                memberOrder.getRecipientName(),
                memberOrder.getRecipientPhoneNumber()
        );
        when(paymentService.confirmTossRequest(any())).thenThrow(new PaymentFailException(
                "Payment fail", "CANCELED"));

        // when
        ResultActions perform = mockMvc.perform(post("/v1/payments/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        perform.andExpect(status().isOk())
                .andExpect(header().stringValues("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.data.orderNumber", equalTo(requestDto.getOrderId())))
                .andExpect(jsonPath("$.data.paymentId", equalTo(requestDto.getPaymentKey())))
                .andExpect(jsonPath(
                        "$.data.totalAmount",
                        equalTo(requestDto.getAmount().intValue())
                ))
                .andExpect(jsonPath("$.errorMessages", hasItem("Payment fail")));

        verify(paymentService, times(1)).confirmTossRequest(dtoArgumentCaptor.capture());
        assertThat(dtoArgumentCaptor.getValue()
                .getPaymentKey()).isEqualTo(requestDto.getPaymentKey());

        perform.andDo(document(
                "confirm-payment-fail",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING)
                                .description("토스 페이먼츠에서 제공하는 결제 id"),
                        fieldWithPath("orderId").type(JsonFieldType.STRING)
                                .description("결제하고자 하는 주문의 번호 - id 아님"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER)
                                .description("결제하고자하는 총 금액")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.paymentId").type(JsonFieldType.STRING)
                                .description("결제정보 아이디").optional(),
                        fieldWithPath("data.totalAmount").type(JsonFieldType.NUMBER)
                                .description("결제 총 금액").optional(),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING)
                                .description("주문 번호").optional(),
                        fieldWithPath("data.method").type(JsonFieldType.STRING)
                                .description("결제 방법").optional(),
                        fieldWithPath("data.currency").type(JsonFieldType.STRING)
                                .description("결제 통화").optional(),
                        fieldWithPath("data.approvedDateTime").type(JsonFieldType.STRING)
                                .description("결제 승인 일시").optional(),
                        fieldWithPath("data.ordererName").type(JsonFieldType.STRING)
                                .description("주문자 이름").optional(),
                        fieldWithPath("data.recipientName").type(JsonFieldType.STRING)
                                .description("수령인 이름").optional(),
                        fieldWithPath("data.recipientPhoneNumber").type(JsonFieldType.STRING)
                                .description("수령인 전화번호").optional(),
                        fieldWithPath("data.orderAddress").type(JsonFieldType.STRING)
                                .description("주문 배송지").optional(),
                        fieldWithPath("data.orderName").type(JsonFieldType.STRING)
                                .description("주문명").optional(),
                        fieldWithPath("data.cardCode").type(JsonFieldType.STRING)
                                .description("카드 종류").optional(),
                        fieldWithPath("data.cardOwnerCode").type(JsonFieldType.STRING)
                                .description("카드 소유 구분").optional(),
                        fieldWithPath("data.cardNumber").type(JsonFieldType.STRING)
                                .description("카드 번호").optional(),
                        fieldWithPath("data.cardInstallmentPlanMonths").type(JsonFieldType.NUMBER)
                                .description("할부 개월 수").optional(),
                        fieldWithPath("data.cardApproveNumber").type(JsonFieldType.STRING)
                                .description("카드 결제 승인 번호").optional(),
                        fieldWithPath("data.cardAcquirerCode").type(JsonFieldType.STRING)
                                .description("카드 매입사").optional(),
                        fieldWithPath("data.easyPayProvider").type(JsonFieldType.STRING)
                                .description("간편결제 제공").optional(),
                        fieldWithPath("data.easyPayAmount").type(JsonFieldType.NUMBER)
                                .description("간편결제 결제금액").optional(),
                        fieldWithPath("data.easyPayDiscountAmount").type(JsonFieldType.NUMBER)
                                .description("간편결제 즉시 할인 금액").optional(),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지").optional()
                )
        ));

    }

}
