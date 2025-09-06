package net.mcreator.aimodgenerator.generators;

import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;
import net.mcreator.aimodgenerator.media.BlockbenchModelGenerator;
import net.mcreator.aimodgenerator.media.TextureGenerator;
import net.mcreator.aimodgenerator.media.SoundGenerator;

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
 * Generator for creating Minecraft entities with Blockbench and GeckoLib support
 * Handles entity models, textures, animations, and behaviors
 */
public class EntityGenerator extends BaseGenerator {
    
    private final BlockbenchModelGenerator modelGenerator;
    private final TextureGenerator textureGenerator;
    private final SoundGenerator soundGenerator;
    
    public EntityGenerator(Object workspace, AIIntegrationService aiService) {
        super(workspace, aiService);
        this.modelGenerator = new BlockbenchModelGenerator(workspace, aiService);
        this.textureGenerator = new TextureGenerator(workspace, aiService);
        this.soundGenerator = new SoundGenerator(workspace, aiService);
    }
    
    /**
     * Generates a complete entity with Blockbench model and GeckoLib animations
     * @param entityName Name of the entity
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @param uploadedImage Optional uploaded character image for conversion
     * @return Generated entity details
     */
    public EntityGenerationResult generateEntity(String entityName, PromptAnalysis analysis, 
                                               SearchResults searchResults, GenerationOptions options,
                                               String uploadedImage) throws Exception {
        
        // Step 1: Analyze entity requirements
        EntityProperties properties = analyzeEntityProperties(entityName, analysis, searchResults, options, uploadedImage);
        
        // Step 2: Generate Blockbench model ideas and mockups
        BlockbenchModelGenerator.ModelIdea modelIdea = modelGenerator.generateModelIdea(
            entityName, "entity", analysis, searchResults, options);
        
        // Step 3: Generate entity textures (including from uploaded image)
        Map<String, String> textures = generateEntityTextures(entityName, properties, uploadedImage, options);
        
        // Step 4: Generate entity sounds
        Map<String, String> sounds = soundGenerator.generateElementSounds(
            entityName, "entity", analysis, searchResults, options);
        
        // Step 5: Generate GeckoLib animation data
        GeckoLibAnimationData animations = generateGeckoLibAnimations(entityName, properties, options);
        
        // Step 6: Generate entity behavior code
        String behaviorCode = generateEntityBehavior(entityName, properties, options);
        
        // Step 7: Create entity configuration
        EntityConfiguration config = createEntityConfiguration(entityName, properties, options);
        
        return new EntityGenerationResult(entityName, properties, modelIdea, textures, sounds, 
                                        animations, behaviorCode, config);
    }
    
    /**
     * Converts uploaded character image to Blockbench entity model
     * @param uploadedImagePath Path to uploaded character image
     * @param entityName Name for the new entity
     * @param analysis Prompt analysis
     * @param options Generation options
     * @return Conversion result with model and textures
     */
    public EntityConversionResult convertImageToEntity(String uploadedImagePath, String entityName,
                                                     PromptAnalysis analysis, GenerationOptions options) throws Exception {
        
        // Step 1: Analyze uploaded image
        ImageAnalysisResult imageAnalysis = analyzeUploadedImage(uploadedImagePath, entityName);
        
        // Step 2: Generate enhanced entity properties from image
        EntityProperties properties = createPropertiesFromImage(imageAnalysis, entityName, analysis);
        
        // Step 3: Generate Blockbench-style flat representation
        String flatModelImage = generateFlatBlockbenchRepresentation(uploadedImagePath, entityName, properties);
        
        // Step 4: Create texture templates from uploaded image
        Map<String, String> textureTemplates = createTextureTemplatesFromImage(uploadedImagePath, entityName, properties);
        
        // Step 5: Generate Blockbench model structure
        BlockbenchModelGenerator.ModelIdea modelIdea = modelGenerator.generateModelIdea(
            entityName, "entity", analysis, null, options);
        
        // Step 6: Generate GeckoLib animations based on image analysis
        GeckoLibAnimationData animations = generateAnimationsFromImageAnalysis(imageAnalysis, entityName, properties);
        
        return new EntityConversionResult(entityName, imageAnalysis, properties, flatModelImage, 
                                        textureTemplates, modelIdea, animations);
    }
    
