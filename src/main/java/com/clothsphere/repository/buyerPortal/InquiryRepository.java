package com.clothsphere.repository.buyerPortal;

import com.clothsphere.model.buyerPortal.Inquiry;
import com.clothsphere.model.buyerPortal.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByBuyer(Buyer buyer);
    List<Inquiry> findByBuyerOrderByCreatedAtDesc(Buyer buyer);
    List<Inquiry> findByStatus(String status);
    List<Inquiry> findByStatusOrderByCreatedAtDesc(String status);
    List<Inquiry> findByBuyerAndStatusOrderByCreatedAtDesc(Buyer buyer, String status);
}
