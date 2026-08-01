package org.example.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
