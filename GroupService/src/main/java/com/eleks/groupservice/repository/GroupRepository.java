package com.eleks.groupservice.repository;

import com.eleks.groupservice.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}