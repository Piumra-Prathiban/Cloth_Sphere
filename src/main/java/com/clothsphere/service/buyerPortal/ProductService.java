package com.clothsphere.service.buyerPortal;

import com.clothsphere.model.buyerPortal.Product;
import com.clothsphere.repository.buyerPortal.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    public Product save(Product product) {
        return productRepository.save(product);
    }
}








