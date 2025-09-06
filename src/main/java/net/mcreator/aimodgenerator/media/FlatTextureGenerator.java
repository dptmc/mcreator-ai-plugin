package net.mcreator.aimodgenerator.media;

import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized generator for flat Minecraft textures using exact user specifications
 * Focuses on 64x64 pixel grid textures with transparent backgrounds
 */
public class FlatTextureGenerator {
    
    private final Object workspace;
    private final AIIntegrationService aiService;
    private final Path texturesPath;
    
    public FlatTextureGenerator(Object workspace, AIIntegrationService aiService) {
        this.workspace = workspace;
        this.aiService = aiService;
        this.texturesPath = Paths.get(getWorkspaceFolder().getAbsolutePath(), "src", "main", "resources", "assets", "textures", "flat");
        
        // Ensure texture directories exist
        createTextureDirectories();
    }
    
    /**
     * Generates flat textures using user's exact specifications
     * @param elementName Name of the element
     * @param elementType Type (item, block, entity)
     * @param analysis Prompt analysis
     * @param searchResults Web search results
     * @param options Generation options
     * @return Map of generated texture paths
     */
    public Map<String, String> generateFlatTextures(String elementName, String elementType,
                                                   PromptAnalysis analysis, SearchResults searchResults,
                                                   GenerationOptions options) throws Exception {
        
        Map<String, String> textures = new HashMap<>();
        
        // Generate main texture
        String mainTexture = generateFlatTexture(elementName, elementType, "main", analysis, options);
        if (mainTexture != null) {
            textures.put("main", mainTexture);
        }
        
        // Generate additional textures based on element type
        if (elementType.equals("block")) {
            // Generate side, top, bottom textures for blocks
            String[] blockTextures = {"side", "top", "bottom"};
            for (String textureType : blockTextures) {
                String texture = generateFlatTexture(elementName, elementType, textureType, analysis, options);
                if (texture != null) {
                    textures.put(textureType, texture);
                }
            }
        } else if (elementType.equals("entity")) {
            // Generate body parts for entities
            String[] entityTextures = {"body", "head", "limbs"};
            for (String textureType : entityTextures) {
                String texture = generateFlatTexture(elementName, elementType, textureType, analysis, options);
                if (texture != null) {
                    textures.put(textureType, texture);
                }
            }
        }
        
        return textures;
    }
    
    /**
     * Generates a single flat texture using user's exact prompt specifications
     */
    private String generateFlatTexture(String elementName, String elementType, String textureType,
                                     PromptAnalysis analysis, GenerationOptions options) throws Exception {
        
        // Build the exact prompt using user's specifications
        String texturePrompt = buildUserSpecifiedPrompt(elementName, elementType, textureType, analysis, options);
        
        // Determine output path
        String fileName = sanitizeFileName(elementName + "_" + textureType + "_flat") + ".png";
        Path outputPath = texturesPath.resolve(elementType).resolve(fileName);
        
        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());
        
