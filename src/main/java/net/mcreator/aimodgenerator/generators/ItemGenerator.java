package net.mcreator.aimodgenerator.generators;

import net.mcreator.workspace.Workspace;
import net.mcreator.element.ModElementType;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.types.Item;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.util.HashMap;
import java.util.Map;

/**
 * Generator for creating Minecraft items using AI
 * Handles swords, tools, armor, food, and other item types
 */
public class ItemGenerator extends BaseGenerator {
    
    public ItemGenerator(Workspace workspace, AIIntegrationService aiService) {
        super(workspace, aiService);
    }
    
    /**
     * Generates a new item based on the provided parameters
     * @param itemName Name of the item to generate
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return Details about the generated item
     */
    public String generateItem(String itemName, PromptAnalysis analysis, 
                              SearchResults searchResults, GenerationOptions options) throws Exception {
        
        // Step 1: Determine item type and properties using AI
        ItemProperties properties = analyzeItemProperties(itemName, analysis, searchResults, options);
        
        // Step 2: Create the MCreator mod element
        ModElement modElement = createItemModElement(itemName, properties);
        
        // Step 3: Configure the item element
        Item itemElement = configureItemElement(modElement, properties, options);
        
        // Step 4: Add to workspace
        workspace.addModElement(modElement);
        workspace.getModElementManager().storeModElement(itemElement);
        
        // Step 5: Generate additional content (lore, recipes, etc.)
        String additionalContent = generateAdditionalItemContent(itemName, properties, options);
        
        return formatItemDetails(itemName, properties, additionalContent);
    }
    
    /**
     * Analyzes item properties using AI and search results
     */
    private ItemProperties analyzeItemProperties(String itemName, PromptAnalysis analysis, 
                                               SearchResults searchResults, GenerationOptions options) throws Exception {
        
        String analysisPrompt = buildItemAnalysisPrompt(itemName, analysis, searchResults, options);
        String aiResponse = aiService.generateMinecraftContent(analysisPrompt, "item");
        
        return parseItemProperties(aiResponse, itemName, analysis);
    }
    
