package com.clothsphere.controller.PM;

import com.clothsphere.model.PM.Product;
import com.clothsphere.service.PM.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/productId/{productId}")
    public ResponseEntity<Product> getProductByProductId(@PathVariable String productId) {
        Product product = productService.getProductByProductId(productId);
        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        // Check if code already exists
        if (productService.isCodeExists(product.getCode())) {
            return new ResponseEntity<>("Product code already exists", HttpStatus.BAD_REQUEST);
        }

        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        // Check if the product exists
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }

        // Check if code is being changed and already exists for another product
        if (!existingProduct.getCode().equals(product.getCode()) &&
                productService.isCodeExists(product.getCode())) {
            return new ResponseEntity<>("Product code already exists", HttpStatus.BAD_REQUEST);
        }

        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct != null) {
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(@RequestParam String name) {
        List<Product> products = productService.searchProductsByName(name);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/next-id")
    public ResponseEntity<String> getNextProductId() {
        String nextId = productService.generateNextProductId();
        return new ResponseEntity<>(nextId, HttpStatus.OK);
    }

    // In ProductController.java - ADD THESE ENDPOINTS
    @GetMapping("/{productId}/stock")
    public ResponseEntity<Integer> getProductStock(@PathVariable String productId) {
        Integer stock = productService.getProductStock(productId);
        return new ResponseEntity<>(stock, HttpStatus.OK);
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<?> updateProductStock(@PathVariable String productId,
                                                @RequestParam Integer quantitySold) {
        boolean success = productService.updateProductStock(productId, quantitySold);
        if (success) {
            return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Insufficient stock or product not found", HttpStatus.BAD_REQUEST);
        }
    }

    // In ProductController.java - Add this endpoint for sales dashboard
    @GetMapping("/for-sales")
    public ResponseEntity<List<Map<String, Object>>> getProductsForSales() {
        List<Product> products = productService.getAllProducts();

        List<Map<String, Object>> productList = products.stream()
                .map(product -> {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("productId", product.getProductId());
                    productMap.put("name", product.getName());
                    productMap.put("code", product.getCode());
                    productMap.put("price", product.getPrice());
                    productMap.put("stock", product.getStock());
                    productMap.put("category", product.getCategory());
                    productMap.put("description", product.getDescription());
                    return productMap;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(productList, HttpStatus.OK);
    }

}