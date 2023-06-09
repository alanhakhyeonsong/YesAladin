package shop.yesaladin.shop.category.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentRequest;
import static shop.yesaladin.shop.docs.ApiDocumentUtils.getDocumentResponse;

import java.util.List;
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
import shop.yesaladin.common.dto.ResponseDto;
import shop.yesaladin.shop.category.dto.SearchCategoryResponseDto;
import shop.yesaladin.shop.category.dto.SearchCategoryResponseDto.SearchedCategoryDto;
import shop.yesaladin.shop.category.service.inter.SearchCategoryService;

@AutoConfigureRestDocs
@WebMvcTest(SearchCategoryController.class)
class SearchCategoryControllerTest {

    private static final String ZERO = "0";
    private static final String ONE = "1";
    private static final String MIN = "-1";
    private static final String NAME = "name";
    private static final String OFFSET = "offset";
    private static final String SIZE = "size";
    @Autowired
    MockMvc mockMvc;
    @MockBean
    SearchCategoryService searchCategoryService;

    @WithMockUser
    @Test
    @DisplayName("페이지 위치가 미이너스 일 경우 ConstraintViolationException")
    void testSearchCategoryByNameOffsetLessThanZeroThrConstraintViolationException()
            throws Exception {
        //when
        ResultActions resultActions = mockMvc.perform(get("/v1/search/categories")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .param(NAME, "name")
                .param(OFFSET, MIN)
                .param(SIZE, ONE));
        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.BAD_REQUEST.getDisplayName())
                ));

        resultActions.andDo(
                document(
                        "search-categories-offset-min-validation",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("name").description("검색할 카테고리 이름"),
                                parameterWithName("size").description("데이터 갯수"),
                                parameterWithName("offset").description("페이지 위치"),
                                parameterWithName("_csrf").description("csrf")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .description("상태"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("null")
                                        .optional(),
                                fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                        .description("에러 메시지")
                        )
                ));
    }

    @WithMockUser
    @Test
    @DisplayName("데이터 갯수가 1보다 작을 경우 일 경우 ConstraintViolationException")
    void testSearchCategoryByNameSizeLessThanOneThrConstraintViolationException()
            throws Exception {
        //when
        ResultActions resultActions = mockMvc.perform(get("/v1/search/categories")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .param(NAME, "name")
                .param(OFFSET, ZERO)
                .param(SIZE, ZERO));
        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.BAD_REQUEST.getDisplayName())
                ));

        resultActions.andDo(
                document(
                        "search-categories-size-min-validation",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("name").description("검색할 카테고리 이름"),
                                parameterWithName("size").description("데이터 갯수"),
                                parameterWithName("offset").description("페이지 위치"),
                                parameterWithName("_csrf").description("csrf")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .description("상태"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("null")
                                        .optional(),
                                fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                        .description("에러 메시지")
                        )
                ));
    }

    @WithMockUser
    @Test
    @DisplayName("데이터 갯수가 20보다 클 경우 일 경우 ConstraintViolationException")
    void testSearchCategoryByNameSizeMoreThan20ThrConstraintViolationException()
            throws Exception {
        //when
        ResultActions resultActions = mockMvc.perform(get("/v1/search/categories")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .param(NAME, "name")
                .param(OFFSET, ZERO)
                .param(SIZE, "21"));
        //then
        resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.status", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath(
                        "$.errorMessages[0]",
                        equalTo(ErrorCode.BAD_REQUEST.getDisplayName())
                ));

        resultActions.andDo(
                document(
                        "search-categories-size-max-validation",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("name").description("검색할 카테고리 이름"),
                                parameterWithName("size").description("데이터 갯수"),
                                parameterWithName("offset").description("페이지 위치"),
                                parameterWithName("_csrf").description("csrf")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("성공 여부"),
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .description("상태"),
                                fieldWithPath("data").type(JsonFieldType.OBJECT)
                                        .description("null")
                                        .optional(),
                                fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                        .description("에러 메시지")
                        )
                ));
    }

    @WithMockUser
    @Test
    @DisplayName("카테고리 이름 검색 성공")
    void testSearchCategoryByNameSuccess() throws Exception {
        //given

        SearchCategoryResponseDto dummy = SearchCategoryResponseDto.builder()
                .count(1L)
                .searchedCategoryDtoList(List.of(new SearchedCategoryDto(
                        1L,
                        "name",
                        "parentName"
                ))).build();

        Mockito.when(searchCategoryService.searchCategoryByName(any()))
                .thenReturn(ResponseDto.<SearchCategoryResponseDto>builder()
                        .status(HttpStatus.OK)
                        .success(true)
                        .data(dummy)
                        .build().getData());

        //when
        ResultActions resultActions = mockMvc.perform(get("/v1/search/categories")
                .param(NAME, "category")
                .param(OFFSET, ZERO)
                .param(SIZE, ONE)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON));

        //then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count", equalTo(1)))
                .andExpect(jsonPath("$.data.searchedCategoryDtoList[0].id", equalTo(1)))
                .andExpect(jsonPath("$.data.searchedCategoryDtoList[0].name", equalTo("name")))
                .andExpect(jsonPath(
                        "$.data.searchedCategoryDtoList[0].parentName",
                        equalTo("parentName")
                ));

        resultActions.andDo(
                document(
                        "search-categories-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("name").description("검색할 카테고리 이름"),
                                parameterWithName("size").description("데이터 갯수"),
                                parameterWithName("offset").description("페이지 위치"),
                                parameterWithName("_csrf").description("csrf")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("동작 성공 여부"),
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .description("상태"),
                                fieldWithPath("errorMessages").type(JsonFieldType.ARRAY)
                                        .description("null")
                                        .optional(),
                                fieldWithPath("data.count").type(JsonFieldType.NUMBER)
                                        .description("총 갯수"),
                                fieldWithPath("data.searchedCategoryDtoList").type(JsonFieldType.ARRAY)
                                        .description("카테고리 검색 결과 리스트"),
                                fieldWithPath("data.searchedCategoryDtoList.[].id").type(
                                        JsonFieldType.NUMBER).description("카테고리 id"),
                                fieldWithPath("data.searchedCategoryDtoList.[].name").type(
                                        JsonFieldType.STRING).description("카테고리 이름"),
                                fieldWithPath("data.searchedCategoryDtoList.[].parentName").type(
                                        JsonFieldType.STRING).description("부모 카테고리 이름")
                        )
                ));
    }
}
