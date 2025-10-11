package com.clothsphere.controller.CPM;

import com.clothsphere.model.CPM.BuyerMessage;
import com.clothsphere.model.CPM.MessageResponse;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.CPM.BuyerMessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/officer/messages")
public class OfficerMessageController {

    @Autowired
    private BuyerMessageService messageService;

    /**
     * Display all buyer messages
     */
    @GetMapping
    public String viewMessages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            HttpSession session,
            Model model) {

        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        List<BuyerMessage> messages;

        // Apply filters
        if (status != null && !status.trim().isEmpty()) {
            messages = messageService.getMessagesByStatus(status);
        } else if (type != null && !type.trim().isEmpty()) {
            messages = messageService.getMessagesByType(type);
        } else {
            messages = messageService.getAllMessages();
        }

        // Get statistics
        Map<String, Long> messageStats = messageService.getMessageStatistics();

        model.addAttribute("officer", officer);
        model.addAttribute("messages", messages);
        model.addAttribute("messageStats", messageStats);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterType", type);

        return "officerMessages";
    }

    /**
     * Display message detail page
     */
    @GetMapping("/view/{id}")
    public String viewMessageDetail(@PathVariable String id, HttpSession session, Model model) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        Optional<BuyerMessage> messageOpt = messageService.getMessageById(id);
        if (messageOpt.isEmpty()) {
            return "redirect:/officer/messages?error=Message not found";
        }

        BuyerMessage message = messageOpt.get();
        List<MessageResponse> responses = messageService.getResponsesByMessageId(id);

        model.addAttribute("officer", officer);
        model.addAttribute("message", message);
        model.addAttribute("responses", responses);

        return "officerMessageDetail";
    }

    /**
     * Add response to a message - AJAX
     */
    @PostMapping("/respond")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> respondToMessage(
            @RequestParam String messageId,
            @RequestParam String responseText,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
            if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.ok(response);
            }

            MessageResponse messageResponse = messageService.addResponse(
                    messageId,
                    responseText,
                    officer.getUserName()
            );

            response.put("success", true);
            response.put("message", "Response added successfully");
            response.put("responseId", messageResponse.getResponseId());
            response.put("responseDate", messageResponse.getResponseDate().toString());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update message status - AJAX
     */
    @PostMapping("/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMessageStatus(
            @RequestParam String messageId,
            @RequestParam String status,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
            if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.ok(response);
            }

            BuyerMessage message = messageService.updateMessageStatus(
                    messageId,
                    status,
                    officer.getUserName()
            );

            response.put("success", true);
            response.put("message", "Status updated to " + status);
            response.put("newStatus", message.getStatus());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update message priority - AJAX
     */
    @PostMapping("/update-priority")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateMessagePriority(
            @RequestParam String messageId,
            @RequestParam String priority,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
            if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.ok(response);
            }

            BuyerMessage message = messageService.updateMessagePriority(messageId, priority);

            response.put("success", true);
            response.put("message", "Priority updated to " + priority);
            response.put("newPriority", message.getPriority());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Mark message as resolved
     */
    @PostMapping("/resolve/{id}")
    public String resolveMessage(@PathVariable String id, HttpSession session) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        try {
            messageService.updateMessageStatus(id, "resolved", officer.getUserName());
            return "redirect:/officer/messages/view/" + id + "?success=Message marked as resolved";
        } catch (Exception e) {
            return "redirect:/officer/messages/view/" + id + "?error=" + e.getMessage();
        }
    }

    /**
     * Delete message
     */
    @PostMapping("/delete/{id}")
    public String deleteMessage(@PathVariable String id, HttpSession session) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        try {
            messageService.deleteMessage(id);
            return "redirect:/officer/messages?success=Message deleted successfully";
        } catch (Exception e) {
            return "redirect:/officer/messages?error=" + e.getMessage();
        }
    }

    /**
     * Get message statistics - AJAX
     */
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getMessageStatistics(HttpSession session) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Long> stats = messageService.getMessageStatistics();
        return ResponseEntity.ok(stats);
    }
}
