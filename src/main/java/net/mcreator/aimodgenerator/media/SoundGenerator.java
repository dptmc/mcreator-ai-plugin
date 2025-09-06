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
 * Generator for creating Minecraft sounds using AI
 * Handles sound effects, ambient sounds, and music
 */
public class SoundGenerator {
    
    private final Workspace workspace;
    private final AIIntegrationService aiService;
    private final Path soundsPath;
    
    public SoundGenerator(Workspace workspace, AIIntegrationService aiService) {
        this.workspace = workspace;
        this.aiService = aiService;
        this.soundsPath = Paths.get(workspace.getWorkspaceFolder().getAbsolutePath(), "src", "main", "resources", "assets", workspace.getModName(), "sounds");
        
        // Ensure sound directories exist
        createSoundDirectories();
    }
    
    /**
     * Generates sound effects for an element
     * @param elementName Name of the element
     * @param elementType Type of element (item, block, entity, etc.)
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return Map of sound types to file paths
     */
    public Map<String, String> generateElementSounds(String elementName, String elementType,
                                                   PromptAnalysis analysis, SearchResults searchResults, 
                                                   GenerationOptions options) throws Exception {
        
        Map<String, String> soundPaths = new HashMap<>();
        
        // Step 1: Analyze sound requirements
        SoundProperties properties = analyzeSoundProperties(elementName, elementType, analysis, searchResults, options);
        
        // Step 2: Generate different types of sounds based on element type
        List<String> soundTypes = determineSoundTypes(elementType, properties);
        
        for (String soundType : soundTypes) {
            String soundPath = generateSound(elementName, elementType, soundType, properties, options);
            if (soundPath != null) {
                soundPaths.put(soundType, soundPath);
            }
        }
        
        return soundPaths;
    }
    
    /**
     * Generates ambient sounds for a theme
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return List of generated ambient sound paths
     */
    public List<String> generateAmbientSounds(PromptAnalysis analysis, SearchResults searchResults, 
                                            GenerationOptions options) throws Exception {
        
        List<String> ambientSounds = new ArrayList<>();
        
        // Analyze ambient sound requirements
        SoundProperties properties = analyzeSoundProperties("ambient", "ambient", analysis, searchResults, options);
        
        // Generate different ambient sounds
        String[] ambientTypes = {"background", "nature", "mechanical", "magical"};
        
        for (String ambientType : ambientTypes) {
            if (shouldGenerateAmbientType(ambientType, analysis)) {
                String soundPath = generateSound("ambient_" + ambientType, "ambient", ambientType, properties, options);
                if (soundPath != null) {
                    ambientSounds.add(soundPath);
                }
            }
        }
        
        return ambientSounds;
    }
    
    /**
     * Generates music tracks for the mod
     * @param analysis Prompt analysis containing context
     * @param searchResults Web search results for reference
     * @param options Generation options
     * @return List of generated music track paths
     */
    public List<String> generateMusic(PromptAnalysis analysis, SearchResults searchResults, 
                                    GenerationOptions options) throws Exception {
        
        List<String> musicTracks = new ArrayList<>();
        
        // Analyze music requirements
        SoundProperties properties = analyzeSoundProperties("music", "music", analysis, searchResults, options);
        
        // Generate different music tracks
        String[] musicTypes = {"main_theme", "battle", "exploration", "ambient_music"};
        
        for (String musicType : musicTypes) {
            if (shouldGenerateMusicType(musicType, analysis, options)) {
                String musicPath = generateMusic(musicType, properties, options);
                if (musicPath != null) {
                    musicTracks.add(musicPath);
                }
            }
        }
        
        return musicTracks;
    }
    
    /**
     * Analyzes sound properties using AI
     */
    private SoundProperties analyzeSoundProperties(String elementName, String elementType,
                                                 PromptAnalysis analysis, SearchResults searchResults, 
                                                 GenerationOptions options) throws Exception {
        
        String analysisPrompt = buildSoundAnalysisPrompt(elementName, elementType, analysis, searchResults, options);
        String aiResponse = aiService.generateText(analysisPrompt, 0.6, 400);
        
        return parseSoundProperties(aiResponse, elementName, elementType, analysis);
    }
    
    /**
     * Builds the AI prompt for sound analysis
     */
    private String buildSoundAnalysisPrompt(String elementName, String elementType,
                                          PromptAnalysis analysis, SearchResults searchResults, 
                                          GenerationOptions options) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Analyze sound requirements for Minecraft ").append(elementType).append(": ").append(elementName).append("\n\n");
        
