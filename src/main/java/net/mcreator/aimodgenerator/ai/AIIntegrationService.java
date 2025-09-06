package net.mcreator.aimodgenerator.ai;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for integrating with AI models for text, image, and audio generation
 * Handles API calls to various AI services
 */
public class AIIntegrationService {
    
    private static final String OPENAI_API_BASE = System.getenv("OPENAI_API_BASE");
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    
    private final ExecutorService executorService;
    
    public AIIntegrationService() {
        this.executorService = Executors.newFixedThreadPool(3);
    }
    
    /**
     * Generates text using AI based on a prompt
     * @param prompt The input prompt
     * @return Generated text response
     */
    public String generateText(String prompt) throws Exception {
        return generateText(prompt, 0.7, 1000);
    }
    
    /**
     * Generates text using AI with custom parameters
     * @param prompt The input prompt
     * @param temperature Creativity level (0.0 - 1.0)
     * @param maxTokens Maximum tokens to generate
     * @return Generated text response
     */
    public String generateText(String prompt, double temperature, int maxTokens) throws Exception {
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            throw new Exception("OpenAI API key not configured");
        }
        
        String apiUrl = (OPENAI_API_BASE != null ? OPENAI_API_BASE : "https://api.openai.com/v1") + "/chat/completions";
        
        // Prepare the request body
        String requestBody = String.format(
            "{\n" +
            "  \"model\": \"gpt-3.5-turbo\",\n" +
            "  \"messages\": [\n" +
            "    {\n" +
            "      \"role\": \"system\",\n" +
            "      \"content\": \"You are an expert Minecraft modder and game designer. Help create detailed, balanced, and creative mod elements.\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"role\": \"user\",\n" +
            "      \"content\": \"%s\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"temperature\": %.1f,\n" +
            "  \"max_tokens\": %d\n" +
            "}",
            escapeJson(prompt), temperature, maxTokens
        );
        
        // Make the API call
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        connection.setDoOutput(true);
        
        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Read response
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("API request failed with code: " + responseCode);
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        // Parse the JSON response to extract the generated text
        return parseOpenAIResponse(response.toString());
    }
    
    /**
     * Generates an image using AI based on a text prompt
     * @param prompt Description of the image to generate
     * @param width Image width
     * @param height Image height
     * @return CompletableFuture with the generated image data
     */
    public CompletableFuture<byte[]> generateImage(String prompt, int width, int height) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // For now, return a placeholder
                // In a real implementation, this would call DALL-E or similar
                return generatePlaceholderImage(prompt, width, height);
            } catch (Exception e) {
                throw new RuntimeException("Image generation failed: " + e.getMessage(), e);
            }
        }, executorService);
    }
    
    /**
     * Generates audio/sound effects using AI
     * @param prompt Description of the sound to generate
     * @param duration Duration in seconds
     * @return CompletableFuture with the generated audio data
     */
    public CompletableFuture<byte[]> generateAudio(String prompt, int duration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // For now, return a placeholder
                // In a real implementation, this would call an audio generation API
                return generatePlaceholderAudio(prompt, duration);
            } catch (Exception e) {
                throw new RuntimeException("Audio generation failed: " + e.getMessage(), e);
            }
        }, executorService);
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
            "Focus on balanced gameplay and compatibility with Minecraft 1.20.1. " +
            "Provide specific values for all properties.",
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
            "Include tooltip text, backstory, and usage hints. Keep it concise but immersive.",
            elementType, elementName, theme
        );
        
        return generateText(prompt, 0.8, 300);
    }
    
    /**
     * Parses OpenAI API response to extract the generated text
     */
    private String parseOpenAIResponse(String jsonResponse) throws Exception {
        // Simple JSON parsing - in a real implementation, use a proper JSON library
        try {
            int contentStart = jsonResponse.indexOf("\"content\":\"") + 11;
            int contentEnd = jsonResponse.indexOf("\",\"role\"", contentStart);
            if (contentEnd == -1) {
                contentEnd = jsonResponse.indexOf("\"}", contentStart);
            }
            
            if (contentStart > 10 && contentEnd > contentStart) {
                String content = jsonResponse.substring(contentStart, contentEnd);
                // Unescape JSON
                content = content.replace("\\n", "\n")
                               .replace("\\\"", "\"")
                               .replace("\\\\", "\\");
                return content;
            } else {
                throw new Exception("Could not parse AI response");
            }
        } catch (Exception e) {
            throw new Exception("Failed to parse AI response: " + e.getMessage());
        }
    }
    
    /**
     * Escapes a string for JSON
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Generates a placeholder image (for demonstration)
     */
    private byte[] generatePlaceholderImage(String prompt, int width, int height) {
        // This would be replaced with actual image generation
        // For now, return empty byte array
        return new byte[0];
    }
    
    /**
     * Generates a placeholder audio (for demonstration)
     */
    private byte[] generatePlaceholderAudio(String prompt, int duration) {
        // This would be replaced with actual audio generation
        // For now, return empty byte array
        return new byte[0];
    }
    
    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}

