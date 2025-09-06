package net.mcreator.aimodgenerator.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for web search results
 * Stores and organizes search results from various sources
 */
public class SearchResults {
    
    private List<SearchResult> results;
    private String query;
    private long searchTime;
    
    public SearchResults() {
        this.results = new ArrayList<>();
        this.searchTime = System.currentTimeMillis();
    }
    
    public SearchResults(String query) {
        this();
        this.query = query;
    }
    
    /**
     * Adds a single search result
     */
    public void addResult(SearchResult result) {
        if (result != null) {
            results.add(result);
        }
    }
    
    /**
     * Adds multiple search results
     */
    public void addResults(List<SearchResult> newResults) {
        if (newResults != null) {
            results.addAll(newResults);
        }
    }
    
    /**
     * Gets all search results
     */
    public List<SearchResult> getResults() {
        return new ArrayList<>(results);
    }
    
    /**
     * Gets results from a specific source
     */
    public List<SearchResult> getResultsFromSource(String source) {
        List<SearchResult> sourceResults = new ArrayList<>();
        for (SearchResult result : results) {
            if (source.equals(result.getSource())) {
                sourceResults.add(result);
            }
        }
        return sourceResults;
    }
    
    /**
     * Gets the number of results
     */
    public int getResultCount() {
        return results.size();
    }
    
    /**
     * Checks if there are any results
     */
    public boolean hasResults() {
        return !results.isEmpty();
    }
    
    /**
     * Gets the top N results
     */
    public List<SearchResult> getTopResults(int count) {
        int limit = Math.min(count, results.size());
        return new ArrayList<>(results.subList(0, limit));
    }
    
    /**
     * Filters results by relevance score
     */
    public List<SearchResult> getRelevantResults(double minScore) {
        List<SearchResult> relevantResults = new ArrayList<>();
        for (SearchResult result : results) {
            if (result.getRelevanceScore() >= minScore) {
                relevantResults.add(result);
            }
        }
        return relevantResults;
    }
    
    /**
     * Gets a summary of all search results
     */
    public String getSummary() {
        if (results.isEmpty()) {
            return "No search results found.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Found ").append(results.size()).append(" search results");
        
        if (query != null) {
            summary.append(" for query: ").append(query);
        }
        
        summary.append("\n\nTop results:\n");
        
        List<SearchResult> topResults = getTopResults(5);
        for (int i = 0; i < topResults.size(); i++) {
            SearchResult result = topResults.get(i);
            summary.append(i + 1).append(". ").append(result.getTitle())
                   .append(" (").append(result.getSource()).append(")\n")
                   .append("   ").append(result.getSnippet()).append("\n\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Combines all snippets into a single text for AI processing
     */
    public String getCombinedContent() {
        StringBuilder content = new StringBuilder();
        
        for (SearchResult result : results) {
            content.append("Source: ").append(result.getSource()).append("\n");
            content.append("Title: ").append(result.getTitle()).append("\n");
            content.append("Content: ").append(result.getSnippet()).append("\n");
            content.append("---\n");
        }
        
        return content.toString();
    }
    
    /**
     * Sorts results by relevance score (highest first)
     */
    public void sortByRelevance() {
        results.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));
    }
    
    /**
     * Removes duplicate results based on URL
     */
    public void removeDuplicates() {
        List<SearchResult> uniqueResults = new ArrayList<>();
        List<String> seenUrls = new ArrayList<>();
        
        for (SearchResult result : results) {
            if (!seenUrls.contains(result.getUrl())) {
                uniqueResults.add(result);
                seenUrls.add(result.getUrl());
            }
        }
        
        results = uniqueResults;
    }
    
    // Getters and setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public long getSearchTime() {
        return searchTime;
    }
    
    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }
    
    @Override
    public String toString() {
        return "SearchResults{" +
                "resultCount=" + results.size() +
                ", query='" + query + '\'' +
                ", searchTime=" + searchTime +
                '}';
    }
}

