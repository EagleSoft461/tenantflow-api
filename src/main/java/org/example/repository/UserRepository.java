package org.example.repository;

import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Burayı List<User> olarak pürüzsüz hale getirdik:
    List<User> findByTenantId(String tenantId);

    Optional<User> findByEmailAndTenantId(String email, String tenantId);
}
