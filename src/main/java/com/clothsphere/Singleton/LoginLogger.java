package com.clothsphere.Singleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Pattern Implementation for Login Activity Logging
 * This class maintains a centralized log of all system login activities
 * across different user roles and controllers.
 */
public class LoginLogger {

    // Single instance of the logger (Singleton)
    private static LoginLogger instance;

    // Thread-safe log storage
    private final List<LoginLogEntry> logEntries;

    // Date formatter for consistent timestamp formatting
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Private constructor to prevent direct instantiation
     * This is a key characteristic of the Singleton pattern
     */
    private LoginLogger() {
        this.logEntries = Collections.synchronizedList(new ArrayList<>());
        System.out.println("LoginLogger instance created at: " +
                LocalDateTime.now().format(formatter));
    }

    /**
     * Thread-safe getInstance method using double-checked locking
     * This ensures only one instance exists throughout the application
     *
     * @return The single instance of LoginLogger
     */
    public static synchronized LoginLogger getInstance() {
        if (instance == null) {
            instance = new LoginLogger();
        }
        return instance;
    }

    /**
     * Log a successful login attempt
     *
     * @param username The username of the logged-in user
     * @param role The role of the user
     * @param controller The controller handling the login
     */
    public void logSuccessfulLogin(String username, String role, String controller) {
        LoginLogEntry entry = new LoginLogEntry(
                username,
                role,
                controller,
                "SUCCESS",
                LocalDateTime.now(),
                "User logged in successfully"
        );
        logEntries.add(entry);

        System.out.println("=== LOGIN SUCCESS LOGGED ===");
        System.out.println("User: " + username);
        System.out.println("Role: " + role);
        System.out.println("Controller: " + controller);
        System.out.println("Time: " + entry.getTimestamp().format(formatter));
        System.out.println("Total logs: " + logEntries.size());
    }

    /**
     * Log a failed login attempt
     *
     * @param username The attempted username
     * @param controller The controller handling the login
     * @param reason The reason for failure
     */
    public void logFailedLogin(String username, String controller, String reason) {
        LoginLogEntry entry = new LoginLogEntry(
                username,
                "UNKNOWN",
                controller,
                "FAILED",
                LocalDateTime.now(),
                reason
        );
        logEntries.add(entry);

        System.out.println("=== LOGIN FAILURE LOGGED ===");
        System.out.println("User: " + username);
        System.out.println("Controller: " + controller);
        System.out.println("Reason: " + reason);
        System.out.println("Time: " + entry.getTimestamp().format(formatter));
    }

    /**
     * Log a logout event
     *
     * @param username The username of the user logging out
     * @param role The role of the user
     */
    public void logLogout(String username, String role) {
        LoginLogEntry entry = new LoginLogEntry(
                username,
                role,
                "LogoutController",
                "LOGOUT",
                LocalDateTime.now(),
                "User logged out"
        );
        logEntries.add(entry);

        System.out.println("=== LOGOUT LOGGED ===");
        System.out.println("User: " + username);
        System.out.println("Role: " + role);
        System.out.println("Time: " + entry.getTimestamp().format(formatter));
    }

    /**
     * Log first-time login for employees
     *
     * @param username The username of the employee
     * @param controller The controller handling the login
     */
    public void logFirstTimeLogin(String username, String controller) {
        LoginLogEntry entry = new LoginLogEntry(
                username,
                "employee",
                controller,
                "FIRST_LOGIN",
                LocalDateTime.now(),
                "First-time employee login - password change required"
        );
        logEntries.add(entry);

        System.out.println("=== FIRST-TIME LOGIN LOGGED ===");
        System.out.println("User: " + username);
        System.out.println("Controller: " + controller);
        System.out.println("Time: " + entry.getTimestamp().format(formatter));
    }

