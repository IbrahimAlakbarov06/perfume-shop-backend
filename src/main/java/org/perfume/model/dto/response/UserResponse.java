package org.perfume.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.perfume.model.enums.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private boolean isGoogleUser;
    private boolean isVerified;
}