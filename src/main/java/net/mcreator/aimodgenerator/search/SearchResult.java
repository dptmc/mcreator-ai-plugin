package net.mcreator.aimodgenerator.search;

/**
 * Represents a single search result from web search
 * Contains title, URL, snippet, and metadata
 */
public class SearchResult {
    
    private String title;
    private String url;
    private String snippet;
    private String source;
    private double relevanceScore;
    private long timestamp;
    
    public SearchResult() {
        this.timestamp = System.currentTimeMillis();
        this.relevanceScore = 0.0;
    }
    
    public SearchResult(String title, String url, String snippet) {
        this();
        this.title = title;
        this.url = url;
        this.snippet = snippet;
        this.source = extractSourceFromUrl(url);
    }
    
    public SearchResult(String title, String url, String snippet, String source) {
        this();
        this.title = title;
        this.url = url;
        this.snippet = snippet;
        this.source = source;
    }
    
    /**
     * Extracts the source domain from a URL
     */
    private String extractSourceFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "Unknown";
        }
        
        try {
            // Remove protocol
            String domain = url.replaceAll("^https?://", "");
            
            // Remove www prefix
            domain = domain.replaceAll("^www\\.", "");
            
            // Extract domain name (before first slash)
            int slashIndex = domain.indexOf('/');
            if (slashIndex > 0) {
                domain = domain.substring(0, slashIndex);
            }
            
            return domain;
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Calculates relevance score based on various factors
     */
    public void calculateRelevanceScore(String query) {
        if (query == null || query.isEmpty()) {
            this.relevanceScore = 0.5; // Default score
            return;
        }
        
        double score = 0.0;
        String lowerQuery = query.toLowerCase();
        String lowerTitle = (title != null ? title.toLowerCase() : "");
        String lowerSnippet = (snippet != null ? snippet.toLowerCase() : "");
        
        // Title relevance (weighted heavily)
        if (lowerTitle.contains(lowerQuery)) {
            score += 0.4;
        } else {
            // Check for individual query words in title
            String[] queryWords = lowerQuery.split("\\s+");
            int titleMatches = 0;
            for (String word : queryWords) {
                if (lowerTitle.contains(word)) {
                    titleMatches++;
                }
            }
            score += (titleMatches / (double) queryWords.length) * 0.3;
        }
        
        // Snippet relevance
        if (lowerSnippet.contains(lowerQuery)) {
            score += 0.3;
        } else {
            // Check for individual query words in snippet
            String[] queryWords = lowerQuery.split("\\s+");
            int snippetMatches = 0;
            for (String word : queryWords) {
                if (lowerSnippet.contains(word)) {
                    snippetMatches++;
                }
            }
            score += (snippetMatches / (double) queryWords.length) * 0.2;
        }
        
        // Source reliability bonus
        if (source != null) {
            String lowerSource = source.toLowerCase();
            if (lowerSource.contains("minecraft.wiki") || lowerSource.contains("minecraft.fandom")) {
                score += 0.2;
            } else if (lowerSource.contains("curseforge") || lowerSource.contains("modrinth")) {
                score += 0.15;
            } else if (lowerSource.contains("wikipedia")) {
                score += 0.1;
            }
        }
        
        this.relevanceScore = Math.min(1.0, score); // Cap at 1.0
    }
    
    /**
     * Checks if this result is relevant to Minecraft modding
     */
    public boolean isMinecraftRelevant() {
        if (title == null && snippet == null) {
            return false;
        }
        
        String content = ((title != null ? title : "") + " " + (snippet != null ? snippet : "")).toLowerCase();
        
        String[] minecraftKeywords = {
            "minecraft", "mod", "block", "item", "recipe", "enchantment",
            "forge", "fabric", "mcreator", "bukkit", "spigot", "plugin"
        };
        
        for (String keyword : minecraftKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a shortened version of the snippet
     */
    public String getShortSnippet(int maxLength) {
        if (snippet == null) {
            return "";
        }
        
        if (snippet.length() <= maxLength) {
            return snippet;
        }
        
        return snippet.substring(0, maxLength - 3) + "...";
    }
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
        if (this.source == null || this.source.equals("Unknown")) {
            this.source = extractSourceFromUrl(url);
        }
    }
    
    public String getSnippet() {
        return snippet;
    }
    
    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public double getRelevanceScore() {
        return relevanceScore;
    }
    
    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = Math.max(0.0, Math.min(1.0, relevanceScore));
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "SearchResult{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", source='" + source + '\'' +
                ", relevanceScore=" + relevanceScore +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SearchResult that = (SearchResult) obj;
        return url != null ? url.equals(that.url) : that.url == null;
    }
    
    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}