        try {
            // Generate the texture using AI service
            String generatedTexturePath = aiService.generateImage(texturePrompt, outputPath.toString(), "square");
            
            // Post-process to ensure perfect 64x64 pixel grid
            String processedPath = outputPath.toString().replace(".png", "_processed.png");
            
            // Apply user's specifications: 64x64 grid, transparent background, no unwanted glow
            if (processToUserSpecifications(generatedTexturePath, processedPath, options)) {
                return processedPath;
            } else {
                return generatedTexturePath;
            }
            
        } catch (Exception e) {
            System.err.println("Failed to generate flat texture for " + elementName + "_" + textureType + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Builds texture prompt using user's exact specifications
     */
    private String buildUserSpecifiedPrompt(String elementName, String elementType, String textureType,
                                          PromptAnalysis analysis, GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        // User's exact opening specification
        prompt.append("You're a Minecraft texture designer for a Minecraft mod. ");
        prompt.append("Only generate in a grid of 64 by 64 pixels with transparent background. ");
        
        // Specific texture request based on element type
        if (elementType.equals("item")) {
            prompt.append("Give me a cool item texture idea for ").append(elementName);
            if (!textureType.equals("main")) {
                prompt.append(" (").append(textureType).append(" variant)");
            }
            prompt.append(". ");
        } else if (elementType.equals("block")) {
            prompt.append("Give me a cool block texture idea for ").append(elementName).append(" block");
            if (!textureType.equals("main")) {
                prompt.append(" (").append(textureType).append(" face)");
            }
            prompt.append(". ");
        } else if (elementType.equals("entity")) {
            prompt.append("Give me an entity texture for ").append(elementName);
            if (!textureType.equals("main")) {
                prompt.append(" (").append(textureType).append(" part)");
            }
            prompt.append(". ");
        }
        
        // Core requirement from user's specification
        prompt.append("Make it a flat texture fully in the grid. ");
        
        // Glow and particle handling based on user's exact specification
        boolean allowGlow = options.isAllowGlowEffects() && 
                           (analysis.getOriginalPrompt().toLowerCase().contains("glow") ||
                            analysis.getOriginalPrompt().toLowerCase().contains("light") ||
                            analysis.getOriginalPrompt().toLowerCase().contains("bright") ||
                            analysis.getOriginalPrompt().toLowerCase().contains("particle"));
        
        if (allowGlow) {
            prompt.append("You may use bright pixels having the illusion of glow since the user requested glowing effects. ");
            prompt.append("But that's still 64 by 64 pixels with the pixel grid structure maintained. ");
        } else {
            prompt.append("No glow or unwanted particles. ");
            prompt.append("Only use bright pixels for glow illusion if it's specifically a particle texture, ");
            prompt.append("and that's also still 64 by 64 bit pixels. ");
        }
        
        // Add context from analysis
        if (analysis.getTheme() != null && !analysis.getTheme().isEmpty()) {
            prompt.append("Theme: ").append(analysis.getTheme()).append(". ");
        }
        
        if (analysis.getStyle() != null && !analysis.getStyle().isEmpty()) {
            prompt.append("Style: ").append(analysis.getStyle()).append(". ");
        }
        
        // Additional context from original prompt
        if (analysis.getOriginalPrompt() != null && !analysis.getOriginalPrompt().isEmpty()) {
            prompt.append("Based on: ").append(analysis.getOriginalPrompt()).append(". ");
        }
        
        // Final specifications
        prompt.append("Ensure perfect 64x64 pixel grid, transparent background, ");
        prompt.append("and Minecraft pixel art style with clear, defined pixels.");
        
        return prompt.toString();
    }
    
    /**
     * Processes texture to meet user's exact specifications
     */
    private boolean processToUserSpecifications(String inputPath, String outputPath, GenerationOptions options) {
        try {
            // Use BackgroundRemover to ensure perfect pixel art with transparent background
            // Force 64x64 size as per user specification
            boolean success = BackgroundRemover.createPerfectPixelArt(inputPath, outputPath, 64, true);
            
            if (success && !options.isAllowGlowEffects()) {
                // Additional processing to remove unwanted glow effects if not allowed
                success = BackgroundRemover.removeGlowEffects(outputPath, outputPath);
            }
            
            return success;
            
        } catch (Exception e) {
            System.err.println("Failed to process texture to user specifications: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates necessary texture directories
     */
    private void createTextureDirectories() {
        try {
            Files.createDirectories(texturesPath.resolve("item"));
            Files.createDirectories(texturesPath.resolve("block"));
            Files.createDirectories(texturesPath.resolve("entity"));
        } catch (IOException e) {
            System.err.println("Failed to create texture directories: " + e.getMessage());
        }
    }
    
    /**
     * Sanitizes file names for safe file system usage
     */
    private String sanitizeFileName(String fileName) {
        return fileName.toLowerCase()
                      .replaceAll("[^a-z0-9_]", "_")
                      .replaceAll("_{2,}", "_")
                      .replaceAll("^_|_$", "");
    }
    
    /**
     * Gets workspace folder (placeholder implementation)
     */
    private File getWorkspaceFolder() {
        // This would be implemented to get the actual workspace folder
        // For now, return a default path
        return new File(System.getProperty("user.home"), "mcreator_workspace");
    }
    
    /**
     * Generates texture ideas based on user's specification
     * @param category Category of texture (cool_items, cool_blocks, particles)
     * @param count Number of ideas to generate
     * @return Map of texture ideas with descriptions
     */
    public Map<String, String> generateTextureIdeas(String category, int count) throws Exception {
        Map<String, String> ideas = new HashMap<>();
        
        String ideaPrompt = buildTextureIdeaPrompt(category, count);
        String aiResponse = aiService.generateText(ideaPrompt, 0.8, 500);
        
        // Parse AI response into individual ideas
        String[] ideaLines = aiResponse.split("\\n");
        int ideaCount = 0;
        
        for (String line : ideaLines) {
            if (line.trim().length() > 0 && ideaCount < count) {
                String ideaName = "idea_" + (ideaCount + 1);
                ideas.put(ideaName, line.trim());
                ideaCount++;
            }
        }
        
        return ideas;
    }
    
    /**
     * Builds prompt for generating texture ideas
     */
    private String buildTextureIdeaPrompt(String category, int count) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You're a Minecraft texture designer for a Minecraft mod. ");
        prompt.append("Only generate in a grid of 64 by 64 pixels with transparent background. ");
        
        switch (category.toLowerCase()) {
            case "cool_items":
                prompt.append("Give me ").append(count).append(" cool item texture ideas for new Minecraft items. ");
                break;
            case "cool_blocks":
                prompt.append("Give me ").append(count).append(" cool block texture ideas for new Minecraft blocks. ");
                break;
            case "particles":
                prompt.append("Give me ").append(count).append(" particle texture ideas. ");
                prompt.append("These can have bright pixels with glow illusion since they're particle textures. ");
                break;
            default:
                prompt.append("Give me ").append(count).append(" texture ideas for Minecraft mod elements. ");
        }
        
        prompt.append("Make them flat textures fully in the grid. ");
        
        if (!category.equals("particles")) {
            prompt.append("No glow or unwanted particles unless specifically requested. ");
        }
        
        prompt.append("Each idea should be described in one line with the texture concept and visual details. ");
        prompt.append("Focus on 64 by 64 pixel grid designs with transparent backgrounds.");
        
        return prompt.toString();
    }
}

