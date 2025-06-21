package org.perfume.mapper;

import org.perfume.domain.entity.User;
import org.perfume.model.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements EntityMapper<User, UserResponse> {

    @Override
    public UserResponse toDto(User entity) {
        if (entity == null) {
            return null;
        }

        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getRole(),
                entity.isGoogleUser(),
                entity.isVerified()
        );
    }

    @Override
    public User toEntity(UserResponse dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(dto.getRole());
        user.setGoogleUser(dto.isGoogleUser());
        user.setVerified(dto.isVerified());
        return user;
    }
}