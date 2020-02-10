package com.eleks.groupservice.service;

import com.eleks.groupservice.dto.GroupRequestDto;
import com.eleks.groupservice.dto.GroupResponseDto;
import com.eleks.groupservice.dto.StatusResponseDto;
import com.eleks.groupservice.exception.ResourceNotFoundException;
import com.eleks.groupservice.exception.UsersIdsValidationException;

import java.util.List;
import java.util.Optional;

public interface GroupService {
    void deleteGroupById(Long id) throws ResourceNotFoundException;

    Optional<GroupResponseDto> getGroup(Long id);

    GroupResponseDto editGroup(Long id, GroupRequestDto requestDto) throws ResourceNotFoundException, UsersIdsValidationException;

    GroupResponseDto saveGroup(GroupRequestDto group) throws UsersIdsValidationException;

    List<StatusResponseDto> getGroupMembersStatus(Long groupId, Long requesterId) throws ResourceNotFoundException, UsersIdsValidationException;
}
