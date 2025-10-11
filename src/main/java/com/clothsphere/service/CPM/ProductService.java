package com.clothsphere.service.CPM;

import com.clothsphere.model.CPM.Product;
import com.clothsphere.repository.CPM.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService{

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public Product getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }


    public Product getProductByProductId(String productId) {
        Optional<Product> product = productRepository.findByProductId(productId);
        return product.orElse(null);
    }

    public Product createProduct(Product product) {
        // Generate product ID if not provided
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            product.setProductId(generateNextProductId());
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productDetails.getName());
            product.setCode(productDetails.getCode());
            product.setPrice(productDetails.getPrice());
            product.setStock(productDetails.getStock());
            product.setDescription(productDetails.getDescription());
            product.setCategory(productDetails.getCategory());
            product.setImagePath(productDetails.getImagePath());

            return productRepository.save(product);
        }
        return null;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public String generateNextProductId() {
        String maxProductId = productRepository.findMaxProductId();

        if (maxProductId == null) {
            return "P01";
        }

        // Extract the number part and increment
        String numberPart = maxProductId.substring(1);
        int nextNumber = Integer.parseInt(numberPart) + 1;

        // Format back to P01, P02, etc.
        return String.format("P%02d", nextNumber);
    }

    public boolean isCodeExists(String code) {
        return productRepository.existsByCode(code);
    }

    public boolean isProductIdExists(String productId) {
        return productRepository.existsByProductId(productId);
    }

    public boolean updateProductStock(String productId, Integer quantitySold) {
        Optional<Product> productOpt = productRepository.findByProductId(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStock() >= quantitySold) {
                product.setStock(product.getStock() - quantitySold);
                productRepository.save(product);
                return true;
            }
        }
        return false;
    }

    public Integer getProductStock(String productId) {
        Optional<Product> product = productRepository.findByProductId(productId);
        return product.map(Product::getStock).orElse(0);
    }

    // In ProductService.java - ADD THIS METHOD
    public Product getProductByName(String name) {
        Optional<Product> product = productRepository.findByName(name);
        return product.orElse(null);
    }

}