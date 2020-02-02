package com.eleks.groupservice.mapper;

import com.eleks.groupservice.domain.Currency;
import com.eleks.groupservice.domain.Group;
import com.eleks.groupservice.dto.GroupRequestDto;
import com.eleks.groupservice.dto.GroupResponseDto;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GroupMapperTest {

    @Test
    void toEntity_NotNullDto_ReturnEntity() {
        GroupRequestDto dto = GroupRequestDto.builder()
                .groupName("groupName")
                .currency(Currency.UAH)
                .members(Arrays.asList(1L, 2L, 3L))
                .build();

        Group entity = GroupMapper.toEntity(dto);

        assertNull(entity.getId());
        assertEquals(dto.getGroupName(), entity.getGroupName());
        assertEquals(dto.getCurrency(), entity.getCurrency());
        assertEquals(dto.getMembers(), entity.getMembers());
    }

    @Test
    void toEntity_NullDto_ReturnNull() {
        assertNull(GroupMapper.toEntity(null));
    }

    @Test
    void toDto_NotNullEntity_ReturnDto() {
        Group entity = Group.builder()
                .id(1L)
                .groupName("groupName")
                .currency(Currency.UAH)
                .members(Arrays.asList(1L, 2L, 3L))
                .build();

        GroupResponseDto dto = GroupMapper.toDto(entity);

        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getGroupName(), dto.getGroupName());
        assertEquals(entity.getCurrency(), dto.getCurrency());
        assertEquals(entity.getMembers(), dto.getMembers());
    }

    @Test
    void toDto_NullEntity_ReturnNull() {
        assertNull(GroupMapper.toDto(null));
    }
}