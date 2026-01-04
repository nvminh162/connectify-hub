package com.nvminh162.notification;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableFeignClients
public class NotificationServiceApplication {
    public static void main(String[] args) {
        // Try to find .env file in multiple locations
        String currentDir = System.getProperty("user.dir");
        String envPath = null;
        File envFile = null;

        // Strategy 1: Check current directory
        envFile = new File(currentDir, ".env");
        if (envFile.exists()) {
            envPath = currentDir;
        }
        // Strategy 2: Check notification-service subdirectory (when running from parent)
        else {
            File notificationServiceDir = new File(currentDir, "notification-service");
            envFile = new File(notificationServiceDir, ".env");
            if (envFile.exists()) {
                envPath = notificationServiceDir.getAbsolutePath();
            }
        }
        // Strategy 3: Check parent directory (when running from target/classes)
        if (envPath == null) {
            File parentDir = new File(currentDir).getParentFile();
            if (parentDir != null) {
                envFile = new File(parentDir, ".env");
                if (envFile.exists()) {
                    envPath = parentDir.getAbsolutePath();
                }
            }
        }
        // Strategy 4: Find based on class location (notification-service directory)
        if (envPath == null) {
            try {
                java.net.URL classUrl = NotificationServiceApplication.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation();
                String classPath = classUrl.getPath();
                // Decode URL-encoded paths (especially for Windows)
                if (classPath.startsWith("/")
                        && System.getProperty("os.name").toLowerCase().contains("win")) {
                    classPath = classPath.substring(1);
                }
                // If running from target/classes, go up to notification-service
                if (classPath.contains("target/classes") || classPath.contains("target\\classes")) {
                    File classesDir = new File(classPath);
                    File projectRoot = classesDir.getParentFile().getParentFile();
                    envFile = new File(projectRoot, ".env");
                    if (envFile.exists()) {
                        envPath = projectRoot.getAbsolutePath();
                    }
                }
            } catch (Exception e) {
                // Ignore and continue with fallback
            }
        }

        Dotenv dotenv;
        if (envPath != null) {
            System.out.println("Loading .env from: " + envPath);
            dotenv = Dotenv.configure().directory(envPath).ignoreIfMissing().load();
        } else {
            System.out.println("Warning: .env file not found. Trying default location: " + currentDir);
            // Fallback to default behavior (current directory)
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        }

        // Load environment variables into system properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            System.out.println("Loaded env: " + entry.getKey() + " = "
                    + (entry.getKey().contains("KEY") || entry.getKey().contains("PASSWORD")
                            ? "***"
                            : entry.getValue()));
        });

        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
