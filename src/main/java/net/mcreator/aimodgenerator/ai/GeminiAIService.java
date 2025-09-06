package net.mcreator.aimodgenerator.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for integrating with Google Gemini AI API
 * Handles both text generation (Gemini Chat) and image generation (Gemini Flash 2.0 - Nano Banana)
 */
public class GeminiAIService {
    
    // Embedded Google Gemini API key for seamless user experience
    private static final String GEMINI_API_KEY = "AIzaSyAM7cSoQsQgYHNp_gHsBgvwgYrdDfpwBuI";
    
    // Gemini API endpoints
    private static final String GEMINI_CHAT_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static final String GEMINI_IMAGE_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent";
    private static final String GEMINI_IMAGEN_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-001:generateImage";
    
    private final HttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executorService;
    
    public GeminiAIService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.executorService = Executors.newFixedThreadPool(3);
    }
    
    /**
     * Generates text using Gemini Chat model with MCreator-specific instructions
     * @param prompt The input prompt
     * @param temperature Creativity level (0.0 to 1.0)
     * @param maxTokens Maximum tokens to generate
     * @return Generated text response
     */
    public String generateText(String prompt, double temperature, int maxTokens) throws Exception {
        // Apply MCreator-specific system instructions
        String systemPrompt = "You are an expert MCreator mod developer and Minecraft game designer. " +
                             "You specialize in creating balanced, creative, and technically sound mod elements for Minecraft. " +
                             "Always provide detailed, practical solutions that work with MCreator and Fabric 1.20.1. " +
                             "Focus on gameplay balance, technical accuracy, and creative design.";
        
        String fullPrompt = systemPrompt + "\n\nUser Request: " + prompt;
        
        JsonObject requestBody = new JsonObject();
        
        // Configure generation parameters
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", temperature);
        generationConfig.addProperty("maxOutputTokens", maxTokens);
        generationConfig.addProperty("topP", 0.8);
        generationConfig.addProperty("topK", 40);
        
        // Create content array
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", fullPrompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        
        requestBody.add("contents", contents);
        requestBody.add("generationConfig", generationConfig);
        
        // Make API request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_CHAT_ENDPOINT + "?key=" + GEMINI_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API request failed: " + response.body());
        }
        
        // Parse response
        JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
        JsonArray candidates = responseJson.getAsJsonArray("candidates");
        
        if (candidates != null && candidates.size() > 0) {
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content_response = candidate.getAsJsonObject("content");
            JsonArray parts_response = content_response.getAsJsonArray("parts");
            
            if (parts_response != null && parts_response.size() > 0) {
                return parts_response.get(0).getAsJsonObject().get("text").getAsString();
            }
        }
        
        throw new RuntimeException("No valid response from Gemini API");
    }
    
    /**
     * Convenience method for text generation with default parameters
     */
    public String generateText(String prompt) throws Exception {
        return generateText(prompt, 0.7, 1000);
    }
    
    /**
     * Generates images using Gemini Flash 2.0 (Nano Banana) with custom instructions
     * @param prompt The image generation prompt
     * @param outputPath Path to save the generated image
     * @param aspectRatio Desired aspect ratio
     * @return Path to the generated image
     */
    public String generateImage(String prompt, String outputPath, String aspectRatio) throws Exception {
        // Apply custom Nano Banana instructions for Minecraft textures
        String enhancedPrompt = applyNanoBananaInstructions(prompt);
        
        JsonObject requestBody = new JsonObject();
        
        // Create content for image generation
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", enhancedPrompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        
        requestBody.add("contents", contents);
        
        // Configure image generation parameters
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("candidateCount", 1);
        
        // Set aspect ratio
        if (aspectRatio != null) {
            switch (aspectRatio.toLowerCase()) {
                case "square":
                    generationConfig.addProperty("aspectRatio", "1:1");
                    break;
                case "landscape":
                    generationConfig.addProperty("aspectRatio", "16:9");
                    break;
                case "portrait":
                    generationConfig.addProperty("aspectRatio", "9:16");
                    break;
                default:
                    generationConfig.addProperty("aspectRatio", "1:1");
            }
        }
        
        requestBody.add("generationConfig", generationConfig);
        
        // Make API request to Gemini Image Generation
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_IMAGEN_ENDPOINT + "?key=" + GEMINI_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini Image API request failed: " + response.body());
        }
        
        // Parse response and save image
        JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
        JsonArray candidates = responseJson.getAsJsonArray("candidates");
        
        if (candidates != null && candidates.size() > 0) {
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            
            // Handle different response formats
            String imageData = null;
            if (candidate.has("image")) {
                JsonObject imageObj = candidate.getAsJsonObject("image");
                if (imageObj.has("data")) {
                    imageData = imageObj.get("data").getAsString();
                }
            } else if (candidate.has("content")) {
                JsonObject content_response = candidate.getAsJsonObject("content");
                JsonArray parts_response = content_response.getAsJsonArray("parts");
                if (parts_response != null && parts_response.size() > 0) {
                    JsonObject partObj = parts_response.get(0).getAsJsonObject();
                    if (partObj.has("inlineData")) {
                        JsonObject inlineData = partObj.getAsJsonObject("inlineData");
                        imageData = inlineData.get("data").getAsString();
                    }
                }
            }
            
            if (imageData != null) {
                // Decode base64 image data and save to file
                byte[] imageBytes = Base64.getDecoder().decode(imageData);
                Path outputFilePath = Paths.get(outputPath);
                Files.createDirectories(outputFilePath.getParent());
                Files.write(outputFilePath, imageBytes);
                return outputPath;
            }
        }
        
        throw new RuntimeException("No valid image data from Gemini API");
    }
    
    /**
     * Generates images asynchronously
     */
    public CompletableFuture<byte[]> generateImage(String prompt, int width, int height) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String tempPath = System.getProperty("java.io.tmpdir") + "/temp_image_" + System.currentTimeMillis() + ".png";
                String resultPath = generateImage(prompt, tempPath, "square");
                return Files.readAllBytes(Paths.get(resultPath));
            } catch (Exception e) {
                throw new RuntimeException("Image generation failed: " + e.getMessage(), e);
            }
        }, executorService);
    }
    
    /**
     * Applies Nano Banana custom instructions for Minecraft texture generation
     * @param originalPrompt The original prompt
     * @return Enhanced prompt with custom instructions
     */
    private String applyNanoBananaInstructions(String originalPrompt) {
        StringBuilder enhancedPrompt = new StringBuilder();
        
        // Check if this is a texture generation request
        if (originalPrompt.toLowerCase().contains("texture") || 
            originalPrompt.toLowerCase().contains("minecraft") ||
            originalPrompt.toLowerCase().contains("pixel")) {
            
            // Apply the user's custom texture instructions
            enhancedPrompt.append("You're a Minecraft texture designer for a Minecraft mod. ");
            enhancedPrompt.append("Only generate in a grid of 64 by 64 pixels with transparent background. ");
            
            // Determine if glow effects are requested
            boolean glowRequested = originalPrompt.toLowerCase().contains("glow") ||
                                  originalPrompt.toLowerCase().contains("light") ||
                                  originalPrompt.toLowerCase().contains("bright") ||
                                  originalPrompt.toLowerCase().contains("particle");
            
            if (glowRequested) {
                enhancedPrompt.append("You may use bright pixels having the illusion of glow since the user requested glowing effects. ");
                enhancedPrompt.append("But still maintain the 64 by 64 pixel grid structure. ");
            } else {
                enhancedPrompt.append("No glow or unwanted particles. ");
                enhancedPrompt.append("Only use bright pixels for glow illusion if it's specifically a particle texture, ");
                enhancedPrompt.append("and that's also still 64 by 64 bit pixels. ");
            }
            
            enhancedPrompt.append("Make it a flat texture fully in the grid. ");
            enhancedPrompt.append("Original request: ");
        }
        
        // Check if this is a Blockbench model request
        if (originalPrompt.toLowerCase().contains("blockbench") ||
            originalPrompt.toLowerCase().contains("entity") ||
            originalPrompt.toLowerCase().contains("model")) {
            
            // Apply Blockbench-specific instructions
            enhancedPrompt.append("I require an image generation that meticulously adheres to the 'Blockbench Minecraft entity/model' aesthetic. ");
            enhancedPrompt.append("The core directive is to produce flat, two-dimensional representations of structures or individual items, ");
            enhancedPrompt.append("mirroring the appearance of a Minecraft inventory item texture, a flattened Blockbench model export, or the GUI icon of a block. ");
            enhancedPrompt.append("Fundamental blocky/pixelated aesthetic with 64x64 pixel texture grid emulation. ");
            enhancedPrompt.append("Strictly flat, isometric-leaning perspective with absolute transparent background. ");
            enhancedPrompt.append("Crisp, defined edges with strategic pixel-perfect shading. ");
            enhancedPrompt.append("Original request: ");
        }
        
        enhancedPrompt.append(originalPrompt);
        return enhancedPrompt.toString();
    }
    
    /**
     * Analyzes a prompt to extract themes, styles, and other metadata
     * @param prompt The input prompt
     * @return PromptAnalysis object with extracted information
     */
    public PromptAnalysis analyzePrompt(String prompt) throws Exception {
        String analysisPrompt = "Analyze this Minecraft mod creation prompt and extract key information. " +
                               "Return a JSON object with the following fields: theme, style, complexity, elements, description. " +
                               "Prompt: " + prompt;
        
        String response = generateText(analysisPrompt, 0.3, 500);
        
        // Parse the JSON response (simplified implementation)
        PromptAnalysis analysis = new PromptAnalysis();
        analysis.setOriginalPrompt(prompt);
        
        try {
            JsonObject analysisJson = gson.fromJson(response, JsonObject.class);
            if (analysisJson.has("theme")) {
                analysis.setTheme(analysisJson.get("theme").getAsString());
            }
            if (analysisJson.has("style")) {
                analysis.setStyle(analysisJson.get("style").getAsString());
            }
            if (analysisJson.has("description")) {
                analysis.setDescription(analysisJson.get("description").getAsString());
            }
        } catch (Exception e) {
            // Fallback: extract basic information from the response text
            analysis.setTheme(extractThemeFromText(response));
            analysis.setStyle(extractStyleFromText(response));
            analysis.setDescription(response);
        }
        
        return analysis;
    }
    
    /**
     * Generates code for MCreator elements using Gemini
     * @param elementType Type of element (item, block, entity)
     * @param elementName Name of the element
     * @param analysis Prompt analysis
     * @param searchResults Web search results for context
     * @return Generated code
     */
    public String generateCode(String elementType, String elementName, PromptAnalysis analysis, SearchResults searchResults) throws Exception {
        StringBuilder codePrompt = new StringBuilder();
        
        codePrompt.append("You are an expert MCreator mod developer. Generate complete, functional code for a ");
        codePrompt.append(elementType).append(" named '").append(elementName).append("'. ");
        codePrompt.append("The code should be compatible with Fabric 1.20.1 and follow MCreator best practices. ");
        codePrompt.append("Include all necessary imports, proper class structure, and MCreator annotations. ");
        
        if (analysis.getTheme() != null) {
            codePrompt.append("Theme: ").append(analysis.getTheme()).append(". ");
        }
        
        if (analysis.getDescription() != null) {
            codePrompt.append("Description: ").append(analysis.getDescription()).append(". ");
        }
        
        // Add search context if available
        if (searchResults != null && !searchResults.getResults().isEmpty()) {
            codePrompt.append("Additional context from web search: ");
            searchResults.getResults().stream()
                    .limit(3)
                    .forEach(result -> codePrompt.append(result.getSnippet()).append(" "));
        }
        
        codePrompt.append("Generate complete Java code with proper imports, annotations, and MCreator compatibility. ");
        codePrompt.append("Ensure the code is ready to use in MCreator without modifications.");
        
        return generateText(codePrompt.toString(), 0.2, 2000);
    }
    
    /**
     * Generates Minecraft-specific content like NBT data or command blocks
     * @param prompt Description of what to generate
     * @param elementType Type of element (item, block, recipe, etc.)
     * @return Generated Minecraft-specific content
     */
    public String generateMinecraftContent(String prompt, String elementType) throws Exception {
        String systemPrompt = String.format(
            "You are an expert Minecraft modder using MCreator. Generate detailed %s specifications " +
            "including all necessary properties, NBT data, and configuration. " +
            "Focus on balanced gameplay and compatibility with Minecraft 1.20.1 and Fabric. " +
            "Provide specific values for all properties and ensure MCreator compatibility.",
            elementType
        );
        
        String fullPrompt = systemPrompt + "\n\nUser Request: " + prompt;
        return generateText(fullPrompt, 0.5, 800);
    }
    
    /**
     * Generates lore and descriptions for mod elements
     * @param elementName Name of the element
     * @param elementType Type of element
     * @param theme Theme/style of the mod
     * @return Generated lore text
     */
    public String generateLore(String elementName, String elementType, String theme) throws Exception {
        String prompt = String.format(
            "Create engaging lore and description for a Minecraft %s called '%s' with a %s theme. " +
            "Include tooltip text, backstory, and usage hints. Keep it concise but immersive. " +
            "Make it suitable for Minecraft's fantasy setting.",
            elementType, elementName, theme
        );
        
        return generateText(prompt, 0.8, 300);
    }
    
    /**
     * Generates audio/sound effects description for later synthesis
     * @param soundName Name of the sound
     * @param context Context for the sound
     * @return Sound description for synthesis
     */
    public String generateSoundDescription(String soundName, String context) throws Exception {
        String soundPrompt = "Generate a detailed description for a Minecraft sound effect named '" + soundName + "'. " +
                           "Context: " + context + ". " +
                           "Describe the sound characteristics, duration, pitch, and style that would fit in Minecraft. " +
                           "Be specific about audio properties for sound synthesis. " +
                           "Consider Minecraft's audio style and technical limitations.";
        
        return generateText(soundPrompt, 0.5, 300);
    }
    
    /**
     * Generates audio asynchronously (placeholder for future audio generation)
     */
    public CompletableFuture<byte[]> generateAudio(String prompt, int duration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // For now, generate a description that could be used with audio synthesis
                String audioDescription = generateSoundDescription(prompt, "Minecraft mod sound effect");
                System.out.println("Generated audio description: " + audioDescription);
                
                // Return empty byte array as placeholder
                // In future, this would integrate with audio generation APIs
                return new byte[0];
            } catch (Exception e) {
                throw new RuntimeException("Audio generation failed: " + e.getMessage(), e);
            }
        }, executorService);
    }
    
    // Helper methods for fallback analysis
    private String extractThemeFromText(String text) {
        // Simple keyword extraction for theme
        String lowerText = text.toLowerCase();
        if (lowerText.contains("magic") || lowerText.contains("mystical")) return "magical";
        if (lowerText.contains("tech") || lowerText.contains("mechanical")) return "technological";
        if (lowerText.contains("nature") || lowerText.contains("organic")) return "natural";
        if (lowerText.contains("dark") || lowerText.contains("shadow")) return "dark";
        if (lowerText.contains("crystal") || lowerText.contains("gem")) return "crystalline";
        return "general";
    }
    
    private String extractStyleFromText(String text) {
        // Simple keyword extraction for style
        String lowerText = text.toLowerCase();
        if (lowerText.contains("modern") || lowerText.contains("sleek")) return "modern";
        if (lowerText.contains("medieval") || lowerText.contains("ancient")) return "medieval";
        if (lowerText.contains("futuristic") || lowerText.contains("sci-fi")) return "futuristic";
        if (lowerText.contains("rustic") || lowerText.contains("wooden")) return "rustic";
        if (lowerText.contains("elegant") || lowerText.contains("refined")) return "elegant";
        return "standard";
    }
    
    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}

