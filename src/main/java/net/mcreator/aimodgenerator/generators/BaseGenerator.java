package net.mcreator.aimodgenerator.generators;

import net.mcreator.workspace.Workspace;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for all mod element generators
 * Provides common functionality and utilities
 */
public abstract class BaseGenerator {
    
    protected final Workspace workspace;
    protected final AIIntegrationService aiService;
    
    public BaseGenerator(Workspace workspace, AIIntegrationService aiService) {
        this.workspace = workspace;
        this.aiService = aiService;
    }
    
    /**
     * Sanitizes a name to be valid for MCreator elements
     * @param name The original name
     * @return Sanitized name suitable for MCreator
     */
    protected String sanitizeElementName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "GeneratedElement";
        }
        
        // Remove special characters and replace spaces with underscores
        String sanitized = name.trim()
                              .replaceAll("[^a-zA-Z0-9\\s]", "")
                              .replaceAll("\\s+", "_");
        
        // Ensure it starts with a letter
        if (!sanitized.matches("^[a-zA-Z].*")) {
            sanitized = "Item_" + sanitized;
        }
        
        // Limit length
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        return sanitized;
    }
    
    /**
     * Extracts a string value from a line of text
     * @param line The line containing the value
     * @return The extracted value
     */
    protected String extractValue(String line) {
        // Look for patterns like "property: value" or "property = value"
        Pattern pattern = Pattern.compile("[:=]\\s*(.+)$");
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // Fallback: return everything after the first space
        String[] parts = line.split("\\s+", 2);
        return parts.length > 1 ? parts[1].trim() : "";
    }
    
    /**
     * Extracts an integer value from a line of text
     * @param line The line containing the value
     * @param defaultValue Default value if extraction fails
     * @return The extracted integer value
     */
    protected int extractIntValue(String line, int defaultValue) {
        String value = extractValue(line);
        
        try {
            // Extract just the numeric part
            Pattern numberPattern = Pattern.compile("(\\d+)");
            Matcher matcher = numberPattern.matcher(value);
            
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            // Fall through to default
        }
        
        return defaultValue;
    }
    
    /**
     * Extracts a double value from a line of text
     * @param line The line containing the value
     * @param defaultValue Default value if extraction fails
     * @return The extracted double value
     */
    protected double extractDoubleValue(String line, double defaultValue) {
        String value = extractValue(line);
        
        try {
            // Extract numeric part (including decimals)
            Pattern numberPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
            Matcher matcher = numberPattern.matcher(value);
            
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            // Fall through to default
        }
        
        return defaultValue;
    }
    
    /**
     * Extracts a boolean value from a line of text
     * @param line The line containing the value
     * @param defaultValue Default value if extraction fails
     * @return The extracted boolean value
     */
    protected boolean extractBooleanValue(String line, boolean defaultValue) {
        String value = extractValue(line).toLowerCase();
        
        if (value.contains("true") || value.contains("yes") || value.contains("enabled")) {
            return true;
        } else if (value.contains("false") || value.contains("no") || value.contains("disabled")) {
            return false;
        }
        
        return defaultValue;
    }
    
    /**
     * Capitalizes the first letter of each word
     * @param text The text to capitalize
     * @return Capitalized text
     */
    protected String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Converts a string to camelCase
     * @param text The text to convert
     * @return CamelCase text
     */
    protected String toCamelCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.split("[\\s_-]+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() > 0) {
                if (i == 0) {
                    result.append(word.toLowerCase());
                } else {
                    result.append(Character.toUpperCase(word.charAt(0)))
                          .append(word.substring(1).toLowerCase());
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * Converts a string to snake_case
     * @param text The text to convert
     * @return snake_case text
     */
    protected String toSnakeCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        return text.trim()
                  .replaceAll("[\\s-]+", "_")
                  .toLowerCase();
    }
    
    /**
     * Validates that a name is suitable for Minecraft registry names
     * @param name The name to validate
     * @return True if valid, false otherwise
     */
    protected boolean isValidRegistryName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Minecraft registry names must be lowercase and contain only letters, numbers, and underscores
        return name.matches("^[a-z0-9_]+$") && name.length() <= 64;
    }
    
    /**
     * Creates a valid registry name from a display name
     * @param displayName The display name
     * @return Valid registry name
     */
    protected String createRegistryName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return "generated_element";
        }
        
        String registryName = displayName.toLowerCase()
                                        .replaceAll("[^a-z0-9\\s]", "")
                                        .replaceAll("\\s+", "_")
                                        .replaceAll("_{2,}", "_")
                                        .replaceAll("^_|_$", "");
        
        // Ensure it starts with a letter
        if (!registryName.matches("^[a-z].*")) {
            registryName = "item_" + registryName;
        }
        
        // Limit length
        if (registryName.length() > 64) {
            registryName = registryName.substring(0, 64);
        }
        
        return registryName;
    }
    
    /**
     * Clamps a value between min and max
     * @param value The value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    protected int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamps a double value between min and max
     * @param value The value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    protected double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Checks if a string contains any of the given keywords
     * @param text The text to check
     * @param keywords Keywords to look for
     * @return True if any keyword is found
     */
    protected boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the workspace associated with this generator
     * @return The workspace
     */
    public Workspace getWorkspace() {
        return workspace;
    }
    
    /**
     * Gets the AI service associated with this generator
     * @return The AI service
     */
    public AIIntegrationService getAiService() {
        return aiService;
    }
}