        // Add context
        prompt.append("Theme: ").append(analysis.getTheme()).append("\n");
        prompt.append("Style: ").append(analysis.getStyle()).append("\n");
        prompt.append("Original Request: ").append(analysis.getOriginalPrompt()).append("\n\n");
        
        // Add search context if available
        if (searchResults != null && searchResults.hasResults()) {
            prompt.append("Reference Information:\n");
            prompt.append(searchResults.getCombinedContent().substring(0, Math.min(300, searchResults.getCombinedContent().length())));
            prompt.append("\n\n");
        }
        
        // Specify requirements
        prompt.append("Please provide:\n");
        prompt.append("1. Sound characteristics (pitch, tone, timbre)\n");
        prompt.append("2. Duration (short, medium, long)\n");
        prompt.append("3. Volume level (quiet, normal, loud)\n");
        prompt.append("4. Sound type (mechanical, organic, magical, etc.)\n");
        prompt.append("5. Emotional tone (happy, scary, neutral, etc.)\n");
        prompt.append("6. Frequency of use (rare, common, constant)\n");
        prompt.append("7. Environmental context (indoor, outdoor, underground)\n");
        
        if (elementType.equals("item")) {
            prompt.append("8. Usage sounds (equip, use, break)\n");
        } else if (elementType.equals("block")) {
            prompt.append("8. Interaction sounds (place, break, step)\n");
        } else if (elementType.equals("entity")) {
            prompt.append("8. Behavior sounds (idle, hurt, death)\n");
        }
        
        prompt.append("\nFormat your response with clear labels for each property.");
        
