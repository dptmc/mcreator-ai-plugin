package net.mcreator.aimodgenerator.media;

import net.mcreator.workspace.Workspace;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for creating Minecraft textures using AI
 * Handles item textures, block textures, entity skins, and armor textures
 */
public class TextureGenerator {
    
    private final Workspace workspace;
    private final AIIntegrationService aiService;
    private final Path texturesPath;
    
    public TextureGenerator(Workspace workspace, AIIntegrationService aiService) {
        this.workspace = workspace;
        this.aiService = aiService;
        this.texturesPath = Paths.get(workspace.getWorkspaceFolder().getAbsolutePath(), "src", "main", "resources", "assets", workspace.getModName(), "textures");
        
        // Ensure texture directories exist
        createTextureDirectories();
    }
    
    /**
     * Generates textures for an element
     */
    public Map<String, String> generateElementTextures(String elementName, String elementType,
                                                     PromptAnalysis analysis, SearchResults searchResults, 
                                                     GenerationOptions options) throws Exception {
        
        Map<String, String> texturePaths = new HashMap<>();
        
        // Step 1: Analyze texture requirements
        TextureProperties properties = analyzeTextureProperties(elementName, elementType, analysis, searchResults, options);
        
        // Step 2: Determine texture types to generate
        List<String> textureTypes = determineTextureTypes(elementType, properties);
        
        // Step 3: Generate each texture type
        for (String textureType : textureTypes) {
            String texturePath = generateTexture(elementName, elementType, textureType, properties, options);
            if (texturePath != null) {
                texturePaths.put(textureType, texturePath);
            }
        }
        
        return texturePaths;
    }
    
    /**
     * Analyzes texture properties using AI
     */
    private TextureProperties analyzeTextureProperties(String elementName, String elementType,
                                                     PromptAnalysis analysis, SearchResults searchResults,
                                                     GenerationOptions options) throws Exception {
        
        String analysisPrompt = buildTextureAnalysisPrompt(elementName, elementType, analysis, searchResults, options);
        String aiResponse = aiService.generateText(analysisPrompt, 0.6, 400);
        
        return parseTextureProperties(aiResponse, elementName, elementType, analysis);
    }
    
