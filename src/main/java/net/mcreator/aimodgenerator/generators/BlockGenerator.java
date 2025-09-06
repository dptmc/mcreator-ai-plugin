package net.mcreator.aimodgenerator.generators;

import net.mcreator.workspace.Workspace;
import net.mcreator.element.ModElementType;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.types.Block;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.util.HashMap;
import java.util.Map;

/**
 * Generator for creating Minecraft blocks using AI
 * Handles ores, decorative blocks, functional blocks, and more
 */
public class BlockGenerator extends BaseGenerator {
    
    public BlockGenerator(Workspace workspace, AIIntegrationService aiService) {
        super(workspace, aiService);
    }
    
    /**
     * Generates a new block based on the provided parameters
     * @param blockName Name of the block to generate
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return Details about the generated block
     */
    public String generateBlock(String blockName, PromptAnalysis analysis, 
                               SearchResults searchResults, GenerationOptions options) throws Exception {
        
        // Step 1: Determine block type and properties using AI
        BlockProperties properties = analyzeBlockProperties(blockName, analysis, searchResults, options);
        
        // Step 2: Create the MCreator mod element
        ModElement modElement = createBlockModElement(blockName, properties);
        
        // Step 3: Configure the block element
        Block blockElement = configureBlockElement(modElement, properties, options);
        
        // Step 4: Add to workspace
        workspace.addModElement(modElement);
        workspace.getModElementManager().storeModElement(blockElement);
        
        // Step 5: Generate additional content
        String additionalContent = generateAdditionalBlockContent(blockName, properties, options);
        
        return formatBlockDetails(blockName, properties, additionalContent);
    }
    
    /**
     * Analyzes block properties using AI and search results
     */
    private BlockProperties analyzeBlockProperties(String blockName, PromptAnalysis analysis, 
                                                  SearchResults searchResults, GenerationOptions options) throws Exception {
        
        String analysisPrompt = buildBlockAnalysisPrompt(blockName, analysis, searchResults, options);
        String aiResponse = aiService.generateMinecraftContent(analysisPrompt, "block");
        
        return parseBlockProperties(aiResponse, blockName, analysis);
    }
    
