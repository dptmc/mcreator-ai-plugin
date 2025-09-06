package net.mcreator.aimodgenerator.generators;

import net.mcreator.workspace.Workspace;
import net.mcreator.element.ModElementType;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.types.Enchantment;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for creating Minecraft enchantments using AI
 * Handles weapon, tool, armor, and special enchantments
 */
public class EnchantmentGenerator extends BaseGenerator {
    
    public EnchantmentGenerator(Workspace workspace, AIIntegrationService aiService) {
        super(workspace, aiService);
    }
    
    /**
     * Generates a new enchantment based on the provided parameters
     * @param enchantmentName Name of the enchantment to generate
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return Details about the generated enchantment
     */
    public String generateEnchantment(String enchantmentName, PromptAnalysis analysis, 
                                    SearchResults searchResults, GenerationOptions options) throws Exception {
        
        // Step 1: Determine enchantment properties using AI
        EnchantmentProperties properties = analyzeEnchantmentProperties(enchantmentName, analysis, searchResults, options);
        
        // Step 2: Create the MCreator mod element
        ModElement modElement = createEnchantmentModElement(enchantmentName, properties);
        
        // Step 3: Configure the enchantment element
        Enchantment enchantmentElement = configureEnchantmentElement(modElement, properties, options);
        
        // Step 4: Add to workspace
        workspace.addModElement(modElement);
        workspace.getModElementManager().storeModElement(enchantmentElement);
        
        // Step 5: Generate additional content
        String additionalContent = generateAdditionalEnchantmentContent(enchantmentName, properties, options);
        
        return formatEnchantmentDetails(enchantmentName, properties, additionalContent);
    }
    
    /**
     * Analyzes enchantment properties using AI and search results
     */
    private EnchantmentProperties analyzeEnchantmentProperties(String enchantmentName, PromptAnalysis analysis, 
                                                             SearchResults searchResults, GenerationOptions options) throws Exception {
        
        String analysisPrompt = buildEnchantmentAnalysisPrompt(enchantmentName, analysis, searchResults, options);
        String aiResponse = aiService.generateMinecraftContent(analysisPrompt, "enchantment");
        
        return parseEnchantmentProperties(aiResponse, enchantmentName, analysis);
    }
    
