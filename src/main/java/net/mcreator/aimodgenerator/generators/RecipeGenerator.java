package net.mcreator.aimodgenerator.generators;

import net.mcreator.workspace.Workspace;
import net.mcreator.element.ModElementType;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.types.Recipe;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator for creating Minecraft recipes using AI
 * Handles crafting, smelting, blasting, smoking, and other recipe types
 */
public class RecipeGenerator extends BaseGenerator {
    
    public RecipeGenerator(Workspace workspace, AIIntegrationService aiService) {
        super(workspace, aiService);
    }
    
    /**
     * Generates recipes for the analyzed mod elements
     * @param analysis Prompt analysis containing items and blocks
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return Details about the generated recipes
     */
    public String generateRecipes(PromptAnalysis analysis, SearchResults searchResults, GenerationOptions options) throws Exception {
        
        StringBuilder allRecipeDetails = new StringBuilder();
        List<RecipeData> generatedRecipes = new ArrayList<>();
        
        // Generate recipes for items
        for (String itemName : analysis.getItemNames()) {
            List<RecipeData> itemRecipes = generateItemRecipes(itemName, analysis, searchResults, options);
            generatedRecipes.addAll(itemRecipes);
        }
        
        // Generate recipes for blocks
        for (String blockName : analysis.getBlockNames()) {
            List<RecipeData> blockRecipes = generateBlockRecipes(blockName, analysis, searchResults, options);
            generatedRecipes.addAll(blockRecipes);
        }
        
        // Generate special recipes based on theme
        List<RecipeData> specialRecipes = generateSpecialRecipes(analysis, searchResults, options);
        generatedRecipes.addAll(specialRecipes);
        
        // Create MCreator recipe elements
        for (RecipeData recipeData : generatedRecipes) {
            createRecipeElement(recipeData);
            allRecipeDetails.append(formatRecipeDetails(recipeData)).append("\n\n");
        }
        
        return allRecipeDetails.toString();
    }
    
    /**
     * Generates recipes for a specific item
     */
    private List<RecipeData> generateItemRecipes(String itemName, PromptAnalysis analysis, 
                                               SearchResults searchResults, GenerationOptions options) throws Exception {
        
        List<RecipeData> recipes = new ArrayList<>();
        
        // Generate crafting recipe
        RecipeData craftingRecipe = generateCraftingRecipe(itemName, "item", analysis, searchResults, options);
        if (craftingRecipe != null) {
            recipes.add(craftingRecipe);
        }
        
        // Generate smelting recipe if applicable
        if (shouldHaveSmeltingRecipe(itemName, analysis)) {
            RecipeData smeltingRecipe = generateSmeltingRecipe(itemName, "item", analysis, options);
            if (smeltingRecipe != null) {
                recipes.add(smeltingRecipe);
            }
        }
        
        return recipes;
    }
    
    /**
     * Generates recipes for a specific block
     */
    private List<RecipeData> generateBlockRecipes(String blockName, PromptAnalysis analysis, 
                                                SearchResults searchResults, GenerationOptions options) throws Exception {
        
        List<RecipeData> recipes = new ArrayList<>();
        
        // Generate crafting recipe
        RecipeData craftingRecipe = generateCraftingRecipe(blockName, "block", analysis, searchResults, options);
        if (craftingRecipe != null) {
            recipes.add(craftingRecipe);
        }
        
        // Generate smelting recipe for ores
        if (blockName.toLowerCase().contains("ore")) {
            RecipeData smeltingRecipe = generateOreSmeltingRecipe(blockName, analysis, options);
            if (smeltingRecipe != null) {
                recipes.add(smeltingRecipe);
            }
        }
        
        return recipes;
    }
    
    /**
     * Generates special themed recipes
     */
    private List<RecipeData> generateSpecialRecipes(PromptAnalysis analysis, SearchResults searchResults, GenerationOptions options) throws Exception {
        
        List<RecipeData> recipes = new ArrayList<>();
        
        // Generate recipes based on theme
        String theme = analysis.getTheme();
        if (theme != null) {
            String specialRecipePrompt = "Create special themed recipes for a " + theme + 
                                       " mod. Include unique crafting combinations that fit the theme.";
            
            String aiResponse = aiService.generateText(specialRecipePrompt, 0.7, 300);
            
            // Parse and create special recipes
            List<RecipeData> themeRecipes = parseSpecialRecipes(aiResponse, theme);
            recipes.addAll(themeRecipes);
        }
        
        return recipes;
    }
    
