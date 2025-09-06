package net.mcreator.aimodgenerator.core;

import net.mcreator.workspace.Workspace;
import net.mcreator.aimodgenerator.ai.GeminiAIService;
import net.mcreator.aimodgenerator.ai.CustomInstructions;
import net.mcreator.aimodgenerator.search.WebSearchService;
import net.mcreator.aimodgenerator.search.SearchResults;
import net.mcreator.aimodgenerator.media.AudioGenerationService;
import net.mcreator.aimodgenerator.media.BackgroundRemovalService;
import net.mcreator.aimodgenerator.media.TextureGenerator;
import net.mcreator.aimodgenerator.media.BlockbenchModelGenerator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Advanced multimodal AI agent core for autonomous MCreator mod development
 * Combines text, image, audio, and web search capabilities for comprehensive mod creation
 */
public class MultimodalAgentCore {
    
    private final Workspace workspace;
    private final GeminiAIService geminiService;
    private final WebSearchService webSearchService;
    private final AudioGenerationService audioService;
    private final BackgroundRemovalService backgroundRemovalService;
    private final TextureGenerator textureGenerator;
    private final BlockbenchModelGenerator modelGenerator;
    private final ExecutorService executorService;
    
    // Agent state and memory
    private final Map<String, Object> agentMemory;
    private final List<AgentAction> actionHistory;
    private AgentState currentState;
    private String currentProject;
    
    // Multimodal capabilities
    private final Set<String> supportedImageFormats;
    private final Set<String> supportedAudioFormats;
    
    public MultimodalAgentCore(Workspace workspace) {
        this.workspace = workspace;
        this.geminiService = new GeminiAIService();
        this.webSearchService = new WebSearchService();
        this.audioService = new AudioGenerationService();
        this.backgroundRemovalService = new BackgroundRemovalService();
        this.textureGenerator = new TextureGenerator(geminiService);
        this.modelGenerator = new BlockbenchModelGenerator(geminiService);
        this.executorService = Executors.newFixedThreadPool(8);
        
        // Initialize agent state
        this.agentMemory = new ConcurrentHashMap<>();
        this.actionHistory = Collections.synchronizedList(new ArrayList<>());
        this.currentState = AgentState.IDLE;
        
        // Supported formats
        this.supportedImageFormats = Set.of("png", "jpg", "jpeg", "gif", "bmp", "webp");
        this.supportedAudioFormats = Set.of("wav", "mp3", "ogg", "m4a");
        
        initializeAgent();
    }
    
    /**
     * Agent states for tracking current operation
     */
    public enum AgentState {
        IDLE, ANALYZING, SEARCHING, GENERATING, CREATING, TESTING, ERROR
    }
    
    /**
     * Represents an action taken by the agent
     */
    public static class AgentAction {
        private final String action;
        private final String description;
        private final long timestamp;
        private final Map<String, Object> parameters;
        private final String result;
        
        public AgentAction(String action, String description, Map<String, Object> parameters, String result) {
            this.action = action;
            this.description = description;
            this.parameters = parameters;
            this.result = result;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getAction() { return action; }
        public String getDescription() { return description; }
        public long getTimestamp() { return timestamp; }
        public Map<String, Object> getParameters() { return parameters; }
        public String getResult() { return result; }
    }
    
    /**
     * Callback interface for agent progress updates
     */
    public interface AgentCallback {
        void onStateChange(AgentState newState, String description);
        void onProgress(String message);
        void onActionCompleted(AgentAction action);
        void onError(String error);
        void onComplete(String summary);
    }
    
