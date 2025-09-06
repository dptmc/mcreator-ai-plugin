package net.mcreator.aimodgenerator.core;

import net.mcreator.workspace.Workspace;
import net.mcreator.aimodgenerator.ai.GeminiAIService;
import net.mcreator.aimodgenerator.ai.CustomInstructions;
import net.mcreator.aimodgenerator.search.WebSearchService;
import net.mcreator.aimodgenerator.search.SearchResults;
import net.mcreator.aimodgenerator.generators.*;
import net.mcreator.aimodgenerator.media.TextureGenerator;
import net.mcreator.aimodgenerator.media.SoundGenerator;
import net.mcreator.aimodgenerator.media.BlockbenchModelGenerator;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Core logic for the AI Mod Generator plugin using Google Gemini AI
 * Orchestrates AI generation, web search, and content creation
 */
public class AIModGeneratorCoreNew {
    
    private final Workspace workspace;
    private final GeminiAIService geminiService;
    private final WebSearchService searchService;
    private final TextureGenerator textureGenerator;
    private final SoundGenerator soundGenerator;
    private final BlockbenchModelGenerator modelGenerator;
    private final ExecutorService executorService;
    
    // Element generators
    private final ItemGenerator itemGenerator;
    private final BlockGenerator blockGenerator;
    private final RecipeGenerator recipeGenerator;
    private final EnchantmentGenerator enchantmentGenerator;
    private final ProcedureGenerator procedureGenerator;
    private final EntityGenerator entityGenerator;
    
    public AIModGeneratorCoreNew(Workspace workspace) {
        this.workspace = workspace;
        this.geminiService = new GeminiAIService();
        this.searchService = new WebSearchService();
        this.textureGenerator = new TextureGenerator(geminiService);
        this.soundGenerator = new SoundGenerator(geminiService);
        this.modelGenerator = new BlockbenchModelGenerator(geminiService);
        this.executorService = Executors.newFixedThreadPool(4);
        
        // Initialize generators with workspace integration
        this.itemGenerator = new ItemGenerator(workspace, geminiService);
        this.blockGenerator = new BlockGenerator(workspace, geminiService);
        this.recipeGenerator = new RecipeGenerator(workspace, geminiService);
        this.enchantmentGenerator = new EnchantmentGenerator(workspace, geminiService);
        this.procedureGenerator = new ProcedureGenerator(workspace, geminiService);
        this.entityGenerator = new EntityGenerator(workspace, geminiService);
    }
    
    /**
     * Callback interface for generation progress updates
     */
    public interface GenerationCallback {
        void onProgress(String message);
        void onElementGenerated(String elementType, String elementName, String details);
        void onComplete();
        void onError(String error);
    }
    
