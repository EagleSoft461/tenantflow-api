package org.example.service;

import org.example.context.TenantContext;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Yeni bir kullanıcı kaydederken o anki aktif dükkanın ID'sini otomatik ekliyoruz
    public User createUser(User user) {
        String activeTenantId = TenantContext.getCurrentTenant();

        if (activeTenantId == null){
            throw  new RuntimeException("Hata: Hangi dükkan adına işlem yapıldığı tespit edilemedi (Tenant ID eksik)!");
        }

        user.setTenantId(activeTenantId);
        return userRepository.save(user);
    }

    // Sadece istek atan dükkana ait kullanıcıları listeler (İzolasyonun kalbi)
    public List<User> getAllUsersByActiveTenant() {
        String activeTenantId = TenantContext.getCurrentTenant();

        if (activeTenantId == null) {
            throw new RuntimeException("Hata: Tenant ID bulunamadı!");
        }

        return userRepository.findByTenantId(activeTenantId);
    }
}