    /**
     * Builds the AI prompt for block analysis
     */
    private String buildBlockAnalysisPrompt(String blockName, PromptAnalysis analysis, 
                                          SearchResults searchResults, GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a detailed Minecraft block specification for: ").append(blockName).append("\n\n");
        
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
        prompt.append("1. Block type (ORE, DECORATIVE, FUNCTIONAL, CRAFTING_TABLE, etc.)\n");
        prompt.append("2. Material type (STONE, WOOD, METAL, etc.)\n");
        prompt.append("3. Hardness (0.5-50.0, stone=1.5, obsidian=50.0)\n");
        prompt.append("4. Resistance (0.0-2000.0, stone=6.0, obsidian=1200.0)\n");
        prompt.append("5. Harvest level (0-4, 0=hand, 1=wood, 2=stone, 3=iron, 4=diamond)\n");
        prompt.append("6. Light emission (0-15)\n");
        prompt.append("7. Sound type (STONE, WOOD, METAL, etc.)\n");
        prompt.append("8. Special properties (redstone power, gravity, etc.)\n");
        prompt.append("9. Drop items (what it drops when mined)\n");
        prompt.append("10. Rarity and spawn conditions (if ore)\n\n");
        
        // Add balance requirements
        if (options.isBalancedStats()) {
            prompt.append("IMPORTANT: Ensure all stats are balanced for Minecraft 1.20.1 gameplay. ");
            prompt.append("Compare to vanilla blocks and avoid overpowered combinations.\n\n");
        }
        
        prompt.append("Format your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to extract block properties
     */
    private BlockProperties parseBlockProperties(String aiResponse, String blockName, PromptAnalysis analysis) {
        BlockProperties properties = new BlockProperties();
        properties.setName(blockName);
        
        // Parse AI response
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            line = line.trim().toLowerCase();
            
            if (line.contains("type:") || line.contains("block type:")) {
                properties.setBlockType(extractValue(line));
            } else if (line.contains("material:")) {
                properties.setMaterial(extractValue(line));
            } else if (line.contains("hardness:")) {
                properties.setHardness(extractDoubleValue(line, 1.5));
            } else if (line.contains("resistance:")) {
                properties.setResistance(extractDoubleValue(line, 6.0));
            } else if (line.contains("harvest level:") || line.contains("mining level:")) {
                properties.setHarvestLevel(extractIntValue(line, 0));
            } else if (line.contains("light:") || line.contains("light emission:")) {
                properties.setLightEmission(extractIntValue(line, 0));
            } else if (line.contains("sound:")) {
                properties.setSoundType(extractValue(line));
            } else if (line.contains("drop:") || line.contains("drops:")) {
                properties.setDrops(extractValue(line));
            } else if (line.contains("rarity:")) {
                properties.setRarity(extractValue(line));
            }
        }
        
        // Set defaults based on analysis if not specified
        if (properties.getBlockType() == null) {
            properties.setBlockType(inferBlockType(blockName, analysis));
        }
        
        if (properties.getMaterial() == null) {
            properties.setMaterial(inferMaterial(blockName, analysis));
        }
        
        // Apply balanced defaults
        applyBalancedDefaults(properties, options);
        
        return properties;
    }
    
    /**
     * Creates a new MCreator mod element for the block
     */
    private ModElement createBlockModElement(String blockName, BlockProperties properties) {
        String elementName = sanitizeElementName(blockName);
        ModElement modElement = new ModElement(workspace, elementName, ModElementType.BLOCK);
        modElement.setRegistryName(createRegistryName(blockName));
        return modElement;
    }
    
    /**
     * Configures the MCreator block element with generated properties
     */
    private Block configureBlockElement(ModElement modElement, BlockProperties properties, GenerationOptions options) {
        Block block = new Block(modElement);
        
        // Basic properties
        block.name = properties.getName();
        
        // Material and sound
        block.material = mapMaterialToMCreator(properties.getMaterial());
        block.soundOnStep = mapSoundToMCreator(properties.getSoundType());
        
        // Physical properties
        block.hardness = clamp(properties.getHardness(), 0.0, 50.0);
        block.resistance = clamp(properties.getResistance(), 0.0, 2000.0);
        block.harvestLevel = clamp(properties.getHarvestLevel(), 0, 4);
        
        // Light emission
        block.luminance = clamp(properties.getLightEmission(), 0, 15);
        
        // Configure based on block type
        String blockType = properties.getBlockType().toUpperCase();
        
        if (blockType.contains("ORE")) {
            configureOreBlock(block, properties);
        } else if (blockType.contains("CRAFTING") || blockType.contains("FUNCTIONAL")) {
            configureFunctionalBlock(block, properties);
        } else if (blockType.contains("DECORATIVE")) {
            configureDecorativeBlock(block, properties);
        } else {
            configureGenericBlock(block, properties);
        }
        
        return block;
    }
    
    /**
     * Configures an ore block
     */
    private void configureOreBlock(Block block, BlockProperties properties) {
        // Ores typically require pickaxe
        block.harvestTool = "pickaxe";
        
        // Set appropriate hardness for ore
        if (block.hardness <= 0) {
            block.hardness = 3.0; // Default ore hardness
        }
        
        // Set drops if specified
        if (properties.getDrops() != null && !properties.getDrops().isEmpty()) {
            // This would configure custom drops in MCreator
            // Implementation depends on MCreator's API
        }
        
        // Ores don't emit light by default unless specified
        if (block.luminance <= 0 && properties.getLightEmission() <= 0) {
            block.luminance = 0;
        }
    }
    
    /**
     * Configures a functional block (like crafting tables)
     */
    private void configureFunctionalBlock(Block block, BlockProperties properties) {
        // Functional blocks often have GUIs
        block.hasInventory = true;
        
        // Set appropriate hardness
        if (block.hardness <= 0) {
            block.hardness = 2.5;
        }
        
        // May emit light if specified
        if (properties.getLightEmission() > 0) {
            block.luminance = properties.getLightEmission();
        }
    }
    
    /**
     * Configures a decorative block
     */
    private void configureDecorativeBlock(Block block, BlockProperties properties) {
        // Decorative blocks are often easier to break
        if (block.hardness <= 0) {
            block.hardness = 1.0;
        }
        
        // May have special visual properties
        if (properties.getLightEmission() > 0) {
            block.luminance = properties.getLightEmission();
        }
    }
    
    /**
     * Configures a generic block
     */
    private void configureGenericBlock(Block block, BlockProperties properties) {
        // Apply standard defaults
        if (block.hardness <= 0) {
            block.hardness = 1.5; // Stone-like hardness
        }
        
        if (block.resistance <= 0) {
            block.resistance = 6.0; // Stone-like resistance
        }
    }
    
    /**
     * Maps material string to MCreator material
     */
    private String mapMaterialToMCreator(String material) {
        if (material == null) return "ROCK";
        
        String lowerMaterial = material.toLowerCase();
        
        if (lowerMaterial.contains("wood")) return "WOOD";
        if (lowerMaterial.contains("stone") || lowerMaterial.contains("rock")) return "ROCK";
        if (lowerMaterial.contains("metal") || lowerMaterial.contains("iron")) return "IRON";
        if (lowerMaterial.contains("glass")) return "GLASS";
        if (lowerMaterial.contains("cloth") || lowerMaterial.contains("wool")) return "CLOTH";
        if (lowerMaterial.contains("sand")) return "SAND";
        if (lowerMaterial.contains("plant") || lowerMaterial.contains("leaves")) return "LEAVES";
        
        return "ROCK"; // Default
    }
    
    /**
     * Maps sound string to MCreator sound
     */
    private String mapSoundToMCreator(String sound) {
        if (sound == null) return "STONE";
        
        String lowerSound = sound.toLowerCase();
        
        if (lowerSound.contains("wood")) return "WOOD";
        if (lowerSound.contains("stone") || lowerSound.contains("rock")) return "STONE";
        if (lowerSound.contains("metal")) return "METAL";
        if (lowerSound.contains("glass")) return "GLASS";
        if (lowerSound.contains("sand")) return "SAND";
        if (lowerSound.contains("gravel")) return "GRAVEL";
        if (lowerSound.contains("grass")) return "GRASS";
        
        return "STONE"; // Default
    }
    
    /**
     * Applies balanced defaults based on options
     */
    private void applyBalancedDefaults(BlockProperties properties, GenerationOptions options) {
        if (!options.isBalancedStats()) {
            return;
        }
        
        // Ensure hardness is reasonable
        if (properties.getHardness() > 10.0 && !properties.getBlockType().toUpperCase().contains("OBSIDIAN")) {
            properties.setHardness(clamp(properties.getHardness(), 0.5, 10.0));
        }
        
        // Ensure resistance is reasonable
        if (properties.getResistance() > 30.0 && !properties.getBlockType().toUpperCase().contains("OBSIDIAN")) {
            properties.setResistance(clamp(properties.getResistance(), 0.0, 30.0));
        }
        
        // Limit light emission for balance
        if (properties.getLightEmission() > 15) {
            properties.setLightEmission(15);
        }
    }
    
    /**
     * Generates additional content for the block
     */
    private String generateAdditionalBlockContent(String blockName, BlockProperties properties, GenerationOptions options) throws Exception {
        StringBuilder content = new StringBuilder();
        
        // Generate lore if requested
        if (options.isGenerateLore()) {
            String lore = aiService.generateLore(blockName, "block", properties.getTheme());
            content.append("Generated Lore:\n").append(lore).append("\n\n");
        }
        
        // Suggest crafting recipe
        if (options.isGenerateRecipes()) {
            String recipePrompt = "Suggest a balanced crafting recipe for " + blockName + 
                                " considering its properties and type: " + properties.getBlockType();
            String recipe = aiService.generateText(recipePrompt, 0.5, 200);
            content.append("Suggested Recipe:\n").append(recipe).append("\n\n");
        }
        
        // Generate world generation info for ores
        if (properties.getBlockType().toUpperCase().contains("ORE")) {
            String worldGenPrompt = "Suggest balanced world generation settings for " + blockName + 
                                  " ore, including spawn height, vein size, and rarity";
            String worldGen = aiService.generateText(worldGenPrompt, 0.3, 150);
            content.append("World Generation:\n").append(worldGen).append("\n\n");
        }
        
        return content.toString();
    }
    
    /**
     * Formats the block details for display
     */
    private String formatBlockDetails(String blockName, BlockProperties properties, String additionalContent) {
        StringBuilder details = new StringBuilder();
        
        details.append("Block: ").append(blockName).append("\n");
        details.append("Type: ").append(properties.getBlockType()).append("\n");
        details.append("Material: ").append(properties.getMaterial()).append("\n");
        details.append("Hardness: ").append(properties.getHardness()).append("\n");
        details.append("Resistance: ").append(properties.getResistance()).append("\n");
        details.append("Harvest Level: ").append(properties.getHarvestLevel()).append("\n");
        
        if (properties.getLightEmission() > 0) {
            details.append("Light Emission: ").append(properties.getLightEmission()).append("\n");
        }
        
        details.append("Sound Type: ").append(properties.getSoundType()).append("\n");
        
        if (properties.getDrops() != null && !properties.getDrops().isEmpty()) {
            details.append("Drops: ").append(properties.getDrops()).append("\n");
        }
        
        if (properties.getRarity() != null && !properties.getRarity().isEmpty()) {
            details.append("Rarity: ").append(properties.getRarity()).append("\n");
        }
        
        details.append("\n").append(additionalContent);
        
        return details.toString();
    }
    
    /**
     * Infers block type from name and analysis
     */
    private String inferBlockType(String blockName, PromptAnalysis analysis) {
        String lowerName = blockName.toLowerCase();
        
        if (lowerName.contains("ore")) return "ORE";
        if (lowerName.contains("crafting") || lowerName.contains("table")) return "FUNCTIONAL";
        if (lowerName.contains("decorative") || lowerName.contains("decoration")) return "DECORATIVE";
        if (lowerName.contains("stone") || lowerName.contains("rock")) return "DECORATIVE";
        if (lowerName.contains("wood") || lowerName.contains("plank")) return "DECORATIVE";
        
        return "DECORATIVE";
    }
    
    /**
     * Infers material from name and analysis
     */
    private String inferMaterial(String blockName, PromptAnalysis analysis) {
        String lowerName = blockName.toLowerCase();
        
        if (lowerName.contains("wood") || lowerName.contains("plank")) return "WOOD";
        if (lowerName.contains("metal") || lowerName.contains("iron") || lowerName.contains("gold")) return "METAL";
        if (lowerName.contains("glass")) return "GLASS";
        if (lowerName.contains("sand")) return "SAND";
        if (lowerName.contains("stone") || lowerName.contains("rock") || lowerName.contains("ore")) return "STONE";
        
        return "STONE";
    }
    
    /**
     * Data class for block properties
     */
    public static class BlockProperties {
        private String name;
        private String blockType;
        private String material;
        private double hardness = 1.5;
        private double resistance = 6.0;
        private int harvestLevel = 0;
        private int lightEmission = 0;
        private String soundType = "STONE";
        private String drops;
        private String rarity;
        private String theme;
        private Map<String, Object> specialProperties = new HashMap<>();
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getBlockType() { return blockType; }
        public void setBlockType(String blockType) { this.blockType = blockType; }
        
        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        
        public double getHardness() { return hardness; }
        public void setHardness(double hardness) { this.hardness = hardness; }
        
        public double getResistance() { return resistance; }
        public void setResistance(double resistance) { this.resistance = resistance; }
        
        public int getHarvestLevel() { return harvestLevel; }
        public void setHarvestLevel(int harvestLevel) { this.harvestLevel = harvestLevel; }
        
        public int getLightEmission() { return lightEmission; }
        public void setLightEmission(int lightEmission) { this.lightEmission = lightEmission; }
        
        public String getSoundType() { return soundType; }
        public void setSoundType(String soundType) { this.soundType = soundType; }
        
        public String getDrops() { return drops; }
        public void setDrops(String drops) { this.drops = drops; }
        
        public String getRarity() { return rarity; }
        public void setRarity(String rarity) { this.rarity = rarity; }
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public Map<String, Object> getSpecialProperties() { return specialProperties; }
        public void setSpecialProperties(Map<String, Object> specialProperties) { this.specialProperties = specialProperties; }
        
        public boolean hasSpecialProperties() { return !specialProperties.isEmpty(); }
    }
}