    /**
     * Builds the AI prompt for item analysis
     */
    private String buildItemAnalysisPrompt(String itemName, PromptAnalysis analysis, 
                                         SearchResults searchResults, GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a detailed Minecraft item specification for: ").append(itemName).append("\n\n");
        
        // Add context from original prompt
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n");
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Style: ").append(analysis.getStyle()).append("\n\n");
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(500, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify what we need
        prompt.append("Please provide:\n");
        prompt.append("1. Item type (SWORD, PICKAXE, ARMOR, FOOD, etc.)\n");
        prompt.append("2. Rarity (COMMON, UNCOMMON, RARE, EPIC)\n");
        prompt.append("3. Stack size (1-64)\n");
        prompt.append("4. Durability (if applicable)\n");
        prompt.append("5. Attack damage (if weapon)\n");
        prompt.append("6. Attack speed (if weapon)\n");
        prompt.append("7. Armor protection (if armor)\n");
        prompt.append("8. Special properties or effects\n");
        prompt.append("9. Crafting difficulty level\n");
        prompt.append("10. Lore/description\n\n");
        
        // Add balance requirements
        if (options.isBalancedStats()) {
            prompt.append("IMPORTANT: Ensure all stats are balanced for Minecraft 1.20.1 gameplay. ");
            prompt.append("Compare to vanilla items and avoid overpowered combinations.\n\n");
        }
        
        prompt.append("Format your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to extract item properties
     */
    private ItemProperties parseItemProperties(String aiResponse, String itemName, PromptAnalysis analysis) {
        ItemProperties properties = new ItemProperties();
        properties.setName(itemName);
        
        // Parse AI response (simplified parsing - in reality, use more robust parsing)
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            line = line.trim().toLowerCase();
            
            if (line.contains("type:") || line.contains("item type:")) {
                properties.setItemType(extractValue(line));
            } else if (line.contains("rarity:")) {
                properties.setRarity(extractValue(line));
            } else if (line.contains("stack size:") || line.contains("stacksize:")) {
                properties.setStackSize(extractIntValue(line, 64));
            } else if (line.contains("durability:")) {
                properties.setDurability(extractIntValue(line, 0));
            } else if (line.contains("attack damage:") || line.contains("damage:")) {
                properties.setAttackDamage(extractDoubleValue(line, 1.0));
            } else if (line.contains("attack speed:") || line.contains("speed:")) {
                properties.setAttackSpeed(extractDoubleValue(line, 1.6));
            } else if (line.contains("armor:") || line.contains("protection:")) {
                properties.setArmorProtection(extractIntValue(line, 0));
            } else if (line.contains("lore:") || line.contains("description:")) {
                properties.setLore(extractValue(line));
            }
        }
        
        // Set defaults based on analysis if not specified
        if (properties.getItemType() == null) {
            properties.setItemType(inferItemType(itemName, analysis));
        }
        
        if (properties.getRarity() == null) {
            properties.setRarity(inferRarity(analysis));
        }
        
        return properties;
    }
    
    /**
     * Creates a new MCreator mod element for the item
     */
    private ModElement createItemModElement(String itemName, ItemProperties properties) {
        String elementName = sanitizeElementName(itemName);
        ModElement modElement = new ModElement(workspace, elementName, ModElementType.ITEM);
        modElement.setRegistryName(elementName.toLowerCase().replace(" ", "_"));
        return modElement;
    }
    
    /**
     * Configures the MCreator item element with generated properties
     */
    private Item configureItemElement(ModElement modElement, ItemProperties properties, GenerationOptions options) {
        Item item = new Item(modElement);
        
        // Basic properties
        item.name = properties.getName();
        item.stackSize = properties.getStackSize();
        
        // Set rarity
        switch (properties.getRarity().toUpperCase()) {
            case "UNCOMMON":
                item.rarity = "UNCOMMON";
                break;
            case "RARE":
                item.rarity = "RARE";
                break;
            case "EPIC":
                item.rarity = "EPIC";
                break;
            default:
                item.rarity = "COMMON";
                break;
        }
        
        // Configure based on item type
        String itemType = properties.getItemType().toUpperCase();
        
        if (itemType.contains("SWORD") || itemType.contains("WEAPON")) {
            configureSword(item, properties);
        } else if (itemType.contains("TOOL") || itemType.contains("PICKAXE") || itemType.contains("AXE")) {
            configureTool(item, properties);
        } else if (itemType.contains("ARMOR")) {
            configureArmor(item, properties);
        } else if (itemType.contains("FOOD")) {
            configureFood(item, properties);
        } else {
            // Generic item
            configureGenericItem(item, properties);
        }
        
        // Add lore if available
        if (properties.getLore() != null && !properties.getLore().isEmpty()) {
            // In MCreator, lore is typically added through procedures
            // This would be implemented based on MCreator's API
        }
        
        return item;
    }
    
    /**
     * Configures a sword item
     */
    private void configureSword(Item item, ItemProperties properties) {
        // Set as sword
        item.toolType = "Sword";
        
        // Set damage and speed
        if (properties.getAttackDamage() > 0) {
            item.damageVsEntity = properties.getAttackDamage();
        } else {
            item.damageVsEntity = 7.0; // Default sword damage
        }
        
        if (properties.getAttackSpeed() > 0) {
            item.attackSpeed = properties.getAttackSpeed();
        } else {
            item.attackSpeed = 1.6; // Default sword speed
        }
        
        // Set durability
        if (properties.getDurability() > 0) {
            item.usageCount = properties.getDurability();
        } else {
            item.usageCount = 250; // Default sword durability
        }
        
        item.toolType = "Sword";
    }
    
    /**
     * Configures a tool item
     */
    private void configureTool(Item item, ItemProperties properties) {
        String itemType = properties.getItemType().toUpperCase();
        
        if (itemType.contains("PICKAXE")) {
            item.toolType = "Pickaxe";
            item.efficiency = 6.0;
            item.harvestLevel = 2;
        } else if (itemType.contains("AXE")) {
            item.toolType = "Axe";
            item.efficiency = 6.0;
            item.harvestLevel = 2;
        } else if (itemType.contains("SHOVEL")) {
            item.toolType = "Shovel";
            item.efficiency = 6.0;
            item.harvestLevel = 1;
        } else if (itemType.contains("HOE")) {
            item.toolType = "Hoe";
        }
        
        // Set durability
        if (properties.getDurability() > 0) {
            item.usageCount = properties.getDurability();
        } else {
            item.usageCount = 250; // Default tool durability
        }
    }
    
    /**
     * Configures an armor item
     */
    private void configureArmor(Item item, ItemProperties properties) {
        // This would be more complex in a real implementation
        // MCreator has specific armor configuration
        item.toolType = "Armor";
        
        if (properties.getArmorProtection() > 0) {
            // Set armor protection value
            // This would use MCreator's armor API
        }
    }
    
    /**
     * Configures a food item
     */
    private void configureFood(Item item, ItemProperties properties) {
        item.isFood = true;
        item.nutritionalValue = 4; // Default food value
        item.saturation = 0.3; // Default saturation
        item.stackSize = Math.min(properties.getStackSize(), 64);
    }
    
    /**
     * Configures a generic item
     */
    private void configureGenericItem(Item item, ItemProperties properties) {
        // Basic item configuration
        item.stackSize = properties.getStackSize();
        
        // Add any special properties
        if (properties.hasSpecialProperties()) {
            // This would be implemented based on the special properties
        }
    }
    
    /**
     * Generates additional content like lore and recipes
     */
    private String generateAdditionalItemContent(String itemName, ItemProperties properties, GenerationOptions options) throws Exception {
        StringBuilder content = new StringBuilder();
        
        // Generate lore if requested
        if (options.isGenerateLore()) {
            String lore = aiService.generateLore(itemName, "item", properties.getTheme());
            content.append("Generated Lore:\n").append(lore).append("\n\n");
        }
        
        // Suggest crafting recipe
        if (options.isGenerateRecipes()) {
            String recipePrompt = "Suggest a balanced crafting recipe for " + itemName + 
                                " considering its properties and rarity: " + properties.getRarity();
            String recipe = aiService.generateText(recipePrompt, 0.5, 200);
            content.append("Suggested Recipe:\n").append(recipe).append("\n\n");
        }
        
        return content.toString();
    }
    
    /**
     * Formats the item details for display
     */
    private String formatItemDetails(String itemName, ItemProperties properties, String additionalContent) {
        StringBuilder details = new StringBuilder();
        
        details.append("Item: ").append(itemName).append("\n");
        details.append("Type: ").append(properties.getItemType()).append("\n");
        details.append("Rarity: ").append(properties.getRarity()).append("\n");
        details.append("Stack Size: ").append(properties.getStackSize()).append("\n");
        
        if (properties.getDurability() > 0) {
            details.append("Durability: ").append(properties.getDurability()).append("\n");
        }
        
        if (properties.getAttackDamage() > 0) {
            details.append("Attack Damage: ").append(properties.getAttackDamage()).append("\n");
        }
        
        if (properties.getAttackSpeed() > 0) {
            details.append("Attack Speed: ").append(properties.getAttackSpeed()).append("\n");
        }
        
        if (properties.getArmorProtection() > 0) {
            details.append("Armor Protection: ").append(properties.getArmorProtection()).append("\n");
        }
        
        if (properties.getLore() != null && !properties.getLore().isEmpty()) {
            details.append("Description: ").append(properties.getLore()).append("\n");
        }
        
        details.append("\n").append(additionalContent);
        
        return details.toString();
    }
    
    /**
     * Infers item type from name and analysis
     */
    private String inferItemType(String itemName, PromptAnalysis analysis) {
        String lowerName = itemName.toLowerCase();
        
        if (lowerName.contains("sword")) return "SWORD";
        if (lowerName.contains("pickaxe")) return "PICKAXE";
        if (lowerName.contains("axe")) return "AXE";
        if (lowerName.contains("shovel")) return "SHOVEL";
        if (lowerName.contains("hoe")) return "HOE";
        if (lowerName.contains("helmet")) return "ARMOR";
        if (lowerName.contains("chestplate")) return "ARMOR";
        if (lowerName.contains("leggings")) return "ARMOR";
        if (lowerName.contains("boots")) return "ARMOR";
        if (lowerName.contains("food") || lowerName.contains("bread") || lowerName.contains("apple")) return "FOOD";
        
        return "GENERIC";
    }
    
    /**
     * Infers rarity from analysis
     */
    private String inferRarity(PromptAnalysis analysis) {
        String theme = analysis.getTheme();
        if (theme != null) {
            String lowerTheme = theme.toLowerCase();
            if (lowerTheme.contains("legendary") || lowerTheme.contains("epic")) return "EPIC";
            if (lowerTheme.contains("rare") || lowerTheme.contains("magic")) return "RARE";
            if (lowerTheme.contains("uncommon")) return "UNCOMMON";
        }
        return "COMMON";
    }
    
    /**
     * Data class for item properties
     */
    public static class ItemProperties {
        private String name;
        private String itemType;
        private String rarity;
        private int stackSize = 64;
        private int durability = 0;
        private double attackDamage = 0;
        private double attackSpeed = 0;
        private int armorProtection = 0;
        private String lore;
        private String theme;
        private Map<String, Object> specialProperties = new HashMap<>();
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }
        
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        
        public int getStackSize() { return stackSize; }
        public void setStackSize(int stackSize) { this.stackSize = stackSize; }
        
        public int getDurability() { return durability; }
        public void setDurability(int durability) { this.durability = durability; }
        
        public double getAttackDamage() { return attackDamage; }
        public void setAttackDamage(double attackDamage) { this.attackDamage = attackDamage; }
        
        public double getAttackSpeed() { return attackSpeed; }
        public void setAttackSpeed(double attackSpeed) { this.attackSpeed = attackSpeed; }
        
        public int getArmorProtection() { return armorProtection; }
        public void setArmorProtection(int armorProtection) { this.armorProtection = armorProtection; }
        
        public String getLore() { return lore; }
        public void setLore(String lore) { this.lore = lore; }
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public Map<String, Object> getSpecialProperties() { return specialProperties; }
        public void setSpecialProperties(Map<String, Object> specialProperties) { this.specialProperties = specialProperties; }
        
        public boolean hasSpecialProperties() { return !specialProperties.isEmpty(); }
    }
}

