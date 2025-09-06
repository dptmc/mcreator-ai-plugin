package net.mcreator.aimodgenerator.generators;

import net.mcreator.workspace.Workspace;
import net.mcreator.element.ModElementType;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.types.Procedure;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.aimodgenerator.ai.AIIntegrationService;
import net.mcreator.aimodgenerator.core.PromptAnalysis;
import net.mcreator.aimodgenerator.core.GenerationOptions;
import net.mcreator.aimodgenerator.search.SearchResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator for creating Minecraft procedures using AI
 * Handles custom logic, events, and behaviors for mod elements
 */
public class ProcedureGenerator extends BaseGenerator {
    
    public ProcedureGenerator(Workspace workspace, AIIntegrationService aiService) {
        super(workspace, aiService);
    }
    
    /**
     * Generates a new procedure based on the provided parameters
     * @param procedureName Name of the procedure to generate
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return Details about the generated procedure
     */
    public String generateProcedure(String procedureName, PromptAnalysis analysis, 
                                  SearchResults searchResults, GenerationOptions options) throws Exception {
        
        // Step 1: Determine procedure properties using AI
        ProcedureProperties properties = analyzeProcedureProperties(procedureName, analysis, searchResults, options);
        
        // Step 2: Create the MCreator mod element
        ModElement modElement = createProcedureModElement(procedureName, properties);
        
        // Step 3: Configure the procedure element
        Procedure procedureElement = configureProcedureElement(modElement, properties, options);
        
        // Step 4: Add to workspace
        workspace.addModElement(modElement);
        workspace.getModElementManager().storeModElement(procedureElement);
        
        // Step 5: Generate additional content
        String additionalContent = generateAdditionalProcedureContent(procedureName, properties, options);
        
        return formatProcedureDetails(procedureName, properties, additionalContent);
    }
    
    /**
     * Analyzes procedure properties using AI and search results
     */
    private ProcedureProperties analyzeProcedureProperties(String procedureName, PromptAnalysis analysis, 
                                                         SearchResults searchResults, GenerationOptions options) throws Exception {
        
        String analysisPrompt = buildProcedureAnalysisPrompt(procedureName, analysis, searchResults, options);
        String aiResponse = aiService.generateMinecraftContent(analysisPrompt, "procedure");
        
        return parseProcedureProperties(aiResponse, procedureName, analysis);
    }
    
