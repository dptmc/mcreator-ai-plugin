package net.mcreator.aimodgenerator.core;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data class for storing the analysis of a user prompt
 * Extracts and organizes information about what mod elements to generate
 */
public class PromptAnalysis {
    
    private final String originalPrompt;
    private final String aiAnalysis;
    
    // Extracted elements
    private List<String> itemNames;
    private List<String> blockNames;
    private List<String> enchantmentNames;
    private List<String> procedureNames;
    
    // Properties and themes
    private Map<String, String> itemProperties;
    private Map<String, String> blockProperties;
    private String theme;
    private String style;
    
    public PromptAnalysis(String originalPrompt, String aiAnalysis) {
        this.originalPrompt = originalPrompt;
        this.aiAnalysis = aiAnalysis;
        
        // Initialize collections
        this.itemNames = new ArrayList<>();
        this.blockNames = new ArrayList<>();
        this.enchantmentNames = new ArrayList<>();
        this.procedureNames = new ArrayList<>();
        this.itemProperties = new HashMap<>();
        this.blockProperties = new HashMap<>();
        
        // Parse the prompt and AI analysis
        parsePrompt();
        parseAIAnalysis();
    }
    
    /**
     * Parses the original prompt using basic keyword detection
     */
    private void parsePrompt() {
        String lowerPrompt = originalPrompt.toLowerCase();
        
        // Common item keywords
        if (containsAny(lowerPrompt, "sword", "weapon", "tool", "armor", "helmet", "chestplate", "leggings", "boots")) {
            extractItemsFromPrompt();
        }
        
        // Common block keywords
        if (containsAny(lowerPrompt, "block", "ore", "stone", "wood", "crafting table", "furnace")) {
            extractBlocksFromPrompt();
        }
        
        // Common enchantment keywords
        if (containsAny(lowerPrompt, "enchantment", "enchant", "magic", "spell", "curse")) {
            extractEnchantsFromPrompt();
        }
        
        // Extract theme and style
        extractThemeAndStyle();
    }
    
