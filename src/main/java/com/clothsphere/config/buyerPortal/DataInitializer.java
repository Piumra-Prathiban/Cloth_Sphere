package com.clothsphere.config.buyerPortal;


import com.clothsphere.model.buyerPortal.Product;
import com.clothsphere.service.buyerPortal.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductService productService;

    public DataInitializer(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productService.getAllProducts().isEmpty()) {

            // Sample products
            Product p1 = new Product();
            p1.setName("Cotton Shirt");
            p1.setDescription("Soft cotton shirt, comfortable for daily wear.");
            p1.setPrice(25.99);
            p1.setImageUrl("/assets/images/products/cotton-shirt.jpg");

            Product p2 = new Product();
            p2.setName("Denim Jeans");
            p2.setDescription("Classic blue denim jeans, perfect fit for all occasions.");
            p2.setPrice(45.50);
            p2.setImageUrl("/assets/images/products/denim-jeans.jpg");

            Product p3 = new Product();
            p3.setName("Silk Saree");
            p3.setDescription("Elegant silk saree with traditional embroidery.");
            p3.setPrice(120.00);
            p3.setImageUrl("/assets/images/products/silk-saree.jpg");

            Product p4 = new Product();
            p4.setName("Leather Jacket");
            p4.setDescription("Premium leather jacket, stylish and durable.");
            p4.setPrice(150.00);
            p4.setImageUrl("/assets/images/products/leather-jacket.jpg");

            // Save to database
            productService.save(p1);
            productService.save(p2);
            productService.save(p3);
            productService.save(p4);

            System.out.println("Sample products inserted into database.");
        }
    }
}

