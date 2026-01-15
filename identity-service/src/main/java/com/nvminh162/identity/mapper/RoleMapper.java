package com.nvminh162.identity.mapper;

import com.nvminh162.identity.dto.request.RoleRequest;
import com.nvminh162.identity.dto.response.RoleResponse;
import com.nvminh162.identity.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
