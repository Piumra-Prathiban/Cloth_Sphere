package com.clothsphere.controller.ProductManagement;

import com.clothsphere.model.ProductManagement.Banners;
import com.clothsphere.service.ProductManagement.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin(origins = "http://localhost:63342")

public class BannerController {
    @Autowired
    private BannerService bannerService;

    // ✅ Create or Update banner with single image
    @PostMapping("/save")
    public Banners saveBanner(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "priority", required = false, defaultValue = "1") Integer priority,
            @RequestParam(value = "status", required = false, defaultValue = "Inactive") String status,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        Banners banner = new Banners();
        banner.setId(id);
        banner.setName(name);
        banner.setPriority(priority);
        banner.setStatus(status);

        // Handle image file
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadDir = "static/UploadBanner/";
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            try {
                Path filePath = Paths.get(uploadDir + fileName);
                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                banner.setImage(fileName); // save filename/path to DB
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bannerService.saveOrUpdate(banner);
    }

    // ✅ Get all banners
    @GetMapping("/list")
    public List<Banners> getAllBanners() {
        List<Banners> banners = bannerService.getAll();

        banners.forEach(b -> {
            if (b.getImage() != null && !b.getImage().isEmpty()) {
                b.setImage("/UploadBanner/" + b.getImage()); // prepend URL path
            }
        });
        return banners;
    }

    // ✅ Get banner by ID
    @GetMapping("/{id}")
    public Banners getBannerById(@PathVariable Long id) {
        return bannerService.getById(id);
    }

    // ✅ Delete banner
    @DeleteMapping("/{id}")
    public String deleteBanner(@PathVariable Long id) {
        bannerService.delete(id);
        return "Banner deleted successfully!";
    }
}