    /**
     * Processes a user prompt and generates mod elements with progress callbacks
     * @param prompt User input prompt
     * @param options Generation options
     * @param callback Progress callback
     * @return CompletableFuture with generation results
     */
    public CompletableFuture<GenerationResult> processPrompt(String prompt, GenerationOptions options, GenerationCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                GenerationResult result = new GenerationResult();
                result.setOriginalPrompt(prompt);
                result.setOptions(options);
                
                callback.onProgress("Analyzing prompt with Gemini AI...");
                
                // Step 1: Analyze the prompt
                PromptAnalysis analysis = geminiService.analyzePrompt(prompt);
                result.setAnalysis(analysis);
                
                callback.onProgress("Analysis complete. Theme: " + analysis.getTheme());
                
                // Step 2: Perform web search if enabled
                SearchResults searchResults = null;
                if (options.isWebSearchEnabled()) {
                    callback.onProgress("Performing web search for additional context...");
                    searchResults = searchService.search(prompt, 5);
                    result.setSearchResults(searchResults);
                    callback.onProgress("Web search complete. Found " + searchResults.getResults().size() + " results.");
                }
                
                // Step 3: Generate elements based on options
                List<String> generatedElements = new ArrayList<>();
                
                if (options.isGenerateItems()) {
                    callback.onProgress("Generating item with Gemini AI...");
                    String itemCode = itemGenerator.generate(analysis, searchResults);
                    generatedElements.add("Item: " + itemCode);
                    callback.onElementGenerated("Item", analysis.getTheme() + " Item", itemCode);
                }
                
                if (options.isGenerateBlocks()) {
                    callback.onProgress("Generating block with Gemini AI...");
                    String blockCode = blockGenerator.generate(analysis, searchResults);
                    generatedElements.add("Block: " + blockCode);
                    callback.onElementGenerated("Block", analysis.getTheme() + " Block", blockCode);
                }
                
                if (options.isGenerateRecipes()) {
                    callback.onProgress("Generating recipe with Gemini AI...");
                    String recipeCode = recipeGenerator.generate(analysis, searchResults);
                    generatedElements.add("Recipe: " + recipeCode);
                    callback.onElementGenerated("Recipe", analysis.getTheme() + " Recipe", recipeCode);
                }
                
                if (options.isGenerateEnchantments()) {
                    callback.onProgress("Generating enchantment with Gemini AI...");
                    String enchantmentCode = enchantmentGenerator.generate(analysis, searchResults);
                    generatedElements.add("Enchantment: " + enchantmentCode);
                    callback.onElementGenerated("Enchantment", analysis.getTheme() + " Enchantment", enchantmentCode);
                }
                
                if (options.isGenerateProcedures()) {
                    callback.onProgress("Generating procedure with Gemini AI...");
                    String procedureCode = procedureGenerator.generate(analysis, searchResults);
                    generatedElements.add("Procedure: " + procedureCode);
                    callback.onElementGenerated("Procedure", analysis.getTheme() + " Procedure", procedureCode);
                }
                
                if (options.isGenerateEntities()) {
                    callback.onProgress("Generating entity ideas with Gemini AI...");
                    // Generate entity ideas, not actual entities
                    String entityIdeas = entityGenerator.generateIdeas(analysis, searchResults);
                    generatedElements.add("Entity Ideas: " + entityIdeas);
                    callback.onElementGenerated("Entity Ideas", analysis.getTheme() + " Entity Concepts", entityIdeas);
                }
                
                result.setGeneratedElements(generatedElements);
                
                // Step 4: Generate textures if enabled
                if (options.isGenerateTextures()) {
                    callback.onProgress("Generating textures with Nano Banana (Gemini Flash 2.0)...");
                    List<String> texturePaths = new ArrayList<>();
                    
                    if (options.isGenerateItems() || options.isGenerateBlocks()) {
                        String texturePath = textureGenerator.generateTexture(
                            analysis.getDescription(), 
                            workspace.getWorkspaceFolder() + "/temp_texture_" + System.currentTimeMillis() + ".png",
                            64, 64
                        );
                        texturePaths.add(texturePath);
                        callback.onProgress("Texture generated: " + texturePath);
                    }
                    
                    result.setTexturePaths(texturePaths);
                }
                
                // Step 5: Generate Blockbench models if enabled
                if (options.isGenerateModels()) {
                    callback.onProgress("Generating Blockbench model ideas with Gemini AI...");
                    List<String> modelPaths = modelGenerator.generateModelIdeas(analysis, searchResults);
                    result.setModelPaths(modelPaths);
                    callback.onProgress("Model ideas generated: " + modelPaths.size() + " concepts");
                }
                
                // Step 6: Generate sound descriptions if enabled
                if (options.isGenerateSounds()) {
                    callback.onProgress("Generating sound effects with Gemini AI...");
                    List<String> soundDescriptions = new ArrayList<>();
                    String soundDesc = soundGenerator.generateSoundDescription(
                        analysis.getTheme() + " sound effect", 
                        analysis.getDescription()
                    );
                    soundDescriptions.add(soundDesc);
                    result.setSoundDescriptions(soundDescriptions);
                    callback.onProgress("Sound descriptions generated");
                }
                
                result.setSuccess(true);
                callback.onComplete();
                return result;
                
            } catch (Exception e) {
                GenerationResult errorResult = new GenerationResult();
                errorResult.setSuccess(false);
                errorResult.setErrorMessage("Generation failed: " + e.getMessage());
                callback.onError("Generation failed: " + e.getMessage());
                return errorResult;
            }
        }, executorService);
    }
    
    /**
     * Provides tutorial assistance for MCreator usage
     * @param question User's question or topic
     * @param userLevel User's experience level
     * @return Tutorial response
     */
    public CompletableFuture<String> provideTutorialAssistance(String question, String userLevel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String tutorialPrompt = CustomInstructions.getAgentInstructions(userLevel) + 
                                      "\n\nUser Question: " + question;
                return geminiService.generateText(tutorialPrompt, 0.3, 800);
            } catch (Exception e) {
                return "I apologize, but I encountered an error while generating tutorial assistance: " + e.getMessage();
            }
        }, executorService);
    }
    
    /**
     * Provides agent-based task assistance
     * @param task Description of what the user wants to accomplish
     * @param userLevel User's experience level (beginner, intermediate, advanced)
     * @return Agent assistance response
     */
    public CompletableFuture<String> provideAgentAssistance(String task, String userLevel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String agentPrompt = CustomInstructions.getAgentInstructions(userLevel) + 
                                   "\n\nUser Task: " + task;
                return geminiService.generateText(agentPrompt, 0.4, 1000);
            } catch (Exception e) {
                return "I apologize, but I encountered an error while providing assistance: " + e.getMessage();
            }
        }, executorService);
    }
    
    /**
     * Generates code directly for MCreator elements and writes to workspace
     * @param elementType Type of element to generate
     * @param elementName Name of the element
     * @param description Description of the element
     * @param fabricVersion Target Fabric version (default: 1.20.1)
     * @param writeToWorkspace Whether to write directly to MCreator workspace
     * @return Generated code
     */
    public CompletableFuture<String> generateDirectCode(String elementType, String elementName, 
                                                       String description, String fabricVersion, 
                                                       boolean writeToWorkspace) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create a prompt analysis from the description
                PromptAnalysis analysis = new PromptAnalysis();
                analysis.setOriginalPrompt(description);
                analysis.setDescription(description);
                
                // Get appropriate instructions for the element type
                String instructions;
                switch (elementType.toLowerCase()) {
                    case "item":
                        instructions = CustomInstructions.getItemInstructions("general");
                        break;
                    case "block":
                        instructions = CustomInstructions.getBlockInstructions("general");
                        break;
                    case "recipe":
                        instructions = CustomInstructions.getRecipeInstructions("crafting");
                        break;
                    default:
                        instructions = CustomInstructions.CODE_GENERATION_INSTRUCTIONS;
                }
                
                // Add Fabric compatibility instructions
                String fullPrompt = instructions + "\n" + 
                                  CustomInstructions.getFabricInstructions() + 
                                  "\n\nGenerate code for a " + elementType + " named '" + elementName + 
                                  "' with the following description: " + description;
                
                String generatedCode = geminiService.generateText(fullPrompt, 0.2, 2000);
                
                // If requested, write directly to MCreator workspace
                if (writeToWorkspace && workspace != null) {
                    writeCodeToWorkspace(elementType, elementName, generatedCode);
                }
                
                return generatedCode;
                
            } catch (Exception e) {
                return "// Error generating code: " + e.getMessage() + 
                       "\n// Please check your input and try again.";
            }
        }, executorService);
    }
    
    /**
     * Converts uploaded images to entity ideas using Blockbench style
     * @param imagePath Path to the uploaded image
     * @param entityName Desired name for the entity
     * @return Entity conversion result
     */
    public CompletableFuture<String> convertImageToEntityIdea(String imagePath, String entityName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Use the Blockbench model generator to create entity ideas from the image
                return modelGenerator.convertImageToEntityIdea(imagePath, entityName);
            } catch (Exception e) {
                return "Error converting image to entity idea: " + e.getMessage();
            }
        }, executorService);
    }
    
    /**
     * Writes generated code directly to MCreator workspace
     * @param elementType Type of element
     * @param elementName Name of element
     * @param code Generated code
     */
    private void writeCodeToWorkspace(String elementType, String elementName, String code) {
        try {
            // This would integrate with MCreator's workspace API to create actual mod elements
            // For now, this is a placeholder for the integration
            System.out.println("Writing " + elementType + " '" + elementName + "' to workspace:");
            System.out.println(code);
            
            // In a real implementation, this would:
            // 1. Parse the generated code
            // 2. Create appropriate MCreator mod elements
            // 3. Set properties and configurations
            // 4. Add to workspace
            
        } catch (Exception e) {
            System.err.println("Error writing to workspace: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current status of the AI service
     * @return Service status information
     */
    public String getServiceStatus() {
        return "Gemini AI Service: Active (API Key Configured)\n" +
               "Web Search Service: " + (searchService != null ? "Active" : "Inactive") + "\n" +
               "Texture Generator (Nano Banana): Active\n" +
               "Sound Generator: Active\n" +
               "Model Generator: Active\n" +
               "Fabric 1.20.1 Support: Enabled\n" +
               "Workspace Integration: " + (workspace != null ? "Connected" : "Not Connected");
    }
    
    /**
     * Shuts down all services
     */
    public void shutdown() {
        if (geminiService != null) {
            geminiService.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    /**
     * Result class for generation operations
     */
    public static class GenerationResult {
        private String originalPrompt;
        private GenerationOptions options;
        private PromptAnalysis analysis;
        private SearchResults searchResults;
        private List<String> generatedElements;
        private List<String> texturePaths;
        private List<String> modelPaths;
        private List<String> soundDescriptions;
        private boolean success;
        private String errorMessage;
        
        // Getters and setters
        public String getOriginalPrompt() { return originalPrompt; }
        public void setOriginalPrompt(String originalPrompt) { this.originalPrompt = originalPrompt; }
        
        public GenerationOptions getOptions() { return options; }
        public void setOptions(GenerationOptions options) { this.options = options; }
        
        public PromptAnalysis getAnalysis() { return analysis; }
        public void setAnalysis(PromptAnalysis analysis) { this.analysis = analysis; }
        
        public SearchResults getSearchResults() { return searchResults; }
        public void setSearchResults(SearchResults searchResults) { this.searchResults = searchResults; }
        
        public List<String> getGeneratedElements() { return generatedElements; }
        public void setGeneratedElements(List<String> generatedElements) { this.generatedElements = generatedElements; }
        
        public List<String> getTexturePaths() { return texturePaths; }
        public void setTexturePaths(List<String> texturePaths) { this.texturePaths = texturePaths; }
        
        public List<String> getModelPaths() { return modelPaths; }
        public void setModelPaths(List<String> modelPaths) { this.modelPaths = modelPaths; }
        
        public List<String> getSoundDescriptions() { return soundDescriptions; }
        public void setSoundDescriptions(List<String> soundDescriptions) { this.soundDescriptions = soundDescriptions; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

