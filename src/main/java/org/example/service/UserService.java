package org.example.service;

import org.example.context.TenantContext;
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
    public User saveUser(User user) {
        // Eğer dışarıdan gelen tenantId "schema_" ile başlıyorsa temizleyelim,
        // sadece saf ismi (örn: "alpha") alalım.
        if (user.getTenantId() != null && user.getTenantId().startsWith("schema_")) {
            user.setTenantId(user.getTenantId().replace("schema_", ""));
        }

        // Eğer tenantId boş geldiyse aktif context'ten alalım
        if (user.getTenantId() == null || user.getTenantId().trim().isEmpty()) {
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null && currentTenant.startsWith("schema_")) {
                currentTenant = currentTenant.replace("schema_", "");
            }
            user.setTenantId(currentTenant);
        }

        return userRepository.save(user);
    }
}
