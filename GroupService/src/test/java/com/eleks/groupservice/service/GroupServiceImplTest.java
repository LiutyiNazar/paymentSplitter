package com.eleks.groupservice.service;

import com.eleks.groupservice.client.UserClient;
import com.eleks.groupservice.domain.Currency;
import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.dto.GroupRequestDto;
import com.eleks.groupservice.dto.GroupResponseDto;
import com.eleks.groupservice.dto.UserDto;
import com.eleks.groupservice.dto.UserStatusDto;
import com.eleks.groupservice.exception.ResourceNotFoundException;
import com.eleks.groupservice.exception.UsersIdsValidationException;
import com.eleks.groupservice.repository.GroupRepository;
import com.eleks.groupservice.service.impl.GroupServiceImpl;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    GroupRepository repository;

    @Mock
    UserClient client;

    GroupServiceImpl service;

    GroupRequestDto requestDto;

    Group group;

    @BeforeEach
    void setUp() {
        service = new GroupServiceImpl(repository, client);

        requestDto = GroupRequestDto.builder()
                .groupName("groupName")
                .currency(Currency.EUR)
                .members(Lists.newArrayList(1L, 2L, 3L))
                .build();

        group = Group.builder()
                .id(21L)
                .groupName(requestDto.getGroupName())
                .currency(requestDto.getCurrency())
                .members(requestDto.getMembers())
                .build();
    }

    @Test
    void saveGroup_GroupMembersAreExist_ReturnSavedUser() {
        when(client.areUserIdsValid(requestDto.getMembers())).thenReturn(true);
        when(repository.save(any(Group.class))).thenReturn(group);

        GroupResponseDto responseDto = service.saveGroup(requestDto);

        assertNotNull(responseDto);
    }

    @Test
    void saveGroup_GroupMembersAreNotExist_ShouldThrowUsersIdsValidationException() {
        when(client.areUserIdsValid(requestDto.getMembers())).thenReturn(false);

        UsersIdsValidationException exception = assertThrows(UsersIdsValidationException.class,
                () -> service.saveGroup(requestDto));

        assertEquals("Group contains non existing users", exception.getMessage());
    }

    @Test
    void getGroup_GroupExists_ReturnGroupResponseDto() {
        when(repository.findById(group.getId())).thenReturn(Optional.of(group));

        Optional<GroupResponseDto> result = service.getGroup(group.getId());

        assertTrue(result.isPresent());
    }

    @Test
    void getGroup_GroupDoesntExist_ReturnEmptyOptional() {
        when(repository.findById(group.getId())).thenReturn(Optional.empty());

        Optional<GroupResponseDto> result = service.getGroup(group.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void editGroup_GroupAndMembersAreExist_ReturnResponseDto() {
        when(repository.findById(group.getId())).thenReturn(Optional.of(group));
        when(client.areUserIdsValid(requestDto.getMembers())).thenReturn(true);

        when(repository.save(any(Group.class))).thenReturn(group);

        GroupResponseDto result = service.editGroup(group.getId(), requestDto);

        assertNotNull(result);
        assertEquals(group.getId(), result.getId());
    }

    @Test
    void editGroup_GroupDoesntExist_ThrowResourceNotFoundException() {
        when(repository.findById(group.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> service.editGroup(group.getId(), requestDto));

        assertEquals("Group does't exist", ex.getMessage());
    }

    @Test
    void editGroup_GroupExistMembersAreNotValid_ThrowUsersIdsValidationException() {
        when(client.areUserIdsValid(requestDto.getMembers())).thenReturn(false);

        UsersIdsValidationException exception = assertThrows(UsersIdsValidationException.class,
                () -> service.saveGroup(requestDto));

        assertEquals("Group contains non existing users", exception.getMessage());
    }

    @Test
    void deleteGroupById_GroupWithIdExists_CallRepositoryDelete() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.of(group));

        service.deleteGroupById(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteGroupById_GroupWithIdDoesntExist_ThrowResourceNotFoundException() {
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.deleteGroupById(id));

        assertEquals("Group does't exist", exception.getMessage());
    }

    @Test
    void getGroupMembersStatus_GroupDoesntExist_ThrowResourceNotFoundException() {
        Long requesterId = 1L;

        when(repository.findById(group.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.getGroupMembersStatus(group.getId(), requesterId));
        assertEquals("Group doesn't exist", exception.getMessage());
    }

    @Test
    void getGroupMembersStatus_RequesterIsNotOneOfMembers_ThrowUsersIdsValidationException() {
        Long requesterId = 1L;
        group.setMembers(Lists.newArrayList(2L, 3L));

        when(repository.findById(group.getId())).thenReturn(Optional.of(group));

        UsersIdsValidationException exception = assertThrows(UsersIdsValidationException.class,
                () -> service.getGroupMembersStatus(group.getId(), requesterId));
        assertEquals("User is not a member of the group", exception.getMessage());
    }

    @Test
    void getGroupMembersStatus_GroupHasRequesterAndOneMoreUserAndNoPayments_ShouldReturnStatusWithRightCurrencyAndUserDataAndZeroBalance() {
        UserDto requester = UserDto.builder().id(1L).username("requester").build();
        UserDto member = UserDto.builder().id(2L).username("member").build();

        group.setMembers(Lists.newArrayList(requester.getId(), member.getId()));

        when(repository.findById(group.getId())).thenReturn(Optional.of(group));
        when(client.getUsersByIds(anyList())).thenReturn(Collections.singletonList(member));

        List<UserStatusDto> result = service.getGroupMembersStatus(group.getId(), requester.getId());

        assertEquals(1, result.size());
        UserStatusDto statusDto = result.get(0);
        assertEquals(group.getCurrency(), statusDto.getCurrency());
        assertEquals(member.getUsername(), statusDto.getUsername());
        assertEquals(member.getId(), statusDto.getUserId());
        assertEquals(0.0, statusDto.getValue());
    }

    @Test
    void getGroupMembersStatus_GroupHasMembersIdsWhichAreNotValidNow_ShouldReturnStatusForOnlyValidUsers() {
        UserDto requester = UserDto.builder().id(1L).username("requester").build();
        UserDto member = UserDto.builder().id(2L).username("member").build();
        UserDto oldMember = UserDto.builder().id(3L).username("oldMember").build();

        group.setMembers(Lists.newArrayList(requester.getId(), member.getId(), oldMember.getId()));

        when(repository.findById(group.getId())).thenReturn(Optional.of(group));
        when(client.getUsersByIds(anyList())).thenReturn(Collections.singletonList(member));

        List<UserStatusDto> result = service.getGroupMembersStatus(group.getId(), requester.getId());

        assertEquals(1, result.size());
        UserStatusDto statusDto = result.get(0);
        assertEquals(member.getId(), statusDto.getUserId());
    }
}