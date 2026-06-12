package com.example.ecommerce.config;

import com.example.ecommerce.inventory.Inventory;
import com.example.ecommerce.inventory.InventoryRepository;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRepository;
import com.example.ecommerce.user.Role;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataSeeder implements ApplicationRunner {
    private final boolean enabled;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(@Value("${app.seed.enabled}") boolean enabled,
                      UserRepository userRepository,
                      ProductRepository productRepository,
                      InventoryRepository inventoryRepository,
                      PasswordEncoder passwordEncoder) {
        this.enabled = enabled;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        seedUsers();
        seedProductsAndInventory();
    }

    private void seedUsers() {
        createUserIfMissing("Admin", "User", "admin@example.com", "+91 9000000001", Role.ADMIN);
        createUserIfMissing("Customer", "User", "customer@example.com", "+91 9000000002", Role.CUSTOMER);
    }

    private void createUserIfMissing(String firstName, String lastName, String email, String phone, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("Password123!"));
        userRepository.save(user);
    }

    private void seedProductsAndInventory() {
        if (productRepository.count() > 0) {
            return;
        }
        Product keyboard = createProduct("Mechanical Keyboard", "Hot-swappable RGB keyboard",
                new BigDecimal("129.99"), "Electronics");
        Product headphones = createProduct("Wireless Headphones", "Noise-cancelling over-ear headphones",
                new BigDecimal("199.99"), "Electronics");
        Product notebook = createProduct("Premium Notebook", "Hardcover dotted notebook",
                new BigDecimal("14.99"), "Stationery");

        createInventory(keyboard, 50);
        createInventory(headphones, 30);
        createInventory(notebook, 200);
    }

    private Product createProduct(String name, String description, BigDecimal price, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setActive(true);
        return productRepository.save(product);
    }

    private void createInventory(Product product, int availableQuantity) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setAvailableQuantity(availableQuantity);
        inventory.setReservedQuantity(0);
        inventoryRepository.save(inventory);
    }
}