        return prompt.toString();
    }
    
    /**
     * Parses AI response to extract sound properties
     */
    private SoundProperties parseSoundProperties(String aiResponse, String elementName, 
                                               String elementType, PromptAnalysis analysis) {
        SoundProperties properties = new SoundProperties();
        properties.setElementName(elementName);
        properties.setElementType(elementType);
        
        String[] lines = aiResponse.split("\n");
        
        for (String line : lines) {
            String lowerLine = line.trim().toLowerCase();
            
            if (lowerLine.contains("characteristics:") || lowerLine.contains("pitch:")) {
                properties.setSoundCharacteristics(extractValue(line));
            } else if (lowerLine.contains("duration:")) {
                properties.setDuration(extractValue(line));
            } else if (lowerLine.contains("volume:") || lowerLine.contains("level:")) {
                properties.setVolumeLevel(extractValue(line));
            } else if (lowerLine.contains("type:") || lowerLine.contains("sound type:")) {
                properties.setSoundType(extractValue(line));
            } else if (lowerLine.contains("emotional:") || lowerLine.contains("tone:")) {
                properties.setEmotionalTone(extractValue(line));
            } else if (lowerLine.contains("frequency:") || lowerLine.contains("use:")) {
                properties.setFrequencyOfUse(extractValue(line));
            } else if (lowerLine.contains("environment:") || lowerLine.contains("context:")) {
                properties.setEnvironmentalContext(extractValue(line));
            } else if (lowerLine.contains("usage:") || lowerLine.contains("interaction:") || lowerLine.contains("behavior:")) {
                properties.setSpecificSounds(parseSpecificSounds(extractValue(line)));
            }
        }
        
        // Set defaults
        if (properties.getSoundType() == null) {
            properties.setSoundType(inferSoundType(elementName, elementType, analysis));
        }
        
        if (properties.getDuration() == null) {
            properties.setDuration(inferDuration(elementType));
        }
        
        return properties;
    }
    
    /**
     * Determines what types of sounds to generate for an element
     */
    private List<String> determineSoundTypes(String elementType, SoundProperties properties) {
        List<String> soundTypes = new ArrayList<>();
        
        switch (elementType.toLowerCase()) {
            case "item":
                soundTypes.add("use");
                soundTypes.add("equip");
                if (properties.getSoundCharacteristics() != null && 
                    properties.getSoundCharacteristics().toLowerCase().contains("break")) {
                    soundTypes.add("break");
                }
                break;
                
            case "block":
                soundTypes.add("place");
                soundTypes.add("break");
                soundTypes.add("step");
                if (properties.getSoundType() != null && 
                    properties.getSoundType().toLowerCase().contains("mechanical")) {
                    soundTypes.add("ambient");
                }
                break;
                
            case "entity":
                soundTypes.add("idle");
                soundTypes.add("hurt");
                soundTypes.add("death");
                break;
                
            case "ambient":
                soundTypes.add("loop");
                break;
                
            default:
                soundTypes.add("generic");
                break;
        }
        
        return soundTypes;
    }
    
    /**
     * Generates a single sound file
     */
    private String generateSound(String elementName, String elementType, String soundType,
                               SoundProperties properties, GenerationOptions options) throws Exception {
        
        // Build the sound generation prompt
        String soundPrompt = buildSoundGenerationPrompt(elementName, elementType, soundType, properties);
        
        // Determine output path
        String fileName = sanitizeFileName(elementName + "_" + soundType) + ".ogg";
        Path outputPath = soundsPath.resolve(elementType).resolve(fileName);
        
        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());
        
        // Generate the sound using AI service
        try {
            String generatedSoundPath = aiService.generateAudio(soundPrompt, outputPath.toString());
            return generatedSoundPath;
        } catch (Exception e) {
            System.err.println("Failed to generate sound for " + elementName + "_" + soundType + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generates a music track
     */
    private String generateMusic(String musicType, SoundProperties properties, GenerationOptions options) throws Exception {
        
        // Build the music generation prompt
        String musicPrompt = buildMusicGenerationPrompt(musicType, properties);
        
        // Determine output path
        String fileName = sanitizeFileName("music_" + musicType) + ".ogg";
        Path outputPath = soundsPath.resolve("music").resolve(fileName);
        
        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());
        
        // Generate the music using AI service
        try {
            String generatedMusicPath = aiService.generateAudio(musicPrompt, outputPath.toString());
            return generatedMusicPath;
        } catch (Exception e) {
            System.err.println("Failed to generate music for " + musicType + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Builds the sound generation prompt
     */
    private String buildSoundGenerationPrompt(String elementName, String elementType, String soundType, SoundProperties properties) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a Minecraft-style ").append(soundType).append(" sound effect for ").append(elementName).append(". ");
        
        // Add sound characteristics
        if (properties.getSoundCharacteristics() != null) {
            prompt.append("Characteristics: ").append(properties.getSoundCharacteristics()).append(". ");
        }
        
        // Add sound type
        if (properties.getSoundType() != null) {
            prompt.append("Type: ").append(properties.getSoundType()).append(". ");
        }
        
        // Add emotional tone
        if (properties.getEmotionalTone() != null) {
            prompt.append("Tone: ").append(properties.getEmotionalTone()).append(". ");
        }
        
        // Add duration
        if (properties.getDuration() != null) {
            prompt.append("Duration: ").append(properties.getDuration()).append(". ");
        }
        
        // Add volume level
        if (properties.getVolumeLevel() != null) {
            prompt.append("Volume: ").append(properties.getVolumeLevel()).append(". ");
        }
        
        // Add technical requirements
        prompt.append("Format: OGG Vorbis, ");
        prompt.append("suitable for Minecraft modding, ");
        prompt.append("clear audio quality, ");
        prompt.append("appropriate for game sound effects");
        
        return prompt.toString();
    }
    
    /**
     * Builds the music generation prompt
     */
    private String buildMusicGenerationPrompt(String musicType, SoundProperties properties) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Create a Minecraft-style ").append(musicType).append(" music track. ");
        
        // Add emotional tone
        if (properties.getEmotionalTone() != null) {
            prompt.append("Mood: ").append(properties.getEmotionalTone()).append(". ");
        }
        
        // Add specific requirements based on music type
        switch (musicType) {
            case "main_theme":
                prompt.append("This should be memorable and represent the mod's overall theme. ");
                break;
            case "battle":
                prompt.append("This should be intense and energetic for combat situations. ");
                break;
            case "exploration":
                prompt.append("This should be calm and atmospheric for exploring. ");
                break;
            case "ambient_music":
                prompt.append("This should be subtle background music that loops well. ");
                break;
        }
        
        // Add technical requirements
        prompt.append("Format: OGG Vorbis, ");
        prompt.append("loopable, ");
        prompt.append("medium length (2-4 minutes), ");
        prompt.append("suitable for Minecraft background music");
        
        return prompt.toString();
    }
    
    /**
     * Creates necessary sound directories
     */
    private void createSoundDirectories() {
        try {
            Files.createDirectories(soundsPath.resolve("item"));
            Files.createDirectories(soundsPath.resolve("block"));
            Files.createDirectories(soundsPath.resolve("entity"));
            Files.createDirectories(soundsPath.resolve("ambient"));
            Files.createDirectories(soundsPath.resolve("music"));
        } catch (IOException e) {
            System.err.println("Failed to create sound directories: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a specific ambient sound type should be generated
     */
    private boolean shouldGenerateAmbientType(String ambientType, PromptAnalysis analysis) {
        String theme = analysis.getTheme();
        if (theme == null) return false;
        
        String lowerTheme = theme.toLowerCase();
        
        switch (ambientType) {
            case "nature":
                return lowerTheme.contains("nature") || lowerTheme.contains("forest") || lowerTheme.contains("garden");
            case "mechanical":
                return lowerTheme.contains("tech") || lowerTheme.contains("machine") || lowerTheme.contains("industrial");
            case "magical":
                return lowerTheme.contains("magic") || lowerTheme.contains("mystical") || lowerTheme.contains("enchant");
            case "background":
                return true; // Always generate background ambient
            default:
                return false;
        }
    }
    
    /**
     * Checks if a specific music type should be generated
     */
    private boolean shouldGenerateMusicType(String musicType, PromptAnalysis analysis, GenerationOptions options) {
        if (!options.isGenerateMusic()) {
            return false;
        }
        
        // Always generate main theme
        if (musicType.equals("main_theme")) {
            return true;
        }
        
        String theme = analysis.getTheme();
        if (theme == null) return false;
        
        String lowerTheme = theme.toLowerCase();
        
        switch (musicType) {
            case "battle":
                return lowerTheme.contains("combat") || lowerTheme.contains("war") || lowerTheme.contains("battle");
            case "exploration":
                return lowerTheme.contains("adventure") || lowerTheme.contains("explore") || lowerTheme.contains("journey");
            case "ambient_music":
                return true; // Usually generate ambient music
            default:
                return false;
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
    
    private List<String> parseSpecificSounds(String soundsText) {
        List<String> sounds = new ArrayList<>();
        if (soundsText != null && !soundsText.isEmpty()) {
            String[] parts = soundsText.split("[,;]");
            for (String part : parts) {
                String sound = part.trim();
                if (!sound.isEmpty()) {
                    sounds.add(sound);
                }
            }
        }
        return sounds;
    }
    
    private String inferSoundType(String elementName, String elementType, PromptAnalysis analysis) {
        String lowerName = elementName.toLowerCase();
        String theme = analysis.getTheme();
        
        if (lowerName.contains("metal") || lowerName.contains("iron") || lowerName.contains("steel")) {
            return "metallic";
        } else if (lowerName.contains("wood") || lowerName.contains("tree")) {
            return "organic";
        } else if (lowerName.contains("magic") || lowerName.contains("enchant")) {
            return "magical";
        } else if (theme != null && theme.toLowerCase().contains("tech")) {
            return "mechanical";
        }
        
        return "generic";
    }
    
    private String inferDuration(String elementType) {
        switch (elementType.toLowerCase()) {
            case "item":
                return "short";
            case "block":
                return "short";
            case "entity":
                return "medium";
            case "ambient":
                return "long";
            case "music":
                return "long";
            default:
                return "short";
        }
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.toLowerCase()
                      .replaceAll("[^a-z0-9_]", "_")
                      .replaceAll("_{2,}", "_")
                      .replaceAll("^_|_$", "");
    }
    
    /**
     * Data class for sound properties
     */
    public static class SoundProperties {
        private String elementName;
        private String elementType;
        private String soundCharacteristics;
        private String duration;
        private String volumeLevel;
        private String soundType;
        private String emotionalTone;
        private String frequencyOfUse;
        private String environmentalContext;
        private List<String> specificSounds = new ArrayList<>();
        
        // Getters and setters
        public String getElementName() { return elementName; }
        public void setElementName(String elementName) { this.elementName = elementName; }
        
        public String getElementType() { return elementType; }
        public void setElementType(String elementType) { this.elementType = elementType; }
        
        public String getSoundCharacteristics() { return soundCharacteristics; }
        public void setSoundCharacteristics(String soundCharacteristics) { this.soundCharacteristics = soundCharacteristics; }
        
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        
        public String getVolumeLevel() { return volumeLevel; }
        public void setVolumeLevel(String volumeLevel) { this.volumeLevel = volumeLevel; }
        
        public String getSoundType() { return soundType; }
        public void setSoundType(String soundType) { this.soundType = soundType; }
        
        public String getEmotionalTone() { return emotionalTone; }
        public void setEmotionalTone(String emotionalTone) { this.emotionalTone = emotionalTone; }
        
        public String getFrequencyOfUse() { return frequencyOfUse; }
        public void setFrequencyOfUse(String frequencyOfUse) { this.frequencyOfUse = frequencyOfUse; }
        
        public String getEnvironmentalContext() { return environmentalContext; }
        public void setEnvironmentalContext(String environmentalContext) { this.environmentalContext = environmentalContext; }
        
        public List<String> getSpecificSounds() { return specificSounds; }
        public void setSpecificSounds(List<String> specificSounds) { this.specificSounds = specificSounds; }
    }
}

