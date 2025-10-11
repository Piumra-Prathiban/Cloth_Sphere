package com.clothsphere.controller.CPM;

import com.clothsphere.model.PM.Product;
import com.clothsphere.model.SystemUser;
import com.clothsphere.service.CPM.ProductManagementService;
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
@RequestMapping("/officer/products")
public class OfficerProductController {

    @Autowired
    private ProductManagementService productService;

    /**
     * Display all products page
     */
    @GetMapping
    public String viewProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        List<Product> products;

        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else if ("active".equals(status)) {
            products = productService.getActiveProducts();
        } else if ("inactive".equals(status)) {
            products = productService.getAllProducts().stream()
                    .filter(p -> p.getIsActive() != null && !p.getIsActive())
                    .toList();
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("officer", officer);
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("status", status);

        return "officerProducts";
    }

    /**
     * Display add product form
     */
    @GetMapping("/add")
    public String showAddProductForm(HttpSession session, Model model) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("officer", officer);
        model.addAttribute("product", new Product());
        model.addAttribute("mode", "add");

        return "officerProductForm";
    }

    /**
     * Create new product
     */
    @PostMapping("/add")
    public String createProduct(
            @ModelAttribute Product product,
            HttpSession session,
            Model model) {

        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        try {
            // Check if code already exists
            if (productService.productCodeExists(product.getCode())) {
                model.addAttribute("error", "Product code already exists");
                model.addAttribute("product", product);
                model.addAttribute("officer", officer);
                model.addAttribute("mode", "add");
                return "officerProductForm";
            }

            productService.createProduct(product, officer.getUserName());
            return "redirect:/officer/products?success=Product added successfully";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to add product: " + e.getMessage());
            model.addAttribute("product", product);
            model.addAttribute("officer", officer);
            model.addAttribute("mode", "add");
            return "officerProductForm";
        }
    }

    /**
     * Display edit product form
     */
    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, HttpSession session, Model model) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/officer/products?error=Product not found";
        }

        model.addAttribute("officer", officer);
        model.addAttribute("product", productOpt.get());
        model.addAttribute("mode", "edit");

        return "officerProductForm";
    }

    /**
     * Update product
     */
    @PostMapping("/update")
    public String updateProduct(
            @RequestParam Long id,
            @ModelAttribute Product product,
            HttpSession session,
            Model model) {

        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        try {
            productService.updateProduct(id, product, officer.getUserName());
            return "redirect:/officer/products?success=Product updated successfully";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to update product: " + e.getMessage());
            model.addAttribute("product", product);
            model.addAttribute("officer", officer);
            model.addAttribute("mode", "edit");
            return "officerProductForm";
        }
    }

    /**
     * Toggle product status (activate/deactivate) - AJAX
     */
    @PostMapping("/toggle-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleProductStatus(
            @RequestParam Long id,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
            if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.ok(response);
            }

            Product product = productService.toggleProductStatus(id, officer.getUserName());

            response.put("success", true);
            response.put("message", product.getIsActive() ? "Product activated" : "Product deactivated");
            response.put("isActive", product.getIsActive());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update product price - AJAX
     */
    @PostMapping("/update-price")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProductPrice(
            @RequestParam Long id,
            @RequestParam Double newPrice,
            @RequestParam(required = false) String reason,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
            if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.ok(response);
            }

            Product product = productService.updateProductPrice(id, newPrice, reason, officer.getUserName());

            response.put("success", true);
            response.put("message", "Price updated successfully");
            response.put("newPrice", product.getPrice());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update product stock - AJAX
     */
    @PostMapping("/update-stock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProductStock(
            @RequestParam Long id,
            @RequestParam Integer newStock,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
            if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.ok(response);
            }

            Product product = productService.updateProductStock(id, newStock, officer.getUserName());

            response.put("success", true);
            response.put("message", "Stock updated successfully");
            response.put("newStock", product.getStock());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Delete product (soft delete)
     */
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        try {
            productService.deleteProduct(id, officer.getUserName());
            return "redirect:/officer/products?success=Product deleted successfully";
        } catch (Exception e) {
            return "redirect:/officer/products?error=" + e.getMessage();
        }
    }

    /**
     * View product details and price history
     */
    @GetMapping("/view/{id}")
    public String viewProductDetails(@PathVariable Long id, HttpSession session, Model model) {
        SystemUser officer = (SystemUser) session.getAttribute("loggedInUser");
        if (officer == null || !"Customer & Product Management Officer".equals(officer.getRole())) {
            return "redirect:/login";
        }

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/officer/products?error=Product not found";
        }

        Product product = productOpt.get();

        model.addAttribute("officer", officer);
        model.addAttribute("product", product);
        model.addAttribute("priceHistory", productService.getProductPriceHistory(product.getProductId()));

        return "officerProductDetail";
    }
}
