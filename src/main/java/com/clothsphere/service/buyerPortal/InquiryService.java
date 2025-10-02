package com.clothsphere.service.buyerPortal;

import com.clothsphere.model.buyerPortal.Buyer;
import com.clothsphere.model.buyerPortal.Inquiry;
import com.clothsphere.repository.buyerPortal.InquiryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    public InquiryService(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    public Inquiry createInquiry(Inquiry inquiry) {
        if (inquiry.getBuyer() == null) {
            throw new IllegalArgumentException("Inquiry must have a buyer");
        }
        if (inquiry.getSubject() == null || inquiry.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("Inquiry must have a subject");
        }
        if (inquiry.getMessage() == null || inquiry.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Inquiry must have a message");
        }
        return inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getInquiriesByBuyer(Buyer buyer) {
        return inquiryRepository.findByBuyer(buyer);
    }

    public Optional<Inquiry> getInquiryById(Long id) {
        return inquiryRepository.findById(id);
    }

    public Inquiry respondToInquiry(Long inquiryId, String response, String respondedBy) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        inquiry.setResponse(response);
        inquiry.setRespondedBy(respondedBy);
        inquiry.setRespondedAt(LocalDateTime.now());
        inquiry.setStatus("RESOLVED");

        return inquiryRepository.save(inquiry);
    }

    public Inquiry updateInquiryStatus(Long inquiryId, String status) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));
        inquiry.setStatus(status);
        return inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAll();
    }

    public List<Inquiry> getInquiriesByStatus(String status) {
        return inquiryRepository.findByStatus(status);
    }
}
