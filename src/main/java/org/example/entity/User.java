package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {
    // Her kullanıcının bir tenant'a bağlı olduğunu belirten alanımız
    @Column(name = "tenant_id", unique = false)
    private String tenantId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_STAFF, ROLE_CUSTOMER
}
