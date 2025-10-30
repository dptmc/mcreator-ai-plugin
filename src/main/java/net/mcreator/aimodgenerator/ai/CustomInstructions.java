package net.mcreator.aimodgenerator.ai;

/**
 * Custom instructions for different types of AI generation tasks
 * Contains specialized prompts for various Minecraft mod elements
 */
public class CustomInstructions {
    
    /**
     * System instruction for MCreator mod development
     */
    public static final String MCREATOR_SYSTEM_PROMPT = 
        "You are an expert MCreator mod developer and Minecraft game designer. " +
        "You specialize in creating balanced, creative, and technically sound mod elements for Minecraft. " +
        "Always provide detailed, practical solutions that work with MCreator and Forge 1.20.1. " +
        "Focus on gameplay balance, technical accuracy, and creative design. " +
        "Generate complete, functional code that can be directly used in MCreator without modifications. " +
        "Include proper imports, class structure, annotations, and follow MCreator best practices.";
    
    /**
     * Nano Banana (Gemini Flash 2.0) texture generation instructions
     */
    public static final String NANO_BANANA_TEXTURE_INSTRUCTIONS = 
        "You're a Minecraft texture designer for a Minecraft mod. " +
        "Only generate in a grid of 64 by 64 pixels with transparent background. " +
        "Make it a flat texture fully in the grid. " +
        "No glow or unwanted particles only if it's a texture of a particle " +
        "but that's different that's also still 64 by 64 bit pixels " +
        "but pixels can be bright having the illusion of glow only if user asks. " +
        "Ensure perfect pixel art style with clear, defined pixels and transparent background.";
    
    /**
     * Blockbench model generation instructions
     */
    public static final String BLOCKBENCH_MODEL_INSTRUCTIONS = 
        "I require an image generation that meticulously adheres to the 'Blockbench Minecraft entity/model' aesthetic. " +
        "The core directive is to produce flat, two-dimensional representations of structures or individual items, " +
        "mirroring the appearance of a Minecraft inventory item texture, a flattened Blockbench model export, or the GUI icon of a block. " +
        "Fundamental blocky/pixelated aesthetic with 64x64 pixel texture grid emulation. " +
        "Strictly flat, isometric-leaning perspective with absolute transparent background. " +
        "Crisp, defined edges with strategic pixel-perfect shading.";
    
    /**
     * Entity generation instructions (ideas only, not actual entities)
     */
    public static final String ENTITY_IDEA_INSTRUCTIONS = 
        "Generate creative ideas and concepts for Minecraft entities. " +
        "Provide detailed descriptions, behavior patterns, spawn conditions, and visual concepts. " +
        "Focus on balanced gameplay integration and unique mechanics. " +
        "Include suggestions for animations, sounds, and interactions. " +
        "Remember: you're generating ideas and concepts, not actual entity code.";
    
    /**
     * Code generation instructions for MCreator elements
     */
    public static final String CODE_GENERATION_INSTRUCTIONS = 
        "Generate complete, functional Java code for MCreator mod elements. " +
        "Ensure compatibility with Forge 1.20.1 and MCreator's code structure. " +
        "Include all necessary imports, proper class hierarchy, and MCreator annotations. " +
        "Follow Minecraft modding best practices and ensure balanced gameplay. " +
        "The code should be ready to compile and run without modifications.";
    
    /**
     * Tutorial and help system instructions
     */
    public static final String TUTORIAL_INSTRUCTIONS = 
        "You are a helpful MCreator tutorial assistant. " +
        "Provide clear, step-by-step instructions for using MCreator and creating mods. " +
        "Break down complex tasks into manageable steps. " +
        "Include tips, best practices, and troubleshooting advice. " +
        "Adapt your explanations to the user's skill level and specific needs.";
    
    /**
     * Agent system instructions for task assistance
     */
    public static final String AGENT_SYSTEM_INSTRUCTIONS = 
        "You are an intelligent MCreator assistant agent. " +
        "Help users accomplish their modding goals by breaking down tasks, " +
        "providing guidance, and identifying what resources or information are needed. " +
        "Ask clarifying questions when requirements are unclear. " +
        "Suggest alternative approaches when facing limitations. " +
        "Always prioritize user success and learning.";
    
    /**
     * Gets texture generation instructions with glow control
     * @param allowGlow Whether glow effects are permitted
     * @param isParticleTexture Whether this is for particle effects
     * @return Customized texture instructions
     */
    public static String getTextureInstructions(boolean allowGlow, boolean isParticleTexture) {
        StringBuilder instructions = new StringBuilder();
        instructions.append(NANO_BANANA_TEXTURE_INSTRUCTIONS);
        
        if (isParticleTexture) {
            instructions.append(" Since this is a particle texture, you may use bright pixels with glow illusion effects. ");
        } else if (allowGlow) {
            instructions.append(" You may use bright pixels having the illusion of glow since the user requested glowing effects. ");
        } else {
            instructions.append(" Avoid any glow effects or bright halos around the texture. ");
        }
        
        return instructions.toString();
    }
    