    /**
     * Initializes the agent with basic capabilities and memory
     */
    private void initializeAgent() {
        agentMemory.put("capabilities", Arrays.asList(
            "text_generation", "image_generation", "audio_generation", 
            "web_search", "code_generation", "texture_creation",
            "model_generation", "background_removal", "multimodal_analysis"
        ));
        
        agentMemory.put("minecraft_versions", Arrays.asList(
            "1.20.1", "1.20.0", "1.19.4", "1.19.2", "1.18.2"
        ));
        
        agentMemory.put("mod_types", Arrays.asList(
            "items", "blocks", "entities", "biomes", "dimensions", 
            "recipes", "enchantments", "procedures", "structures"
        ));
        
        agentMemory.put("initialized", true);
        agentMemory.put("session_start", System.currentTimeMillis());
    }
    
    /**
     * Main agent entry point - processes natural language requests autonomously
     * @param userRequest Natural language description of what to create
     * @param callback Progress callback
     * @return CompletableFuture with creation results
     */
    public CompletableFuture<AgentResult> processUserRequest(String userRequest, AgentCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                currentState = AgentState.ANALYZING;
                callback.onStateChange(currentState, "Analyzing your request with multimodal AI");
                
                // Step 1: Analyze the request using Gemini
                RequestAnalysis analysis = analyzeRequest(userRequest);
                recordAction("analyze_request", "Analyzed user request", 
                           Map.of("request", userRequest), analysis.toString());
                
                callback.onProgress("Analysis complete: " + analysis.getProjectType() + " mod with " + 
                                  analysis.getRequiredElements().size() + " elements");
                
                // Step 2: Gather additional context through web search if needed
                SearchResults searchResults = null;
                if (analysis.isRequiresWebSearch()) {
                    currentState = AgentState.SEARCHING;
                    callback.onStateChange(currentState, "Searching the web for additional context");
                    
                    searchResults = webSearchService.search(analysis.getSearchQuery(), 10);
                    recordAction("web_search", "Searched for additional context", 
                               Map.of("query", analysis.getSearchQuery()), 
                               "Found " + searchResults.getResults().size() + " results");
                    
                    callback.onProgress("Web search complete: " + searchResults.getResults().size() + " results found");
                }
                
                // Step 3: Generate mod elements autonomously
                currentState = AgentState.GENERATING;
                callback.onStateChange(currentState, "Generating mod elements with AI");
                
                AgentResult result = new AgentResult();
                result.setOriginalRequest(userRequest);
                result.setAnalysis(analysis);
                result.setSearchResults(searchResults);
                
                List<CompletableFuture<ElementResult>> elementFutures = new ArrayList<>();
                
                // Generate each required element
                for (String elementType : analysis.getRequiredElements()) {
                    CompletableFuture<ElementResult> elementFuture = generateElement(
                        elementType, analysis, searchResults, callback
                    );
                    elementFutures.add(elementFuture);
                }
                
                // Wait for all elements to complete
                List<ElementResult> elements = new ArrayList<>();
                for (CompletableFuture<ElementResult> future : elementFutures) {
                    elements.add(future.get());
                }
                result.setGeneratedElements(elements);
                
                // Step 4: Create supporting assets (textures, sounds, models)
                currentState = AgentState.CREATING;
                callback.onStateChange(currentState, "Creating supporting assets");
                
                List<AssetResult> assets = generateAssets(analysis, elements, callback);
                result.setGeneratedAssets(assets);
                
                // Step 5: Integrate everything into MCreator workspace
                if (workspace != null) {
                    integrateIntoWorkspace(result, callback);
                }
                
                currentState = AgentState.IDLE;
                callback.onComplete("Mod creation complete! Generated " + elements.size() + 
                                  " elements and " + assets.size() + " assets.");
                
                return result;
                
            } catch (Exception e) {
                currentState = AgentState.ERROR;
                callback.onError("Agent failed: " + e.getMessage());
                throw new RuntimeException("Agent processing failed", e);
            }
        }, executorService);
    }
    
    /**
     * Analyzes user request to determine what needs to be created
     * @param userRequest User's natural language request
     * @return Analysis of what to create
     */
    private RequestAnalysis analyzeRequest(String userRequest) {
        try {
            String analysisPrompt = CustomInstructions.getRequestAnalysisInstructions() + 
                                  "\n\nUser Request: " + userRequest;
            
            String analysisResponse = geminiService.generateText(analysisPrompt, 0.3, 1000);
            
            // Parse the analysis response
            RequestAnalysis analysis = new RequestAnalysis();
            analysis.setOriginalRequest(userRequest);
            analysis.setAnalysisResponse(analysisResponse);
            
            // Extract key information using simple parsing
            String lowerRequest = userRequest.toLowerCase();
            
            // Determine project type
            if (lowerRequest.contains("weapon") || lowerRequest.contains("sword") || lowerRequest.contains("tool")) {
                analysis.setProjectType("weapons_and_tools");
                analysis.addRequiredElement("item");
                analysis.addRequiredElement("recipe");
            } else if (lowerRequest.contains("block") || lowerRequest.contains("ore") || lowerRequest.contains("stone")) {
                analysis.setProjectType("blocks_and_materials");
                analysis.addRequiredElement("block");
                analysis.addRequiredElement("recipe");
            } else if (lowerRequest.contains("mob") || lowerRequest.contains("entity") || lowerRequest.contains("creature")) {
                analysis.setProjectType("entities_and_mobs");
                analysis.addRequiredElement("entity_ideas");
            } else if (lowerRequest.contains("magic") || lowerRequest.contains("spell") || lowerRequest.contains("enchant")) {
                analysis.setProjectType("magic_and_enchantments");
                analysis.addRequiredElement("enchantment");
                analysis.addRequiredElement("procedure");
            } else {
                analysis.setProjectType("general_mod");
                analysis.addRequiredElement("item");
            }
            
            // Determine if web search is needed
            analysis.setRequiresWebSearch(lowerRequest.contains("real") || lowerRequest.contains("based on") || 
                                        lowerRequest.contains("like") || lowerRequest.contains("similar to"));
            
            if (analysis.isRequiresWebSearch()) {
                analysis.setSearchQuery(extractSearchQuery(userRequest));
            }
            
            // Determine required assets
            analysis.setRequiresTextures(true); // Always generate textures
            analysis.setRequiresAudio(lowerRequest.contains("sound") || lowerRequest.contains("music") || 
                                    lowerRequest.contains("audio"));
            analysis.setRequiresModels(lowerRequest.contains("model") || lowerRequest.contains("3d") || 
                                     analysis.getProjectType().equals("entities_and_mobs"));
            
            return analysis;
            
        } catch (Exception e) {
            throw new RuntimeException("Request analysis failed", e);
        }
    }
    
    /**
     * Extracts search query from user request
     * @param userRequest Original request
     * @return Search query
     */
    private String extractSearchQuery(String userRequest) {
        // Simple extraction - in a real implementation, this would be more sophisticated
        String[] keywords = userRequest.toLowerCase().split("\\s+");
        List<String> searchTerms = new ArrayList<>();
        
        for (String keyword : keywords) {
            if (keyword.length() > 3 && !isStopWord(keyword)) {
                searchTerms.add(keyword);
            }
        }
        
        return String.join(" ", searchTerms) + " minecraft mod";
    }
    
    /**
     * Checks if a word is a stop word
     * @param word Word to check
     * @return True if stop word
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "for", "are", "but", "not", "you", "all", "can", "had", "her", "was", "one", "our", "out", "day", "get", "has", "him", "his", "how", "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did", "its", "let", "put", "say", "she", "too", "use");
        return stopWords.contains(word);
    }
    
    /**
     * Generates a specific mod element
     * @param elementType Type of element to generate
     * @param analysis Request analysis
     * @param searchResults Web search results
     * @param callback Progress callback
     * @return Generated element result
     */
    private CompletableFuture<ElementResult> generateElement(String elementType, RequestAnalysis analysis, 
                                                           SearchResults searchResults, AgentCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                callback.onProgress("Generating " + elementType + " with Gemini AI");
                
                String prompt = buildElementPrompt(elementType, analysis, searchResults);
                String generatedCode = geminiService.generateText(prompt, 0.2, 2000);
                
                ElementResult result = new ElementResult();
                result.setElementType(elementType);
                result.setGeneratedCode(generatedCode);
                result.setSuccess(true);
                
                recordAction("generate_element", "Generated " + elementType, 
                           Map.of("type", elementType), "Success");
                
                callback.onProgress(elementType + " generated successfully");
                
                return result;
                
            } catch (Exception e) {
                ElementResult errorResult = new ElementResult();
                errorResult.setElementType(elementType);
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                
                callback.onProgress("Failed to generate " + elementType + ": " + e.getMessage());
                
                return errorResult;
            }
        }, executorService);
    }
    
    /**
     * Builds prompt for specific element generation
     * @param elementType Element type
     * @param analysis Request analysis
     * @param searchResults Search results
     * @return Generated prompt
     */
    private String buildElementPrompt(String elementType, RequestAnalysis analysis, SearchResults searchResults) {
        StringBuilder prompt = new StringBuilder();
        
        // Add element-specific instructions
        switch (elementType) {
            case "item":
                prompt.append(CustomInstructions.getItemInstructions("general"));
                break;
            case "block":
                prompt.append(CustomInstructions.getBlockInstructions("general"));
                break;
            case "recipe":
                prompt.append(CustomInstructions.getRecipeInstructions("crafting"));
                break;
            case "enchantment":
                prompt.append(CustomInstructions.getEnchantmentInstructions());
                break;
            case "procedure":
                prompt.append(CustomInstructions.getProcedureInstructions());
                break;
            default:
                prompt.append(CustomInstructions.CODE_GENERATION_INSTRUCTIONS);
        }
        
        // Add Fabric compatibility
        prompt.append("\n").append(CustomInstructions.getFabricInstructions());
        
        // Add context from analysis
        prompt.append("\n\nProject Context:\n");
        prompt.append("- Project Type: ").append(analysis.getProjectType()).append("\n");
        prompt.append("- Original Request: ").append(analysis.getOriginalRequest()).append("\n");
        
        // Add search context if available
        if (searchResults != null && !searchResults.getResults().isEmpty()) {
            prompt.append("\nWeb Search Context:\n");
            for (int i = 0; i < Math.min(3, searchResults.getResults().size()); i++) {
                var result = searchResults.getResults().get(i);
                prompt.append("- ").append(result.getTitle()).append(": ").append(result.getSnippet()).append("\n");
            }
        }
        
        prompt.append("\nGenerate the ").append(elementType).append(" code:");
        
        return prompt.toString();
    }
    
    /**
     * Generates supporting assets (textures, sounds, models)
     * @param analysis Request analysis
     * @param elements Generated elements
     * @param callback Progress callback
     * @return List of generated assets
     */
    private List<AssetResult> generateAssets(RequestAnalysis analysis, List<ElementResult> elements, AgentCallback callback) {
        List<AssetResult> assets = new ArrayList<>();
        List<CompletableFuture<AssetResult>> assetFutures = new ArrayList<>();
        
        // Generate textures
        if (analysis.isRequiresTextures()) {
            for (ElementResult element : elements) {
                if (element.isSuccess() && needsTexture(element.getElementType())) {
                    CompletableFuture<AssetResult> textureFuture = generateTexture(element, analysis, callback);
                    assetFutures.add(textureFuture);
                }
            }
        }
        
        // Generate audio
        if (analysis.isRequiresAudio()) {
            CompletableFuture<AssetResult> audioFuture = generateAudio(analysis, callback);
            assetFutures.add(audioFuture);
        }
        
        // Generate models
        if (analysis.isRequiresModels()) {
            CompletableFuture<AssetResult> modelFuture = generateModel(analysis, callback);
            assetFutures.add(modelFuture);
        }
        
        // Wait for all assets to complete
        for (CompletableFuture<AssetResult> future : assetFutures) {
            try {
                AssetResult asset = future.get();
                if (asset != null) {
                    assets.add(asset);
                }
            } catch (Exception e) {
                callback.onProgress("Asset generation failed: " + e.getMessage());
            }
        }
        
        return assets;
    }
    
    /**
     * Checks if element type needs a texture
     * @param elementType Element type
     * @return True if needs texture
     */
    private boolean needsTexture(String elementType) {
        return elementType.equals("item") || elementType.equals("block");
    }
    
    /**
     * Generates texture for an element
     * @param element Element to generate texture for
     * @param analysis Request analysis
     * @param callback Progress callback
     * @return Texture asset result
     */
    private CompletableFuture<AssetResult> generateTexture(ElementResult element, RequestAnalysis analysis, AgentCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                callback.onProgress("Generating texture for " + element.getElementType() + " with Nano Banana");
                
                String textureDescription = analysis.getOriginalRequest() + " " + element.getElementType() + " texture";
                String outputPath = workspace.getWorkspaceFolder() + "/textures/" + 
                                  element.getElementType() + "_" + System.currentTimeMillis() + ".png";
                
                // Generate texture with Nano Banana (Gemini Flash 2.0)
                String texturePath = textureGenerator.generateTexture(textureDescription, outputPath, 64, 64);
                
                // Remove background for transparency
                String transparentPath = outputPath.replace(".png", "_transparent.png");
                backgroundRemovalService.removeBackground(texturePath, transparentPath).get();
                
                // Optimize for Minecraft
                String optimizedPath = transparentPath.replace("_transparent.png", "_minecraft.png");
                backgroundRemovalService.optimizeForMinecraft(transparentPath, optimizedPath).get();
                
                AssetResult result = new AssetResult();
                result.setAssetType("texture");
                result.setFilePath(optimizedPath);
                result.setSuccess(true);
                result.setDescription("64x64 Minecraft texture with transparent background");
                
                recordAction("generate_texture", "Generated texture", 
                           Map.of("element", element.getElementType()), optimizedPath);
                
                callback.onProgress("Texture generated and optimized for " + element.getElementType());
                
                return result;
                
            } catch (Exception e) {
                AssetResult errorResult = new AssetResult();
                errorResult.setAssetType("texture");
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                
                callback.onProgress("Texture generation failed: " + e.getMessage());
                
                return errorResult;
            }
        }, executorService);
    }
    
    /**
     * Generates audio assets
     * @param analysis Request analysis
     * @param callback Progress callback
     * @return Audio asset result
     */
    private CompletableFuture<AssetResult> generateAudio(RequestAnalysis analysis, AgentCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                callback.onProgress("Generating audio with free AI models");
                
                String audioDescription = analysis.getOriginalRequest() + " sound effect";
                String outputPath = workspace.getWorkspaceFolder() + "/sounds/" + 
                                  "generated_" + System.currentTimeMillis() + ".wav";
                
                String audioPath = audioService.generateSoundEffect(audioDescription, 3, outputPath).get();
                
                AssetResult result = new AssetResult();
                result.setAssetType("audio");
                result.setFilePath(audioPath);
                result.setSuccess(true);
                result.setDescription("Generated sound effect");
                
                recordAction("generate_audio", "Generated audio", 
                           Map.of("description", audioDescription), audioPath);
                
                callback.onProgress("Audio generated successfully");
                
                return result;
                
            } catch (Exception e) {
                AssetResult errorResult = new AssetResult();
                errorResult.setAssetType("audio");
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                
                callback.onProgress("Audio generation failed: " + e.getMessage());
                
                return errorResult;
            }
        }, executorService);
    }
    
    /**
     * Generates 3D model ideas
     * @param analysis Request analysis
     * @param callback Progress callback
     * @return Model asset result
     */
    private CompletableFuture<AssetResult> generateModel(RequestAnalysis analysis, AgentCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                callback.onProgress("Generating Blockbench model ideas");
                
                List<String> modelIdeas = modelGenerator.generateModelIdeas(
                    createPromptAnalysis(analysis), null
                );
                
                AssetResult result = new AssetResult();
                result.setAssetType("model");
                result.setSuccess(true);
                result.setDescription("Blockbench model ideas and templates");
                result.setAdditionalData(modelIdeas);
                
                recordAction("generate_model", "Generated model ideas", 
                           Map.of("count", modelIdeas.size()), "Success");
                
                callback.onProgress("Model ideas generated successfully");
                
                return result;
                
            } catch (Exception e) {
                AssetResult errorResult = new AssetResult();
                errorResult.setAssetType("model");
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                
                callback.onProgress("Model generation failed: " + e.getMessage());
                
                return errorResult;
            }
        }, executorService);
    }
    
    /**
     * Creates PromptAnalysis from RequestAnalysis
     * @param requestAnalysis Request analysis
     * @return Prompt analysis
     */
    private PromptAnalysis createPromptAnalysis(RequestAnalysis requestAnalysis) {
        PromptAnalysis promptAnalysis = new PromptAnalysis();
        promptAnalysis.setOriginalPrompt(requestAnalysis.getOriginalRequest());
        promptAnalysis.setDescription(requestAnalysis.getOriginalRequest());
        promptAnalysis.setTheme(requestAnalysis.getProjectType());
        return promptAnalysis;
    }
    
    /**
     * Integrates generated content into MCreator workspace
     * @param result Agent result
     * @param callback Progress callback
     */
    private void integrateIntoWorkspace(AgentResult result, AgentCallback callback) {
        try {
            callback.onProgress("Integrating generated content into MCreator workspace");
            
            // This would integrate with MCreator's actual API
            // For now, we'll just log what would be created
            
            for (ElementResult element : result.getGeneratedElements()) {
                if (element.isSuccess()) {
                    callback.onProgress("Would create " + element.getElementType() + " in workspace");
                    // workspace.createElement(element.getElementType(), element.getGeneratedCode());
                }
            }
            
            for (AssetResult asset : result.getGeneratedAssets()) {
                if (asset.isSuccess()) {
                    callback.onProgress("Would add " + asset.getAssetType() + " to workspace");
                    // workspace.addAsset(asset.getAssetType(), asset.getFilePath());
                }
            }
            
            recordAction("integrate_workspace", "Integrated content into workspace", 
                       Map.of("elements", result.getGeneratedElements().size(), 
                              "assets", result.getGeneratedAssets().size()), "Success");
            
        } catch (Exception e) {
            callback.onProgress("Workspace integration failed: " + e.getMessage());
        }
    }
    
    /**
     * Records an action in the agent's history
     * @param action Action name
     * @param description Action description
     * @param parameters Action parameters
     * @param result Action result
     */
    private void recordAction(String action, String description, Map<String, Object> parameters, String result) {
        AgentAction agentAction = new AgentAction(action, description, parameters, result);
        actionHistory.add(agentAction);
        
        // Keep only last 100 actions to prevent memory issues
        if (actionHistory.size() > 100) {
            actionHistory.remove(0);
        }
    }
    
    /**
     * Processes uploaded files (images, audio, etc.) for multimodal input
     * @param filePath Path to uploaded file
     * @param callback Progress callback
     * @return Processing result
     */
    public CompletableFuture<FileProcessingResult> processUploadedFile(String filePath, AgentCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File file = new File(filePath);
                String fileName = file.getName().toLowerCase();
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
                
                FileProcessingResult result = new FileProcessingResult();
                result.setOriginalPath(filePath);
                result.setFileType(extension);
                
                if (supportedImageFormats.contains(extension)) {
                    // Process image
                    result = processImage(filePath, callback);
                } else if (supportedAudioFormats.contains(extension)) {
                    // Process audio
                    result = processAudio(filePath, callback);
                } else {
                    result.setSuccess(false);
                    result.setErrorMessage("Unsupported file format: " + extension);
                }
                
                return result;
                
            } catch (Exception e) {
                FileProcessingResult errorResult = new FileProcessingResult();
                errorResult.setOriginalPath(filePath);
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                return errorResult;
            }
        }, executorService);
    }
    
    /**
     * Processes uploaded image
     * @param imagePath Image path
     * @param callback Progress callback
     * @return Processing result
     */
    private FileProcessingResult processImage(String imagePath, AgentCallback callback) throws Exception {
        callback.onProgress("Processing uploaded image with multimodal AI");
        
        // Analyze image with Gemini Vision
        String imageAnalysis = geminiService.analyzeImage(imagePath, 
            "Analyze this image for Minecraft mod creation. Describe what you see and suggest mod elements.");
        
        // Remove background if needed
        String transparentPath = imagePath.replace(".", "_transparent.");
        backgroundRemovalService.removeBackground(imagePath, transparentPath).get();
        
        FileProcessingResult result = new FileProcessingResult();
        result.setOriginalPath(imagePath);
        result.setFileType("image");
        result.setProcessedPath(transparentPath);
        result.setAnalysis(imageAnalysis);
        result.setSuccess(true);
        
        recordAction("process_image", "Processed uploaded image", 
                   Map.of("path", imagePath), "Success");
        
        return result;
    }
    
    /**
     * Processes uploaded audio
     * @param audioPath Audio path
     * @param callback Progress callback
     * @return Processing result
     */
    private FileProcessingResult processAudio(String audioPath, AgentCallback callback) throws Exception {
        callback.onProgress("Processing uploaded audio file");
        
        // For now, just analyze the file properties
        File audioFile = new File(audioPath);
        String analysis = "Audio file: " + audioFile.getName() + 
                         ", Size: " + audioFile.length() + " bytes";
        
        FileProcessingResult result = new FileProcessingResult();
        result.setOriginalPath(audioPath);
        result.setFileType("audio");
        result.setAnalysis(analysis);
        result.setSuccess(true);
        
        recordAction("process_audio", "Processed uploaded audio", 
                   Map.of("path", audioPath), "Success");
        
        return result;
    }
    
    /**
     * Gets agent status and capabilities
     * @return Status information
     */
    public String getAgentStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Multimodal AI Agent Status:\n");
        status.append("- Current State: ").append(currentState).append("\n");
        status.append("- Actions Performed: ").append(actionHistory.size()).append("\n");
        status.append("- Session Duration: ").append(
            (System.currentTimeMillis() - (Long) agentMemory.get("session_start")) / 1000
        ).append(" seconds\n");
        status.append("- Capabilities: ").append(agentMemory.get("capabilities")).append("\n");
        status.append("- Supported Image Formats: ").append(supportedImageFormats).append("\n");
        status.append("- Supported Audio Formats: ").append(supportedAudioFormats).append("\n");
        status.append("- Workspace Connected: ").append(workspace != null).append("\n");
        
        return status.toString();
    }
    
    /**
     * Gets recent action history
     * @param limit Maximum number of actions to return
     * @return List of recent actions
     */
    public List<AgentAction> getRecentActions(int limit) {
        int size = actionHistory.size();
        int start = Math.max(0, size - limit);
        return new ArrayList<>(actionHistory.subList(start, size));
    }
    
    /**
     * Shuts down the agent and all services
     */
    public void shutdown() {
        currentState = AgentState.IDLE;
        
        if (geminiService != null) {
            geminiService.shutdown();
        }
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        recordAction("shutdown", "Agent shutdown", Map.of(), "Success");
    }
    
    // Result classes
    public static class AgentResult {
        private String originalRequest;
        private RequestAnalysis analysis;
        private SearchResults searchResults;
        private List<ElementResult> generatedElements;
        private List<AssetResult> generatedAssets;
        
        // Getters and setters
        public String getOriginalRequest() { return originalRequest; }
        public void setOriginalRequest(String originalRequest) { this.originalRequest = originalRequest; }
        
        public RequestAnalysis getAnalysis() { return analysis; }
        public void setAnalysis(RequestAnalysis analysis) { this.analysis = analysis; }
        
        public SearchResults getSearchResults() { return searchResults; }
        public void setSearchResults(SearchResults searchResults) { this.searchResults = searchResults; }
        
        public List<ElementResult> getGeneratedElements() { return generatedElements; }
        public void setGeneratedElements(List<ElementResult> generatedElements) { this.generatedElements = generatedElements; }
        
        public List<AssetResult> getGeneratedAssets() { return generatedAssets; }
        public void setGeneratedAssets(List<AssetResult> generatedAssets) { this.generatedAssets = generatedAssets; }
    }
    
    public static class RequestAnalysis {
        private String originalRequest;
        private String analysisResponse;
        private String projectType;
        private List<String> requiredElements = new ArrayList<>();
        private boolean requiresWebSearch;
        private String searchQuery;
        private boolean requiresTextures;
        private boolean requiresAudio;
        private boolean requiresModels;
        
        // Getters and setters
        public String getOriginalRequest() { return originalRequest; }
        public void setOriginalRequest(String originalRequest) { this.originalRequest = originalRequest; }
        
        public String getAnalysisResponse() { return analysisResponse; }
        public void setAnalysisResponse(String analysisResponse) { this.analysisResponse = analysisResponse; }
        
        public String getProjectType() { return projectType; }
        public void setProjectType(String projectType) { this.projectType = projectType; }
        
        public List<String> getRequiredElements() { return requiredElements; }
        public void addRequiredElement(String element) { this.requiredElements.add(element); }
        
        public boolean isRequiresWebSearch() { return requiresWebSearch; }
        public void setRequiresWebSearch(boolean requiresWebSearch) { this.requiresWebSearch = requiresWebSearch; }
        
        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
        
        public boolean isRequiresTextures() { return requiresTextures; }
        public void setRequiresTextures(boolean requiresTextures) { this.requiresTextures = requiresTextures; }
        
        public boolean isRequiresAudio() { return requiresAudio; }
        public void setRequiresAudio(boolean requiresAudio) { this.requiresAudio = requiresAudio; }
        
        public boolean isRequiresModels() { return requiresModels; }
        public void setRequiresModels(boolean requiresModels) { this.requiresModels = requiresModels; }
    }
    
    public static class ElementResult {
        private String elementType;
        private String generatedCode;
        private boolean success;
        private String errorMessage;
        
        // Getters and setters
        public String getElementType() { return elementType; }
        public void setElementType(String elementType) { this.elementType = elementType; }
        
        public String getGeneratedCode() { return generatedCode; }
        public void setGeneratedCode(String generatedCode) { this.generatedCode = generatedCode; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    public static class AssetResult {
        private String assetType;
        private String filePath;
        private String description;
        private boolean success;
        private String errorMessage;
        private Object additionalData;
        
        // Getters and setters
        public String getAssetType() { return assetType; }
        public void setAssetType(String assetType) { this.assetType = assetType; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Object getAdditionalData() { return additionalData; }
        public void setAdditionalData(Object additionalData) { this.additionalData = additionalData; }
    }
    
    public static class FileProcessingResult {
        private String originalPath;
        private String processedPath;
        private String fileType;
        private String analysis;
        private boolean success;
        private String errorMessage;
        
        // Getters and setters
        public String getOriginalPath() { return originalPath; }
        public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }
        
        public String getProcessedPath() { return processedPath; }
        public void setProcessedPath(String processedPath) { this.processedPath = processedPath; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public String getAnalysis() { return analysis; }
        public void setAnalysis(String analysis) { this.analysis = analysis; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

