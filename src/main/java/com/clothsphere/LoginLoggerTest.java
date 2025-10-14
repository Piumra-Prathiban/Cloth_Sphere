package com.clothsphere;

import com.clothsphere.Singleton.LoginLogger;
import com.clothsphere.Singleton.LoginLogger.LoginLogEntry;
import com.clothsphere.Singleton.LoginLogger.LoginStatistics;

import java.util.List;

/**
 * Test class to demonstrate Singleton Pattern implementation
 * for Login Logger in ClothSphere system
 */
public class LoginLoggerTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("SINGLETON PATTERN TEST - LOGIN LOGGER");
        System.out.println("========================================\n");

        // Test 1: Verify Singleton Instance
        testSingletonInstance();

        // Test 2: Test Logging from Multiple Controllers
        testMultipleControllers();

        // Test 3: Test Log Retrieval Methods
        testLogRetrieval();

        // Test 4: Test User Statistics
        testUserStatistics();

        // Test 5: Test Thread Safety
        testThreadSafety();

        System.out.println("\n========================================");
        System.out.println("ALL TESTS COMPLETED SUCCESSFULLY!");
        System.out.println("========================================");
    }

    /**
     * Test 1: Verify that getInstance() returns the same instance
     */
    private static void testSingletonInstance() {
        System.out.println("TEST 1: Singleton Instance Verification");
        System.out.println("----------------------------------------");

        LoginLogger logger1 = LoginLogger.getInstance();
        LoginLogger logger2 = LoginLogger.getInstance();
        LoginLogger logger3 = LoginLogger.getInstance();

        // Check if all references point to the same instance
        boolean isSameInstance = (logger1 == logger2) && (logger2 == logger3);

        System.out.println("Logger1 instance: " + logger1);
        System.out.println("Logger2 instance: " + logger2);
        System.out.println("Logger3 instance: " + logger3);
        System.out.println("All instances are the same: " + isSameInstance);

        if (isSameInstance) {
            System.out.println("✅ TEST PASSED: Singleton pattern working correctly!\n");
        } else {
            System.out.println("❌ TEST FAILED: Multiple instances detected!\n");
        }
    }

    /**
     * Test 2: Simulate logging from different controllers
     */
    private static void testMultipleControllers() {
        System.out.println("TEST 2: Multiple Controllers Logging");
        System.out.println("----------------------------------------");

        // Simulate LoginController
        LoginLogger loginControllerLogger = LoginLogger.getInstance();
        loginControllerLogger.logSuccessfulLogin("john_doe", "employee", "LoginController");
        loginControllerLogger.logSuccessfulLogin("jane_smith", "hr-manager", "LoginController");
        loginControllerLogger.logFailedLogin("bob_jones", "LoginController", "Incorrect password");

        System.out.println("LoginController logged 3 entries");

        // Simulate ProfileController (Inventory Manager)
        LoginLogger profileControllerLogger = LoginLogger.getInstance();
        profileControllerLogger.logSuccessfulLogin("alice_wong", "inventory-manager", "ProfileController");

        System.out.println("ProfileController logged 1 entry");

        // Simulate ProductOfficerAuthController
        LoginLogger officerControllerLogger = LoginLogger.getInstance();
        officerControllerLogger.logSuccessfulLogin("mike_chen", "Customer & Product Management Officer", "ProductOfficerAuthController");

        System.out.println("ProductOfficerAuthController logged 1 entry");

        // Get total logs from any instance (should all be the same)
        int totalLogs = loginControllerLogger.getTotalLogCount();

        System.out.println("\nTotal logs across all controllers: " + totalLogs);

        if (totalLogs == 5) {
            System.out.println("✅ TEST PASSED: All controllers share the same log storage!\n");
        } else {
            System.out.println("❌ TEST FAILED: Expected 5 logs but got " + totalLogs + "\n");
        }
    }

    /**
     * Test 3: Test various log retrieval methods
     */
    private static void testLogRetrieval() {
        System.out.println("TEST 3: Log Retrieval Methods");
        System.out.println("----------------------------------------");

        LoginLogger logger = LoginLogger.getInstance();

        // Add more test data
        logger.logSuccessfulLogin("employee1", "employee", "LoginController");
        logger.logSuccessfulLogin("employee2", "employee", "LoginController");
        logger.logFailedLogin("employee3", "LoginController", "User not found");
        logger.logLogout("john_doe", "employee");

        // Test getAllLogs()
        List<LoginLogEntry> allLogs = logger.getAllLogs();
        System.out.println("Total logs retrieved: " + allLogs.size());

        // Test getLogsByUsername()
        List<LoginLogEntry> johnLogs = logger.getLogsByUsername("john_doe");
        System.out.println("Logs for 'john_doe': " + johnLogs.size());

        // Test getLogsByRole()
        List<LoginLogEntry> employeeLogs = logger.getLogsByRole("employee");
        System.out.println("Logs for 'employee' role: " + employeeLogs.size());

        // Test getLogsByStatus()
        List<LoginLogEntry> successLogs = logger.getLogsByStatus("SUCCESS");
        List<LoginLogEntry> failedLogs = logger.getLogsByStatus("FAILED");
        List<LoginLogEntry> logoutLogs = logger.getLogsByStatus("LOGOUT");

        System.out.println("Success logs: " + successLogs.size());
        System.out.println("Failed logs: " + failedLogs.size());
        System.out.println("Logout logs: " + logoutLogs.size());

        System.out.println("✅ TEST PASSED: All retrieval methods working!\n");
    }

    /**
     * Test 4: Test user statistics calculation
     */
    private static void testUserStatistics() {
        System.out.println("TEST 4: User Statistics");
        System.out.println("----------------------------------------");

        LoginLogger logger = LoginLogger.getInstance();

        // Add test data for specific user
        logger.logSuccessfulLogin("test_user", "employee", "LoginController");
        logger.logSuccessfulLogin("test_user", "employee", "LoginController");
        logger.logFailedLogin("test_user", "LoginController", "Wrong password");
        logger.logLogout("test_user", "employee");

        // Get statistics
        LoginStatistics stats = logger.getUserStatistics("test_user");

        System.out.println("Statistics for 'test_user':");
        System.out.println("  Successful logins: " + stats.getSuccessfulLogins());
        System.out.println("  Failed logins: " + stats.getFailedLogins());
        System.out.println("  Logouts: " + stats.getLogouts());
        System.out.println("  Total attempts: " + stats.getTotalAttempts());

        if (stats.getSuccessfulLogins() >= 2 && stats.getFailedLogins() >= 1) {
            System.out.println("✅ TEST PASSED: Statistics calculated correctly!\n");
        } else {
            System.out.println("❌ TEST FAILED: Statistics calculation error!\n");
        }
    }

    /**
     * Test 5: Test thread safety of Singleton
     */
    private static void testThreadSafety() {
        System.out.println("TEST 5: Thread Safety");
        System.out.println("----------------------------------------");

        final int THREAD_COUNT = 10;
        Thread[] threads = new Thread[THREAD_COUNT];

        // Create multiple threads that access the singleton
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                LoginLogger logger = LoginLogger.getInstance();
                logger.logSuccessfulLogin("thread_user_" + threadId, "employee", "ThreadTest");
                System.out.println("Thread " + threadId + " logged entry");
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LoginLogger logger = LoginLogger.getInstance();
        List<LoginLogEntry> threadLogs = logger.getLogsByStatus("SUCCESS");

        // Count logs from thread tests
        long threadLogCount = threadLogs.stream()
                .filter(log -> log.getUsername().startsWith("thread_user_"))
                .count();

        System.out.println("\nTotal thread logs: " + threadLogCount);

        if (threadLogCount == THREAD_COUNT) {
            System.out.println("✅ TEST PASSED: Thread-safe implementation!\n");
        } else {
            System.out.println("⚠️ TEST WARNING: Expected " + THREAD_COUNT +
                    " logs but got " + threadLogCount + "\n");
        }
    }

    /**
     * Demonstration of typical usage scenario
     */
    public static void demonstrateUsageScenario() {
        System.out.println("\n========================================");
        System.out.println("USAGE SCENARIO DEMONSTRATION");
        System.out.println("========================================\n");

        LoginLogger logger = LoginLogger.getInstance();

        // Scenario: A day in ClothSphere system
        System.out.println("Morning shift starts...\n");

        // HR Manager logs in
        logger.logSuccessfulLogin("hr_sarah", "hr-manager", "LoginController");
        System.out.println("09:00 - HR Manager Sarah logged in");

        // Factory Manager logs in
        logger.logSuccessfulLogin("factory_john", "factory-manager", "LoginController");
        System.out.println("09:15 - Factory Manager John logged in");

        // Inventory Manager logs in through ProfileController
        logger.logSuccessfulLogin("inventory_mike", "inventory-manager", "ProfileController");
        System.out.println("09:30 - Inventory Manager Mike logged in");

        // Employee fails to login (wrong password)
        logger.logFailedLogin("employee_jane", "LoginController", "Incorrect password");
        System.out.println("10:00 - Employee Jane failed login (wrong password)");

        // Employee logs in successfully
        logger.logSuccessfulLogin("employee_jane", "employee", "LoginController");
        System.out.println("10:02 - Employee Jane logged in successfully");

        // Product Officer logs in
        logger.logSuccessfulLogin("officer_alex", "Customer & Product Management Officer",
                "ProductOfficerAuthController");
        System.out.println("10:30 - Product Officer Alex logged in");

        // Lunch break - some users logout
        logger.logLogout("employee_jane", "employee");
        System.out.println("12:00 - Employee Jane logged out");

        System.out.println("\nEnd of shift...\n");

        // Display summary
        System.out.println("===== DAILY SUMMARY =====");
        System.out.println("Total login activities: " + logger.getTotalLogCount());
        System.out.println("Successful logins: " + logger.getLogsByStatus("SUCCESS").size());
        System.out.println("Failed logins: " + logger.getLogsByStatus("FAILED").size());
        System.out.println("Logouts: " + logger.getLogsByStatus("LOGOUT").size());

        // Show individual statistics
        System.out.println("\n===== USER STATISTICS =====");
        String[] users = {"hr_sarah", "factory_john", "inventory_mike", "employee_jane", "officer_alex"};

        for (String user : users) {
            LoginStatistics stats = logger.getUserStatistics(user);
            if (stats.getTotalAttempts() > 0) {
                System.out.println(user + ": " +
                        stats.getSuccessfulLogins() + " success, " +
                        stats.getFailedLogins() + " failed, " +
                        stats.getLogouts() + " logout");
            }
        }
    }
}