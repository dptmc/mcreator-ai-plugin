package net.mcreator.aimodgenerator.ui.components;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Panel for displaying generation progress and status messages
 * Shows real-time updates during the generation process
 */
public class ProgressPanel extends JPanel {
    
    private JProgressBar progressBar;
    private JTextArea logTextArea;
    private JLabel statusLabel;
    private JScrollPane logScrollPane;
    
    private SimpleDateFormat timeFormat;
    
    public ProgressPanel() {
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setValue(0);
        
        // Status label
        statusLabel = new JLabel("Ready to generate mod elements");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Log text area
        logTextArea = new JTextArea(6, 50);
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logTextArea.setBackground(new Color(248, 249, 250));
        logTextArea.setText("Generation log will appear here...\n");
        
        logScrollPane = new JScrollPane(logTextArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));
        
        // Top panel with progress bar and status
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(progressBar, BorderLayout.CENTER);
        
        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Updates the progress with a message
     * @param message The progress message to display
     */
    public void updateProgress(String message) {
        SwingUtilities.invokeLater(() -> {
            // Update status label
            statusLabel.setText(message);
            
            // Add timestamped message to log
            String timestamp = timeFormat.format(new Date());
            String logMessage = "[" + timestamp + "] " + message + "\n";
            logTextArea.append(logMessage);
            
            // Auto-scroll to bottom
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            
            // Update progress bar based on message content
            updateProgressBar(message);
        });
    }
    
    /**
     * Updates the progress bar based on the message content
     */
    private void updateProgressBar(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("starting")) {
            progressBar.setValue(10);
            progressBar.setString("Starting...");
        } else if (lowerMessage.contains("searching") || lowerMessage.contains("web search")) {
            progressBar.setValue(25);
            progressBar.setString("Searching...");
        } else if (lowerMessage.contains("generating") || lowerMessage.contains("creating")) {
            progressBar.setValue(50);
            progressBar.setString("Generating...");
        } else if (lowerMessage.contains("texture") || lowerMessage.contains("sound")) {
            progressBar.setValue(75);
            progressBar.setString("Creating Assets...");
        } else if (lowerMessage.contains("complete") || lowerMessage.contains("finished")) {
            progressBar.setValue(100);
            progressBar.setString("Complete");
        } else if (lowerMessage.contains("error")) {
            progressBar.setString("Error");
            // Don't change progress value on error
        }
    }
    
    /**
     * Sets the progress bar to a specific value
     * @param value Progress value (0-100)
     * @param text Text to display on progress bar
     */
    public void setProgress(int value, String text) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(text);
        });
    }
    
    /**
     * Adds a message to the log without updating status
     * @param message The message to add to the log
     */
    public void addLogMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            String logMessage = "[" + timestamp + "] " + message + "\n";
            logTextArea.append(logMessage);
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }
    
    /**
     * Adds an error message to the log
     * @param error The error message
     */
    public void addErrorMessage(String error) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            String logMessage = "[" + timestamp + "] ERROR: " + error + "\n";
            logTextArea.append(logMessage);
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            
            statusLabel.setText("Error: " + error);
            progressBar.setString("Error");
        });
    }
    
    /**
     * Clears all progress and log messages
     */
    public void clearProgress() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setString("Ready");
            statusLabel.setText("Ready to generate mod elements");
            logTextArea.setText("Generation log will appear here...\n");
        });
    }
    
    /**
     * Marks the generation as complete
     */
    public void markComplete() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(100);
            progressBar.setString("Complete");
            statusLabel.setText("Generation completed successfully");
            addLogMessage("All mod elements generated successfully!");
        });
    }
    
    /**
     * Gets the current log text
     * @return The complete log text
     */
    public String getLogText() {
        return logTextArea.getText();
    }
    
    /**
     * Saves the log to a file
     * @param filename The filename to save to
     */
    public void saveLogToFile(String filename) {
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filename), 
                logTextArea.getText().getBytes()
            );
            addLogMessage("Log saved to: " + filename);
        } catch (Exception e) {
            addErrorMessage("Failed to save log: " + e.getMessage());
        }
    }
}