    /**
     * Gets item-specific generation instructions
     * @param itemType Type of item (weapon, tool, food, etc.)
     * @return Customized item instructions
     */
    public static String getItemInstructions(String itemType) {
        StringBuilder instructions = new StringBuilder();
        instructions.append(MCREATOR_SYSTEM_PROMPT);
        instructions.append(" Focus on creating a ").append(itemType).append(" item. ");
        
        switch (itemType.toLowerCase()) {
            case "weapon":
                instructions.append("Consider damage values, attack speed, durability, and special abilities. ");
                instructions.append("Ensure balanced combat mechanics and appropriate rarity. ");
                break;
            case "tool":
                instructions.append("Consider mining speed, durability, harvest level, and efficiency. ");
                instructions.append("Ensure proper tool material properties and balanced progression. ");
                break;
            case "food":
                instructions.append("Consider hunger restoration, saturation, eating time, and special effects. ");
                instructions.append("Ensure balanced nutrition values and appropriate crafting cost. ");
                break;
            case "armor":
                instructions.append("Consider protection values, durability, toughness, and set bonuses. ");
                instructions.append("Ensure balanced defense mechanics and material consistency. ");
                break;
            default:
                instructions.append("Consider the item's purpose, rarity, and integration with existing gameplay. ");
        }
        
        return instructions.toString();
    }
    
    /**
     * Gets block-specific generation instructions
     * @param blockType Type of block (decorative, functional, ore, etc.)
     * @return Customized block instructions
     */
    public static String getBlockInstructions(String blockType) {
        StringBuilder instructions = new StringBuilder();
        instructions.append(MCREATOR_SYSTEM_PROMPT);
        instructions.append(" Focus on creating a ").append(blockType).append(" block. ");
        
        switch (blockType.toLowerCase()) {
            case "ore":
                instructions.append("Consider spawn conditions, rarity, mining requirements, and drops. ");
                instructions.append("Ensure balanced resource generation and world integration. ");
                break;
            case "machine":
            case "functional":
                instructions.append("Consider functionality, power requirements, GUI design, and recipes. ");
                instructions.append("Ensure balanced automation and resource processing. ");
                break;
            case "decorative":
                instructions.append("Consider aesthetic appeal, variants, and building integration. ");
                instructions.append("Focus on visual design and construction utility. ");
                break;
            case "plant":
                instructions.append("Consider growth stages, biome requirements, and harvest yields. ");
                instructions.append("Ensure balanced farming mechanics and environmental integration. ");
                break;
            default:
                instructions.append("Consider the block's purpose, placement rules, and player interaction. ");
        }
        
        return instructions.toString();
    }
    
    /**
     * Gets recipe generation instructions
     * @param recipeType Type of recipe (crafting, smelting, etc.)
     * @return Customized recipe instructions
     */
    public static String getRecipeInstructions(String recipeType) {
        StringBuilder instructions = new StringBuilder();
        instructions.append(MCREATOR_SYSTEM_PROMPT);
        instructions.append(" Focus on creating balanced ").append(recipeType).append(" recipes. ");
        
        switch (recipeType.toLowerCase()) {
            case "crafting":
                instructions.append("Consider ingredient availability, crafting complexity, and output value. ");
                instructions.append("Ensure logical ingredient combinations and balanced resource costs. ");
                break;
            case "smelting":
                instructions.append("Consider fuel efficiency, processing time, and material transformation. ");
                instructions.append("Ensure realistic smelting processes and balanced resource conversion. ");
                break;
            case "brewing":
                instructions.append("Consider potion effects, brewing time, and ingredient rarity. ");
                instructions.append("Ensure balanced potion mechanics and logical ingredient combinations. ");
                break;
            default:
                instructions.append("Consider resource balance, crafting progression, and recipe complexity. ");
        }
        
        return instructions.toString();
    }
    
    /**
     * Gets Forge 1.20.1 specific instructions
     * @return Forge compatibility instructions
     */
    public static String getForgeInstructions() {
        return "Ensure full compatibility with Forge 1.20.1 mod loader. " +
               "Use Forge API features and follow Forge modding conventions. " +
               "Include proper META-INF/mods.toml configuration and dependencies. " +
               "Use Forge's registry system and lifecycle events. " +
               "Ensure client-server compatibility and proper networking.";
    }
    
    /**
     * Gets agent task assistance instructions
     * @param taskType Type of task the user needs help with
     * @return Customized agent instructions
     */
    public static String getAgentInstructions(String taskType) {
        StringBuilder instructions = new StringBuilder();
        instructions.append(AGENT_SYSTEM_INSTRUCTIONS);
        
        switch (taskType.toLowerCase()) {
            case "beginner":
                instructions.append(" The user is new to MCreator. Provide detailed explanations and basic concepts. ");
                instructions.append("Start with simple examples and gradually introduce complexity. ");
                break;
            case "advanced":
                instructions.append(" The user has MCreator experience. Focus on advanced techniques and optimization. ");
                instructions.append("Provide technical details and assume familiarity with basic concepts. ");
                break;
            case "troubleshooting":
                instructions.append(" Help diagnose and solve MCreator issues. ");
                instructions.append("Ask specific questions to identify problems and provide step-by-step solutions. ");
                break;
            case "planning":
                instructions.append(" Help plan and organize mod development projects. ");
                instructions.append("Break down large projects into manageable tasks and suggest development order. ");
                break;
            default:
                instructions.append(" Adapt your assistance to the user's specific needs and skill level. ");
        }
        
        return instructions.toString();
    }
}

