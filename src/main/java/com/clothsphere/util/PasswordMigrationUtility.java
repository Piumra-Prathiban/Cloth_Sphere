package com.clothsphere.util;

import com.clothsphere.model.SystemUser;
import com.clothsphere.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * One-time utility to migrate existing plain-text passwords to BCrypt encrypted passwords.
 * This will run once when the application starts and encrypt all existing passwords.
 *
 * IMPORTANT: After running this once, comment out the @Component annotation
 * to prevent it from running on every application startup.
 */
@Component // Comment this out after first successful run
public class PasswordMigrationUtility implements CommandLineRunner {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== PASSWORD MIGRATION UTILITY ===");
        System.out.println("Starting password encryption for existing users...");

        try {
            // Get all system users
            var users = systemUserRepository.findAll();
            int migratedCount = 0;
            int skippedCount = 0;

            for (SystemUser user : users) {
                String currentPassword = user.getPassword();

                // Check if password is already encrypted (BCrypt hashes start with $2a$, $2b$, or $2y$)
                if (currentPassword.startsWith("$2a$") ||
                        currentPassword.startsWith("$2b$") ||
                        currentPassword.startsWith("$2y$")) {
                    System.out.println("Skipping " + user.getUserName() + " - already encrypted");
                    skippedCount++;
                    continue;
                }

                // Encrypt the plain-text password
                String encryptedPassword = PasswordEncoder.encryptPassword(currentPassword);
                user.setPassword(encryptedPassword);
                systemUserRepository.save(user);

                System.out.println("Encrypted password for user: " + user.getUserName() + " (role: " + user.getRole() + ")");
                migratedCount++;
            }

            System.out.println("=== MIGRATION COMPLETE ===");
            System.out.println("Total users processed: " + users.size());
            System.out.println("Passwords encrypted: " + migratedCount);
            System.out.println("Already encrypted (skipped): " + skippedCount);
            System.out.println("\nIMPORTANT: Comment out @Component annotation in PasswordMigrationUtility.java");
            System.out.println("to prevent this from running again on next startup.");

        } catch (Exception e) {
            System.err.println("Error during password migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}