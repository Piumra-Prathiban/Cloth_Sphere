package com.clothsphere.service.CPM;

import com.clothsphere.model.CPM.ProductPricingHistory;
import com.clothsphere.model.CPM.Product;
import com.clothsphere.repository.CPM.ProductPricingHistoryRepository;
import com.clothsphere.repository.CPM.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductManagementService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductPricingHistoryRepository pricingHistoryRepository;

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get all active products
     */
    public List<Product> getActiveProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .toList();
    }

    /**
     * Get product by ID
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by product ID (string)
     */
    public Optional<Product> getProductByProductId(String productId) {
        return productRepository.findByProductId(productId);
    }

    /**
     * Search products by name or code
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                           p.getCode().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Create a new product
     */
    public Product createProduct(Product product, String createdBy) {
        // Generate product ID if not set
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            product.setProductId(generateProductId());
        }

        // Set audit fields
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setCreatedBy(createdBy);
        product.setUpdatedBy(createdBy);
        product.setIsActive(true);

        return productRepository.save(product);
    }

    /**
     * Update product information
     */
    public Product updateProduct(Long id, Product updatedProduct, String updatedBy) {
        Optional<Product> existingProductOpt = productRepository.findById(id);

        if (existingProductOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + id);
        }

        Product existingProduct = existingProductOpt.get();

        // Check if price changed and record it
        if (!existingProduct.getPrice().equals(updatedProduct.getPrice())) {
            recordPriceChange(existingProduct.getProductId(),
                    existingProduct.getPrice(),
                    updatedProduct.getPrice(),
                    "Price updated by officer",
                    updatedBy);
        }

        // Update fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setCode(updatedProduct.getCode());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setImagePath(updatedProduct.getImagePath());
        existingProduct.setUpdatedAt(LocalDateTime.now());
        existingProduct.setUpdatedBy(updatedBy);

        return productRepository.save(existingProduct);
    }

    /**
     * Update product price only
     */
    public Product updateProductPrice(Long id, Double newPrice, String reason, String updatedBy) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + id);
        }

        Product product = productOpt.get();
        Double oldPrice = product.getPrice();

        // Record price change
        recordPriceChange(product.getProductId(), oldPrice, newPrice, reason, updatedBy);

        // Update price
        product.setPrice(newPrice);
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(updatedBy);

        return productRepository.save(product);
    }

    /**
     * Update product stock
     */
    public Product updateProductStock(Long id, Integer newStock, String updatedBy) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + id);
        }

        Product product = productOpt.get();
        product.setStock(newStock);
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(updatedBy);

        return productRepository.save(product);
    }

    /**
     * Activate/Deactivate product
     */
    public Product toggleProductStatus(Long id, String updatedBy) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + id);
        }

        Product product = productOpt.get();
        // Handle null isActive (for existing products)
        Boolean currentStatus = product.getIsActive();
        product.setIsActive(currentStatus == null || !currentStatus);
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(updatedBy);

        return productRepository.save(product);
    }

    /**
     * Delete product (soft delete by deactivating)
     */
    public void deleteProduct(Long id, String deletedBy) {
        Optional<Product> productOpt = productRepository.findById(id);

        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + id);
        }

        Product product = productOpt.get();
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(deletedBy);
        productRepository.save(product);
    }

    /**
     * Permanently delete product (hard delete)
     */
    public void permanentlyDeleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Get price history for a product
     */
    public List<ProductPricingHistory> getProductPriceHistory(String productId) {
        return pricingHistoryRepository.findByProductIdOrderByChangedAtDesc(productId);
    }

    /**
     * Get recent price changes (all products)
     */
    public List<ProductPricingHistory> getRecentPriceChanges() {
        return pricingHistoryRepository.findTop10ByOrderByChangedAtDesc();
    }

    /**
     * Record price change in history
     */
    private void recordPriceChange(String productId, Double oldPrice, Double newPrice,
                                   String reason, String changedBy) {
        ProductPricingHistory history = new ProductPricingHistory(
                productId, oldPrice, newPrice, reason, changedBy
        );
        pricingHistoryRepository.save(history);
    }

    /**
     * Generate unique product ID
     */
    private String generateProductId() {
        long count = productRepository.count();
        return String.format("PROD%06d", count + 1);
    }

    /**
     * Check if product code exists
     */
    public boolean productCodeExists(String code) {
        return productRepository.findByCode(code).isPresent();
    }

    /**
     * Check if product ID exists
     */
    public boolean productIdExists(String productId) {
        return productRepository.findByProductId(productId).isPresent();
    }
}