    /**
     * Analyzes entity properties using AI with enhanced prompting
     */
    private EntityProperties analyzeEntityProperties(String entityName, PromptAnalysis analysis,
                                                   SearchResults searchResults, GenerationOptions options,
                                                   String uploadedImage) throws Exception {
        
        String analysisPrompt = buildEntityAnalysisPrompt(entityName, analysis, searchResults, options, uploadedImage);
        String aiResponse = aiService.generateText(analysisPrompt, 0.6, 600);
        
        return parseEntityProperties(aiResponse, entityName, analysis);
    }
    
    /**
     * Builds comprehensive entity analysis prompt
     */
    private String buildEntityAnalysisPrompt(String entityName, PromptAnalysis analysis,
                                           SearchResults searchResults, GenerationOptions options,
                                           String uploadedImage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze comprehensive entity requirements for Minecraft entity: ").append(entityName).append("\n\n");
        
        // Add context
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Style: ").append(analysis.getStyle()).append("\n");
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n\n");
        
        // Add uploaded image context if available
        if (uploadedImage != null) {
            prompt.append("IMPORTANT: User has uploaded a character image for conversion to Minecraft entity.\n");
            prompt.append("Focus on analyzing how to adapt the uploaded character to Minecraft's blocky style.\n\n");
        }
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(400, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify comprehensive requirements
        prompt.append("Please provide detailed analysis for:\n");
        prompt.append("1. Entity dimensions and hitbox size\n");
        prompt.append("2. Body structure and limb configuration\n");
        prompt.append("3. Movement type (walking, flying, swimming, etc.)\n");
        prompt.append("4. Behavior patterns (passive, neutral, hostile)\n");
        prompt.append("5. Special abilities and attacks\n");
        prompt.append("6. Health, armor, and damage values\n");
        prompt.append("7. Spawn conditions and biomes\n");
        prompt.append("8. Drops and loot tables\n");
        prompt.append("9. Sound requirements (idle, hurt, death, etc.)\n");
        prompt.append("10. Animation requirements for GeckoLib\n");
        prompt.append("11. Texture mapping and UV layout\n");
        prompt.append("12. Blockbench model complexity level\n");
        
        if (uploadedImage != null) {
            prompt.append("13. How to adapt uploaded character to Minecraft blocky style\n");
            prompt.append("14. Key visual elements to preserve from original image\n");
            prompt.append("15. Suggested modifications for Minecraft compatibility\n");
        }
        
        prompt.append("\nFormat your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Generates entity textures with enhanced Blockbench style
     */
    private Map<String, String> generateEntityTextures(String entityName, EntityProperties properties,
                                                      String uploadedImage, GenerationOptions options) throws Exception {
        
        Map<String, String> textures = new HashMap<>();
        
        if (uploadedImage != null) {
            // Generate textures based on uploaded image
            textures = generateTexturesFromUploadedImage(uploadedImage, entityName, properties, options);
        } else {
            // Generate textures from description
            textures = textureGenerator.generateElementTextures(entityName, "entity", 
                getPromptAnalysisFromProperties(properties), null, options);
        }
        
        // Generate additional Blockbench-specific textures
        String flatRepresentation = generateFlatEntityRepresentation(entityName, properties, options);
        if (flatRepresentation != null) {
            textures.put("flat_representation", flatRepresentation);
        }
        
        return textures;
    }
    
    /**
     * Generates flat Blockbench representation using enhanced prompt
     */
    private String generateFlatBlockbenchRepresentation(String uploadedImagePath, String entityName, 
                                                       EntityProperties properties) throws Exception {
        
        String enhancedPrompt = buildEnhancedBlockbenchPrompt(entityName, properties, true);
        
        // Determine output path
        String fileName = sanitizeFileName(entityName + "_flat_blockbench") + ".png";
        Path outputPath = Paths.get(getWorkspace().getWorkspaceFolder().getAbsolutePath(), 
                                   "models", "flat_representations", fileName);
        
        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());
        
        try {
            // Use uploaded image as reference if available
            List<String> references = uploadedImagePath != null ? 
                List.of(uploadedImagePath) : new ArrayList<>();
            
            String generatedImagePath = aiService.generateImageWithReferences(
                enhancedPrompt, outputPath.toString(), "square", references);
            
            // Post-process to ensure perfect Blockbench style
            String processedPath = outputPath.toString().replace(".png", "_processed.png");
            if (processBlockbenchImage(generatedImagePath, processedPath)) {
                return processedPath;
            } else {
                return generatedImagePath;
            }
        } catch (Exception e) {
            System.err.println("Failed to generate flat Blockbench representation: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Builds enhanced Blockbench prompt based on user's detailed specifications
     */
    private String buildEnhancedBlockbenchPrompt(String entityName, EntityProperties properties, boolean isFlat) {
        StringBuilder prompt = new StringBuilder();
        
        // Core directive from user's specification
        prompt.append("I require an image generation that meticulously adheres to the 'Blockbench Minecraft entity/model' aesthetic. ");
        prompt.append("The core directive is to produce flat, two-dimensional representations of structures or individual items, ");
        prompt.append("mirroring the appearance of a Minecraft inventory item texture, a flattened Blockbench model export, or the GUI icon of a block.\n\n");
        
        // Fundamental requirements
        prompt.append("Please ensure the following visual and stylistic guidelines are strictly observed:\n\n");
        
        prompt.append("Fundamental Blocky/Pixelated Aesthetic: The absolute foundation of the image must be a distinct, ");
        prompt.append("chunky pixel art style, instantly recognizable as belonging to the Minecraft universe. ");
        prompt.append("Every element should appear constructed from individual, clearly defined pixels, ");
        prompt.append("eschewing any form of anti-aliasing that would soften these edges.\n\n");
        
        prompt.append("Imposed 64x64 Pixel Texture Grid: The entire visual composition and all its constituent parts ");
        prompt.append("must emulate being rendered with 64x64 pixel texture maps. This means details should be intricate ");
        prompt.append("enough to suggest a higher resolution than the default 16x16, but still fundamentally composed of ");
        prompt.append("visible, distinct pixels, rather than smooth gradients or high-fidelity realism.\n\n");
        
        prompt.append("Strictly Flat, Isometric-Leaning Perspective: The object should primarily appear flat, ");
        prompt.append("avoiding the depth and rotation typical of fully 3D models. Aim for either a direct top-down, ");
        prompt.append("direct front-on, or a subtly isometric top-down/front-side view. The key is 'subtly' – ");
        prompt.append("it should imply just enough depth to distinguish faces, but not so much that it feels like ");
        prompt.append("a fully navigable 3D object.\n\n");
        
        prompt.append("Absolute Transparent Background: The final output image must have a perfectly transparent background, ");
        prompt.append("as if it were a .png file with a robust alpha channel. No background colors, gradients, or scenes ");
        prompt.append("are permissible. The object should stand alone, ready for overlay onto any other visual context.\n\n");
        
        prompt.append("Crisp, Defined Edges and Borders: All lines, outlines, and transitions between different colors ");
        prompt.append("or simulated 'blocks' within the item/structure must be razor-sharp and clearly defined. ");
        prompt.append("There should be no blur, smudge, or ambiguous edges, maintaining the clean, digital precision of pixel art.\n\n");
        
        prompt.append("Strategic, Pixel-Perfect Shading for Minimal Depth: While the overall impression should be flat, ");
        prompt.append("judicious and subtle shading is crucial to give the impression of form without breaking the 2D illusion. ");
        prompt.append("This shading must be pixel-perfect, using distinct color changes rather than soft gradients.\n\n");
        
        // Entity-specific details
        prompt.append("Subject Specification: Create a ").append(entityName).append(" entity ");
        
        if (properties.getBodyStructure() != null) {
            prompt.append("with body structure: ").append(properties.getBodyStructure()).append(". ");
        }
        
        if (properties.getDimensions() != null) {
            prompt.append("Dimensions: ").append(properties.getDimensions()).append(". ");
        }
        
        if (properties.getColorScheme() != null) {
            prompt.append("Colors: ").append(properties.getColorScheme()).append(". ");
        }
        
        prompt.append("Rendered as a flat, inventory-style icon suitable for Blockbench modeling reference.");
        
        return prompt.toString();
    }
    
    /**
     * Generates GeckoLib animation data
     */
    private GeckoLibAnimationData generateGeckoLibAnimations(String entityName, EntityProperties properties, 
                                                           GenerationOptions options) throws Exception {
        
        String animationPrompt = buildGeckoLibAnimationPrompt(entityName, properties);
        String aiResponse = aiService.generateText(animationPrompt, 0.5, 800);
        
        return parseGeckoLibAnimations(aiResponse, entityName, properties);
    }
    
    /**
     * Builds GeckoLib animation generation prompt
     */
    private String buildGeckoLibAnimationPrompt(String entityName, EntityProperties properties) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Generate comprehensive GeckoLib animation data for Minecraft entity: ").append(entityName).append("\n\n");
        
        prompt.append("Entity Properties:\n");
        if (properties.getMovementType() != null) {
            prompt.append("- Movement Type: ").append(properties.getMovementType()).append("\n");
        }
        if (properties.getBehaviorType() != null) {
            prompt.append("- Behavior: ").append(properties.getBehaviorType()).append("\n");
        }
        if (properties.getSpecialAbilities() != null && !properties.getSpecialAbilities().isEmpty()) {
            prompt.append("- Special Abilities: ").append(String.join(", ", properties.getSpecialAbilities())).append("\n");
        }
        
        prompt.append("\nGenerate GeckoLib animation definitions for:\n");
        prompt.append("1. Idle animation (breathing, subtle movements)\n");
        prompt.append("2. Walking/movement animation\n");
        prompt.append("3. Attack animations (if applicable)\n");
        prompt.append("4. Hurt/damage reaction\n");
        prompt.append("5. Death animation\n");
        prompt.append("6. Special ability animations\n");
        prompt.append("7. Spawn/birth animation\n");
        
        if (properties.getMovementType() != null) {
            switch (properties.getMovementType().toLowerCase()) {
                case "flying":
                    prompt.append("8. Flying/hovering animations\n");
                    prompt.append("9. Landing/takeoff animations\n");
                    break;
                case "swimming":
                    prompt.append("8. Swimming animations\n");
                    prompt.append("9. Floating animations\n");
                    break;
                case "climbing":
                    prompt.append("8. Climbing animations\n");
                    prompt.append("9. Wall-clinging animations\n");
                    break;
            }
        }
        
        prompt.append("\nFor each animation, provide:\n");
        prompt.append("- Animation name and duration\n");
        prompt.append("- Keyframe descriptions\n");
        prompt.append("- Bone movements and rotations\n");
        prompt.append("- Easing/interpolation type\n");
        prompt.append("- Loop settings\n");
        prompt.append("- Trigger conditions\n");
        
        return prompt.toString();
    }
    
    /**
     * Analyzes uploaded image for entity conversion
     */
    private ImageAnalysisResult analyzeUploadedImage(String imagePath, String entityName) throws Exception {
        
        String analysisPrompt = "Analyze this uploaded character image for conversion to a Minecraft Blockbench entity. " +
                               "Identify key visual elements, body structure, proportions, colors, and distinctive features " +
                               "that should be preserved when adapting to Minecraft's blocky pixel art style. " +
                               "Consider how to translate smooth curves and details into cubic, pixelated forms while " +
                               "maintaining the character's recognizable identity.";
        
        String aiResponse = aiService.analyzeImage(imagePath, analysisPrompt);
        
        return parseImageAnalysis(aiResponse, imagePath, entityName);
    }
    
    /**
     * Processes generated image to ensure perfect Blockbench style
     */
    private boolean processBlockbenchImage(String inputPath, String outputPath) {
        try {
            // Use the BackgroundRemover to ensure perfect pixel art style
            return net.mcreator.aimodgenerator.media.BackgroundRemover.createPerfectPixelArt(
                inputPath, outputPath, 64, true);
        } catch (Exception e) {
            System.err.println("Failed to process Blockbench image: " + e.getMessage());
            return false;
        }
    }
    
    // Utility methods and data classes
    
    private String sanitizeFileName(String fileName) {
        return fileName.toLowerCase()
                      .replaceAll("[^a-z0-9_]", "_")
                      .replaceAll("_{2,}", "_")
                      .replaceAll("^_|_$", "");
    }
    
    // Data classes for entity generation results
    
    public static class EntityGenerationResult {
        private String entityName;
        private EntityProperties properties;
        private BlockbenchModelGenerator.ModelIdea modelIdea;
        private Map<String, String> textures;
        private Map<String, String> sounds;
        private GeckoLibAnimationData animations;
        private String behaviorCode;
        private EntityConfiguration config;
        
        public EntityGenerationResult(String entityName, EntityProperties properties,
                                    BlockbenchModelGenerator.ModelIdea modelIdea,
                                    Map<String, String> textures, Map<String, String> sounds,
                                    GeckoLibAnimationData animations, String behaviorCode,
                                    EntityConfiguration config) {
            this.entityName = entityName;
            this.properties = properties;
            this.modelIdea = modelIdea;
            this.textures = textures;
            this.sounds = sounds;
            this.animations = animations;
            this.behaviorCode = behaviorCode;
            this.config = config;
        }
        
        // Getters
        public String getEntityName() { return entityName; }
        public EntityProperties getProperties() { return properties; }
        public BlockbenchModelGenerator.ModelIdea getModelIdea() { return modelIdea; }
        public Map<String, String> getTextures() { return textures; }
        public Map<String, String> getSounds() { return sounds; }
        public GeckoLibAnimationData getAnimations() { return animations; }
        public String getBehaviorCode() { return behaviorCode; }
        public EntityConfiguration getConfig() { return config; }
    }
    
    public static class EntityConversionResult {
        private String entityName;
        private ImageAnalysisResult imageAnalysis;
        private EntityProperties properties;
        private String flatModelImage;
        private Map<String, String> textureTemplates;
        private BlockbenchModelGenerator.ModelIdea modelIdea;
        private GeckoLibAnimationData animations;
        
        public EntityConversionResult(String entityName, ImageAnalysisResult imageAnalysis,
                                    EntityProperties properties, String flatModelImage,
                                    Map<String, String> textureTemplates,
                                    BlockbenchModelGenerator.ModelIdea modelIdea,
                                    GeckoLibAnimationData animations) {
            this.entityName = entityName;
            this.imageAnalysis = imageAnalysis;
            this.properties = properties;
            this.flatModelImage = flatModelImage;
            this.textureTemplates = textureTemplates;
            this.modelIdea = modelIdea;
            this.animations = animations;
        }
        
        // Getters
        public String getEntityName() { return entityName; }
        public ImageAnalysisResult getImageAnalysis() { return imageAnalysis; }
        public EntityProperties getProperties() { return properties; }
        public String getFlatModelImage() { return flatModelImage; }
        public Map<String, String> getTextureTemplates() { return textureTemplates; }
        public BlockbenchModelGenerator.ModelIdea getModelIdea() { return modelIdea; }
        public GeckoLibAnimationData getAnimations() { return animations; }
    }
    
    public static class EntityProperties {
        private String entityName;
        private String dimensions;
        private String bodyStructure;
        private String movementType;
        private String behaviorType;
        private List<String> specialAbilities = new ArrayList<>();
        private String healthValue;
        private String armorValue;
        private String damageValue;
        private List<String> spawnConditions = new ArrayList<>();
        private List<String> drops = new ArrayList<>();
        private String colorScheme;
        private String complexityLevel;
        
        // Getters and setters
        public String getEntityName() { return entityName; }
        public void setEntityName(String entityName) { this.entityName = entityName; }
        
        public String getDimensions() { return dimensions; }
        public void setDimensions(String dimensions) { this.dimensions = dimensions; }
        
        public String getBodyStructure() { return bodyStructure; }
        public void setBodyStructure(String bodyStructure) { this.bodyStructure = bodyStructure; }
        
        public String getMovementType() { return movementType; }
        public void setMovementType(String movementType) { this.movementType = movementType; }
        
        public String getBehaviorType() { return behaviorType; }
        public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }
        
        public List<String> getSpecialAbilities() { return specialAbilities; }
        public void setSpecialAbilities(List<String> specialAbilities) { this.specialAbilities = specialAbilities; }
        
        public String getHealthValue() { return healthValue; }
        public void setHealthValue(String healthValue) { this.healthValue = healthValue; }
        
        public String getArmorValue() { return armorValue; }
        public void setArmorValue(String armorValue) { this.armorValue = armorValue; }
        
        public String getDamageValue() { return damageValue; }
        public void setDamageValue(String damageValue) { this.damageValue = damageValue; }
        
        public List<String> getSpawnConditions() { return spawnConditions; }
        public void setSpawnConditions(List<String> spawnConditions) { this.spawnConditions = spawnConditions; }
        
        public List<String> getDrops() { return drops; }
        public void setDrops(List<String> drops) { this.drops = drops; }
        
        public String getColorScheme() { return colorScheme; }
        public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }
        
        public String getComplexityLevel() { return complexityLevel; }
        public void setComplexityLevel(String complexityLevel) { this.complexityLevel = complexityLevel; }
    }
    
    public static class GeckoLibAnimationData {
        private String entityName;
        private List<AnimationDefinition> animations = new ArrayList<>();
        private String animationController;
        private String geometryModel;
        
        // Getters and setters
        public String getEntityName() { return entityName; }
        public void setEntityName(String entityName) { this.entityName = entityName; }
        
        public List<AnimationDefinition> getAnimations() { return animations; }
        public void setAnimations(List<AnimationDefinition> animations) { this.animations = animations; }
        
        public String getAnimationController() { return animationController; }
        public void setAnimationController(String animationController) { this.animationController = animationController; }
        
        public String getGeometryModel() { return geometryModel; }
        public void setGeometryModel(String geometryModel) { this.geometryModel = geometryModel; }
    }
    
    public static class AnimationDefinition {
        private String name;
        private double duration;
        private boolean loop;
        private List<String> keyframes = new ArrayList<>();
        private String triggerCondition;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }
        
        public boolean isLoop() { return loop; }
        public void setLoop(boolean loop) { this.loop = loop; }
        
        public List<String> getKeyframes() { return keyframes; }
        public void setKeyframes(List<String> keyframes) { this.keyframes = keyframes; }
        
        public String getTriggerCondition() { return triggerCondition; }
        public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }
    }
    
