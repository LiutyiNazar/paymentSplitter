package com.eleks.groupservice.controller;

import com.eleks.common.dto.ErrorDto;
import com.eleks.groupservice.domain.Currency;
import com.eleks.groupservice.dto.GroupRequestDto;
import com.eleks.groupservice.dto.GroupResponseDto;
import com.eleks.groupservice.dto.StatusResponseDto;
import com.eleks.groupservice.exception.ResourceNotFoundException;
import com.eleks.groupservice.exception.UserServiceException;
import com.eleks.groupservice.exception.UsersIdsValidationException;
import com.eleks.groupservice.handler.CustomExceptionHandler;
import com.eleks.groupservice.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private GroupController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;

    private GroupRequestDto requestDto;
    private GroupResponseDto responseDto;

    private static String randomString = RandomStringUtils.random(51);

    @BeforeEach
    public void setUp() {
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        requestDto = GroupRequestDto.builder()
                .groupName("groupName")
                .currency(Currency.EUR)
                .members(Lists.newArrayList(1L, 2L, 3L))
                .build();

        responseDto = GroupResponseDto.builder()
                .id(21L)
                .groupName(requestDto.getGroupName())
                .currency(requestDto.getCurrency())
                .members(requestDto.getMembers())
                .build();
    }

    @Test
    public void saveGroup_AllDataIsCorrect_ShouldReturnOkAndModel() throws Exception {

        when(groupService.saveGroup(any(GroupRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    public void saveGroup_GroupWithoutGroupName_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setGroupName(null);

        postGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "groupName is required");
    }

    @Test
    public void saveGroup_GroupWithBlankGroupName_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setGroupName("");

        postGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "groupName length should be between 1 and 50");
    }


    @Test
    public void saveGroup_GroupWithLongGroupName_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setGroupName(randomString);

        postGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "groupName length should be between 1 and 50");
    }

    @Test
    public void saveGroup_GroupWithoutCurrency_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setCurrency(null);

        postGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "currency is required");
    }

    @Test
    public void saveGroup_GroupWithoutMembers_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setMembers(null);

        postGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "members is required");
    }

    @Test
    public void saveGroup_GroupMembersIdsAreInvalid_ShouldReturnBadRequestAndErrorWithMsgFromException() throws Exception {
        Exception error = new UsersIdsValidationException("msg");

        when(groupService.saveGroup(any(GroupRequestDto.class))).thenThrow(error);

        postGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                error.getMessage());
    }

    @Test
    public void saveGroup_UserServiceError_ShouldReturnServerErrorAndErrorWithMsgFromException() throws Exception {
        Exception error = new UserServiceException("msg");

        when(groupService.saveGroup(any(GroupRequestDto.class))).thenThrow(error);

        postGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                500,
                error.getMessage());
    }

    private void postGroupAndExpectStatusAndErrorWithMessage(String content, int expectedStatus, String expectedMsg) throws Exception {
        String errorJson = mockMvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        System.out.println(errorJson);
        ErrorDto error = objectMapper.readValue(errorJson, ErrorDto.class);

        assertEquals(error.getStatusCode(), expectedStatus);
        assertNotNull(error.getMessages());
        assertEquals(1, error.getMessages().size());
        assertEquals(expectedMsg, error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void getGroup_GettingExistingGroup_ReturnOkAndGroupData() throws Exception {
        when(groupService.getGroup(responseDto.getId())).thenReturn(Optional.of(responseDto));

        mockMvc.perform(get("/groups/" + responseDto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    public void getGroup_GroupDoesntExist_ReturnNotFoundAndError() throws Exception {
        Long id = 1L;
        when(groupService.getGroup(id)).thenReturn(Optional.empty());

        String responseBody = mockMvc.perform(get("/groups/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);
        assertEquals(error.getStatusCode(), HttpStatus.NOT_FOUND.value());
        assertEquals("group with this id does't exist", error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void editGroup_GroupExists_ReturnOkAndUpdatedData() throws Exception {
        when(groupService.editGroup(anyLong(), any(GroupRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/groups/" + responseDto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    public void editGroup_GroupDoesntExist_ReturnNotFoundAndError() throws Exception {
        ResourceNotFoundException ex = new ResourceNotFoundException("msg");

        when(groupService.editGroup(anyLong(), any(GroupRequestDto.class))).thenThrow(ex);

        String responseBody = mockMvc.perform(put("/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.NOT_FOUND.value());
        assertNotNull(error.getMessages());
        assertEquals(ex.getMessage(), error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void editGroup_GroupWithoutGroupName_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setGroupName(null);

        putGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "groupName is required");
    }

    @Test
    public void editGroup_GroupWithBlankGroupName_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setGroupName("");

        putGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "groupName length should be between 1 and 50");
    }


    @Test
    public void editGroup_GroupWithLongGroupName_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setGroupName(randomString);

        putGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "groupName length should be between 1 and 50");
    }

    @Test
    public void editGroup_GroupWithoutCurrency_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setCurrency(null);

        putGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "currency is required");
    }

    @Test
    public void editGroup_GroupWithoutMembers_ShouldReturnBadRequestAndError() throws Exception {
        requestDto.setMembers(null);

        putGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                "members is required");
    }

    @Test
    public void editGroup_GroupMembersIdsAreInvalid_ShouldReturnBadRequestAndErrorWithMsgFromException() throws Exception {
        Exception error = new UsersIdsValidationException("msg");

        when(groupService.editGroup(anyLong(), any(GroupRequestDto.class))).thenThrow(error);

        putGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                400,
                error.getMessage());
    }

    @Test
    public void editGroup_UserServiceError_ShouldReturnServerErrorAndErrorWithMsgFromException() throws Exception {
        Exception error = new UserServiceException("msg");

        when(groupService.editGroup(anyLong(), any(GroupRequestDto.class))).thenThrow(error);

        putGroupAndExpectStatusAndErrorWithMessage(objectMapper.writeValueAsString(requestDto),
                500,
                error.getMessage());
    }

    private void putGroupAndExpectStatusAndErrorWithMessage(String content, int expectedStatus, String expectedMsg) throws Exception {
        String errorJson = mockMvc.perform(put("/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(errorJson, ErrorDto.class);

        assertEquals(error.getStatusCode(), expectedStatus);
        assertNotNull(error.getMessages());
        assertEquals(1, error.getMessages().size());
        assertEquals(expectedMsg, error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void deleteGroup_GroupExists_ReturnOk() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/groups/" + id))
                .andExpect(status().isOk());

        verify(groupService).deleteGroupById(eq(id));
    }

    @Test
    public void deleteGroup_GroupDoesntExist_ReturnNotFoundAndError() throws Exception {
        Long id = 1L;
        ResourceNotFoundException ex = new ResourceNotFoundException("msg");
        doThrow(ex).when(groupService).deleteGroupById(id);

        String responseBody = mockMvc.perform(delete("/groups/" + id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();


        ErrorDto error = objectMapper.readValue(responseBody, ErrorDto.class);

        assertEquals(error.getStatusCode(), HttpStatus.NOT_FOUND.value());
        assertNotNull(error.getMessages());
        assertEquals(ex.getMessage(), error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

    @Test
    public void getGroupMembersStatus_GroupExistsRequesterIsGroupMember_ReturnListOfStatuses() throws Exception {
        List<StatusResponseDto> status = Arrays.asList(
                StatusResponseDto.builder()
                        .userId(1L)
                        .username("username1")
                        .currency(Currency.UAH)
                        .value(10D)
                        .build(),
                StatusResponseDto.builder()
                        .userId(2L)
                        .username("username2")
                        .currency(Currency.UAH)
                        .value(20D)
                        .build()
        );
        Long groupId = 1L;
        Long requesterId = 3L;

        when(groupService.getGroupMembersStatus(groupId, requesterId)).thenReturn(status);

        mockMvc.perform(get("/groups/" + groupId + "/users/" + requesterId + "/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(status)));
    }

    @Test
    public void getGroupMembersStatus_GroupDoesntExist_ReturnNotFoundAndError() throws Exception {
        Long groupId = 1L;
        Long requesterId = 1L;
        ResourceNotFoundException ex = new ResourceNotFoundException("msg");

        when(groupService.getGroupMembersStatus(groupId, requesterId)).thenThrow(ex);

        performStatusGetAndExpectErrorStatusAndMessage(groupId, requesterId, 404, ex.getMessage());
    }

    @Test
    public void getGroupMembersStatus_GroupExistsButRequesterIsNotAMember_ReturnBadRequestAndError() throws Exception {
        Long groupId = 1L;
        Long requesterId = 1L;
        UsersIdsValidationException ex = new UsersIdsValidationException("msg");

        when(groupService.getGroupMembersStatus(groupId, requesterId)).thenThrow(ex);

        performStatusGetAndExpectErrorStatusAndMessage(groupId, requesterId, 400, ex.getMessage());
    }

    @Test
    public void getGroupMembersStatus_UserServiceError_ReturnServerErrorAndError() throws Exception {
        Long groupId = 1L;
        Long requesterId = 1L;
        UserServiceException ex = new UserServiceException("msg");

        when(groupService.getGroupMembersStatus(groupId, requesterId)).thenThrow(ex);

        performStatusGetAndExpectErrorStatusAndMessage(groupId, requesterId, 500, ex.getMessage());
    }

    private void performStatusGetAndExpectErrorStatusAndMessage(Long groupId, Long requesterId, int expectedStatus, String expectedMsg) throws Exception {
        String errorJson = mockMvc.perform(get("/groups/" + groupId + "/users/" + requesterId + "/status"))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ErrorDto error = objectMapper.readValue(errorJson, ErrorDto.class);

        assertEquals(error.getStatusCode(), expectedStatus);
        assertNotNull(error.getMessages());
        assertEquals(1, error.getMessages().size());
        assertEquals(expectedMsg, error.getMessages().get(0));
        assertNotNull(error.getTimestamp());
    }

}