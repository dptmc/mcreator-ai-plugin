package net.mcreator.aimodgenerator.search;

import net.mcreator.aimodgenerator.core.PromptAnalysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for performing web searches to gather information for mod generation
 * Searches for Minecraft-related content, existing mods, and real-world references
 */
public class WebSearchService {
    
    // For demonstration purposes, we'll use a simple search approach
    // In a real implementation, this would use Google Search API or similar
    
    /**
     * Searches for information relevant to the user's prompt
     * @param prompt The original user prompt
     * @param analysis The analyzed prompt data
     * @return Search results containing relevant information
     */
    public SearchResults searchForPrompt(String prompt, PromptAnalysis analysis) {
        SearchResults results = new SearchResults();
        
        try {
            // Search for general Minecraft mod information
            List<SearchResult> minecraftResults = searchMinecraftWiki(prompt, analysis);
            results.addResults(minecraftResults);
            
            // Search for existing mod examples
            List<SearchResult> modResults = searchExistingMods(analysis);
            results.addResults(modResults);
            
            // Search for real-world references if applicable
            List<SearchResult> realWorldResults = searchRealWorldReferences(analysis);
            results.addResults(realWorldResults);
            
        } catch (Exception e) {
            System.err.println("Web search failed: " + e.getMessage());
            // Return empty results on failure
        }
        
        return results;
    }
    
    /**
     * Searches the Minecraft Wiki for relevant information
     */
    private List<SearchResult> searchMinecraftWiki(String prompt, PromptAnalysis analysis) {
        List<SearchResult> results = new ArrayList<>();
        
        // Create search queries based on the analysis
        List<String> queries = new ArrayList<>();
        
        // Add item-related queries
        for (String itemName : analysis.getItemNames()) {
            queries.add("minecraft " + itemName.toLowerCase() + " wiki");
        }
        
        // Add block-related queries
        for (String blockName : analysis.getBlockNames()) {
            queries.add("minecraft " + blockName.toLowerCase() + " wiki");
        }
        
        // Add general theme queries
        if (analysis.getTheme() != null) {
            queries.add("minecraft " + analysis.getTheme().toLowerCase() + " items blocks");
        }
        
        // Perform searches (simplified for demonstration)
        for (String query : queries) {
            try {
                SearchResult result = performSimpleSearch(query, "minecraft.wiki");
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                // Continue with other queries if one fails
                System.err.println("Search failed for query: " + query);
            }
        }
        
        return results;
    }
    
    /**
     * Searches for existing mods with similar functionality
     */
    private List<SearchResult> searchExistingMods(PromptAnalysis analysis) {
        List<SearchResult> results = new ArrayList<>();
        
        // Search CurseForge and similar sites for existing mods
        List<String> modQueries = new ArrayList<>();
        
        if (analysis.hasItems()) {
            modQueries.add("minecraft mod " + String.join(" ", analysis.getItemNames()));
        }
        
        if (analysis.hasBlocks()) {
            modQueries.add("minecraft mod " + String.join(" ", analysis.getBlockNames()));
        }
        
        if (analysis.getTheme() != null) {
            modQueries.add("minecraft " + analysis.getTheme().toLowerCase() + " mod curseforge");
        }
        
        for (String query : modQueries) {
            try {
                SearchResult result = performSimpleSearch(query, "curseforge.com");
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                System.err.println("Mod search failed for query: " + query);
            }
        }
        
        return results;
    }
    
    /**
     * Searches for real-world references and inspiration
     */
    private List<SearchResult> searchRealWorldReferences(PromptAnalysis analysis) {
        List<SearchResult> results = new ArrayList<>();
        
        // Search for real-world items that might inspire the mod elements
        List<String> realWorldQueries = new ArrayList<>();
        
        for (String itemName : analysis.getItemNames()) {
            // Extract the base concept (e.g., "sword" from "magic sword")
            String[] words = itemName.toLowerCase().split("\\s+");
            for (String word : words) {
                if (isRealWorldConcept(word)) {
                    realWorldQueries.add(word + " history properties");
                }
            }
        }
        
        for (String query : realWorldQueries) {
            try {
                SearchResult result = performSimpleSearch(query, "wikipedia.org");
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                System.err.println("Real-world search failed for query: " + query);
            }
        }
        
        return results;
    }
    
    /**
     * Performs a simple web search (demonstration implementation)
     * In a real implementation, this would use a proper search API
     */
    private SearchResult performSimpleSearch(String query, String preferredSite) throws Exception {
        // This is a simplified demonstration
        // In reality, you would use Google Search API, Bing API, etc.
        
        String encodedQuery = URLEncoder.encode(query + " site:" + preferredSite, StandardCharsets.UTF_8);
        String searchUrl = "https://www.google.com/search?q=" + encodedQuery;
        
        // For demonstration, return a mock result
        SearchResult result = new SearchResult();
        result.setTitle("Search result for: " + query);
        result.setUrl(searchUrl);
        result.setSnippet("This would contain relevant information about " + query + " from " + preferredSite);
        result.setSource(preferredSite);
        
        return result;
    }
    
    /**
     * Checks if a word represents a real-world concept worth researching
     */
    private boolean isRealWorldConcept(String word) {
        String[] realWorldConcepts = {
            "sword", "axe", "bow", "shield", "armor", "helmet", "spear", "dagger",
            "gold", "silver", "iron", "copper", "diamond", "emerald", "ruby",
            "fire", "ice", "lightning", "earth", "water", "air",
            "magic", "spell", "enchantment", "potion", "crystal"
        };
        
        for (String concept : realWorldConcepts) {
            if (word.contains(concept)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Extracts text content from a web page
     * This would be used to get detailed information from search results
     */
    public String extractPageContent(String url) {
        try {
            URL pageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "MCreator AI Plugin");
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            // Simple HTML tag removal (in reality, use a proper HTML parser)
            String text = content.toString().replaceAll("<[^>]+>", "");
            
            // Return first 1000 characters
            return text.length() > 1000 ? text.substring(0, 1000) + "..." : text;
            
        } catch (Exception e) {
            return "Could not extract content from: " + url;
        }
    }
}

