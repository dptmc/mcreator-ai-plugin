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
 * Generator for creating Blockbench model ideas and mockups
 * Handles entity models, item models, and block models for Minecraft
 */
public class BlockbenchModelGenerator {
    
    private final Workspace workspace;
    private final AIIntegrationService aiService;
    private final Path modelsPath;
    
    public BlockbenchModelGenerator(Workspace workspace, AIIntegrationService aiService) {
        this.workspace = workspace;
        this.aiService = aiService;
        this.modelsPath = Paths.get(workspace.getWorkspaceFolder().getAbsolutePath(), "models");
        
        // Ensure model directories exist
        createModelDirectories();
    }
    
    /**
     * Generates a Blockbench model idea with mockup images
     * @param modelName Name of the model
     * @param modelType Type of model (entity, item, block)
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return Model idea details with generated mockups
     */
    public ModelIdea generateModelIdea(String modelName, String modelType, 
                                     PromptAnalysis analysis, SearchResults searchResults, 
                                     GenerationOptions options) throws Exception {
         /**
     * Converts uploaded character image to Blockbench entity model idea
     * @param uploadedImagePath Path to uploaded character image
     * @param entityName Name for the entity
     * @param analysis Prompt analysis
     * @param options Generation options
     * @return Model idea with conversion suggestions
     */
    public ModelIdea convertImageToBlockbenchEntity(String uploadedImagePath, String entityName,
                                                  PromptAnalysis analysis, GenerationOptions options) throws Exception {
        
        // Step 1: Analyze uploaded image for Blockbench conversion
        String analysisPrompt = buildImageAnalysisPrompt(entityName, uploadedImagePath);
        String imageAnalysis = aiService.analyzeImage(uploadedImagePath, analysisPrompt);
        
        // Step 2: Generate flat Blockbench representation using enhanced prompt
        String flatRepresentation = generateFlatBlockbenchFromImage(uploadedImagePath, entityName, imageAnalysis);
        
        // Step 3: Generate multiple view mockups
        Map<String, String> viewMockups = generateEntityViewMockups(entityName, imageAnalysis, uploadedImagePath);
        
        // Step 4: Create modeling instructions
        String modelingInstructions = generateEntityModelingInstructions(entityName, imageAnalysis);
        
        // Step 5: Generate texture templates
        Map<String, String> textureTemplates = generateEntityTextureTemplates(entityName, imageAnalysis, uploadedImagePath);
        
        // Step 6: Create Blockbench project template
        String projectTemplate = generateBlockbenchProjectTemplate(entityName, imageAnalysis);
        
        return new ModelIdea(entityName, "entity", flatRepresentation, viewMockups, 
                           modelingInstructions, textureTemplates, projectTemplate, imageAnalysis);
    }
    
    /**
     * Builds image analysis prompt for entity conversion
     */
    private String buildImageAnalysisPrompt(String entityName, String imagePath) {
        return "Analyze this uploaded character image for conversion to a Minecraft Blockbench entity model. " +
               "Focus on identifying:\n" +
               "1. Overall body structure and proportions\n" +
               "2. Key visual elements that define the character\n" +
               "3. Color palette and texture patterns\n" +
               "4. How to adapt smooth curves to blocky Minecraft style\n" +
               "5. Suggested limb and body part divisions for modeling\n" +
               "6. Animation potential and joint placement\n" +
               "7. Distinctive features to preserve in pixel art form\n" +
               "8. Recommended model complexity level for Minecraft\n\n" +
               "Provide detailed analysis for creating a faithful Blockbench adaptation.";
    }
    
    /**
     * Generates flat Blockbench representation from uploaded image
     */
    private String generateFlatBlockbenchFromImage(String uploadedImagePath, String entityName, String imageAnalysis) throws Exception {
        
        // Use the enhanced Blockbench prompt from user's specification
        String enhancedPrompt = buildEnhancedBlockbenchPrompt(entityName, imageAnalysis);
        
        String fileName = sanitizeFileName(entityName + "_flat_entity") + ".png";
        Path outputPath = modelsPath.resolve("entity_flats").resolve(fileName);
        Files.createDirectories(outputPath.getParent());
        
        try {
            // Generate with uploaded image as reference
            List<String> references = List.of(uploadedImagePath);
            String generatedPath = aiService.generateImageWithReferences(
                enhancedPrompt, outputPath.toString(), "square", references);
            
            // Post-process for perfect Blockbench style
            String processedPath = outputPath.toString().replace(".png", "_processed.png");
            if (processBlockbenchStyle(generatedPath, processedPath)) {
                return processedPath;
            }
            return generatedPath;
            
        } catch (Exception e) {
            System.err.println("Failed to generate flat Blockbench representation: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Builds enhanced Blockbench prompt using user's detailed specifications
     */
    private String buildEnhancedBlockbenchPrompt(String entityName, String imageAnalysis) {
        StringBuilder prompt = new StringBuilder();
        
        // Core directive from user's specification
        prompt.append("I require an image generation that meticulously adheres to the 'Blockbench Minecraft entity/model' aesthetic. ");
        prompt.append("The core directive is to produce flat, two-dimensional representations of structures or individual items, ");
        prompt.append("mirroring the appearance of a Minecraft inventory item texture, a flattened Blockbench model export, or the GUI icon of a block.\n\n");
        
        // Enhanced visual guidelines
        prompt.append("Please ensure the following visual and stylistic guidelines are strictly observed:\n\n");
        
        prompt.append("Fundamental Blocky/Pixelated Aesthetic: The absolute foundation of the image must be a distinct, ");
        prompt.append("chunky pixel art style, instantly recognizable as belonging to the Minecraft universe. ");
        prompt.append("Every element should appear constructed from individual, clearly defined pixels, ");
        prompt.append("eschewing any form of anti-aliasing that would soften these edges.\n\n");
        
        prompt.append("Imposed 64x64 Pixel Texture Grid: The entire visual composition and all its constituent parts ");
        prompt.append("must emulate being rendered with 64x64 pixel texture maps. This means details should be intricate ");
        prompt.append("enough to suggest a higher resolution than the default 16x16, but still fundamentally composed of ");
        prompt.append("visible, distinct pixels, rather than smooth gradients or high-fidelity realism. ");
        prompt.append("Consider how a highly detailed custom Minecraft resource pack texture would look.\n\n");
        
        prompt.append("Strictly Flat, Isometric-Leaning Perspective: The object should primarily appear flat, ");
        prompt.append("avoiding the depth and rotation typical of fully 3D models.\n\n");
        
        prompt.append("Perspective Type: Aim for either a direct top-down, direct front-on, or a subtly isometric ");
        prompt.append("top-down/front-side view. The key is 'subtly' – it should imply just enough depth to distinguish ");
        prompt.append("faces (e.g., the top and one side of a block), but not so much that it feels like a fully navigable 3D object.\n\n");
        
        prompt.append("No Dynamic Posing/Complex Angles: Avoid any sense of dynamic camera angles or deep perspective. ");
        prompt.append("The goal is an iconic, almost diagrammatic representation suitable for an inventory slot or UI element.\n\n");
        
        prompt.append("Absolute Transparent Background: The final output image must have a perfectly transparent background, ");
        prompt.append("as if it were a .png file with a robust alpha channel. No background colors, gradients, or scenes ");
        prompt.append("are permissible. The object should stand alone, ready for overlay onto any other visual context.\n\n");
        
        prompt.append("Crisp, Defined Edges and Borders: All lines, outlines, and transitions between different colors ");
        prompt.append("or simulated 'blocks' within the item/structure must be razor-sharp and clearly defined. ");
        prompt.append("There should be no blur, smudge, or ambiguous edges, maintaining the clean, digital precision of pixel art.\n\n");
        
        prompt.append("Strategic, Pixel-Perfect Shading for Minimal Depth: While the overall impression should be flat, ");
        prompt.append("judicious and subtle shading is crucial to give the impression of form without breaking the 2D illusion.\n\n");
        
        prompt.append("Purpose: Shading should serve to define edges, indicate different planes, and suggest volume ");
        prompt.append("(e.g., the roundness of an apple or the layers of a crafting bench).\n\n");
        
        prompt.append("Execution: This shading must be pixel-perfect, using distinct color changes (often slightly darker ");
        prompt.append("or lighter shades of the base color) rather than soft gradients. Think of how Minecraft block ");
        prompt.append("textures often have slight variations in pixel color to create texture and depth.\n\n");
        
        prompt.append("Light Source (Implied): If shading is applied, imply a consistent, simple light source ");
        prompt.append("(e.g., top-left) to unify the design, but keep it subtle.\n\n");
        
        // Subject specification with image analysis context
        prompt.append("Subject Specification: Create a flat, Blockbench-style representation of ").append(entityName);
        prompt.append(" entity based on the uploaded character image. ");
        
        if (imageAnalysis != null && !imageAnalysis.isEmpty()) {
            prompt.append("Key elements to incorporate from the original image: ");
            prompt.append(imageAnalysis.substring(0, Math.min(200, imageAnalysis.length())));
            prompt.append(". ");
        }
        
        prompt.append("Adapt the character to Minecraft's blocky aesthetic while preserving recognizable features. ");
        prompt.append("Render as a flat, inventory-style icon suitable for Blockbench modeling reference.");
        
        return prompt.toString();
    }
    
    /**
     * Generates multiple view mockups for entity modeling
     */
    private Map<String, String> generateEntityViewMockups(String entityName, String imageAnalysis, String uploadedImagePath) throws Exception {
        Map<String, String> mockups = new HashMap<>();
        
        String[] views = {"front", "side", "back", "perspective"};
        
        for (String view : views) {
            String viewPrompt = buildViewSpecificPrompt(entityName, view, imageAnalysis);
            String fileName = sanitizeFileName(entityName + "_" + view + "_view") + ".png";
            Path outputPath = modelsPath.resolve("entity_views").resolve(fileName);
            Files.createDirectories(outputPath.getParent());
            
            try {
                List<String> references = List.of(uploadedImagePath);
                String generatedPath = aiService.generateImageWithReferences(
                    viewPrompt, outputPath.toString(), "square", references);
                mockups.put(view, generatedPath);
            } catch (Exception e) {
                System.err.println("Failed to generate " + view + " view for " + entityName + ": " + e.getMessage());
            }
        }
        
        return mockups;
    }
    
    /**
     * Builds view-specific prompts for entity mockups
     */
    private String buildViewSpecificPrompt(String entityName, String view, String imageAnalysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a ").append(view).append(" view mockup of ").append(entityName);
        prompt.append(" entity in Blockbench/Minecraft style. ");
        
        switch (view) {
            case "front":
                prompt.append("Show the entity facing directly forward, displaying the main features and face. ");
                break;
            case "side":
                prompt.append("Show the entity from the side profile, displaying body depth and limb positioning. ");
                break;
            case "back":
                prompt.append("Show the entity from behind, displaying back details and rear features. ");
                break;
            case "perspective":
                prompt.append("Show the entity from a 3/4 perspective angle, displaying overall form and proportions. ");
                break;
        }
        
        prompt.append("Use 64x64 pixel resolution, blocky Minecraft aesthetic, transparent background, ");
        prompt.append("and pixel-perfect shading. ");
        
        if (imageAnalysis != null && !imageAnalysis.isEmpty()) {
            prompt.append("Incorporate key features: ");
            prompt.append(imageAnalysis.substring(0, Math.min(150, imageAnalysis.length())));
        }
        
        return prompt.toString();
    }
    
    /**
     * Generates entity modeling instructions
     */
    private String generateEntityModelingInstructions(String entityName, String imageAnalysis) throws Exception {
        String instructionPrompt = "Create detailed Blockbench modeling instructions for " + entityName + " entity. " +
                                 "Based on the image analysis: " + imageAnalysis + "\n\n" +
                                 "Provide step-by-step instructions including:\n" +
                                 "1. Setting up the Blockbench project\n" +
                                 "2. Creating the main body structure\n" +
                                 "3. Adding limbs and appendages\n" +
                                 "4. Detailing facial features\n" +
                                 "5. UV mapping and texture application\n" +
                                 "6. Setting up bones for animation\n" +
                                 "7. Exporting for Minecraft/MCreator\n" +
                                 "8. Tips for maintaining Minecraft aesthetic\n" +
                                 "9. Common pitfalls to avoid\n" +
                                 "10. Optimization suggestions";
        
        return aiService.generateText(instructionPrompt, 0.3, 1000);
    }
    
    /**
     * Generates entity texture templates
     */
    private Map<String, String> generateEntityTextureTemplates(String entityName, String imageAnalysis, String uploadedImagePath) throws Exception {
        Map<String, String> templates = new HashMap<>();
        
        String[] templateTypes = {"body", "head", "limbs", "details"};
        
        for (String templateType : templateTypes) {
            String templatePrompt = buildTextureTemplatePrompt(entityName, templateType, imageAnalysis);
            String fileName = sanitizeFileName(entityName + "_" + templateType + "_template") + ".png";
            Path outputPath = modelsPath.resolve("texture_templates").resolve(fileName);
            Files.createDirectories(outputPath.getParent());
            
            try {
                List<String> references = List.of(uploadedImagePath);
                String generatedPath = aiService.generateImageWithReferences(
                    templatePrompt, outputPath.toString(), "square", references);
                templates.put(templateType, generatedPath);
            } catch (Exception e) {
                System.err.println("Failed to generate " + templateType + " template: " + e.getMessage());
            }
        }
        
        return templates;
    }
    
    /**
     * Builds texture template prompts
     */
    private String buildTextureTemplatePrompt(String entityName, String templateType, String imageAnalysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a Minecraft entity texture template for ").append(entityName);
        prompt.append(" ").append(templateType).append(" parts. ");
        
        switch (templateType) {
            case "body":
                prompt.append("Focus on torso, chest, and main body textures. ");
                break;
            case "head":
                prompt.append("Focus on face, eyes, mouth, and head details. ");
                break;
            case "limbs":
                prompt.append("Focus on arms, legs, hands, and feet textures. ");
                break;
            case "details":
                prompt.append("Focus on accessories, clothing, and special features. ");
                break;
        }
        
        prompt.append("Use 64x64 pixel resolution, flat UV mapping layout, ");
        prompt.append("transparent background, and Minecraft pixel art style. ");
        
        if (imageAnalysis != null && !imageAnalysis.isEmpty()) {
            prompt.append("Based on image analysis: ");
            prompt.append(imageAnalysis.substring(0, Math.min(100, imageAnalysis.length())));
        }
        
        return prompt.toString();
    }
    
    /**
     * Generates Blockbench project template
     */
    private String generateBlockbenchProjectTemplate(String entityName, String imageAnalysis) throws Exception {
        String templatePrompt = "Generate a Blockbench project template JSON structure for " + entityName + " entity. " +
                              "Based on analysis: " + imageAnalysis + "\n\n" +
                              "Include:\n" +
                              "1. Project metadata and settings\n" +
                              "2. Geometry definitions\n" +
                              "3. Bone structure hierarchy\n" +
                              "4. Cube/element definitions\n" +
                              "5. UV mapping coordinates\n" +
                              "6. Animation bone setup\n" +
                              "7. Texture references\n" +
                              "8. Export settings for Minecraft\n\n" +
                              "Format as valid Blockbench JSON.";
        
        return aiService.generateText(templatePrompt, 0.2, 1200);
    }
    
    /**
     * Processes image to ensure perfect Blockbench style
     */
    private boolean processBlockbenchStyle(String inputPath, String outputPath) {
        try {
            // Use BackgroundRemover to create perfect pixel art
            return net.mcreator.aimodgenerator.media.BackgroundRemover.createPerfectPixelArt(
                inputPath, outputPath, 64, true);
        } catch (Exception e) {
            System.err.println("Failed to process Blockbench style: " + e.getMessage());
            return false;
        }
    }
        ModelProperties properties = analyzeModelProperties(modelName, modelType, analysis, searchResults, options);
        
        // Step 2: Generate model mockup images
        List<String> mockupPaths = generateModelMockups(modelName, modelType, properties, options);
        
        // Step 3: Generate texture templates
        List<String> texturePaths = generateTextureTemplates(modelName, modelType, properties, options);
        
        // Step 4: Create model idea documentation
        String documentation = generateModelDocumentation(modelName, modelType, properties, options);
        
        // Step 5: Create Blockbench project template
        String projectTemplate = generateBlockbenchTemplate(modelName, modelType, properties);
        
        return new ModelIdea(modelName, modelType, properties, mockupPaths, texturePaths, documentation, projectTemplate);
    }
    
    /**
     * Generates multiple model ideas for a theme
     * @param elementNames List of element names
     * @param modelType Type of models to generate
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return List of model ideas
     */
    public List<ModelIdea> generateThemeModelIdeas(List<String> elementNames, String modelType,
                                                 PromptAnalysis analysis, SearchResults searchResults,
                                                 GenerationOptions options) throws Exception {
        
        List<ModelIdea> modelIdeas = new ArrayList<>();
        
        // Generate consistent style guide for the theme
        ModelStyleGuide styleGuide = generateModelStyleGuide(analysis, searchResults, options);
        
        for (String elementName : elementNames) {
            ModelProperties properties = analyzeModelProperties(elementName, modelType, analysis, searchResults, options);
            properties.setStyleGuide(styleGuide);
            
            ModelIdea idea = generateModelIdea(elementName, modelType, analysis, searchResults, options);
            modelIdeas.add(idea);
        }
        
        return modelIdeas;
    }
    
    /**
     * Analyzes model properties using AI
     */
    private ModelProperties analyzeModelProperties(String modelName, String modelType,
                                                 PromptAnalysis analysis, SearchResults searchResults,
                                                 GenerationOptions options) throws Exception {
        
        String analysisPrompt = buildModelAnalysisPrompt(modelName, modelType, analysis, searchResults, options);
        String aiResponse = aiService.generateText(analysisPrompt, 0.6, 500);
        
        return parseModelProperties(aiResponse, modelName, modelType, analysis);
    }
    
    /**
     * Builds the AI prompt for model analysis
     */
    private String buildModelAnalysisPrompt(String modelName, String modelType,
                                          PromptAnalysis analysis, SearchResults searchResults,
                                          GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze Blockbench model requirements for Minecraft ").append(modelType).append(": ").append(modelName).append("\n\n");
        
        // Add context
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Style: ").append(analysis.getStyle()).append("\n");
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n\n");
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(400, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify requirements based on model type
        prompt.append("Please provide:\n");
        prompt.append("1. Overall dimensions and proportions\n");
        prompt.append("2. Key body parts and components\n");
        prompt.append("3. Animation requirements (if entity)\n");
        prompt.append("4. Texture mapping areas\n");
        prompt.append("5. Special features or details\n");
        prompt.append("6. Color scheme and materials\n");
        prompt.append("7. Complexity level (simple, medium, complex)\n");
        
        if (modelType.equals("entity")) {
            prompt.append("8. Behavior and movement style\n");
            prompt.append("9. Size relative to player\n");
            prompt.append("10. Limb structure and joints\n");
        } else if (modelType.equals("item")) {
            prompt.append("8. Held position and orientation\n");
            prompt.append("9. 3D depth and thickness\n");
            prompt.append("10. GUI display considerations\n");
        } else if (modelType.equals("block")) {
            prompt.append("8. Voxel structure and shape\n");
            prompt.append("9. Connection points for adjacent blocks\n");
            prompt.append("10. Rotation and placement variants\n");
        }
        
        prompt.append("\nFormat your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to extract model properties
     */
    private ModelProperties parseModelProperties(String aiResponse, String modelName,
                                               String modelType, PromptAnalysis analysis) {
        ModelProperties properties = new ModelProperties();
        properties.setModelName(modelName);
        properties.setModelType(modelType);
        
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            String lowerLine = line.trim().toLowerCase();
            
            if (lowerLine.contains("dimensions:") || lowerLine.contains("proportions:")) {
                properties.setDimensions(extractValue(line));
            } else if (lowerLine.contains("body parts:") || lowerLine.contains("components:")) {
                properties.setBodyParts(parseList(extractValue(line)));
            } else if (lowerLine.contains("animation:")) {
                properties.setAnimationRequirements(parseList(extractValue(line)));
            } else if (lowerLine.contains("texture:") || lowerLine.contains("mapping:")) {
                properties.setTextureAreas(parseList(extractValue(line)));
            } else if (lowerLine.contains("features:") || lowerLine.contains("details:")) {
                properties.setSpecialFeatures(parseList(extractValue(line)));
            } else if (lowerLine.contains("color:") || lowerLine.contains("materials:")) {
                properties.setColorScheme(extractValue(line));
            } else if (lowerLine.contains("complexity:")) {
                properties.setComplexityLevel(extractValue(line));
            } else if (lowerLine.contains("behavior:") || lowerLine.contains("movement:")) {
                properties.setBehaviorStyle(extractValue(line));
            } else if (lowerLine.contains("size:") || lowerLine.contains("relative:")) {
                properties.setRelativeSize(extractValue(line));
            }
        }
        
        // Set defaults
        if (properties.getComplexityLevel() == null) {
            properties.setComplexityLevel("medium");
        }
        
        return properties;
    }
    
    /**
     * Generates model mockup images
     */
    private List<String> generateModelMockups(String modelName, String modelType,
                                            ModelProperties properties, GenerationOptions options) throws Exception {
        
        List<String> mockupPaths = new ArrayList<>();
        
        // Generate different views of the model
        String[] views = {"front", "side", "back", "perspective"};
        
        for (String view : views) {
            String mockupPrompt = buildModelMockupPrompt(modelName, modelType, view, properties);
            
            String fileName = sanitizeFileName(modelName + "_" + view + "_mockup") + ".png";
            Path outputPath = modelsPath.resolve("mockups").resolve(fileName);
            
            // Ensure parent directory exists
            Files.createDirectories(outputPath.getParent());
            
            try {
                String generatedImagePath = aiService.generateImage(mockupPrompt, outputPath.toString(), "square");
                
                // Post-process the image to remove background and ensure proper format
                String processedPath = outputPath.toString().replace(".png", "_processed.png");
                if (BackgroundRemover.createPerfectPixelArt(generatedImagePath, processedPath, 64, true)) {
                    mockupPaths.add(processedPath);
                } else {
                    mockupPaths.add(generatedImagePath);
                }
            } catch (Exception e) {
                System.err.println("Failed to generate mockup for " + modelName + " " + view + ": " + e.getMessage());
            }
        }
        
        return mockupPaths;
    }
    
    /**
     * Builds the model mockup generation prompt
     */
    private String buildModelMockupPrompt(String modelName, String modelType, String view, ModelProperties properties) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a Blockbench-style ").append(view).append(" view mockup of a Minecraft ").append(modelType).append(" model for ").append(modelName).append(". ");
        
        // Add specific view instructions
        switch (view) {
            case "front":
                prompt.append("Show the front-facing view with clear details of the face and front features. ");
                break;
            case "side":
                prompt.append("Show the side profile view highlighting the depth and side features. ");
                break;
            case "back":
                prompt.append("Show the back view with any rear details or features. ");
                break;
            case "perspective":
                prompt.append("Show a 3/4 perspective view that displays multiple sides and gives a good overall impression. ");
                break;
        }
        
        // Add model-specific details
        if (properties.getDimensions() != null) {
            prompt.append("Dimensions: ").append(properties.getDimensions()).append(". ");
        }
        
        if (properties.getColorScheme() != null) {
            prompt.append("Colors: ").append(properties.getColorScheme()).append(". ");
        }
        
        if (!properties.getSpecialFeatures().isEmpty()) {
            prompt.append("Features: ").append(String.join(", ", properties.getSpecialFeatures())).append(". ");
        }
        
        // Add technical requirements
        prompt.append("Style: Blockbench model mockup, ");
        prompt.append("64x64 pixel grid, ");
        prompt.append("no glow effects, ");
        prompt.append("transparent background, ");
        prompt.append("clean pixel art style, ");
        prompt.append("Minecraft-compatible proportions, ");
        prompt.append("suitable for 3D modeling reference");
        
        return prompt.toString();
    }
    
    /**
     * Generates texture templates for the model
     */
    private List<String> generateTextureTemplates(String modelName, String modelType,
                                                ModelProperties properties, GenerationOptions options) throws Exception {
        
        List<String> texturePaths = new ArrayList<>();
        
        // Generate texture templates based on model type
        if (modelType.equals("entity")) {
            texturePaths.addAll(generateEntityTextureTemplates(modelName, properties, options));
        } else if (modelType.equals("item")) {
            texturePaths.addAll(generateItemTextureTemplates(modelName, properties, options));
        } else if (modelType.equals("block")) {
            texturePaths.addAll(generateBlockTextureTemplates(modelName, properties, options));
        }
        
        return texturePaths;
    }
    
    /**
     * Generates entity texture templates
     */
    private List<String> generateEntityTextureTemplates(String modelName, ModelProperties properties, GenerationOptions options) throws Exception {
        List<String> texturePaths = new ArrayList<>();
        
        // Generate main entity texture
        String texturePrompt = buildEntityTexturePrompt(modelName, properties);
        
        String fileName = sanitizeFileName(modelName + "_texture") + ".png";
        Path outputPath = modelsPath.resolve("textures").resolve("entity").resolve(fileName);
        
        Files.createDirectories(outputPath.getParent());
        
        try {
            String generatedTexturePath = aiService.generateImage(texturePrompt, outputPath.toString(), "square");
            
            // Post-process to create proper entity texture
            String processedPath = outputPath.toString().replace(".png", "_processed.png");
            if (BackgroundRemover.createMinecraftTexture(generatedTexturePath, processedPath, 64)) {
                texturePaths.add(processedPath);
            } else {
                texturePaths.add(generatedTexturePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to generate entity texture for " + modelName + ": " + e.getMessage());
        }
        
        return texturePaths;
    }
    
    /**
     * Generates item texture templates
     */
    private List<String> generateItemTextureTemplates(String modelName, ModelProperties properties, GenerationOptions options) throws Exception {
        List<String> texturePaths = new ArrayList<>();
        
        // Generate item texture
        String texturePrompt = buildItemTexturePrompt(modelName, properties);
        
        String fileName = sanitizeFileName(modelName + "_item_texture") + ".png";
        Path outputPath = modelsPath.resolve("textures").resolve("item").resolve(fileName);
        
        Files.createDirectories(outputPath.getParent());
        
        try {
            String generatedTexturePath = aiService.generateImage(texturePrompt, outputPath.toString(), "square");
            
            // Post-process to create proper item texture
            String processedPath = outputPath.toString().replace(".png", "_processed.png");
            if (BackgroundRemover.createPerfectPixelArt(generatedTexturePath, processedPath, 16, true)) {
                texturePaths.add(processedPath);
            } else {
                texturePaths.add(generatedTexturePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to generate item texture for " + modelName + ": " + e.getMessage());
        }
        
        return texturePaths;
    }
    
    /**
     * Generates block texture templates
     */
    private List<String> generateBlockTextureTemplates(String modelName, ModelProperties properties, GenerationOptions options) throws Exception {
        List<String> texturePaths = new ArrayList<>();
        
        // Generate block textures for different faces
        String[] faces = {"all", "top", "side", "bottom"};
        
        for (String face : faces) {
            String texturePrompt = buildBlockTexturePrompt(modelName, face, properties);
            
            String fileName = sanitizeFileName(modelName + "_" + face + "_texture") + ".png";
            Path outputPath = modelsPath.resolve("textures").resolve("block").resolve(fileName);
            
            Files.createDirectories(outputPath.getParent());
            
            try {
                String generatedTexturePath = aiService.generateImage(texturePrompt, outputPath.toString(), "square");
                
                // Post-process to create proper block texture
                String processedPath = outputPath.toString().replace(".png", "_processed.png");
                if (BackgroundRemover.createMinecraftTexture(generatedTexturePath, processedPath, 16)) {
                    texturePaths.add(processedPath);
                } else {
                    texturePaths.add(generatedTexturePath);
                }
            } catch (Exception e) {
                System.err.println("Failed to generate block texture for " + modelName + " " + face + ": " + e.getMessage());
            }
        }
        
        return texturePaths;
    }
    
    /**
     * Builds entity texture generation prompt
     */
    private String buildEntityTexturePrompt(String modelName, ModelProperties properties) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a Minecraft entity texture template for ").append(modelName).append(". ");
        prompt.append("64x64 pixel texture map, ");
        prompt.append("entity UV mapping layout, ");
        prompt.append("no glow effects, ");
        prompt.append("transparent background where appropriate, ");
        
        if (properties.getColorScheme() != null) {
            prompt.append("colors: ").append(properties.getColorScheme()).append(", ");
        }
        
        prompt.append("clean pixel art style, ");
        prompt.append("suitable for Minecraft entity modeling");
        
        return prompt.toString();
    }
    
    /**
     * Builds item texture generation prompt
     */
    private String buildItemTexturePrompt(String modelName, ModelProperties properties) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a Minecraft item texture for ").append(modelName).append(". ");
        prompt.append("16x16 pixel texture, ");
        prompt.append("no glow effects, ");
        prompt.append("transparent background, ");
        
        if (properties.getColorScheme() != null) {
            prompt.append("colors: ").append(properties.getColorScheme()).append(", ");
        }
        
        prompt.append("clean pixel art style, ");
        prompt.append("suitable for Minecraft item modeling and GUI display");
        
        return prompt.toString();
    }
    
    /**
     * Builds block texture generation prompt
     */
    private String buildBlockTexturePrompt(String modelName, String face, ModelProperties properties) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a Minecraft block texture for the ").append(face).append(" face of ").append(modelName).append(". ");
        prompt.append("16x16 pixel texture, ");
        prompt.append("tileable, ");
        prompt.append("no glow effects, ");
        
        if (properties.getColorScheme() != null) {
            prompt.append("colors: ").append(properties.getColorScheme()).append(", ");
        }
        
        prompt.append("clean pixel art style, ");
        prompt.append("suitable for Minecraft block modeling");
        
        return prompt.toString();
    }
    
    /**
     * Generates model documentation
     */
    private String generateModelDocumentation(String modelName, String modelType,
                                            ModelProperties properties, GenerationOptions options) throws Exception {
        
        String docPrompt = "Create detailed Blockbench modeling instructions for " + modelName + 
                          " (" + modelType + "). Include step-by-step modeling guide, " +
                          "texture mapping instructions, and animation suggestions.";
        
        return aiService.generateText(docPrompt, 0.4, 800);
    }
    
    /**
     * Generates Blockbench project template
     */
    private String generateBlockbenchTemplate(String modelName, String modelType, ModelProperties properties) {
        // This would generate a basic Blockbench project JSON template
        // For now, return a simple template structure
        
        StringBuilder template = new StringBuilder();
        template.append("{\n");
        template.append("  \"name\": \"").append(modelName).append("\",\n");
        template.append("  \"model_identifier\": \"").append(sanitizeFileName(modelName)).append("\",\n");
        template.append("  \"visible_box\": [1, 1, 0],\n");
        template.append("  \"variable_placeholders\": \"\",\n");
        template.append("  \"resolution\": {\"width\": 64, \"height\": 64},\n");
        template.append("  \"elements\": [],\n");
        template.append("  \"outliner\": []\n");
        template.append("}");
        
        return template.toString();
    }
    
    /**
     * Generates a style guide for consistent model theming
     */
    private ModelStyleGuide generateModelStyleGuide(PromptAnalysis analysis, SearchResults searchResults, GenerationOptions options) throws Exception {
        
        String stylePrompt = "Create a consistent 3D modeling style guide for a " + analysis.getTheme() + 
                           " themed Minecraft mod. Include proportions, color schemes, and design principles.";
        
        String aiResponse = aiService.generateText(stylePrompt, 0.5, 400);
        
        return parseModelStyleGuide(aiResponse, analysis);
    }
    
    /**
     * Parses model style guide from AI response
     */
    private ModelStyleGuide parseModelStyleGuide(String aiResponse, PromptAnalysis analysis) {
        ModelStyleGuide guide = new ModelStyleGuide();
        guide.setTheme(analysis.getTheme());
        
        // Parse the response for style guide elements
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            String lowerLine = line.trim().toLowerCase();
            
            if (lowerLine.contains("proportion")) {
                guide.setProportionGuidelines(extractValue(line));
            } else if (lowerLine.contains("color")) {
                guide.setColorGuidelines(extractValue(line));
            } else if (lowerLine.contains("design") || lowerLine.contains("principle")) {
                guide.setDesignPrinciples(extractValue(line));
            }
        }
        
        return guide;
    }
    
    /**
     * Creates necessary model directories
     */
    private void createModelDirectories() {
        try {
            Files.createDirectories(modelsPath.resolve("mockups"));
            Files.createDirectories(modelsPath.resolve("textures").resolve("entity"));
            Files.createDirectories(modelsPath.resolve("textures").resolve("item"));
            Files.createDirectories(modelsPath.resolve("textures").resolve("block"));
            Files.createDirectories(modelsPath.resolve("templates"));
        } catch (IOException e) {
            System.err.println("Failed to create model directories: " + e.getMessage());
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
    
    private String sanitizeFileName(String fileName) {
        return fileName.toLowerCase()
                      .replaceAll("[^a-z0-9_]", "_")
                      .replaceAll("_{2,}", "_")
                      .replaceAll("^_|_$", "");
    }
    
    // Data classes
    
    public static class ModelIdea {
        private String modelName;
        private String modelType;
        private ModelProperties properties;
        private List<String> mockupPaths;
        private List<String> texturePaths;
        private String documentation;
        private String projectTemplate;
        
        public ModelIdea(String modelName, String modelType, ModelProperties properties,
                        List<String> mockupPaths, List<String> texturePaths,
                        String documentation, String projectTemplate) {
            this.modelName = modelName;
            this.modelType = modelType;
            this.properties = properties;
            this.mockupPaths = mockupPaths;
            this.texturePaths = texturePaths;
            this.documentation = documentation;
            this.projectTemplate = projectTemplate;
        }
        
        // Getters
        public String getModelName() { return modelName; }
        public String getModelType() { return modelType; }
        public ModelProperties getProperties() { return properties; }
        public List<String> getMockupPaths() { return mockupPaths; }
        public List<String> getTexturePaths() { return texturePaths; }
        public String getDocumentation() { return documentation; }
        public String getProjectTemplate() { return projectTemplate; }
    }
    
    public static class ModelProperties {
        private String modelName;
        private String modelType;
        private String dimensions;
        private List<String> bodyParts = new ArrayList<>();
        private List<String> animationRequirements = new ArrayList<>();
        private List<String> textureAreas = new ArrayList<>();
        private List<String> specialFeatures = new ArrayList<>();
        private String colorScheme;
        private String complexityLevel;
        private String behaviorStyle;
        private String relativeSize;
        private ModelStyleGuide styleGuide;
        
        // Getters and setters
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        
        public String getModelType() { return modelType; }
        public void setModelType(String modelType) { this.modelType = modelType; }
        
        public String getDimensions() { return dimensions; }
        public void setDimensions(String dimensions) { this.dimensions = dimensions; }
        
        public List<String> getBodyParts() { return bodyParts; }
        public void setBodyParts(List<String> bodyParts) { this.bodyParts = bodyParts; }
        
        public List<String> getAnimationRequirements() { return animationRequirements; }
        public void setAnimationRequirements(List<String> animationRequirements) { this.animationRequirements = animationRequirements; }
        
        public List<String> getTextureAreas() { return textureAreas; }
        public void setTextureAreas(List<String> textureAreas) { this.textureAreas = textureAreas; }
        
        public List<String> getSpecialFeatures() { return specialFeatures; }
        public void setSpecialFeatures(List<String> specialFeatures) { this.specialFeatures = specialFeatures; }
        
        public String getColorScheme() { return colorScheme; }
        public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }
        
        public String getComplexityLevel() { return complexityLevel; }
        public void setComplexityLevel(String complexityLevel) { this.complexityLevel = complexityLevel; }
        
        public String getBehaviorStyle() { return behaviorStyle; }
        public void setBehaviorStyle(String behaviorStyle) { this.behaviorStyle = behaviorStyle; }
        
        public String getRelativeSize() { return relativeSize; }
        public void setRelativeSize(String relativeSize) { this.relativeSize = relativeSize; }
        
        public ModelStyleGuide getStyleGuide() { return styleGuide; }
        public void setStyleGuide(ModelStyleGuide styleGuide) { this.styleGuide = styleGuide; }
    }
    
    public static class ModelStyleGuide {
        private String theme;
        private String proportionGuidelines;
        private String colorGuidelines;
        private String designPrinciples;
        
        // Getters and setters
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public String getProportionGuidelines() { return proportionGuidelines; }
        public void setProportionGuidelines(String proportionGuidelines) { this.proportionGuidelines = proportionGuidelines; }
        
        public String getColorGuidelines() { return colorGuidelines; }
        public void setColorGuidelines(String colorGuidelines) { this.colorGuidelines = colorGuidelines; }
        
        public String getDesignPrinciples() { return designPrinciples; }
        public void setDesignPrinciples(String designPrinciples) { this.designPrinciples = designPrinciples; }
    }
}