    /**
     * Generates a crafting recipe using AI
     */
    private RecipeData generateCraftingRecipe(String itemName, String elementType, PromptAnalysis analysis, 
                                            SearchResults searchResults, GenerationOptions options) throws Exception {
        
        String recipePrompt = buildCraftingRecipePrompt(itemName, elementType, analysis, searchResults, options);
        String aiResponse = aiService.generateText(recipePrompt, 0.5, 400);
        
        return parseCraftingRecipe(aiResponse, itemName, elementType);
    }
    
    /**
     * Builds the AI prompt for crafting recipe generation
     */
    private String buildCraftingRecipePrompt(String itemName, String elementType, PromptAnalysis analysis, 
                                           SearchResults searchResults, GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a balanced Minecraft crafting recipe for: ").append(itemName).append(" (").append(elementType).append(")\n\n");
        
        // Add context
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n\n");
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(300, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify requirements
        prompt.append("Please provide:\n");
        prompt.append("1. Recipe type (SHAPED, SHAPELESS, or suggest if unsure)\n");
        prompt.append("2. Ingredients needed (use vanilla Minecraft items when possible)\n");
        prompt.append("3. Recipe pattern (for shaped recipes, use 3x3 grid)\n");
        prompt.append("4. Output quantity (1-64)\n");
        prompt.append("5. Difficulty level (EASY, MEDIUM, HARD)\n\n");
        
        // Add balance requirements
        if (options.isBalancedStats()) {
            prompt.append("IMPORTANT: Ensure the recipe is balanced - not too cheap or expensive. ");
            prompt.append("Consider the item's power level and rarity. Use appropriate materials.\n\n");
        }
        
        prompt.append("Example format:\n");
        prompt.append("Recipe Type: SHAPED\n");
        prompt.append("Pattern:\n");
        prompt.append("  I I I\n");
        prompt.append("  I S I\n");
        prompt.append("  _ S _\n");
        prompt.append("Where I = Iron Ingot, S = Stick, _ = Empty\n");
        prompt.append("Output: 1x Iron Sword\n");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to create crafting recipe data
     */
    private RecipeData parseCraftingRecipe(String aiResponse, String itemName, String elementType) {
        RecipeData recipe = new RecipeData();
        recipe.setOutputItem(itemName);
        recipe.setOutputType(elementType);
        recipe.setRecipeType("CRAFTING");
        
        String[] lines = aiResponse.split("\n");
        boolean inPattern = false;
        List<String> patternLines = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.toLowerCase().contains("recipe type:")) {
                String type = extractValue(line).toUpperCase();
                if (type.contains("SHAPED")) {
                    recipe.setShapedRecipe(true);
                } else if (type.contains("SHAPELESS")) {
                    recipe.setShapedRecipe(false);
                }
            } else if (line.toLowerCase().contains("pattern:")) {
                inPattern = true;
            } else if (line.toLowerCase().contains("output:")) {
                String output = extractValue(line);
                recipe.setOutputQuantity(extractIntValue(line, 1));
                inPattern = false;
            } else if (inPattern && (line.contains("_") || line.contains(" "))) {
                patternLines.add(line);
            } else if (line.toLowerCase().contains("where") || line.contains("=")) {
                parseIngredientMapping(line, recipe);
            }
        }
        
        recipe.setPattern(patternLines);
        
        // Set defaults if not specified
        if (recipe.getOutputQuantity() <= 0) {
            recipe.setOutputQuantity(1);
        }
        
        return recipe;
    }
    
    /**
     * Generates a smelting recipe
     */
    private RecipeData generateSmeltingRecipe(String itemName, String elementType, PromptAnalysis analysis, GenerationOptions options) throws Exception {
        
        String smeltingPrompt = "Create a smelting recipe for " + itemName + ". " +
                              "What raw material should be smelted to create this item? " +
                              "Consider the theme: " + analysis.getTheme() + ". " +
                              "Provide the input item and cooking time.";
        
        String aiResponse = aiService.generateText(smeltingPrompt, 0.4, 200);
        
        return parseSmeltingRecipe(aiResponse, itemName, elementType);
    }
    
    /**
     * Generates smelting recipe for ore blocks
     */
    private RecipeData generateOreSmeltingRecipe(String oreName, PromptAnalysis analysis, GenerationOptions options) throws Exception {
        
        // Determine what the ore should smelt into
        String outputItem = oreName.replace("Ore", "Ingot").replace("ore", "ingot");
        
        RecipeData recipe = new RecipeData();
        recipe.setRecipeType("SMELTING");
        recipe.setInputItem(oreName);
        recipe.setOutputItem(outputItem);
        recipe.setOutputType("item");
        recipe.setOutputQuantity(1);
        recipe.setCookingTime(200); // Standard smelting time
        recipe.setExperience(0.7); // Standard ore experience
        
        return recipe;
    }
    
