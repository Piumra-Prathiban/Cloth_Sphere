package com.clothsphere.service.ProductManagement;

import com.clothsphere.model.ProductManagement.Banners;
import com.clothsphere.repository.ProductManagement.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    // ✅ Save or Update banner
    public Banners saveOrUpdate(Banners banner) {
        if (banner.getId() != null) {
            // Check if banner exists in DB
            Banners existing = bannerRepository.findById(banner.getId()).orElse(null);
            if (existing != null) {
                // Update existing banner
                existing.setName(banner.getName());
                existing.setPriority(banner.getPriority());
                existing.setStatus(banner.getStatus());
                existing.setImage(banner.getImage());
                return bannerRepository.save(existing);
            }
        }
        // If new banner or no existing found
        return bannerRepository.save(banner);
    }

    // ✅ Get all banners
    public List<Banners> getAll() {
        return bannerRepository.findAll();
    }

    // ✅ Get banner by ID
    public Banners getById(Long id) {
        return bannerRepository.findById(id).orElse(null);
    }

    // ✅ Delete banner
    public void delete(Long id) {
        bannerRepository.deleteById(id);
    }
}
