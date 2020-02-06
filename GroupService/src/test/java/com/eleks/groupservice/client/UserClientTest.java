package com.eleks.groupservice.client;

import com.eleks.common.security.SecurityPrincipalHolder;
import com.eleks.common.security.model.LoggedInUserPrincipal;
import com.eleks.groupservice.dto.UserDto;
import com.eleks.groupservice.dto.UserSearchDto;
import com.eleks.groupservice.exception.UserServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;

import static com.eleks.common.security.SecurityConstants.BEARER_TOKEN_PREFIX;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@SpringBootTest
class UserClientTest {

    private static WireMockServer wireMockServer = new WireMockServer(8085);

    private static List<Long> userIds;

    @Autowired
    private UserClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecurityPrincipalHolder securityPrincipalHolder;

    private LoggedInUserPrincipal fakePrincipal;

    @BeforeAll
    static void setUpAll() {
        wireMockServer.start();
        userIds = Lists.newArrayList(1L, 2L, 3L);
    }

    @BeforeEach
    void setUpEach() {
        fakePrincipal = new LoggedInUserPrincipal("testUser", 1L, "fake_token");
        when(securityPrincipalHolder.getPrincipal()).thenReturn(fakePrincipal);
    }

    @AfterAll
    static void cleanUpAll() {
        wireMockServer.stop();
    }

    @Test
    void areUserIdsValid_ServiceReturnsSameCountOfIdsAsRequested_ShouldReturnTrue() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("user_search_response_with_three_users.json")));

        boolean isValid = client.areUserIdsValid(userIds);

        verifyPostOnSearchWithRequestDto(searchDto);
        assertTrue(isValid);
    }

    @Test
    void areUserIdsValid_ServiceReturnsLessCountOfIdsAsRequested_ShouldReturnFalse() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("user_search_response_with_two_users.json")));

        boolean isValid = client.areUserIdsValid(userIds);

        verifyPostOnSearchWithRequestDto(searchDto);
        assertFalse(isValid);
    }

    @Test
    void areUserIdsValid_ServiceReturnsBadRequest_ShouldReturnFalse() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(status(400)));

        boolean isValid = client.areUserIdsValid(userIds);

        verifyPostOnSearchWithRequestDto(searchDto);
        assertFalse(isValid);
    }

    @Test
    void areUserIdsValid_ServiceReturnsServerError_ShouldThrowException() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(status(500)));

        UserServiceException exception = assertThrows(UserServiceException.class,
                () -> client.areUserIdsValid(userIds));

        verifyPostOnSearchWithRequestDto(searchDto);
        assertEquals("Server error during request to UserService", exception.getMessage());
    }

    @Test
    void getUsersByIds_ServiceReturnsThreeUsers_ShouldReturnListOfThreeUsers() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("user_search_response_with_three_users.json")));

        List<UserDto> result = client.getListOfUsersByIds(userIds);

        verifyPostOnSearchWithRequestDto(searchDto);
        assertEquals(3, result.size());
    }

    @Test
    void getUsersByIds_ServiceReturnsEmptyList_ShouldReturnEmptyList() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        List<UserDto> result = client.getListOfUsersByIds(userIds);

        verifyPostOnSearchWithRequestDto(searchDto);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByIds_ServiceReturnsBadRequest_ShouldReturnEmptyList() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(status(400)));

        List<UserDto> result = client.getListOfUsersByIds(userIds);

        verifyPostOnSearchWithRequestDto(searchDto);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUsersByIds_ServiceReturnsServerError_ShouldThrowException() throws Exception {
        UserSearchDto searchDto = new UserSearchDto(userIds);

        wireMockServer.stubFor(post(urlEqualTo("/users/search"))
                .willReturn(status(500)));

        UserServiceException exception = assertThrows(UserServiceException.class,
                () -> client.getListOfUsersByIds(userIds));

        verifyPostOnSearchWithRequestDto(searchDto);
        assertEquals("Server error during request to UserService", exception.getMessage());
    }

    private void verifyPostOnSearchWithRequestDto(UserSearchDto dto) throws Exception {
        wireMockServer.verify((postRequestedFor(urlEqualTo("/users/search"))
                .withHeader(CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(AUTHORIZATION, equalTo(BEARER_TOKEN_PREFIX + fakePrincipal.getJwt()))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(dto)))));
    }
}