    public static class ImageAnalysisResult {
        private String imagePath;
        private String entityName;
        private String visualDescription;
        private String bodyStructure;
        private String colorPalette;
        private List<String> keyFeatures = new ArrayList<>();
        private String adaptationSuggestions;
        
        // Getters and setters
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
        
        public String getEntityName() { return entityName; }
        public void setEntityName(String entityName) { this.entityName = entityName; }
        
        public String getVisualDescription() { return visualDescription; }
        public void setVisualDescription(String visualDescription) { this.visualDescription = visualDescription; }
        
        public String getBodyStructure() { return bodyStructure; }
        public void setBodyStructure(String bodyStructure) { this.bodyStructure = bodyStructure; }
        
        public String getColorPalette() { return colorPalette; }
        public void setColorPalette(String colorPalette) { this.colorPalette = colorPalette; }
        
        public List<String> getKeyFeatures() { return keyFeatures; }
        public void setKeyFeatures(List<String> keyFeatures) { this.keyFeatures = keyFeatures; }
        
        public String getAdaptationSuggestions() { return adaptationSuggestions; }
        public void setAdaptationSuggestions(String adaptationSuggestions) { this.adaptationSuggestions = adaptationSuggestions; }
    }
    
    public static class EntityConfiguration {
        private String entityName;
        private String entityType;
        private Map<String, Object> properties = new HashMap<>();
        private String mcreatorCode;
        
