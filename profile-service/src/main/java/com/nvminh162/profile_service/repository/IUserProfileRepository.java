package com.nvminh162.profile_service.repository;

import com.nvminh162.profile_service.entity.UserProfile;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserProfileRepository extends Neo4jRepository<UserProfile, String> {}
