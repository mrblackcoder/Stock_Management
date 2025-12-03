package com.ims.stockmanagement.security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Password Validator
 * Validates passwords against common security requirements and common password lists
 */
@Component
public class PasswordValidator {

    // Common weak passwords list (expanded)
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
        // Top 100 most common passwords
        "123456", "password", "123456789", "12345678", "12345", "1234567", "1234567890",
        "qwerty", "abc123", "111111", "123123", "admin", "letmein", "welcome", "monkey",
        "dragon", "master", "1234", "login", "sunshine", "princess", "admin123", "root",
        "passw0rd", "shadow", "123321", "654321", "superman", "qazwsx", "michael", "football",
        "password1", "password123", "batman", "trustno1", "iloveyou", "whatever", "freedom",
        "qwerty123", "000000", "121212", "baseball", "starwars", "696969", "mustang",
        "access", "hello", "charlie", "donald", "password1234", "killer", "soccer",
        "jordan", "jennifer", "hunter", "ranger", "harley", "thomas", "robert",
        "soccer", "hockey", "thomas", "daniel", "andrew", "joshua", "maggie",
        "pepper", "ginger", "banana", "summer", "flower", "chicken", "cheese",
        "computer", "internet", "samsung", "secret", "test", "testing", "guest",
        // Sequential patterns
        "abcd1234", "1q2w3e4r", "qwer1234", "asdf1234", "zxcv1234",
        // Turkish common passwords
        "sifre", "sifre123", "parola", "parola123", "turkiye", "istanbul", "ankara",
        "galatasaray", "fenerbahce", "besiktas", "trabzonspor"
    ));

    // Keyboard sequential patterns
    private static final List<String> KEYBOARD_PATTERNS = Arrays.asList(
        "qwerty", "qwertyuiop", "asdfgh", "asdfghjkl", "zxcvbn", "zxcvbnm",
        "1234567890", "0987654321", "qazwsx", "wsxedc", "rfvtgb"
    );

    /**
     * Validate password strength
     * @param password The password to validate
     * @return List of validation errors (empty if valid)
     */
    public List<String> validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return errors;
        }

        // Length check (minimum 8 characters)
        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long");
        }

        // Maximum length check
        if (password.length() > 128) {
            errors.add("Password cannot exceed 128 characters");
        }

        // Uppercase check
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter");
        }

        // Lowercase check
        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter");
        }

        // Digit check
        if (!password.matches(".*[0-9].*")) {
            errors.add("Password must contain at least one digit");
        }

        // Special character check
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.add("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;':\",./<>?)");
        }

        // Common password check
        if (isCommonPassword(password)) {
            errors.add("Password is too common and easily guessable. Please choose a stronger password");
        }

        // Keyboard pattern check
        if (containsKeyboardPattern(password.toLowerCase())) {
            errors.add("Password contains keyboard patterns (e.g., qwerty, asdf). Please choose a more random password");
        }

        // Repeated characters check (more than 3 consecutive same characters)
        if (hasRepeatedCharacters(password)) {
            errors.add("Password cannot contain more than 3 consecutive repeated characters");
        }

        // Sequential characters check
        if (hasSequentialCharacters(password)) {
            errors.add("Password cannot contain sequential characters (e.g., abc, 123)");
        }

        return errors;
    }

    /**
     * Check if password is valid
     */
    public boolean isValid(String password) {
        return validate(password).isEmpty();
    }

    /**
     * Check if password is in common passwords list
     */
    private boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Direct match
        if (COMMON_PASSWORDS.contains(lowerPassword)) {
            return true;
        }
        
        // Check with common suffixes removed
        String[] suffixes = {"!", "1", "123", "1234", "12345", "@", "#", "!"};
        for (String suffix : suffixes) {
            if (lowerPassword.endsWith(suffix)) {
                String withoutSuffix = lowerPassword.substring(0, lowerPassword.length() - suffix.length());
                if (COMMON_PASSWORDS.contains(withoutSuffix)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Check if password contains keyboard patterns
     */
    private boolean containsKeyboardPattern(String password) {
        for (String pattern : KEYBOARD_PATTERNS) {
            if (password.contains(pattern)) {
                return true;
            }
            // Check reverse pattern too
            String reversed = new StringBuilder(pattern).reverse().toString();
            if (password.contains(reversed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for more than 3 consecutive repeated characters
     */
    private boolean hasRepeatedCharacters(String password) {
        int count = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                count++;
                if (count > 3) {
                    return true;
                }
            } else {
                count = 1;
            }
        }
        return false;
    }

    /**
     * Check for sequential characters (abc, 123, etc.)
     */
    private boolean hasSequentialCharacters(String password) {
        int sequentialCount = 1;
        
        for (int i = 1; i < password.length(); i++) {
            char current = password.charAt(i);
            char previous = password.charAt(i - 1);
            
            // Check if characters are sequential (ascending or descending)
            if (current == previous + 1 || current == previous - 1) {
                sequentialCount++;
                if (sequentialCount >= 4) { // More than 3 sequential chars
                    return true;
                }
            } else {
                sequentialCount = 1;
            }
        }
        
        return false;
    }

    /**
     * Calculate password strength score (0-100)
     */
    public int calculateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (up to 25 points)
        score += Math.min(password.length() * 2, 25);

        // Character variety (up to 25 points)
        if (password.matches(".*[a-z].*")) score += 5;
        if (password.matches(".*[A-Z].*")) score += 5;
        if (password.matches(".*[0-9].*")) score += 5;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score += 10;

        // Bonus for length > 12 (up to 20 points)
        if (password.length() > 12) score += Math.min((password.length() - 12) * 2, 20);

        // Penalty for common patterns
        if (isCommonPassword(password)) score -= 30;
        if (containsKeyboardPattern(password.toLowerCase())) score -= 20;
        if (hasRepeatedCharacters(password)) score -= 10;
        if (hasSequentialCharacters(password)) score -= 10;

        // Ensure score is between 0 and 100
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Get strength label based on score
     */
    public String getStrengthLabel(int score) {
        if (score < 20) return "Very Weak";
        if (score < 40) return "Weak";
        if (score < 60) return "Fair";
        if (score < 80) return "Strong";
        return "Very Strong";
    }
}