    /**
     * Parses the AI analysis response for more detailed information
     */
    private void parseAIAnalysis() {
        if (aiAnalysis == null || aiAnalysis.trim().isEmpty()) {
            return;
        }
        
        // Try to extract structured information from AI response
        // This would be more sophisticated in a real implementation
        String[] lines = aiAnalysis.split("\n");
        String currentSection = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.toLowerCase().contains("items:") || line.toLowerCase().contains("1. items")) {
                currentSection = "items";
            } else if (line.toLowerCase().contains("blocks:") || line.toLowerCase().contains("2. blocks")) {
                currentSection = "blocks";
            } else if (line.toLowerCase().contains("enchantments:") || line.toLowerCase().contains("4. enchantments")) {
                currentSection = "enchantments";
            } else if (line.toLowerCase().contains("procedures:") || line.toLowerCase().contains("5. special behaviors")) {
                currentSection = "procedures";
            } else if (line.toLowerCase().contains("theme:") || line.toLowerCase().contains("6. theme")) {
                currentSection = "theme";
            } else {
                // Parse content based on current section
                parseLineForSection(line, currentSection);
            }
        }
    }
    
    private void parseLineForSection(String line, String section) {
        switch (section) {
            case "items":
                if (line.startsWith("-") || line.matches("^\\d+\\..*")) {
                    String itemName = extractNameFromLine(line);
                    if (!itemName.isEmpty() && !itemNames.contains(itemName)) {
                        itemNames.add(itemName);
                    }
                }
                break;
            case "blocks":
                if (line.startsWith("-") || line.matches("^\\d+\\..*")) {
                    String blockName = extractNameFromLine(line);
                    if (!blockName.isEmpty() && !blockNames.contains(blockName)) {
                        blockNames.add(blockName);
                    }
                }
                break;
            case "enchantments":
                if (line.startsWith("-") || line.matches("^\\d+\\..*")) {
                    String enchantName = extractNameFromLine(line);
                    if (!enchantName.isEmpty() && !enchantmentNames.contains(enchantName)) {
                        enchantmentNames.add(enchantName);
                    }
                }
                break;
            case "procedures":
                if (line.startsWith("-") || line.matches("^\\d+\\..*")) {
                    String procName = extractNameFromLine(line);
                    if (!procName.isEmpty() && !procedureNames.contains(procName)) {
                        procedureNames.add(procName);
                    }
                }
                break;
            case "theme":
                if (theme == null || theme.isEmpty()) {
                    theme = line;
                }
                break;
        }
    }
    
    private String extractNameFromLine(String line) {
        // Remove list markers and extract the main name
        line = line.replaceAll("^[-*\\d+\\.\\s]+", "").trim();
        
        // Extract the first part before any description
        if (line.contains("(")) {
            line = line.substring(0, line.indexOf("(")).trim();
        }
        if (line.contains(":")) {
            line = line.substring(0, line.indexOf(":")).trim();
        }
        
        return line;
    }
    
    private void extractItemsFromPrompt() {
        String lowerPrompt = originalPrompt.toLowerCase();
        
        // Look for specific item mentions
        Pattern itemPattern = Pattern.compile("(\\w+)\\s+(sword|axe|pickaxe|shovel|hoe|helmet|chestplate|leggings|boots|bow|crossbow|shield)");
        Matcher matcher = itemPattern.matcher(lowerPrompt);
        
        while (matcher.find()) {
            String itemName = matcher.group(1) + " " + matcher.group(2);
            itemName = capitalizeWords(itemName);
            if (!itemNames.contains(itemName)) {
                itemNames.add(itemName);
            }
        }
        
        // If no specific items found, infer from context
        if (itemNames.isEmpty()) {
            if (containsAny(lowerPrompt, "sword", "weapon")) {
                itemNames.add("Magic Sword");
            }
            if (containsAny(lowerPrompt, "armor", "protection")) {
                itemNames.add("Magic Armor");
            }
        }
    }
    
    private void extractBlocksFromPrompt() {
        String lowerPrompt = originalPrompt.toLowerCase();
        
        // Look for specific block mentions
        if (lowerPrompt.contains("crafting table")) {
            blockNames.add("Enhanced Crafting Table");
        }
        if (containsAny(lowerPrompt, "ore", "mineral")) {
            blockNames.add("Magic Ore");
        }
        if (containsAny(lowerPrompt, "block", "stone")) {
            blockNames.add("Magic Block");
        }
    }
    
    private void extractEnchantsFromPrompt() {
        String lowerPrompt = originalPrompt.toLowerCase();
        
        if (containsAny(lowerPrompt, "fire", "flame", "burning")) {
            enchantmentNames.add("Fire Aspect");
        }
        if (containsAny(lowerPrompt, "ice", "frost", "freeze")) {
            enchantmentNames.add("Frost Touch");
        }
        if (containsAny(lowerPrompt, "repair", "mending", "fix")) {
            enchantmentNames.add("Auto Repair");
        }
    }
    
    private void extractThemeAndStyle() {
        String lowerPrompt = originalPrompt.toLowerCase();
        
        if (containsAny(lowerPrompt, "magic", "magical", "mystical", "arcane")) {
            theme = "Magical";
        } else if (containsAny(lowerPrompt, "tech", "technology", "mechanical", "industrial")) {
            theme = "Technological";
        } else if (containsAny(lowerPrompt, "nature", "natural", "organic", "plant")) {
            theme = "Natural";
        } else if (containsAny(lowerPrompt, "dark", "shadow", "evil", "cursed")) {
            theme = "Dark";
        } else {
            theme = "Fantasy";
        }
        
        // Extract style preferences
        if (containsAny(lowerPrompt, "glowing", "glow", "light")) {
            style = "Glowing";
        } else if (containsAny(lowerPrompt, "ancient", "old", "relic")) {
            style = "Ancient";
        } else {
            style = "Modern";
        }
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String capitalizeWords(String text) {
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
    
    // Getters
    public String getOriginalPrompt() { return originalPrompt; }
    public String getAiAnalysis() { return aiAnalysis; }
    
    public List<String> getItemNames() { return new ArrayList<>(itemNames); }
    public List<String> getBlockNames() { return new ArrayList<>(blockNames); }
    public List<String> getEnchantmentNames() { return new ArrayList<>(enchantmentNames); }
    public List<String> getProcedureNames() { return new ArrayList<>(procedureNames); }
    
    public Map<String, String> getItemProperties() { return new HashMap<>(itemProperties); }
    public Map<String, String> getBlockProperties() { return new HashMap<>(blockProperties); }
    
    public String getTheme() { return theme; }
    public String getStyle() { return style; }
    
    // Utility methods
    public boolean hasItems() { return !itemNames.isEmpty(); }
    public boolean hasBlocks() { return !blockNames.isEmpty(); }
    public boolean hasEnchantments() { return !enchantmentNames.isEmpty(); }
    public boolean hasProcedures() { return !procedureNames.isEmpty(); }
    
    @Override
    public String toString() {
        return "PromptAnalysis{" +
                "originalPrompt='" + originalPrompt + '\'' +
                ", itemNames=" + itemNames +
                ", blockNames=" + blockNames +
                ", enchantmentNames=" + enchantmentNames +
                ", procedureNames=" + procedureNames +
                ", theme='" + theme + '\'' +
                ", style='" + style + '\'' +
                '}';
    }
}

