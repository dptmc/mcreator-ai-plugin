package net.mcreator.aimodgenerator.core;

import net.mcreator.workspace.Workspace;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.search.WebSearchService;
import net.mcreator.aimodgenerator.generators.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Core logic class that orchestrates the mod generation process
 * Coordinates between AI services, web search, and mod element generators
 */
public class AIModGeneratorCore {
    
    private final Workspace workspace;
    private final AIIntegrationService aiService;
    private final WebSearchService webSearchService;
    private final ExecutorService executorService;
    
    // Generators for different mod elements
    private final ItemGenerator itemGenerator;
    private final BlockGenerator blockGenerator;
    private final RecipeGenerator recipeGenerator;
    private final EnchantmentGenerator enchantmentGenerator;
    private final ProcedureGenerator procedureGenerator;
    
    public AIModGeneratorCore(Workspace workspace) {
        this.workspace = workspace;
        this.aiService = new AIIntegrationService();
        this.webSearchService = new WebSearchService();
        this.executorService = Executors.newFixedThreadPool(4);
        
        // Initialize generators
        this.itemGenerator = new ItemGenerator(workspace, aiService);
        this.blockGenerator = new BlockGenerator(workspace, aiService);
        this.recipeGenerator = new RecipeGenerator(workspace, aiService);
        this.enchantmentGenerator = new EnchantmentGenerator(workspace, aiService);
        this.procedureGenerator = new ProcedureGenerator(workspace, aiService);
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
     * Main method to generate mod elements based on a prompt
     * @param prompt The natural language prompt
     * @param options Generation options
     * @param callback Callback for progress updates
     */
    public void generateModElements(String prompt, GenerationOptions options, GenerationCallback callback) {
        CompletableFuture.runAsync(() -> {
            try {
                callback.onProgress("Analyzing prompt: " + prompt);
                
                // Step 1: Parse and analyze the prompt
                PromptAnalysis analysis = analyzePrompt(prompt, callback);
                
                // Step 2: Perform web search if enabled
                SearchResults searchResults = null;
                if (options.isUseWebSearch()) {
                    callback.onProgress("Performing web search for relevant information...");
                    searchResults = webSearchService.searchForPrompt(prompt, analysis);
                    callback.onProgress("Web search completed, found " + searchResults.getResultCount() + " relevant results");
                }
                
                // Step 3: Generate mod elements based on analysis and options
                generateElements(analysis, searchResults, options, callback);
                
                callback.onComplete();
                
            } catch (Exception e) {
                callback.onError("Generation failed: " + e.getMessage());
                e.printStackTrace();
            }
        }, executorService);
    }
    
    /**
     * Analyzes the user prompt to understand what needs to be generated
     */
    private PromptAnalysis analyzePrompt(String prompt, GenerationCallback callback) {
        callback.onProgress("Using AI to analyze prompt and identify mod elements...");
        
        // Use AI to analyze the prompt and extract key information
        String analysisPrompt = 
            "Analyze this Minecraft mod request and identify what elements need to be created:\n\n" +
            "User Request: " + prompt + "\n\n" +
            "Please identify:\n" +
            "1. Items to create (name, type, properties)\n" +
            "2. Blocks to create (name, type, properties)\n" +
            "3. Recipes needed\n" +
            "4. Enchantments needed\n" +
            "5. Special behaviors or procedures\n" +
            "6. Theme and style preferences\n\n" +
            "Format your response as structured data that can be parsed.";
        
        try {
            String aiResponse = aiService.generateText(analysisPrompt);
            return new PromptAnalysis(prompt, aiResponse);
        } catch (Exception e) {
            callback.onProgress("AI analysis failed, using basic parsing: " + e.getMessage());
            return new PromptAnalysis(prompt, ""); // Fallback to basic parsing
        }
    }
    
    /**
     * Generates the actual mod elements based on analysis and options
     */
    private void generateElements(PromptAnalysis analysis, SearchResults searchResults, 
                                GenerationOptions options, GenerationCallback callback) {
        
        // Generate items
        if (options.isGenerateItems() && analysis.hasItems()) {
            callback.onProgress("Generating items...");
            for (String itemName : analysis.getItemNames()) {
                try {
                    String itemDetails = itemGenerator.generateItem(itemName, analysis, searchResults, options);
                    callback.onElementGenerated("Item", itemName, itemDetails);
                } catch (Exception e) {
                    callback.onError("Failed to generate item " + itemName + ": " + e.getMessage());
                }
            }
        }
        
        // Generate blocks
        if (options.isGenerateBlocks() && analysis.hasBlocks()) {
            callback.onProgress("Generating blocks...");
            for (String blockName : analysis.getBlockNames()) {
                try {
                    String blockDetails = blockGenerator.generateBlock(blockName, analysis, searchResults, options);
                    callback.onElementGenerated("Block", blockName, blockDetails);
                } catch (Exception e) {
                    callback.onError("Failed to generate block " + blockName + ": " + e.getMessage());
                }
            }
        }
        
        // Generate recipes
        if (options.isGenerateRecipes() && (analysis.hasItems() || analysis.hasBlocks())) {
            callback.onProgress("Generating crafting recipes...");
            try {
                String recipeDetails = recipeGenerator.generateRecipes(analysis, searchResults, options);
                callback.onElementGenerated("Recipes", "Crafting Recipes", recipeDetails);
            } catch (Exception e) {
                callback.onError("Failed to generate recipes: " + e.getMessage());
            }
        }
        
        // Generate enchantments
        if (options.isGenerateEnchantments() && analysis.hasEnchantments()) {
            callback.onProgress("Generating enchantments...");
            for (String enchantmentName : analysis.getEnchantmentNames()) {
                try {
                    String enchantmentDetails = enchantmentGenerator.generateEnchantment(enchantmentName, analysis, searchResults, options);
                    callback.onElementGenerated("Enchantment", enchantmentName, enchantmentDetails);
                } catch (Exception e) {
                    callback.onError("Failed to generate enchantment " + enchantmentName + ": " + e.getMessage());
                }
            }
        }
        
        // Generate procedures
        if (options.isGenerateProcedures() && analysis.hasProcedures()) {
            callback.onProgress("Generating procedures and custom logic...");
            for (String procedureName : analysis.getProcedureNames()) {
                try {
                    String procedureDetails = procedureGenerator.generateProcedure(procedureName, analysis, searchResults, options);
                    callback.onElementGenerated("Procedure", procedureName, procedureDetails);
                } catch (Exception e) {
                    callback.onError("Failed to generate procedure " + procedureName + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
    
    /**
     * Gets the workspace associated with this core instance
     */
    public Workspace getWorkspace() {
        return workspace;
    }
}

