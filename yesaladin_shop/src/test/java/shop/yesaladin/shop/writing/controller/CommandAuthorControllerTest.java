package shop.yesaladin.shop.writing.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentRequest;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import shop.yesaladin.shop.writing.dto.AuthorRequestDto;
import shop.yesaladin.shop.writing.dto.AuthorResponseDto;
import shop.yesaladin.shop.writing.service.inter.CommandAuthorService;

@AutoConfigureRestDocs
@WebMvcTest(CommandAuthorController.class)
class CommandAuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommandAuthorService service;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser
    @Test
    @DisplayName("저자 등록 성공")
    void registerAuthor_success() throws Exception {
        // given
        String name = "저자1";
        AuthorRequestDto createDto = new AuthorRequestDto(name, null);
        AuthorResponseDto responseDto = new AuthorResponseDto(1L, name, null);
        Mockito.when(service.create(any())).thenReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(post("/v1/authors")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createDto)));

        // then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.CREATED.value())))
                .andExpect(jsonPath("$.data.id", equalTo(1)))
                .andExpect(jsonPath("$.data.name", equalTo(name)));

        verify(service, times(1)).create(any());

        // docs
        result.andDo(document(
                "register-author",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).description("저자명"),
                        fieldWithPath("loginId").description("저자 로그인 아이디")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                .description("생성된 저자 아이디"),
                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("저자명"),
                        fieldWithPath("data.member").description("저자 멤버 엔터티"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }

    @WithMockUser
    @Test
    @DisplayName("저자 등록 실패_존재하지 않는 멤버 로그인 아이디를 입력한 경우 예외 발생")
    void registerAuthor_notExistsLoginId_throwMemberNotFoundException() throws Exception {
        // given
        String name = "저자1";
        AuthorRequestDto createDto = new AuthorRequestDto(name, "notExist");

        Mockito.when(service.create(any()))
                .thenThrow(new ClientException(ErrorCode.MEMBER_NOT_FOUND, ""));

        // when
        ResultActions result = mockMvc.perform(post("/v1/authors")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(createDto)));

        // then
        result.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.MEMBER_NOT_FOUND.getDisplayName())
                ));

        verify(service, times(1)).create(any());
    }

    @WithMockUser
    @Test
    @DisplayName("저자 수정 성공")
    void modifyAuthor_success() throws Exception {
        // given
        Long id = 1L;
        String name1 = "저자1";
        String name2 = "저자2";

        AuthorRequestDto modifyDto = new AuthorRequestDto(name1, null);
        AuthorResponseDto responseDto = new AuthorResponseDto(id, name2, null);
        Mockito.when(service.modify(anyLong(), any())).thenReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(put("/v1/authors/{authorId}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(modifyDto)));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.data.id", equalTo(1)))
                .andExpect(jsonPath("$.data.name", equalTo(name2)));

        verify(service, times(1)).modify(anyLong(), any());

        // docs
        result.andDo(document(
                "modify-author",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).description("저자명"),
                        fieldWithPath("loginId").description("저자 로그인 아이디")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                .description("수정된 저자 아이디"),
                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("저자명"),
                        fieldWithPath("data.member").description("저자"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }

    @WithMockUser
    @Test
    @DisplayName("저자 수정 실패_존재하지 않는 멤버 로그인 아이디를 입력한 경우 예외 발생")
    void modifyAuthor_notExistsLoginId_throwMemberNotFoundException() throws Exception {
        // given
        Long id = 1L;
        String name = "저자1";
        AuthorRequestDto modifyDto = new AuthorRequestDto(name, "notExist");

        Mockito.when(service.modify(anyLong(), any()))
                .thenThrow(new ClientException(ErrorCode.MEMBER_NOT_FOUND, ""));

        // when
        ResultActions result = mockMvc.perform(put("/v1/authors/{authorId}", id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(modifyDto)));

        // then
        result.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.MEMBER_NOT_FOUND.getDisplayName())
                ));

        verify(service, times(1)).modify(anyLong(), any());
    }
}