        // Getters and setters
        public String getEntityName() { return entityName; }
        public void setEntityName(String entityName) { this.entityName = entityName; }
        
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        
        public Map<String, Object> getProperties() { return properties; }
        public void setProperties(Map<String, Object> properties) { this.properties = properties; }
        
        public String getMcreatorCode() { return mcreatorCode; }
        public void setMcreatorCode(String mcreatorCode) { this.mcreatorCode = mcreatorCode; }
    }
    
    // Placeholder methods for missing implementations
    private EntityProperties parseEntityProperties(String aiResponse, String entityName, PromptAnalysis analysis) {
        // Implementation would parse AI response into EntityProperties
        EntityProperties properties = new EntityProperties();
        properties.setEntityName(entityName);
        // Parse other properties from AI response
        return properties;
    }
    
    private PromptAnalysis getPromptAnalysisFromProperties(EntityProperties properties) {
        // Convert properties back to PromptAnalysis for texture generation
        return new PromptAnalysis();
    }
    
    private String generateFlatEntityRepresentation(String entityName, EntityProperties properties, GenerationOptions options) throws Exception {
        return generateFlatBlockbenchRepresentation(null, entityName, properties);
    }
    
    private Map<String, String> generateTexturesFromUploadedImage(String uploadedImage, String entityName, EntityProperties properties, GenerationOptions options) throws Exception {
        // Implementation would generate textures based on uploaded image
        return new HashMap<>();
    }
    
