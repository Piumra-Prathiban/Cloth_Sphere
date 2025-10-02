package com.clothsphere.repository.ProductManagement;

import com.clothsphere.model.ProductManagement.Banners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<Banners, Long> {

}