    /**
     * Builds the AI prompt for enchantment analysis
     */
    private String buildEnchantmentAnalysisPrompt(String enchantmentName, PromptAnalysis analysis, 
                                                SearchResults searchResults, GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a detailed Minecraft enchantment specification for: ").append(enchantmentName).append("\n\n");
        
        // Add context from original prompt
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n");
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Style: ").append(analysis.getStyle()).append("\n\n");
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(400, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify what we need
        prompt.append("Please provide:\n");
        prompt.append("1. Enchantment type (WEAPON, TOOL, ARMOR, BOW, FISHING_ROD, TRIDENT, etc.)\n");
        prompt.append("2. Compatible items (what items can have this enchantment)\n");
        prompt.append("3. Maximum level (1-10, most enchantments are 1-5)\n");
        prompt.append("4. Rarity (COMMON, UNCOMMON, RARE, VERY_RARE)\n");
        prompt.append("5. Effect description (what the enchantment does)\n");
        prompt.append("6. Treasure enchantment (true/false - can only be found, not crafted)\n");
        prompt.append("7. Curse enchantment (true/false - negative effect)\n");
        prompt.append("8. Incompatible enchantments (conflicts with other enchantments)\n");
        prompt.append("9. Effect strength per level\n");
        prompt.append("10. Special conditions or requirements\n\n");
        
        // Add balance requirements
        if (options.isBalancedStats()) {
            prompt.append("IMPORTANT: Ensure the enchantment is balanced for Minecraft 1.20.1 gameplay. ");
            prompt.append("Compare to vanilla enchantments and avoid overpowered effects. ");
            prompt.append("Consider the maximum level and rarity carefully.\n\n");
        }
        
        prompt.append("Format your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to extract enchantment properties
     */
    private EnchantmentProperties parseEnchantmentProperties(String aiResponse, String enchantmentName, PromptAnalysis analysis) {
        EnchantmentProperties properties = new EnchantmentProperties();
        properties.setName(enchantmentName);
        
        // Parse AI response
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            line = line.trim().toLowerCase();
            
            if (line.contains("type:") || line.contains("enchantment type:")) {
                properties.setEnchantmentType(extractValue(line));
            } else if (line.contains("compatible:") || line.contains("items:")) {
                properties.setCompatibleItems(parseCompatibleItems(extractValue(line)));
            } else if (line.contains("maximum level:") || line.contains("max level:")) {
                properties.setMaxLevel(extractIntValue(line, 1));
            } else if (line.contains("rarity:")) {
                properties.setRarity(extractValue(line));
            } else if (line.contains("effect:") || line.contains("description:")) {
                properties.setEffectDescription(extractValue(line));
            } else if (line.contains("treasure:")) {
                properties.setTreasureEnchantment(extractBooleanValue(line, false));
            } else if (line.contains("curse:")) {
                properties.setCurseEnchantment(extractBooleanValue(line, false));
            } else if (line.contains("incompatible:") || line.contains("conflicts:")) {
                properties.setIncompatibleEnchantments(parseIncompatibleEnchantments(extractValue(line)));
            } else if (line.contains("strength:") || line.contains("power:")) {
                properties.setEffectStrength(extractDoubleValue(line, 1.0));
            }
        }
        
        // Set defaults based on analysis if not specified
        if (properties.getEnchantmentType() == null) {
            properties.setEnchantmentType(inferEnchantmentType(enchantmentName, analysis));
        }
        
        if (properties.getRarity() == null) {
            properties.setRarity(inferRarity(enchantmentName, analysis));
        }
        
        // Apply balanced defaults
        applyBalancedDefaults(properties, options);
        
        return properties;
    }
    
    /**
     * Parses compatible items from text
     */
    private List<String> parseCompatibleItems(String itemsText) {
        List<String> items = new ArrayList<>();
        
        if (itemsText == null || itemsText.isEmpty()) {
            return items;
        }
        
        String[] parts = itemsText.split("[,;]");
        for (String part : parts) {
            String item = part.trim();
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
        
        return items;
    }
    
    /**
     * Parses incompatible enchantments from text
     */
    private List<String> parseIncompatibleEnchantments(String enchantmentsText) {
        List<String> enchantments = new ArrayList<>();
        
        if (enchantmentsText == null || enchantmentsText.isEmpty()) {
            return enchantments;
        }
        
        String[] parts = enchantmentsText.split("[,;]");
        for (String part : parts) {
            String enchantment = part.trim();
            if (!enchantment.isEmpty()) {
                enchantments.add(enchantment);
            }
        }
        
        return enchantments;
    }
    
    /**
     * Creates a new MCreator mod element for the enchantment
     */
    private ModElement createEnchantmentModElement(String enchantmentName, EnchantmentProperties properties) {
        String elementName = sanitizeElementName(enchantmentName);
        ModElement modElement = new ModElement(workspace, elementName, ModElementType.ENCHANTMENT);
        modElement.setRegistryName(createRegistryName(enchantmentName));
        return modElement;
    }
    
    /**
     * Configures the MCreator enchantment element with generated properties
     */
    private Enchantment configureEnchantmentElement(ModElement modElement, EnchantmentProperties properties, GenerationOptions options) {
        Enchantment enchantment = new Enchantment(modElement);
        
        // Basic properties
        enchantment.name = properties.getName();
        enchantment.maxLevel = clamp(properties.getMaxLevel(), 1, 10);
        
        // Set rarity
        enchantment.rarity = mapRarityToMCreator(properties.getRarity());
        
        // Set type and compatibility
        enchantment.type = mapTypeToMCreator(properties.getEnchantmentType());
        
        // Special properties
        enchantment.isTreasureEnchantment = properties.isTreasureEnchantment();
        enchantment.isCurse = properties.isCurseEnchantment();
        
        // Configure effects based on enchantment type
        configureEnchantmentEffects(enchantment, properties);
        
        return enchantment;
    }
    
    /**
     * Configures enchantment effects based on type and properties
     */
    private void configureEnchantmentEffects(Enchantment enchantment, EnchantmentProperties properties) {
        String type = properties.getEnchantmentType().toUpperCase();
        
        if (type.contains("WEAPON") || type.contains("SWORD")) {
            configureWeaponEnchantment(enchantment, properties);
        } else if (type.contains("TOOL")) {
            configureToolEnchantment(enchantment, properties);
        } else if (type.contains("ARMOR")) {
            configureArmorEnchantment(enchantment, properties);
        } else if (type.contains("BOW") || type.contains("CROSSBOW")) {
            configureBowEnchantment(enchantment, properties);
        } else {
            configureSpecialEnchantment(enchantment, properties);
        }
    }
    
    /**
     * Configures weapon enchantment effects
     */
    private void configureWeaponEnchantment(Enchantment enchantment, EnchantmentProperties properties) {
        // This would configure weapon-specific effects
        // Implementation depends on MCreator's enchantment API
        
        String effectDesc = properties.getEffectDescription().toLowerCase();
        
        if (effectDesc.contains("damage") || effectDesc.contains("attack")) {
            // Damage bonus enchantment
            enchantment.damageBonus = properties.getEffectStrength();
        } else if (effectDesc.contains("fire") || effectDesc.contains("flame")) {
            // Fire aspect type enchantment
            // This would be implemented with procedures in MCreator
        } else if (effectDesc.contains("knockback")) {
            // Knockback enchantment
            // This would be implemented with procedures in MCreator
        }
    }
    
    /**
     * Configures tool enchantment effects
     */
    private void configureToolEnchantment(Enchantment enchantment, EnchantmentProperties properties) {
        String effectDesc = properties.getEffectDescription().toLowerCase();
        
        if (effectDesc.contains("efficiency") || effectDesc.contains("speed")) {
            // Efficiency type enchantment
            // This would be implemented with procedures in MCreator
        } else if (effectDesc.contains("fortune") || effectDesc.contains("drop")) {
            // Fortune type enchantment
            // This would be implemented with procedures in MCreator
        } else if (effectDesc.contains("silk") || effectDesc.contains("touch")) {
            // Silk touch type enchantment
            // This would be implemented with procedures in MCreator
        }
    }
    
    /**
     * Configures armor enchantment effects
     */
    private void configureArmorEnchantment(Enchantment enchantment, EnchantmentProperties properties) {
        String effectDesc = properties.getEffectDescription().toLowerCase();
        
        if (effectDesc.contains("protection") || effectDesc.contains("defense")) {
            // Protection enchantment
            enchantment.protectionBonus = properties.getEffectStrength();
        } else if (effectDesc.contains("thorns")) {
            // Thorns type enchantment
            // This would be implemented with procedures in MCreator
        } else if (effectDesc.contains("speed") || effectDesc.contains("swift")) {
            // Speed enchantment for boots
            // This would be implemented with procedures in MCreator
        }
    }
    
    /**
     * Configures bow enchantment effects
     */
    private void configureBowEnchantment(Enchantment enchantment, EnchantmentProperties properties) {
        String effectDesc = properties.getEffectDescription().toLowerCase();
        
        if (effectDesc.contains("power") || effectDesc.contains("damage")) {
            // Power enchantment
            // This would be implemented with procedures in MCreator
        } else if (effectDesc.contains("punch") || effectDesc.contains("knockback")) {
            // Punch enchantment
            // This would be implemented with procedures in MCreator
        } else if (effectDesc.contains("flame") || effectDesc.contains("fire")) {
            // Flame enchantment
            // This would be implemented with procedures in MCreator
        }
    }
    
    /**
     * Configures special enchantment effects
     */
    private void configureSpecialEnchantment(Enchantment enchantment, EnchantmentProperties properties) {
        // Special enchantments would be implemented with custom procedures
        // This is where the most creative enchantments would be configured
    }
    
    /**
     * Maps rarity string to MCreator rarity
     */
    private String mapRarityToMCreator(String rarity) {
        if (rarity == null) return "COMMON";
        
        String lowerRarity = rarity.toLowerCase();
        
        if (lowerRarity.contains("very_rare") || lowerRarity.contains("very rare")) return "VERY_RARE";
        if (lowerRarity.contains("rare")) return "RARE";
        if (lowerRarity.contains("uncommon")) return "UNCOMMON";
        
        return "COMMON";
    }
    
    /**
     * Maps type string to MCreator enchantment type
     */
    private String mapTypeToMCreator(String type) {
        if (type == null) return "ALL";
        
        String lowerType = type.toLowerCase();
        
        if (lowerType.contains("weapon") || lowerType.contains("sword")) return "WEAPON";
        if (lowerType.contains("tool")) return "DIGGER";
        if (lowerType.contains("armor")) return "ARMOR";
        if (lowerType.contains("bow")) return "BOW";
        if (lowerType.contains("fishing")) return "FISHING_ROD";
        if (lowerType.contains("trident")) return "TRIDENT";
        if (lowerType.contains("crossbow")) return "CROSSBOW";
        
        return "ALL";
    }
    
    /**
     * Applies balanced defaults based on options
     */
    private void applyBalancedDefaults(EnchantmentProperties properties, GenerationOptions options) {
        if (!options.isBalancedStats()) {
            return;
        }
        
        // Limit maximum level for balance
        if (properties.getMaxLevel() > 5 && !properties.isTreasureEnchantment()) {
            properties.setMaxLevel(5);
        }
        
        // Limit effect strength
        if (properties.getEffectStrength() > 5.0) {
            properties.setEffectStrength(5.0);
        }
        
        // Ensure treasure enchantments are rare
        if (properties.isTreasureEnchantment() && properties.getRarity().equals("COMMON")) {
            properties.setRarity("RARE");
        }
    }
    
    /**
     * Generates additional content for the enchantment
     */
    private String generateAdditionalEnchantmentContent(String enchantmentName, EnchantmentProperties properties, GenerationOptions options) throws Exception {
        StringBuilder content = new StringBuilder();
        
        // Generate lore if requested
        if (options.isGenerateLore()) {
            String lore = aiService.generateLore(enchantmentName, "enchantment", properties.getTheme());
            content.append("Generated Lore:\n").append(lore).append("\n\n");
        }
        
        // Generate procedure suggestions
        String procedurePrompt = "Suggest MCreator procedures to implement the " + enchantmentName + 
                               " enchantment with the following effect: " + properties.getEffectDescription();
        String procedures = aiService.generateText(procedurePrompt, 0.4, 300);
        content.append("Implementation Suggestions:\n").append(procedures).append("\n\n");
        
        return content.toString();
    }
    
    /**
     * Formats the enchantment details for display
     */
    private String formatEnchantmentDetails(String enchantmentName, EnchantmentProperties properties, String additionalContent) {
        StringBuilder details = new StringBuilder();
        
        details.append("Enchantment: ").append(enchantmentName).append("\n");
        details.append("Type: ").append(properties.getEnchantmentType()).append("\n");
        details.append("Max Level: ").append(properties.getMaxLevel()).append("\n");
        details.append("Rarity: ").append(properties.getRarity()).append("\n");
        
        if (properties.isTreasureEnchantment()) {
            details.append("Treasure Enchantment: Yes\n");
        }
        
        if (properties.isCurseEnchantment()) {
            details.append("Curse Enchantment: Yes\n");
        }
        
        details.append("Effect: ").append(properties.getEffectDescription()).append("\n");
        
        if (!properties.getCompatibleItems().isEmpty()) {
            details.append("Compatible Items: ").append(String.join(", ", properties.getCompatibleItems())).append("\n");
        }
        
        if (!properties.getIncompatibleEnchantments().isEmpty()) {
            details.append("Incompatible With: ").append(String.join(", ", properties.getIncompatibleEnchantments())).append("\n");
        }
        
        details.append("Effect Strength: ").append(properties.getEffectStrength()).append("\n");
        
        details.append("\n").append(additionalContent);
        
        return details.toString();
    }
    
    /**
     * Infers enchantment type from name and analysis
     */
    private String inferEnchantmentType(String enchantmentName, PromptAnalysis analysis) {
        String lowerName = enchantmentName.toLowerCase();
        
        if (lowerName.contains("sharp") || lowerName.contains("damage") || lowerName.contains("power")) return "WEAPON";
        if (lowerName.contains("efficiency") || lowerName.contains("fortune") || lowerName.contains("silk")) return "TOOL";
        if (lowerName.contains("protection") || lowerName.contains("thorns")) return "ARMOR";
        if (lowerName.contains("punch") || lowerName.contains("flame")) return "BOW";
        if (lowerName.contains("luck") || lowerName.contains("sea")) return "FISHING_ROD";
        
        return "ALL";
    }
    
    /**
     * Infers rarity from name and analysis
     */
    private String inferRarity(String enchantmentName, PromptAnalysis analysis) {
        String lowerName = enchantmentName.toLowerCase();
        String theme = analysis.getTheme();
        
        if (lowerName.contains("curse") || lowerName.contains("cursed")) return "VERY_RARE";
        if (lowerName.contains("legendary") || lowerName.contains("ultimate")) return "VERY_RARE";
        if (lowerName.contains("rare") || lowerName.contains("magic")) return "RARE";
        if (theme != null && theme.toLowerCase().contains("dark")) return "RARE";
        
        return "UNCOMMON";
    }
    
    /**
     * Data class for enchantment properties
     */
    public static class EnchantmentProperties {
        private String name;
        private String enchantmentType;
        private List<String> compatibleItems = new ArrayList<>();
        private int maxLevel = 1;
        private String rarity = "COMMON";
        private String effectDescription;
        private boolean treasureEnchantment = false;
        private boolean curseEnchantment = false;
        private List<String> incompatibleEnchantments = new ArrayList<>();
        private double effectStrength = 1.0;
        private String theme;
        private Map<String, Object> specialProperties = new HashMap<>();
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEnchantmentType() { return enchantmentType; }
        public void setEnchantmentType(String enchantmentType) { this.enchantmentType = enchantmentType; }
        
        public List<String> getCompatibleItems() { return compatibleItems; }
        public void setCompatibleItems(List<String> compatibleItems) { this.compatibleItems = compatibleItems; }
        
        public int getMaxLevel() { return maxLevel; }
        public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
        
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        
        public String getEffectDescription() { return effectDescription; }
        public void setEffectDescription(String effectDescription) { this.effectDescription = effectDescription; }
        
        public boolean isTreasureEnchantment() { return treasureEnchantment; }
        public void setTreasureEnchantment(boolean treasureEnchantment) { this.treasureEnchantment = treasureEnchantment; }
        
        public boolean isCurseEnchantment() { return curseEnchantment; }
        public void setCurseEnchantment(boolean curseEnchantment) { this.curseEnchantment = curseEnchantment; }
        
        public List<String> getIncompatibleEnchantments() { return incompatibleEnchantments; }
        public void setIncompatibleEnchantments(List<String> incompatibleEnchantments) { this.incompatibleEnchantments = incompatibleEnchantments; }
        
        public double getEffectStrength() { return effectStrength; }
        public void setEffectStrength(double effectStrength) { this.effectStrength = effectStrength; }
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public Map<String, Object> getSpecialProperties() { return specialProperties; }
        public void setSpecialProperties(Map<String, Object> specialProperties) { this.specialProperties = specialProperties; }
        
        public boolean hasSpecialProperties() { return !specialProperties.isEmpty(); }
    }
}