    /**
     * Builds the AI prompt for procedure analysis
     */
    private String buildProcedureAnalysisPrompt(String procedureName, PromptAnalysis analysis, 
                                              SearchResults searchResults, GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a detailed MCreator procedure specification for: ").append(procedureName).append("\n\n");
        
        // Add context from original prompt
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n");
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Style: ").append(analysis.getStyle()).append("\n\n");
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(400, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify what we need
        prompt.append("Please provide:\n");
        prompt.append("1. Procedure type (EVENT, FUNCTION, COMMAND, etc.)\n");
        prompt.append("2. Trigger event (when this procedure should run)\n");
        prompt.append("3. Input parameters (what data the procedure needs)\n");
        prompt.append("4. Return type (what the procedure returns, if anything)\n");
        prompt.append("5. Logic description (step-by-step what the procedure does)\n");
        prompt.append("6. Required dependencies (other mod elements needed)\n");
        prompt.append("7. Performance considerations (how often it runs)\n");
        prompt.append("8. Side effects (what changes it makes to the world/player)\n");
        prompt.append("9. Error handling (what happens if something goes wrong)\n");
        prompt.append("10. MCreator blockly blocks needed (specific blocks to use)\n\n");
        
        // Add implementation guidance
        prompt.append("Focus on MCreator's visual programming system (Blockly). ");
        prompt.append("Describe the logic in terms of MCreator blocks like:\n");
        prompt.append("- If/else statements\n");
        prompt.append("- Variable operations\n");
        prompt.append("- Entity/player actions\n");
        prompt.append("- World interactions\n");
        prompt.append("- Math operations\n");
        prompt.append("- Item/block operations\n\n");
        
        prompt.append("Format your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to extract procedure properties
     */
    private ProcedureProperties parseProcedureProperties(String aiResponse, String procedureName, PromptAnalysis analysis) {
        ProcedureProperties properties = new ProcedureProperties();
        properties.setName(procedureName);
        
        // Parse AI response
        String[] lines = aiResponse.split("\n");
        StringBuilder logicBuilder = new StringBuilder();
        boolean inLogic = false;
        
        for (String line : lines) {
            String lowerLine = line.trim().toLowerCase();
            
            if (lowerLine.contains("type:") || lowerLine.contains("procedure type:")) {
                properties.setProcedureType(extractValue(line));
            } else if (lowerLine.contains("trigger:") || lowerLine.contains("event:")) {
                properties.setTriggerEvent(extractValue(line));
            } else if (lowerLine.contains("parameters:") || lowerLine.contains("input:")) {
                properties.setInputParameters(parseParameters(extractValue(line)));
            } else if (lowerLine.contains("return:") || lowerLine.contains("return type:")) {
                properties.setReturnType(extractValue(line));
            } else if (lowerLine.contains("logic:") || lowerLine.contains("description:")) {
                inLogic = true;
                String logicStart = extractValue(line);
                if (!logicStart.isEmpty()) {
                    logicBuilder.append(logicStart).append("\n");
                }
            } else if (lowerLine.contains("dependencies:") || lowerLine.contains("required:")) {
                properties.setDependencies(parseDependencies(extractValue(line)));
                inLogic = false;
            } else if (lowerLine.contains("performance:")) {
                properties.setPerformanceNotes(extractValue(line));
                inLogic = false;
            } else if (lowerLine.contains("side effects:") || lowerLine.contains("effects:")) {
                properties.setSideEffects(extractValue(line));
                inLogic = false;
            } else if (lowerLine.contains("error:") || lowerLine.contains("handling:")) {
                properties.setErrorHandling(extractValue(line));
                inLogic = false;
            } else if (lowerLine.contains("blocks:") || lowerLine.contains("blockly:")) {
                properties.setBlocklyBlocks(parseBlocklyBlocks(extractValue(line)));
                inLogic = false;
            } else if (inLogic && !line.trim().isEmpty()) {
                logicBuilder.append(line.trim()).append("\n");
            }
        }
        
        properties.setLogicDescription(logicBuilder.toString().trim());
        
        // Set defaults based on analysis if not specified
        if (properties.getProcedureType() == null) {
            properties.setProcedureType(inferProcedureType(procedureName, analysis));
        }
        
        if (properties.getTriggerEvent() == null) {
            properties.setTriggerEvent(inferTriggerEvent(procedureName, analysis));
        }
        
        return properties;
    }
    
    /**
     * Parses parameters from text
     */
    private List<String> parseParameters(String parametersText) {
        List<String> parameters = new ArrayList<>();
        
        if (parametersText == null || parametersText.isEmpty()) {
            return parameters;
        }
        
        String[] parts = parametersText.split("[,;]");
        for (String part : parts) {
            String param = part.trim();
            if (!param.isEmpty()) {
                parameters.add(param);
            }
        }
        
        return parameters;
    }
    
    /**
     * Parses dependencies from text
     */
    private List<String> parseDependencies(String dependenciesText) {
        List<String> dependencies = new ArrayList<>();
        
        if (dependenciesText == null || dependenciesText.isEmpty()) {
            return dependencies;
        }
        
        String[] parts = dependenciesText.split("[,;]");
        for (String part : parts) {
            String dependency = part.trim();
            if (!dependency.isEmpty()) {
                dependencies.add(dependency);
            }
        }
        
        return dependencies;
    }
    
    /**
     * Parses Blockly blocks from text
     */
    private List<String> parseBlocklyBlocks(String blocksText) {
        List<String> blocks = new ArrayList<>();
        
        if (blocksText == null || blocksText.isEmpty()) {
            return blocks;
        }
        
        String[] parts = blocksText.split("[,;\\n]");
        for (String part : parts) {
            String block = part.trim();
            if (!block.isEmpty() && !block.startsWith("-")) {
                // Remove bullet points and clean up
                block = block.replaceAll("^[-*•]\\s*", "");
                if (!block.isEmpty()) {
                    blocks.add(block);
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Creates a new MCreator mod element for the procedure
     */
    private ModElement createProcedureModElement(String procedureName, ProcedureProperties properties) {
        String elementName = sanitizeElementName(procedureName);
        ModElement modElement = new ModElement(workspace, elementName, ModElementType.PROCEDURE);
        modElement.setRegistryName(createRegistryName(procedureName));
        return modElement;
    }
    
    /**
     * Configures the MCreator procedure element with generated properties
     */
    private Procedure configureProcedureElement(ModElement modElement, ProcedureProperties properties, GenerationOptions options) {
        Procedure procedure = new Procedure(modElement);
        
        // Basic properties
        procedure.name = properties.getName();
        
        // Set return type
        if (properties.getReturnType() != null && !properties.getReturnType().isEmpty()) {
            procedure.returnType = mapReturnTypeToMCreator(properties.getReturnType());
        } else {
            procedure.returnType = "void";
        }
        
        // Configure triggers based on procedure type
        configureProcedureTriggers(procedure, properties);
        
        // Generate the actual procedure logic
        generateProcedureLogic(procedure, properties);
        
        return procedure;
    }
    
    /**
     * Configures procedure triggers based on type and event
     */
    private void configureProcedureTriggers(Procedure procedure, ProcedureProperties properties) {
        String triggerEvent = properties.getTriggerEvent();
        
        if (triggerEvent == null || triggerEvent.isEmpty()) {
            return;
        }
        
        String lowerTrigger = triggerEvent.toLowerCase();
        
        // Map common trigger events to MCreator triggers
        if (lowerTrigger.contains("player") && lowerTrigger.contains("tick")) {
            // Player tick trigger
            procedure.triggers.add("player_tick");
        } else if (lowerTrigger.contains("world") && lowerTrigger.contains("tick")) {
            // World tick trigger
            procedure.triggers.add("world_tick");
        } else if (lowerTrigger.contains("right") && lowerTrigger.contains("click")) {
            // Right click trigger
            procedure.triggers.add("item_right_clicked");
        } else if (lowerTrigger.contains("left") && lowerTrigger.contains("click")) {
            // Left click trigger
            procedure.triggers.add("item_left_clicked");
        } else if (lowerTrigger.contains("block") && lowerTrigger.contains("break")) {
            // Block break trigger
            procedure.triggers.add("block_broken");
        } else if (lowerTrigger.contains("block") && lowerTrigger.contains("place")) {
            // Block place trigger
            procedure.triggers.add("block_placed");
        } else if (lowerTrigger.contains("entity") && lowerTrigger.contains("hurt")) {
            // Entity hurt trigger
            procedure.triggers.add("entity_hurt");
        } else if (lowerTrigger.contains("player") && lowerTrigger.contains("join")) {
            // Player join trigger
            procedure.triggers.add("player_joins");
        } else if (lowerTrigger.contains("player") && lowerTrigger.contains("leave")) {
            // Player leave trigger
            procedure.triggers.add("player_leaves");
        }
    }
    
    /**
     * Generates the actual procedure logic using Blockly
     */
    private void generateProcedureLogic(Procedure procedure, ProcedureProperties properties) {
        // This would generate the actual Blockly XML for the procedure
        // For now, we'll create a simple structure
        
        StringBuilder blocklyXml = new StringBuilder();
        blocklyXml.append("<xml xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        
        // Add procedure start block
        blocklyXml.append("  <block type=\"event_trigger\">\n");
        
        // Add logic blocks based on description
        String logic = properties.getLogicDescription();
        if (logic != null && !logic.isEmpty()) {
            addLogicBlocks(blocklyXml, logic, properties);
        }
        
        blocklyXml.append("  </block>\n");
        blocklyXml.append("</xml>");
        
        // In a real implementation, this would set the procedure's Blockly XML
        // procedure.blocklyXML = blocklyXml.toString();
    }
    
    /**
     * Adds logic blocks to the Blockly XML based on description
     */
    private void addLogicBlocks(StringBuilder xml, String logic, ProcedureProperties properties) {
        String[] logicLines = logic.split("\n");
        
        for (String line : logicLines) {
            String lowerLine = line.toLowerCase().trim();
            
            if (lowerLine.contains("if") || lowerLine.contains("check")) {
                xml.append("    <block type=\"controls_if\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("set") || lowerLine.contains("variable")) {
                xml.append("    <block type=\"variables_set\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("damage") || lowerLine.contains("hurt")) {
                xml.append("    <block type=\"entity_deal_damage\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("heal") || lowerLine.contains("health")) {
                xml.append("    <block type=\"entity_set_health\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("teleport") || lowerLine.contains("move")) {
                xml.append("    <block type=\"entity_set_position\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("give") || lowerLine.contains("item")) {
                xml.append("    <block type=\"player_give_item\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("message") || lowerLine.contains("chat")) {
                xml.append("    <block type=\"player_send_chat\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("sound") || lowerLine.contains("play")) {
                xml.append("    <block type=\"world_play_sound\">\n");
                xml.append("    </block>\n");
            } else if (lowerLine.contains("particle") || lowerLine.contains("effect")) {
                xml.append("    <block type=\"world_spawn_particle\">\n");
                xml.append("    </block>\n");
            }
        }
    }
    
    /**
     * Maps return type string to MCreator return type
     */
    private String mapReturnTypeToMCreator(String returnType) {
        if (returnType == null) return "void";
        
        String lowerType = returnType.toLowerCase();
        
        if (lowerType.contains("number") || lowerType.contains("int")) return "number";
        if (lowerType.contains("string") || lowerType.contains("text")) return "string";
        if (lowerType.contains("boolean") || lowerType.contains("bool")) return "logic";
        if (lowerType.contains("item")) return "itemstack";
        if (lowerType.contains("block")) return "blockstate";
        if (lowerType.contains("entity")) return "entity";
        
        return "void";
    }
    
    /**
     * Generates additional content for the procedure
     */
    private String generateAdditionalProcedureContent(String procedureName, ProcedureProperties properties, GenerationOptions options) throws Exception {
        StringBuilder content = new StringBuilder();
        
        // Generate implementation guide
        String implementationPrompt = "Provide a step-by-step guide for implementing the " + procedureName + 
                                    " procedure in MCreator's visual editor. Include specific Blockly blocks to use.";
        String implementation = aiService.generateText(implementationPrompt, 0.3, 400);
        content.append("Implementation Guide:\n").append(implementation).append("\n\n");
        
        // Generate testing suggestions
        String testingPrompt = "Suggest ways to test the " + procedureName + " procedure in Minecraft. " +
                             "Include test scenarios and expected outcomes.";
        String testing = aiService.generateText(testingPrompt, 0.4, 200);
        content.append("Testing Suggestions:\n").append(testing).append("\n\n");
        
        return content.toString();
    }
    
    /**
     * Formats the procedure details for display
     */
    private String formatProcedureDetails(String procedureName, ProcedureProperties properties, String additionalContent) {
        StringBuilder details = new StringBuilder();
        
        details.append("Procedure: ").append(procedureName).append("\n");
        details.append("Type: ").append(properties.getProcedureType()).append("\n");
        details.append("Trigger Event: ").append(properties.getTriggerEvent()).append("\n");
        details.append("Return Type: ").append(properties.getReturnType()).append("\n");
        
        if (!properties.getInputParameters().isEmpty()) {
            details.append("Parameters: ").append(String.join(", ", properties.getInputParameters())).append("\n");
        }
        
        details.append("\nLogic Description:\n").append(properties.getLogicDescription()).append("\n");
        
        if (!properties.getDependencies().isEmpty()) {
            details.append("\nDependencies: ").append(String.join(", ", properties.getDependencies())).append("\n");
        }
        
        if (!properties.getBlocklyBlocks().isEmpty()) {
            details.append("\nRequired Blocks: ").append(String.join(", ", properties.getBlocklyBlocks())).append("\n");
        }
        
        if (properties.getPerformanceNotes() != null && !properties.getPerformanceNotes().isEmpty()) {
            details.append("\nPerformance Notes: ").append(properties.getPerformanceNotes()).append("\n");
        }
        
        if (properties.getSideEffects() != null && !properties.getSideEffects().isEmpty()) {
            details.append("Side Effects: ").append(properties.getSideEffects()).append("\n");
        }
        
        details.append("\n").append(additionalContent);
        
        return details.toString();
    }
    
    /**
     * Infers procedure type from name and analysis
     */
    private String inferProcedureType(String procedureName, PromptAnalysis analysis) {
        String lowerName = procedureName.toLowerCase();
        
        if (lowerName.contains("event") || lowerName.contains("trigger")) return "EVENT";
        if (lowerName.contains("function") || lowerName.contains("calculate")) return "FUNCTION";
        if (lowerName.contains("command")) return "COMMAND";
        if (lowerName.contains("tick") || lowerName.contains("update")) return "EVENT";
        
        return "FUNCTION";
    }
    
    /**
     * Infers trigger event from name and analysis
     */
    private String inferTriggerEvent(String procedureName, PromptAnalysis analysis) {
        String lowerName = procedureName.toLowerCase();
        
        if (lowerName.contains("tick")) return "player tick";
        if (lowerName.contains("click")) return "right click";
        if (lowerName.contains("break")) return "block break";
        if (lowerName.contains("place")) return "block place";
        if (lowerName.contains("hurt") || lowerName.contains("damage")) return "entity hurt";
        if (lowerName.contains("join")) return "player join";
        if (lowerName.contains("leave")) return "player leave";
        
        return "manual trigger";
    }
    
    /**
     * Data class for procedure properties
     */
    public static class ProcedureProperties {
        private String name;
        private String procedureType;
        private String triggerEvent;
        private List<String> inputParameters = new ArrayList<>();
        private String returnType = "void";
        private String logicDescription;
        private List<String> dependencies = new ArrayList<>();
        private String performanceNotes;
        private String sideEffects;
        private String errorHandling;
        private List<String> blocklyBlocks = new ArrayList<>();
        private Map<String, Object> customProperties = new HashMap<>();
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getProcedureType() { return procedureType; }
        public void setProcedureType(String procedureType) { this.procedureType = procedureType; }
        
        public String getTriggerEvent() { return triggerEvent; }
        public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }
        
        public List<String> getInputParameters() { return inputParameters; }
        public void setInputParameters(List<String> inputParameters) { this.inputParameters = inputParameters; }
        
        public String getReturnType() { return returnType; }
        public void setReturnType(String returnType) { this.returnType = returnType; }
        
        public String getLogicDescription() { return logicDescription; }
        public void setLogicDescription(String logicDescription) { this.logicDescription = logicDescription; }
        
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
        
        public String getPerformanceNotes() { return performanceNotes; }
        public void setPerformanceNotes(String performanceNotes) { this.performanceNotes = performanceNotes; }
        
        public String getSideEffects() { return sideEffects; }
        public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }
        
        public String getErrorHandling() { return errorHandling; }
        public void setErrorHandling(String errorHandling) { this.errorHandling = errorHandling; }
        
        public List<String> getBlocklyBlocks() { return blocklyBlocks; }
        public void setBlocklyBlocks(List<String> blocklyBlocks) { this.blocklyBlocks = blocklyBlocks; }
        
        public Map<String, Object> getCustomProperties() { return customProperties; }
        public void setCustomProperties(Map<String, Object> customProperties) { this.customProperties = customProperties; }
    }
}