    private Map<String, String> createTextureTemplatesFromImage(String uploadedImagePath, String entityName, EntityProperties properties) throws Exception {
        // Implementation would create texture templates from uploaded image
        return new HashMap<>();
    }
    
    private GeckoLibAnimationData parseGeckoLibAnimations(String aiResponse, String entityName, EntityProperties properties) {
        // Implementation would parse AI response into GeckoLib animation data
        GeckoLibAnimationData data = new GeckoLibAnimationData();
        data.setEntityName(entityName);
        return data;
    }
    
    private GeckoLibAnimationData generateAnimationsFromImageAnalysis(ImageAnalysisResult imageAnalysis, String entityName, EntityProperties properties) {
        // Implementation would generate animations based on image analysis
        GeckoLibAnimationData data = new GeckoLibAnimationData();
        data.setEntityName(entityName);
        return data;
    }
    
    private ImageAnalysisResult parseImageAnalysis(String aiResponse, String imagePath, String entityName) {
        // Implementation would parse AI response into ImageAnalysisResult
        ImageAnalysisResult result = new ImageAnalysisResult();
        result.setImagePath(imagePath);
        result.setEntityName(entityName);
        return result;
    }
    
    private EntityProperties createPropertiesFromImage(ImageAnalysisResult imageAnalysis, String entityName, PromptAnalysis analysis) {
        // Implementation would create entity properties from image analysis
        EntityProperties properties = new EntityProperties();
        properties.setEntityName(entityName);
        return properties;
    }
    
    private String generateEntityBehavior(String entityName, EntityProperties properties, GenerationOptions options) throws Exception {
        // Implementation would generate entity behavior code
        return "// Generated entity behavior code for " + entityName;
    }
    
    private EntityConfiguration createEntityConfiguration(String entityName, EntityProperties properties, GenerationOptions options) {
        // Implementation would create entity configuration
        EntityConfiguration config = new EntityConfiguration();
        config.setEntityName(entityName);
        return config;
    }
}

