package shop.yesaladin.shop.writing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentRequest;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentResponse;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.yesaladin.shop.common.dto.PaginatedResponseDto;
import shop.yesaladin.shop.writing.dto.AuthorsResponseDto;
import shop.yesaladin.shop.writing.service.inter.QueryAuthorService;

@AutoConfigureRestDocs
@WebMvcTest(QueryAuthorController.class)
class QueryAuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryAuthorService service;

    @WithMockUser
    @Test
    @DisplayName("저자 관리자용 페이징 전체 조회 성공")
    void getAuthorsForManager() throws Exception {
        // given
        List<AuthorsResponseDto> authors = new ArrayList<>();
        for (long i = 1L; i <= 10L; i++) {
            authors.add(new AuthorsResponseDto(i, "저자" + i, null));
        }

        Page<AuthorsResponseDto> page = new PageImpl<>(
                authors,
                PageRequest.of(0, 5),
                authors.size()
        );
        PaginatedResponseDto<AuthorsResponseDto> paginated = PaginatedResponseDto.<AuthorsResponseDto>builder()
                .totalPage(page.getTotalPages())
                .currentPage(page.getNumber())
                .totalDataCount(page.getTotalElements())
                .dataList(authors)
                .build();
        Mockito.when(service.findAllForManager(any())).thenReturn(paginated);

        // when
        ResultActions result = mockMvc.perform(get("/v1/authors/manager")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));
        Mockito.when(service.findAllForManager(PageRequest.of(0, 5))).thenReturn(paginated);

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(service, times(1)).findAllForManager(PageRequest.of(0, 5));

        // docs
        result.andDo(document(
                "find-all-for-manager-author",
                getDocumentRequest(),
                getDocumentResponse(),
                requestParameters(
                        parameterWithName("size").description("페이지네이션 사이즈"),
                        parameterWithName("page").description("페이지네이션 페이지 번호")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.totalPage").type(JsonFieldType.NUMBER)
                                .description("전체 페이지"),
                        fieldWithPath("data.currentPage").type(JsonFieldType.NUMBER)
                                .description("현재 페이지"),
                        fieldWithPath("data.totalDataCount").type(JsonFieldType.NUMBER)
                                .description("데이터 개수"),
                        fieldWithPath("data.dataList.[].id").type(JsonFieldType.NUMBER)
                                .description("저자 아이디"),
                        fieldWithPath("data.dataList.[].name").type(JsonFieldType.STRING)
                                .description("저자명"),
                        fieldWithPath("data.dataList.[].loginId").description("저자 로그인 아이디"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }

    @WithMockUser
    @Test
    @DisplayName("관리자가 저자명으로 조회 성공")
    void getAuthorsByNameForManager() throws Exception {
        // given
        List<AuthorsResponseDto> authors = new ArrayList<>();
        for (long i = 1L; i <= 10L; i++) {
            authors.add(new AuthorsResponseDto(i, "저자" + i, null));
        }

        Page<AuthorsResponseDto> page = new PageImpl<>(
                authors,
                PageRequest.of(0, 5),
                authors.size()
        );
        PaginatedResponseDto<AuthorsResponseDto> paginated = PaginatedResponseDto.<AuthorsResponseDto>builder()
                .totalPage(page.getTotalPages())
                .currentPage(page.getNumber())
                .totalDataCount(page.getTotalElements())
                .dataList(authors)
                .build();
        Mockito.when(service.findAllByNameForManager(any(), any())).thenReturn(paginated);

        // when
        ResultActions result = mockMvc.perform(get("/v1/authors/manager")
                .param("page", "0")
                .param("size", "5")
                .param("name", "name")
                .contentType(MediaType.APPLICATION_JSON));
        Mockito.when(service.findAllByNameForManager("name", PageRequest.of(0, 5)))
                .thenReturn(paginated);

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(service, times(1)).findAllByNameForManager("name", PageRequest.of(0, 5));

        // docs
        result.andDo(document(
                "find-by-name-for-manager-author",
                getDocumentRequest(),
                getDocumentResponse(),
                requestParameters(
                        parameterWithName("size").description("페이지네이션 사이즈"),
                        parameterWithName("page").description("페이지네이션 페이지 번호"),
                        parameterWithName("name").description("저자명")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.totalPage").type(JsonFieldType.NUMBER)
                                .description("전체 페이지"),
                        fieldWithPath("data.currentPage").type(JsonFieldType.NUMBER)
                                .description("현재 페이지"),
                        fieldWithPath("data.totalDataCount").type(JsonFieldType.NUMBER)
                                .description("데이터 개수"),
                        fieldWithPath("data.dataList.[].id").type(JsonFieldType.NUMBER)
                                .description("저자 아이디"),
                        fieldWithPath("data.dataList.[].name").type(JsonFieldType.STRING)
                                .description("저자명"),
                        fieldWithPath("data.dataList.[].loginId").description("저자 로그인 아이디"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }

    @WithMockUser
    @Test
    @DisplayName("관리자가 저자 로그인 아이디로 조회 성공")
    void getAuthorsByLoginIdForManager() throws Exception {
        // given
        List<AuthorsResponseDto> authors = new ArrayList<>();
        for (long i = 1L; i <= 10L; i++) {
            authors.add(new AuthorsResponseDto(i, "저자" + i, null));
        }

        Page<AuthorsResponseDto> page = new PageImpl<>(
                authors,
                PageRequest.of(0, 5),
                authors.size()
        );
        PaginatedResponseDto<AuthorsResponseDto> paginated = PaginatedResponseDto.<AuthorsResponseDto>builder()
                .totalPage(page.getTotalPages())
                .currentPage(page.getNumber())
                .totalDataCount(page.getTotalElements())
                .dataList(authors)
                .build();
        Mockito.when(service.findAllByLoginIdForManager(any(), any())).thenReturn(paginated);

        // when
        ResultActions result = mockMvc.perform(get("/v1/authors/manager")
                .param("page", "0")
                .param("size", "5")
                .param("loginid", "loginId")
                .contentType(MediaType.APPLICATION_JSON));
        Mockito.when(service.findAllByLoginIdForManager(any(), any())).thenReturn(paginated);

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(service, times(1)).findAllByLoginIdForManager("loginId", PageRequest.of(0, 5));

        // docs
        result.andDo(document(
                "find-by-loginid-for-manager-author",
                getDocumentRequest(),
                getDocumentResponse(),
                requestParameters(
                        parameterWithName("size").description("페이지네이션 사이즈"),
                        parameterWithName("page").description("페이지네이션 페이지 번호"),
                        parameterWithName("loginid").description("저자 로그인아이디")
                ),
                responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                .description("동작 성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER)
                                .description("HTTP 상태 코드"),
                        fieldWithPath("data.totalPage").type(JsonFieldType.NUMBER)
                                .description("전체 페이지"),
                        fieldWithPath("data.currentPage").type(JsonFieldType.NUMBER)
                                .description("현재 페이지"),
                        fieldWithPath("data.totalDataCount").type(JsonFieldType.NUMBER)
                                .description("데이터 개수"),
                        fieldWithPath("data.dataList.[].id").type(JsonFieldType.NUMBER)
                                .description("저자 아이디"),
                        fieldWithPath("data.dataList.[].name").type(JsonFieldType.STRING)
                                .description("저자명"),
                        fieldWithPath("data.dataList.[].loginId").description("저자 로그인 아이디"),
                        fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                .description("에러 메세지")
                                .optional()
                )
        ));
    }
}