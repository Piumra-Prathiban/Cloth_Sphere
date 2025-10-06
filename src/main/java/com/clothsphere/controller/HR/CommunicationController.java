package com.clothsphere.controller.HR;

import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.SystemUserRepository;
import com.clothsphere.service.HR.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/communication")
public class CommunicationController {

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private SystemUserRepository systemUserRepository;

    /**
     * Show communication page
     */
    @GetMapping("/internal")
    public String communicationPage(HttpSession session, Model model) {
        SystemUser currentUser = (SystemUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/systemUserLogin";
        }

        // Set user email in session
        session.setAttribute("userEmail", currentUser.getEmail());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentUserEmail", currentUser.getEmail());

        return "communication/internal-communication";
    }

    /**
     * Get available users for messaging dropdown
     */
    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAvailableUsers(@RequestParam String currentUserEmail) {
        Map<String, Object> result = communicationService.getAvailableUsers(currentUserEmail);
        return ResponseEntity.ok(result);
    }

    /**
     * Send message using service layer
     */
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam String senderEmail,
            @RequestParam String receiverEmail,
            @RequestParam String subject,
            @RequestParam String messageText) {

        Map<String, Object> result = communicationService.sendMessage(senderEmail, receiverEmail, subject, messageText);
        return ResponseEntity.ok(result);
    }

    /**
     * Get inbox messages with improved error handling
     */
    @GetMapping("/inbox")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInboxMessages(@RequestParam String userEmail) {
        Map<String, Object> result = communicationService.getInboxMessages(userEmail);
        return ResponseEntity.ok(result);
    }

    /**
     * Get sent messages
     */
    @GetMapping("/sent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSentMessages(@RequestParam String userEmail) {
        Map<String, Object> result = communicationService.getSentMessages(userEmail);
        return ResponseEntity.ok(result);
    }

    /**
     * Get message details by ID
     */
    @GetMapping("/message/{messageId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMessageDetails(@PathVariable int messageId) {
        Map<String, Object> result = communicationService.getMessageDetails(messageId);
        return ResponseEntity.ok(result);
    }

    /**
     * Mark message as read
     */
    @PostMapping("/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@RequestParam int messageId) {
        Map<String, Object> result = communicationService.markMessageAsRead(messageId);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete message (soft delete)
     */
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMessage(@RequestParam int messageId) {
        Map<String, Object> result = communicationService.deleteMessage(messageId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get message statistics using GROUPING WITH HAVING
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMessageStatistics(@RequestParam String userEmail) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get role-based statistics (GROUPING WITH HAVING)
            Map<String, Object> statisticsResult = communicationService.getMessageStatistics();

            // Get user's personal summary
            Map<String, Object> userSummaryResult = communicationService.getUserCommunicationSummary(userEmail);

            if (statisticsResult.get("success").equals(true) && userSummaryResult.get("success").equals(true)) {
                result.put("success", true);
                result.put("statistics", statisticsResult.get("statistics"));
                result.put("userSummary", userSummaryResult.get("summary"));
            } else {
                result.put("success", false);
                result.put("message", "Error fetching statistics");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error fetching statistics: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get active communicators using SUBQUERY
     */
    @GetMapping("/active-communicators")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getActiveCommunicators() {
        Map<String, Object> result = communicationService.getActiveCommunicators();
        return ResponseEntity.ok(result);
    }

    /**
     * Get conversation between two users
     */
    @GetMapping("/conversation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConversation(
            @RequestParam String user1,
            @RequestParam String user2) {

        Map<String, Object> result = communicationService.getConversation(user1, user2);
        return ResponseEntity.ok(result);
    }

    /**
     * Check if communication is allowed
     */
    @GetMapping("/can-communicate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> canCommunicate(
            @RequestParam String senderEmail,
            @RequestParam String receiverEmail) {

        Map<String, Object> result = new HashMap<>();

        try {
            // Use service to check communication permission
            Optional<SystemUser> senderOpt = systemUserRepository.findByEmail(senderEmail);
            Optional<SystemUser> receiverOpt = systemUserRepository.findByEmail(receiverEmail);

            if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
                result.put("success", false);
                result.put("canCommunicate", false);
                result.put("message", "User not found");
                return ResponseEntity.ok(result);
            }

            SystemUser sender = senderOpt.get();
            SystemUser receiver = receiverOpt.get();

            // Check if employee is trying to message factory manager
            boolean canCommunicate = !("Employee".equals(sender.getRole()) && "Factory Manager".equals(receiver.getRole()));

            result.put("success", true);
            result.put("canCommunicate", canCommunicate);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error checking communication permission: " + e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }
}