    /**
     * Builds the AI prompt for texture analysis
     */
    private String buildTextureAnalysisPrompt(String elementName, String elementType,
                                            PromptAnalysis analysis, SearchResults searchResults,
                                            GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze texture requirements for Minecraft ").append(elementType).append(": ").append(elementName).append("\n\n");
        
        // Add context
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Style: ").append(analysis.getStyle()).append("\n");
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n\n");
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(300, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify requirements
        prompt.append("Please provide:\n");
        prompt.append("1. Texture style (realistic, cartoon, pixel art, etc.)\n");
        prompt.append("2. Color scheme (primary colors)\n");
        prompt.append("3. Material type (metal, wood, stone, fabric, etc.)\n");
        prompt.append("4. Pattern type (solid, striped, spotted, etc.)\n");
        prompt.append("5. Special effects (glowing, transparent, etc.)\n");
        prompt.append("6. Resolution requirements\n");
        
        prompt.append("\nFormat your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to extract texture properties
     */
    private TextureProperties parseTextureProperties(String aiResponse, String elementName,
                                                   String elementType, PromptAnalysis analysis) {
        TextureProperties properties = new TextureProperties();
        properties.setElementName(elementName);
        properties.setElementType(elementType);
        
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            String lowerLine = line.trim().toLowerCase();
            
            if (lowerLine.contains("style:")) {
                properties.setTextureStyle(extractValue(line));
            } else if (lowerLine.contains("color:") || lowerLine.contains("colors:")) {
                properties.setColorScheme(extractValue(line));
            } else if (lowerLine.contains("material:")) {
                properties.setMaterialType(extractValue(line));
            } else if (lowerLine.contains("pattern:")) {
                properties.setPatternType(extractValue(line));
            } else if (lowerLine.contains("effect:") || lowerLine.contains("effects:")) {
                properties.setSpecialEffects(parseList(extractValue(line)));
            } else if (lowerLine.contains("resolution:")) {
                properties.setResolution(extractValue(line));
            }
        }
        
        // Set defaults
        if (properties.getTextureStyle() == null) {
            properties.setTextureStyle(inferTextureStyle(elementName, elementType, analysis));
        }
        
        if (properties.getResolution() == null) {
            properties.setResolution(getTargetTextureSize(elementType, "main") + "x" + getTargetTextureSize(elementType, "main"));
        }
        
        return properties;
    }
    
    /**
     * Generates a texture using AI
     */
    private String generateTexture(String elementName, String elementType, String textureType,
                                 TextureProperties properties, GenerationOptions options) throws Exception {
        
        // Build the enhanced texture generation prompt
        String texturePrompt = buildEnhancedTexturePrompt(elementName, elementType, textureType, 
                                                         getPromptAnalysisFromProperties(properties), 
                                                         options.isAllowGlowEffects());
        
        // Determine output path
        String fileName = sanitizeFileName(elementName + "_" + textureType) + ".png";
        Path outputPath = texturesPath.resolve(elementType).resolve(fileName);
        
        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());
        
        // Generate the texture using AI service
        try {
            String generatedTexturePath = aiService.generateImage(texturePrompt, outputPath.toString(), "square");
            
            // Post-process the texture to remove background and ensure proper format
            String processedPath = outputPath.toString().replace(".png", "_processed.png");
            
            // Determine target size based on element type
            int targetSize = getTargetTextureSize(elementType, textureType);
            
            // Process the texture for Minecraft compatibility
            if (BackgroundRemover.createPerfectPixelArt(generatedTexturePath, processedPath, targetSize, true)) {
                return processedPath;
            } else {
                // Fallback to basic background removal
                if (BackgroundRemover.removeBackground(generatedTexturePath, processedPath)) {
                    return processedPath;
                } else {
                    return generatedTexturePath;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to generate texture for " + elementName + "_" + textureType + ": " + e.getMessage());
            return null;
        }
    }
    /**
     * Builds enhanced texture generation prompt with user's specifications
     * @param elementName Name of the element
     * @param elementType Type of element (item, block, entity)
     * @param textureType Specific texture type
     * @param analysis Prompt analysis
     * @param allowGlow Whether glow effects are allowed
     * @return Enhanced texture prompt
     */
    private String buildEnhancedTexturePrompt(String elementName, String elementType, String textureType, 
                                            PromptAnalysis analysis, boolean allowGlow) {
        StringBuilder prompt = new StringBuilder();
        
        // Core designer identity and specifications
        prompt.append("You're a Minecraft texture designer for a Minecraft mod. ");
        prompt.append("Only generate in a grid of 64 by 64 pixels with transparent background. ");
        
        // Specific texture request
        if (elementType.equals("item")) {
            prompt.append("Give me a cool item texture idea for ").append(elementName).append(". ");
        } else if (elementType.equals("block")) {
            prompt.append("Give me a cool block texture idea for ").append(elementName).append(" block. ");
        } else if (elementType.equals("entity")) {
            prompt.append("Give me an entity texture for ").append(elementName).append(". ");
        }
        
        // Core texture requirements
        prompt.append("Make it a flat texture fully in the grid. ");
        
        // Glow and particle specifications
        if (allowGlow && (analysis.getOriginalPrompt().toLowerCase().contains("glow") || 
                         analysis.getOriginalPrompt().toLowerCase().contains("light") ||
                         analysis.getOriginalPrompt().toLowerCase().contains("bright") ||
                         analysis.getOriginalPrompt().toLowerCase().contains("particle"))) {
            prompt.append("You may use bright pixels having the illusion of glow since the user requested glowing effects. ");
            prompt.append("But still maintain the 64 by 64 pixel grid structure. ");
        } else {
            prompt.append("No glow or unwanted particles. ");
            prompt.append("Only use bright pixels for glow illusion if it's specifically a particle texture, ");
            prompt.append("and that's still 64 by 64 pixels. ");
        }
        
        // Additional context from analysis
        if (analysis.getTheme() != null && !analysis.getTheme().isEmpty()) {
            prompt.append("Theme: ").append(analysis.getTheme()).append(". ");
        }
        
        if (analysis.getStyle() != null && !analysis.getStyle().isEmpty()) {
            prompt.append("Style: ").append(analysis.getStyle()).append(". ");
        }
        
        // Minecraft-specific requirements
        prompt.append("Ensure the texture follows Minecraft's pixel art style with clear, defined pixels. ");
        prompt.append("Use appropriate colors for the ").append(elementType).append(" type. ");
        prompt.append("Make it tileable if it's a block texture. ");
        prompt.append("Transparent background is mandatory for proper Minecraft integration.");
        
        return prompt.toString();
    }
    
    /**
     * Converts TextureProperties to PromptAnalysis for enhanced prompt building
     */
    private PromptAnalysis getPromptAnalysisFromProperties(TextureProperties properties) {
        PromptAnalysis analysis = new PromptAnalysis();
        analysis.setTheme(properties.getTheme());
        analysis.setStyle(properties.getStyle());
        analysis.setOriginalPrompt(properties.getDescription());
        return analysis;
    }
        
        // Add size specifications based on element type
        int targetSize = getTargetTextureSize(elementType, textureType);
        prompt.append(targetSize).append("x").append(targetSize).append(" pixel grid, ");
        
        // Add texture characteristics
        if (properties.getTextureStyle() != null) {
            prompt.append("Style: ").append(properties.getTextureStyle()).append(". ");
        }
        
        // Add color scheme
        if (properties.getColorScheme() != null) {
            prompt.append("Colors: ").append(properties.getColorScheme()).append(". ");
        }
        
        // Add material type
        if (properties.getMaterialType() != null) {
            prompt.append("Material: ").append(properties.getMaterialType()).append(". ");
        }
        
        // Add pattern information
        if (properties.getPatternType() != null) {
            prompt.append("Pattern: ").append(properties.getPatternType()).append(". ");
        }
        
        // Add technical requirements - CRITICAL for proper Minecraft textures
        prompt.append("IMPORTANT: ");
        prompt.append("no glow effects, ");
        prompt.append("no lighting effects, ");
        prompt.append("no shadows, ");
        prompt.append("transparent background where appropriate, ");
        prompt.append("clean pixel art style, ");
        prompt.append("sharp edges, ");
        prompt.append("no anti-aliasing, ");
        prompt.append("suitable for Minecraft modding, ");
        
        // Add element-specific requirements
        switch (elementType.toLowerCase()) {
            case "item":
                prompt.append("16x16 pixel item texture, ");
                prompt.append("suitable for inventory display, ");
                prompt.append("clear recognizable shape");
                break;
            case "block":
                prompt.append("16x16 pixel block texture, ");
                prompt.append("tileable pattern, ");
                prompt.append("suitable for world placement");
                break;
            case "entity":
                prompt.append("64x64 pixel entity texture, ");
                prompt.append("UV mapping compatible, ");
                prompt.append("suitable for 3D model texturing");
                break;
            case "armor":
                prompt.append("64x64 pixel armor texture, ");
                prompt.append("player model compatible, ");
                prompt.append("layer-based design");
                break;
            default:
                prompt.append("standard Minecraft texture format");
                break;
        }
        
        return prompt.toString();
    }
    
    /**
     * Determines the target texture size based on element type
     */
    private int getTargetTextureSize(String elementType, String textureType) {
        switch (elementType.toLowerCase()) {
            case "item":
                return 16;
            case "block":
                return 16;
            case "entity":
                return 64;
            case "armor":
                return 64;
            case "gui":
                return 32;
            default:
                return 16;
        }
    }
    
    /**
     * Determines what types of textures to generate for an element
     */
    private List<String> determineTextureTypes(String elementType, TextureProperties properties) {
        List<String> textureTypes = new ArrayList<>();
        
        switch (elementType.toLowerCase()) {
            case "item":
                textureTypes.add("main");
                break;
            case "block":
                textureTypes.add("all");
                if (properties.getTextureStyle() != null && 
                    properties.getTextureStyle().toLowerCase().contains("directional")) {
                    textureTypes.add("top");
                    textureTypes.add("side");
                    textureTypes.add("bottom");
                }
                break;
            case "entity":
                textureTypes.add("skin");
                break;
            case "armor":
                textureTypes.add("layer_1");
                textureTypes.add("layer_2");
                break;
            default:
                textureTypes.add("main");
                break;
        }
        
        return textureTypes;
    }
    
    /**
     * Creates necessary texture directories
     */
    private void createTextureDirectories() {
        try {
            Files.createDirectories(texturesPath.resolve("item"));
            Files.createDirectories(texturesPath.resolve("block"));
            Files.createDirectories(texturesPath.resolve("entity"));
            Files.createDirectories(texturesPath.resolve("armor"));
        } catch (IOException e) {
            System.err.println("Failed to create texture directories: " + e.getMessage());
        }
    }
    
    // Utility methods
    
    private String extractValue(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex >= 0 && colonIndex < line.length() - 1) {
            return line.substring(colonIndex + 1).trim();
        }
        return "";
    }
    
    private List<String> parseList(String listText) {
        List<String> items = new ArrayList<>();
        if (listText != null && !listText.isEmpty()) {
            String[] parts = listText.split("[,;]");
            for (String part : parts) {
                String item = part.trim();
                if (!item.isEmpty()) {
                    items.add(item);
                }
            }
        }
        return items;
    }
    
    private String inferTextureStyle(String elementName, String elementType, PromptAnalysis analysis) {
        String theme = analysis.getTheme();
        if (theme != null) {
            String lowerTheme = theme.toLowerCase();
            if (lowerTheme.contains("realistic")) return "realistic";
            if (lowerTheme.contains("cartoon")) return "cartoon";
            if (lowerTheme.contains("dark")) return "dark";
            if (lowerTheme.contains("bright")) return "bright";
        }
        return "minecraft";
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.toLowerCase()
                      .replaceAll("[^a-z0-9_]", "_")
                      .replaceAll("_{2,}", "_")
                      .replaceAll("^_|_$", "");
    }
    
    /**
     * Data class for texture properties
     */
    public static class TextureProperties {
        private String elementName;
        private String elementType;
        private String textureStyle;
        private String colorScheme;
        private String materialType;
        private String patternType;
        private List<String> specialEffects = new ArrayList<>();
        private String resolution;
        
        // Getters and setters
        public String getElementName() { return elementName; }
        public void setElementName(String elementName) { this.elementName = elementName; }
        
        public String getElementType() { return elementType; }
        public void setElementType(String elementType) { this.elementType = elementType; }
        
        public String getTextureStyle() { return textureStyle; }
        public void setTextureStyle(String textureStyle) { this.textureStyle = textureStyle; }
        
        public String getColorScheme() { return colorScheme; }
        public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }
        
        public String getMaterialType() { return materialType; }
        public void setMaterialType(String materialType) { this.materialType = materialType; }
        
        public String getPatternType() { return patternType; }
        public void setPatternType(String patternType) { this.patternType = patternType; }
        
        public List<String> getSpecialEffects() { return specialEffects; }
        public void setSpecialEffects(List<String> specialEffects) { this.specialEffects = specialEffects; }
        
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
    }
}

