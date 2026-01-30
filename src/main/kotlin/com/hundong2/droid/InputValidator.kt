package com.hundong2.droid

/**
 * Input validation utilities for security
 */
object InputValidator {
    
    /**
     * Validate that a string contains only safe characters for shell commands
     */
    fun isSafeShellString(input: String): Boolean {
        // Allow alphanumeric, spaces, and common safe punctuation
        val safePattern = Regex("^[a-zA-Z0-9\\s.,/\\-_:]+$")
        return safePattern.matches(input)
    }
    
    /**
     * Validate that a string is a valid package name
     */
    fun isValidPackageName(packageName: String): Boolean {
        // Package names should follow Java package naming convention
        val packagePattern = Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$")
        return packagePattern.matches(packageName)
    }
    
    /**
     * Validate that a string is a numeric value
     */
    fun isNumeric(value: String): Boolean {
        return value.toIntOrNull() != null
    }
    
    /**
     * Validate that a string is within a range
     */
    fun isInRange(value: String, min: Int, max: Int): Boolean {
        val num = value.toIntOrNull() ?: return false
        return num in min..max
    }
    
    /**
     * Escape text for safe use in shell commands
     * Removes dangerous characters that could break out of quotes
     */
    fun escapeShellText(text: String): String {
        // Remove or escape potentially dangerous characters
        // IMPORTANT: Escape backslash first to avoid double-escaping
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("$", "\\$")
                  .replace("`", "\\`")
                  .replace(";", "")
                  .replace("&", "")
                  .replace("|", "")
                  .replace("\n", "")
                  .replace("\r", "")
    }
    
    /**
     * Validate device serial format
     */
    fun isValidDeviceSerial(serial: String): Boolean {
        // Device serials are typically alphanumeric with hyphens and colons
        val serialPattern = Regex("^[a-zA-Z0-9:\\-._]+$")
        return serialPattern.matches(serial) && serial.length < 256
    }
}