    /**
     * Get all log entries (read-only)
     *
     * @return Unmodifiable list of all log entries
     */
    public List<LoginLogEntry> getAllLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logEntries));
    }

    /**
     * Get logs for a specific user
     *
     * @param username The username to filter by
     * @return List of log entries for the specified user
     */
    public List<LoginLogEntry> getLogsByUsername(String username) {
        List<LoginLogEntry> userLogs = new ArrayList<>();
        for (LoginLogEntry entry : logEntries) {
            if (entry.getUsername().equals(username)) {
                userLogs.add(entry);
            }
        }
        return userLogs;
    }

    /**
     * Get logs for a specific role
     *
     * @param role The role to filter by
     * @return List of log entries for the specified role
     */
    public List<LoginLogEntry> getLogsByRole(String role) {
        List<LoginLogEntry> roleLogs = new ArrayList<>();
        for (LoginLogEntry entry : logEntries) {
            if (entry.getRole().equalsIgnoreCase(role)) {
                roleLogs.add(entry);
            }
        }
        return roleLogs;
    }

    /**
     * Get logs by status (SUCCESS, FAILED, LOGOUT)
     *
     * @param status The status to filter by
     * @return List of log entries with the specified status
     */
    public List<LoginLogEntry> getLogsByStatus(String status) {
        List<LoginLogEntry> statusLogs = new ArrayList<>();
        for (LoginLogEntry entry : logEntries) {
            if (entry.getStatus().equals(status)) {
                statusLogs.add(entry);
            }
        }
        return statusLogs;
    }

    /**
     * Get the total number of log entries
     *
     * @return Total count of logs
     */
    public int getTotalLogCount() {
        return logEntries.size();
    }

    /**
     * Get login statistics for a specific user
     *
     * @param username The username to analyze
     * @return A map containing login statistics
     */
    public LoginStatistics getUserStatistics(String username) {
        List<LoginLogEntry> userLogs = getLogsByUsername(username);

        int successCount = 0;
        int failedCount = 0;
        int logoutCount = 0;

        for (LoginLogEntry entry : userLogs) {
            switch (entry.getStatus()) {
                case "SUCCESS":
                case "FIRST_LOGIN":
                    successCount++;
                    break;
                case "FAILED":
                    failedCount++;
                    break;
                case "LOGOUT":
                    logoutCount++;
                    break;
            }
        }

        return new LoginStatistics(username, successCount, failedCount, logoutCount);
    }

    /**
     * Clear all log entries (use with caution)
     */
    public void clearLogs() {
        logEntries.clear();
        System.out.println("All login logs cleared at: " +
                LocalDateTime.now().format(formatter));
    }

    /**
     * Print all logs to console (for debugging)
     */
    public void printAllLogs() {
        System.out.println("\n=== ALL LOGIN LOGS ===");
        System.out.println("Total entries: " + logEntries.size());
        System.out.println("-----------------------------------");

        for (LoginLogEntry entry : logEntries) {
            System.out.println(entry);
            System.out.println("-----------------------------------");
        }
    }

    /**
     * Inner class representing a single log entry
     */
    public static class LoginLogEntry {
        private final String username;
        private final String role;
        private final String controller;
        private final String status;
        private final LocalDateTime timestamp;
        private final String message;

        public LoginLogEntry(String username, String role, String controller,
                             String status, LocalDateTime timestamp, String message) {
            this.username = username;
            this.role = role;
            this.controller = controller;
            this.status = status;
            this.timestamp = timestamp;
            this.message = message;
        }

        // Getters
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getController() { return controller; }
        public String getStatus() { return status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return String.format(
                    "LoginLog[user='%s', role='%s', controller='%s', status='%s', time='%s', msg='%s']",
                    username, role, controller, status,
                    timestamp.format(formatter), message
            );
        }
    }

    /**
     * Inner class for login statistics
     */
    public static class LoginStatistics {
        private final String username;
        private final int successfulLogins;
        private final int failedLogins;
        private final int logouts;

        public LoginStatistics(String username, int successfulLogins,
                               int failedLogins, int logouts) {
            this.username = username;
            this.successfulLogins = successfulLogins;
            this.failedLogins = failedLogins;
            this.logouts = logouts;
        }

        public String getUsername() { return username; }
        public int getSuccessfulLogins() { return successfulLogins; }
        public int getFailedLogins() { return failedLogins; }
        public int getLogouts() { return logouts; }
        public int getTotalAttempts() { return successfulLogins + failedLogins; }

        @Override
        public String toString() {
            return String.format(
                    "Statistics[user='%s', success=%d, failed=%d, logout=%d]",
                    username, successfulLogins, failedLogins, logouts
            );
        }
    }
}