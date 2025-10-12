package com.clothsphere.service.BY;

import com.clothsphere.model.CPM.Product;
import com.clothsphere.model.BY.Cart;
import com.clothsphere.repository.CPM.ProductRepository;
import com.clothsphere.repository.BY.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Get all cart items for a buyer with product details
     */
    public List<Cart> getCartItems(String buyerId) {
        List<Cart> cartItems = cartRepository.findByBuyerIdOrderByAddedAtDesc(buyerId);

        // Populate product details for each cart item
        for (Cart cartItem : cartItems) {
            Optional<Product> product = productRepository.findById(cartItem.getProductId());
            product.ifPresent(cartItem::setProduct);
        }

        return cartItems;
    }

    /**
     * Add item to cart or update quantity if already exists
     */
    public Cart addToCart(String buyerId, Long productId, Integer quantity) {
        // Validate product exists and has sufficient stock
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        Product product = productOpt.get();
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }

        // Check if item already in cart
        Optional<Cart> existingCart = cartRepository.findByBuyerIdAndProductId(buyerId, productId);

        if (existingCart.isPresent()) {
            // Update quantity
            Cart cart = existingCart.get();
            int newQuantity = cart.getQuantity() + quantity;

            // Check stock for new quantity
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
            }

            cart.setQuantity(newQuantity);
            return cartRepository.save(cart);
        } else {
            // Create new cart item
            Cart cart = new Cart(buyerId, productId, quantity);
            return cartRepository.save(cart);
        }
    }

    /**
     * Update cart item quantity
     */
    public Cart updateCartItemQuantity(String buyerId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Optional<Cart> cartOpt = cartRepository.findByBuyerIdAndProductId(buyerId, productId);
        if (cartOpt.isEmpty()) {
            throw new RuntimeException("Cart item not found");
        }

        // Check product stock
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        Product product = productOpt.get();
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }

        Cart cart = cartOpt.get();
        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    /**
     * Remove item from cart
     */
    public void removeFromCart(String buyerId, Long productId) {
        cartRepository.deleteByBuyerIdAndProductId(buyerId, productId);
    }

    /**
     * Remove specific cart item by cart ID
     */
    public void removeCartItem(String buyerId, Long cartId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        if (cartOpt.isPresent() && cartOpt.get().getBuyerId().equals(buyerId)) {
            cartRepository.deleteById(cartId);
        }
    }

    /**
     * Clear all items from cart (e.g., after checkout)
     */
    public void clearCart(String buyerId) {
        cartRepository.deleteAllByBuyerId(buyerId);
    }

    /**
     * Get total number of items in cart
     */
    public int getCartItemCount(String buyerId) {
        Integer count = cartRepository.getTotalQuantityByBuyer(buyerId);
        return count != null ? count : 0;
    }

    /**
     * Get number of unique products in cart
     */
    public long getUniqueProductCount(String buyerId) {
        return cartRepository.countByBuyerId(buyerId);
    }

    /**
     * Calculate cart subtotal
     */
    public BigDecimal calculateCartSubtotal(String buyerId) {
        List<Cart> cartItems = getCartItems(buyerId);
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Cart cartItem : cartItems) {
            if (cartItem.getProduct() != null) {
                BigDecimal itemTotal = BigDecimal.valueOf(cartItem.getProduct().getPrice())
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                subtotal = subtotal.add(itemTotal);
            }
        }

        return subtotal;
    }

    /**
     * Check if product is in cart
     */
    public boolean isProductInCart(String buyerId, Long productId) {
        return cartRepository.existsByBuyerIdAndProductId(buyerId, productId);
    }

    /**
     * Validate cart items (check stock availability before checkout)
     */
    public boolean validateCartItems(String buyerId) {
        List<Cart> cartItems = getCartItems(buyerId);

        for (Cart cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product == null || product.getStock() < cartItem.getQuantity()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get cart items with out of stock warning
     */
    public List<Cart> getCartItemsWithStockCheck(String buyerId) {
        List<Cart> cartItems = getCartItems(buyerId);

        // You can add additional logic here to flag items with stock issues
        for (Cart cartItem : cartItems) {
            if (cartItem.getProduct() != null) {
                Product product = cartItem.getProduct();
                // Check if quantity exceeds available stock
                if (cartItem.getQuantity() > product.getStock()) {
                    // Could set a flag or adjust quantity here
                    System.out.println("Warning: Cart quantity exceeds stock for product: " + product.getName());
                }
            }
        }

        return cartItems;
    }
}
