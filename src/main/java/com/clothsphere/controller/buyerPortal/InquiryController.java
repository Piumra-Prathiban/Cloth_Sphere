package com.clothsphere.controller.buyerPortal;

import com.clothsphere.model.buyerPortal.Buyer;
import com.clothsphere.model.buyerPortal.Inquiry;
import com.clothsphere.repository.buyerPortal.BuyerRepository;
import com.clothsphere.service.buyerPortal.InquiryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private BuyerRepository buyerRepository;

    // Show inquiry form
    @GetMapping("/new")
    public String newInquiryForm(HttpSession session, Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }
        model.addAttribute("inquiry", new Inquiry());
        return "inquiry-form";
    }

    // Submit inquiry
    @PostMapping("/submit")
    public String submitInquiry(@RequestParam String subject,
                                @RequestParam String message,
                                @RequestParam String inquiryType,
                                @RequestParam(required = false) Long relatedOrderId,
                                HttpSession session,
                                Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }

        Optional<Buyer> buyerOpt = buyerRepository.findById(buyerId);
        if (!buyerOpt.isPresent()) {
            return "redirect:/login";
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setBuyer(buyerOpt.get());
        inquiry.setSubject(subject);
        inquiry.setMessage(message);
        inquiry.setInquiryType(inquiryType);
        inquiry.setRelatedOrderId(relatedOrderId);

        inquiryService.createInquiry(inquiry);

        model.addAttribute("success", "Inquiry submitted successfully!");
        return "redirect:/inquiries/my";
    }

    // View buyer's inquiries
    @GetMapping("/my")
    public String myInquiries(HttpSession session, Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }

        Optional<Buyer> buyerOpt = buyerRepository.findById(buyerId);
        if (!buyerOpt.isPresent()) {
            return "redirect:/login";
        }

        model.addAttribute("inquiries", inquiryService.getInquiriesByBuyer(buyerOpt.get()));
        return "my-inquiries";
    }

    // View specific inquiry details
    @GetMapping("/{id}")
    public String viewInquiry(@PathVariable Long id, HttpSession session, Model model) {
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            return "redirect:/login";
        }

        Optional<Inquiry> inquiryOpt = inquiryService.getInquiryById(id);
        if (!inquiryOpt.isPresent() || !inquiryOpt.get().getBuyer().getId().equals(buyerId)) {
            return "redirect:/inquiries/my";
        }

        model.addAttribute("inquiry", inquiryOpt.get());
        return "inquiry-details";
    }
}