    /**
     * Parses smelting recipe from AI response
     */
    private RecipeData parseSmeltingRecipe(String aiResponse, String outputItem, String elementType) {
        RecipeData recipe = new RecipeData();
        recipe.setRecipeType("SMELTING");
        recipe.setOutputItem(outputItem);
        recipe.setOutputType(elementType);
        recipe.setOutputQuantity(1);
        recipe.setCookingTime(200); // Default
        recipe.setExperience(0.1); // Default
        
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            line = line.trim().toLowerCase();
            
            if (line.contains("input:") || line.contains("material:")) {
                recipe.setInputItem(extractValue(line));
            } else if (line.contains("time:") || line.contains("cooking time:")) {
                recipe.setCookingTime(extractIntValue(line, 200));
            } else if (line.contains("experience:") || line.contains("xp:")) {
                recipe.setExperience(extractDoubleValue(line, 0.1));
            }
        }
        
        return recipe;
    }
    
    /**
     * Parses special recipes from AI response
     */
    private List<RecipeData> parseSpecialRecipes(String aiResponse, String theme) {
        List<RecipeData> recipes = new ArrayList<>();
        
        // This would parse multiple recipes from the AI response
        // For now, return empty list as this is complex parsing
        
        return recipes;
    }
    
    /**
     * Parses ingredient mapping from recipe description
     */
    private void parseIngredientMapping(String line, RecipeData recipe) {
        // Parse lines like "I = Iron Ingot, S = Stick"
        String[] parts = line.split(",");
        
        for (String part : parts) {
            if (part.contains("=")) {
                String[] mapping = part.split("=", 2);
                if (mapping.length == 2) {
                    String symbol = mapping[0].trim();
                    String ingredient = mapping[1].trim();
                    recipe.addIngredientMapping(symbol, ingredient);
                }
            }
        }
    }
    
    /**
     * Creates a MCreator recipe element
     */
    private void createRecipeElement(RecipeData recipeData) {
        try {
            String elementName = sanitizeElementName(recipeData.getOutputItem() + "_recipe");
            ModElement modElement = new ModElement(workspace, elementName, ModElementType.RECIPE);
            
            Recipe recipe = new Recipe(modElement);
            
            // Configure based on recipe type
            switch (recipeData.getRecipeType().toUpperCase()) {
                case "CRAFTING":
                    configureCraftingRecipe(recipe, recipeData);
                    break;
                case "SMELTING":
                    configureSmeltingRecipe(recipe, recipeData);
                    break;
                case "BLASTING":
                    configureBlastingRecipe(recipe, recipeData);
                    break;
                case "SMOKING":
                    configureSmokingRecipe(recipe, recipeData);
                    break;
            }
            
            workspace.addModElement(modElement);
            workspace.getModElementManager().storeModElement(recipe);
            
        } catch (Exception e) {
            System.err.println("Failed to create recipe element: " + e.getMessage());
        }
    }
    
    /**
     * Configures a crafting recipe
     */
    private void configureCraftingRecipe(Recipe recipe, RecipeData recipeData) {
        if (recipeData.isShapedRecipe()) {
            recipe.recipeType = "Crafting";
            // Configure shaped recipe pattern
            // This would use MCreator's recipe API to set the pattern
        } else {
            recipe.recipeType = "Crafting";
            // Configure shapeless recipe
        }
        
        // Set output
        recipe.recipeReturnStack = createItemStack(recipeData.getOutputItem(), recipeData.getOutputQuantity());
    }
    
    /**
     * Configures a smelting recipe
     */
    private void configureSmeltingRecipe(Recipe recipe, RecipeData recipeData) {
        recipe.recipeType = "Smelting";
        recipe.smeltingInputStack = createItemStack(recipeData.getInputItem(), 1);
        recipe.recipeReturnStack = createItemStack(recipeData.getOutputItem(), recipeData.getOutputQuantity());
        recipe.smeltingReturnStack = recipe.recipeReturnStack;
        recipe.xpReward = recipeData.getExperience();
        recipe.cookingTime = recipeData.getCookingTime();
    }
    
    /**
     * Configures a blasting recipe
     */
    private void configureBlastingRecipe(Recipe recipe, RecipeData recipeData) {
        recipe.recipeType = "Blasting";
        recipe.blastingInputStack = createItemStack(recipeData.getInputItem(), 1);
        recipe.recipeReturnStack = createItemStack(recipeData.getOutputItem(), recipeData.getOutputQuantity());
        recipe.blastingReturnStack = recipe.recipeReturnStack;
        recipe.xpReward = recipeData.getExperience();
        recipe.cookingTime = recipeData.getCookingTime() / 2; // Blasting is faster
    }
    
    /**
     * Configures a smoking recipe
     */
    private void configureSmokingRecipe(Recipe recipe, RecipeData recipeData) {
        recipe.recipeType = "Smoking";
        recipe.smokingInputStack = createItemStack(recipeData.getInputItem(), 1);
        recipe.recipeReturnStack = createItemStack(recipeData.getOutputItem(), recipeData.getOutputQuantity());
        recipe.smokingReturnStack = recipe.recipeReturnStack;
        recipe.xpReward = recipeData.getExperience();
        recipe.cookingTime = recipeData.getCookingTime() / 2; // Smoking is faster
    }
    
    /**
     * Creates an item stack representation
     */
    private String createItemStack(String itemName, int quantity) {
        // This would create a proper MCreator item stack
        // For now, return a simple string representation
        return itemName + (quantity > 1 ? " x" + quantity : "");
    }
    
    /**
     * Checks if an item should have a smelting recipe
     */
    private boolean shouldHaveSmeltingRecipe(String itemName, PromptAnalysis analysis) {
        String lowerName = itemName.toLowerCase();
        return lowerName.contains("ingot") || lowerName.contains("metal") || 
               lowerName.contains("cooked") || lowerName.contains("smelted");
    }
    
    /**
     * Formats recipe details for display
     */
    private String formatRecipeDetails(RecipeData recipe) {
        StringBuilder details = new StringBuilder();
        
        details.append("Recipe: ").append(recipe.getOutputItem()).append("\n");
        details.append("Type: ").append(recipe.getRecipeType()).append("\n");
        
        if (recipe.getRecipeType().equals("CRAFTING")) {
            details.append("Shaped: ").append(recipe.isShapedRecipe() ? "Yes" : "No").append("\n");
            if (recipe.isShapedRecipe() && !recipe.getPattern().isEmpty()) {
                details.append("Pattern:\n");
                for (String line : recipe.getPattern()) {
                    details.append("  ").append(line).append("\n");
                }
            }
            if (!recipe.getIngredientMappings().isEmpty()) {
                details.append("Ingredients:\n");
                recipe.getIngredientMappings().forEach((symbol, ingredient) -> 
                    details.append("  ").append(symbol).append(" = ").append(ingredient).append("\n"));
            }
        } else {
            details.append("Input: ").append(recipe.getInputItem()).append("\n");
            details.append("Cooking Time: ").append(recipe.getCookingTime()).append(" ticks\n");
            details.append("Experience: ").append(recipe.getExperience()).append("\n");
        }
        
        details.append("Output: ").append(recipe.getOutputQuantity()).append("x ").append(recipe.getOutputItem());
        
        return details.toString();
    }
    
    /**
     * Data class for recipe information
     */
    public static class RecipeData {
        private String recipeType;
        private String outputItem;
        private String outputType;
        private int outputQuantity = 1;
        private String inputItem;
        private boolean shapedRecipe = true;
        private List<String> pattern = new ArrayList<>();
        private java.util.Map<String, String> ingredientMappings = new java.util.HashMap<>();
        private int cookingTime = 200;
        private double experience = 0.1;
        
        // Getters and setters
        public String getRecipeType() { return recipeType; }
        public void setRecipeType(String recipeType) { this.recipeType = recipeType; }
        
        public String getOutputItem() { return outputItem; }
        public void setOutputItem(String outputItem) { this.outputItem = outputItem; }
        
        public String getOutputType() { return outputType; }
        public void setOutputType(String outputType) { this.outputType = outputType; }
        
        public int getOutputQuantity() { return outputQuantity; }
        public void setOutputQuantity(int outputQuantity) { this.outputQuantity = outputQuantity; }
        
        public String getInputItem() { return inputItem; }
        public void setInputItem(String inputItem) { this.inputItem = inputItem; }
        
        public boolean isShapedRecipe() { return shapedRecipe; }
        public void setShapedRecipe(boolean shapedRecipe) { this.shapedRecipe = shapedRecipe; }
        
        public List<String> getPattern() { return pattern; }
        public void setPattern(List<String> pattern) { this.pattern = pattern; }
        
        public java.util.Map<String, String> getIngredientMappings() { return ingredientMappings; }
        public void addIngredientMapping(String symbol, String ingredient) { 
            this.ingredientMappings.put(symbol, ingredient); 
        }
        
        public int getCookingTime() { return cookingTime; }
        public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
        
        public double getExperience() { return experience; }
        public void setExperience(double experience) { this.experience = experience; }
    }
}

