package net.mcreator.aimodgenerator.core;

/**
 * Data class for storing generation options and preferences
 */
public class GenerationOptions {
    
    // Element types to generate
    private boolean generateItems = true;
    private boolean generateBlocks = true;
    private boolean generateRecipes = true;
    private boolean generateEnchantments = false;
    private boolean generateProcedures = false;
    private boolean generateEntities = false;
    private boolean generateTextures = true;
    private boolean generateSounds = false;
    
    // Texture-specific options
    private boolean allowGlowEffects = false;
    private boolean generateBlockbenchModels = false;
    private boolean uploadImageConversion = false;
    
    // Advanced options
    private boolean useWebSearch = true;
    private boolean generateLore = true;
    private boolean balancedStats = true;
    
    // Quality settings
    private String qualityMode = "Balanced"; // Fast, Balanced, High Quality
    private int creativityLevel = 70; // 0-100
    
    // Constructors
    public GenerationOptions() {}
    
    public GenerationOptions(boolean generateItems, boolean generateBlocks, boolean generateRecipes) {
        this.generateItems = generateItems;
        this.generateBlocks = generateBlocks;
        this.generateRecipes = generateRecipes;
    }
    
    // Getters and setters
    public boolean isGenerateItems() { return generateItems; }
    public void setGenerateItems(boolean generateItems) { this.generateItems = generateItems; }
    
    public boolean isGenerateBlocks() { return generateBlocks; }
    public void setGenerateBlocks(boolean generateBlocks) { this.generateBlocks = generateBlocks; }
    
    public boolean isGenerateRecipes() { return generateRecipes; }
    public void setGenerateRecipes(boolean generateRecipes) { this.generateRecipes = generateRecipes; }
    
    public boolean isGenerateEnchantments() { return generateEnchantments; }
    public void setGenerateEnchantments(boolean generateEnchantments) { this.generateEnchantments = generateEnchantments; }
    
    public boolean isGenerateProcedures() { return generateProcedures; }
    public void setGenerateProcedures(boolean generateProcedures) { this.generateProcedures = generateProcedures; }
    
    public boolean isGenerateTextures() { return generateTextures; }
    public void setGenerateTextures(boolean generateTextures) { this.generateTextures = generateTextures; }
    
    public boolean isGenerateSounds() { return generateSounds; }
    public void setGenerateSounds(boolean generateSounds) { this.generateSounds = generateSounds; }
    
    public boolean isUseWebSearch() { return useWebSearch; }
    public void setUseWebSearch(boolean useWebSearch) { this.useWebSearch = useWebSearch; }
    
    public boolean isGenerateLore() { return generateLore; }
    public void setGenerateLore(boolean generateLore) { this.generateLore = generateLore; }
    
    public boolean isBalancedStats() { return balancedStats; }
    public void setBalancedStats(boolean balancedStats) { this.balancedStats = balancedStats; }
    
    public String getQualityMode() { return qualityMode; }
    public void setQualityMode(String qualityMode) { this.qualityMode = qualityMode; }
    
    public int getCreativityLevel() { return creativityLevel; }
    public void setCreativityLevel(int creativityLevel) { this.creativityLevel = creativityLevel; }
    
    /**
     * Returns true if any element type is selected for generation
     */
    public boolean hasElementsToGenerate() {
        return generateItems || generateBlocks || generateRecipes || 
               generateEnchantments || generateProcedures;
    }
    
    /**
     * Returns true if any assets need to be generated
     */
    public boolean hasAssetsToGenerate() {
        return generateTextures || generateSounds;
    }
    
    @Override
    public String toString() {
        return "GenerationOptions{" +
                "generateItems=" + generateItems +
                ", generateBlocks=" + generateBlocks +
                ", generateRecipes=" + generateRecipes +
                ", generateEnchantments=" + generateEnchantments +
                ", generateProcedures=" + generateProcedures +
                ", generateTextures=" + generateTextures +
                ", generateSounds=" + generateSounds +
                ", useWebSearch=" + useWebSearch +
                ", generateLore=" + generateLore +
                ", balancedStats=" + balancedStats +
                ", qualityMode='" + qualityMode + '\'' +
                ", creativityLevel=" + creativityLevel +
                '}';
    }
}


    
    public boolean isGenerateEntities() { return generateEntities; }
    public void setGenerateEntities(boolean generateEntities) { this.generateEntities = generateEntities; }
    
    public boolean isAllowGlowEffects() { return allowGlowEffects; }
    public void setAllowGlowEffects(boolean allowGlowEffects) { this.allowGlowEffects = allowGlowEffects; }
    
    public boolean isGenerateBlockbenchModels() { return generateBlockbenchModels; }
    public void setGenerateBlockbenchModels(boolean generateBlockbenchModels) { this.generateBlockbenchModels = generateBlockbenchModels; }
    
    public boolean isUploadImageConversion() { return uploadImageConversion; }
    public void setUploadImageConversion(boolean uploadImageConversion) { this.uploadImageConversion = uploadImageConversion; }

