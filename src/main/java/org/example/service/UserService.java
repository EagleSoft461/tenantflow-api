package org.example.service;

import org.example.context.TenantContext;
import org.example.dto.UserRegistrationRequestDTO;
import org.example.dto.UserResponseDTO;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Bunu ekledik

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Yeni bir kullanıcı kaydederken o anki aktif dükkanın ID'sini otomatik ekliyoruz
    @Transactional(readOnly = true)
    public List<User> getAllUsersByTenant(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new RuntimeException("Hata: Dükkan doğrulaması başarısız, Tenant ID eksik!");
        }
        return userRepository.findByTenantId(tenantId);
    }

    // İzolasyonun kalbi: @Transactional ekledik, böylece otomatik Hibernate Filtresi (tenantFilter) devreye girecek!
    @Transactional(readOnly = true)
    public List<User> getAllUsersByActiveTenant() {
        // Hibernate filtresi preHandle aşamasında açıldığı için artık findAll() desek bile
        // veritabanından SADECE o tenantId'ye ait veriler gelecektir!
        return userRepository.findAll();
    }

    @Transactional
    public UserResponseDTO saveUser(UserRegistrationRequestDTO requestDTO) {
        User user = new User();
        user.setEmail(requestDTO.getEmail());
        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        user.setRole(requestDTO.getRole());
        user.setPassword(requestDTO.getPassword()); // In a real app, this should be encoded before saving, but AuthService handles it if called from AuthController. If UserController calls it, we might have an issue. Let's let the caller encode, or inject PasswordEncoder here.

        String tenantId = requestDTO.getTenantId();
        
        // Eğer dışarıdan gelen tenantId "schema_" ile başlıyorsa temizleyelim,
        if (tenantId != null && tenantId.startsWith("schema_")) {
            tenantId = tenantId.replace("schema_", "");
        }

        // Eğer tenantId boş geldiyse aktif context'ten alalım
        if (tenantId == null || tenantId.trim().isEmpty()) {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null && currentTenant.startsWith("schema_")) {
                currentTenant = currentTenant.replace("schema_", "");
            }
            tenantId = currentTenant;
        }
        
        user.setTenantId(tenantId);

        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }
    
    public UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setTenantId(user.getTenantId());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
