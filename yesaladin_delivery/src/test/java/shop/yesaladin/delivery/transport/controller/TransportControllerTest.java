package shop.yesaladin.delivery.transport.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.yesaladin.delivery.transport.domain.model.Transport;
import shop.yesaladin.delivery.transport.domain.model.TransportStatusCode;
import shop.yesaladin.delivery.transport.dto.TransportResponseDto;
import shop.yesaladin.delivery.transport.exception.TransportNotFoundException;
import shop.yesaladin.delivery.transport.service.inter.TransportService;

@WebMvcTest(TransportController.class)
class TransportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransportService transportService;

    private Transport transport;
    private TransportResponseDto responseDto;

    private String trackingNo;

    @BeforeEach
    void setUp() {
        long id = 1L;
        long orderId = 1L;
        trackingNo = UUID.randomUUID().toString();

        transport = Transport.builder()
                .id(id)
                .orderId(orderId)
                .receptionDatetime(LocalDate.now())
                .trackingNo(trackingNo)
                .transportStatusCode(TransportStatusCode.INPROGRESS)
                .build();

        responseDto = TransportResponseDto.fromEntity(transport);
    }

    @Test
    @DisplayName("배송 등록 성공")
    void register() throws Exception {
        //given
        long orderId = 1L;

        Mockito.when(transportService.registerTransport(orderId)).thenReturn(responseDto);

        //when
        ResultActions perform = mockMvc.perform(post("/api/delivery/{orderId}", orderId));

        //then
        perform.andDo(print()).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id", equalTo(transport.getId().intValue())))
                .andExpect(jsonPath("$.data.receptionDatetime", equalTo(transport.getReceptionDatetime().toString())))
                .andExpect(jsonPath("$.data.completionDatetime", equalTo(transport.getCompletionDatetime())))
                .andExpect(jsonPath("$.data.orderId", equalTo(transport.getOrderId().intValue())))
                .andExpect(jsonPath("$.data.trackingNo", equalTo(transport.getTrackingNo())))
                .andExpect(jsonPath("$.data.transportStatus", equalTo(transport.getTransportStatusCode().name())));

        verify(transportService, times(1)).registerTransport(orderId);
    }

    @Test
    @DisplayName("배송 전체 조회 성공")
    void findAll() throws Exception {
        //given
        long id = 1L;

        Mockito.when(transportService.findAll()).thenReturn(List.of(responseDto));

        //when
        ResultActions perform = mockMvc.perform(get("/api/delivery"));

        //then
        perform.andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.[0].id", equalTo(transport.getId().intValue())))
                .andExpect(jsonPath("$.data.[0].receptionDatetime", equalTo(transport.getReceptionDatetime().toString())))
                .andExpect(jsonPath("$.data.[0].completionDatetime", equalTo(transport.getCompletionDatetime())))
                .andExpect(jsonPath("$.data.[0].orderId", equalTo(transport.getOrderId().intValue())))
                .andExpect(jsonPath("$.data.[0].trackingNo", equalTo(transport.getTrackingNo())))
                .andExpect(jsonPath("$.data.[0].transportStatus", equalTo(transport.getTransportStatusCode().name())));

        verify(transportService, times(1)).findAll();
    }

    @Test
    @DisplayName("존재 하지 않는 배송을 조회할 경우 예외가 발생한다.")
    void findByDeliveryId_fail_whenNotExist() throws Exception {
        //given
        long id = 1L;

        Mockito.when(transportService.findById(id)).thenThrow(TransportNotFoundException.class);

        //when
        ResultActions perform = mockMvc.perform(get("/api/delivery/{transportId}", id));

        //then
        perform.andDo(print()).andExpect(status().is4xxClientError());

        verify(transportService, times(1)).findById(id);
    }

    @Test
    @DisplayName("배송 단건 조회 성공")
    void findByDeliveryId() throws Exception {
        //given
        long id = 1L;

        Mockito.when(transportService.findById(id)).thenReturn(responseDto);

        //when
        ResultActions perform = mockMvc.perform(get("/api/delivery/{transportId}", id));

        //then
        perform.andDo(print()).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id", equalTo(transport.getId().intValue())))
                .andExpect(jsonPath("$.data.receptionDatetime", equalTo(transport.getReceptionDatetime().toString())))
                .andExpect(jsonPath("$.data.completionDatetime", equalTo(transport.getCompletionDatetime())))
                .andExpect(jsonPath("$.data.orderId", equalTo(transport.getOrderId().intValue())))
                .andExpect(jsonPath("$.data.trackingNo", equalTo(transport.getTrackingNo())))
                .andExpect(jsonPath("$.data.transportStatus", equalTo(transport.getTransportStatusCode().name())));

        verify(transportService, times(1)).findById(id);
    }
}