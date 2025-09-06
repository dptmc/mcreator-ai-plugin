package net.mcreator.aimodgenerator.ui;

import net.mcreator.ui.MCreator;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.workspace.Workspace;
import net.mcreator.aimodgenerator.core.AIModGeneratorCore;
import net.mcreator.aimodgenerator.ui.components.PromptInputPanel;
import net.mcreator.aimodgenerator.ui.components.GenerationOptionsPanel;
import net.mcreator.aimodgenerator.ui.components.OutputDisplayPanel;
import net.mcreator.aimodgenerator.ui.components.ProgressPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main dialog for the AI Mod Generator plugin
 * Provides a comprehensive UI for generating mod elements through AI
 */
public class AIModGeneratorDialog extends JDialog {
    
    private final MCreator mcreator;
    private final Workspace workspace;
    private final AIModGeneratorCore core;
    
    // UI Components
    private PromptInputPanel promptInputPanel;
    private GenerationOptionsPanel optionsPanel;
    private OutputDisplayPanel outputPanel;
    private ProgressPanel progressPanel;
    
    private JButton generateButton;
    private JButton clearButton;
    private JButton helpButton;
    
    public AIModGeneratorDialog(MCreator mcreator, Workspace workspace) {
        super(mcreator, "AI Mod Generator", true);
        this.mcreator = mcreator;
        this.workspace = workspace;
        this.core = new AIModGeneratorCore(workspace);
        
        initializeUI();
        setupEventHandlers();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(mcreator);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Create main panels
        promptInputPanel = new PromptInputPanel();
        optionsPanel = new GenerationOptionsPanel();
        outputPanel = new OutputDisplayPanel();
        progressPanel = new ProgressPanel();
        
        // Create top panel with prompt input and options
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        // Prompt input section
        JPanel promptSection = new JPanel(new BorderLayout());
        promptSection.setBorder(new TitledBorder("Natural Language Prompt"));
        promptSection.add(promptInputPanel, BorderLayout.CENTER);
        
        // Options section
        JPanel optionsSection = new JPanel(new BorderLayout());
        optionsSection.setBorder(new TitledBorder("Generation Options"));
        optionsSection.add(optionsPanel, BorderLayout.CENTER);
        
        topPanel.add(promptSection, BorderLayout.CENTER);
        topPanel.add(optionsSection, BorderLayout.EAST);
        
        // Create center panel with output display
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JPanel outputSection = new JPanel(new BorderLayout());
        outputSection.setBorder(new TitledBorder("Generated Content"));
        outputSection.add(outputPanel, BorderLayout.CENTER);
        
        centerPanel.add(outputSection, BorderLayout.CENTER);
        
        // Create bottom panel with progress and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // Progress section
        JPanel progressSection = new JPanel(new BorderLayout());
        progressSection.setBorder(new TitledBorder("Progress"));
        progressSection.add(progressPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        bottomPanel.add(progressSection, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add all panels to main dialog
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        generateButton = new JButton("Generate Mod Elements");
        generateButton.setPreferredSize(new Dimension(180, 35));
        generateButton.setBackground(new Color(76, 175, 80));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFocusPainted(false);
        generateButton.setBorderPainted(false);
        
        clearButton = new JButton("Clear All");
        clearButton.setPreferredSize(new Dimension(100, 35));
        clearButton.setBackground(new Color(244, 67, 54));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorderPainted(false);
        
        helpButton = new JButton("Help");
        helpButton.setPreferredSize(new Dimension(80, 35));
        helpButton.setBackground(new Color(33, 150, 243));
        helpButton.setForeground(Color.WHITE);
        helpButton.setFocusPainted(false);
        helpButton.setBorderPainted(false);
        
        buttonPanel.add(helpButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(generateButton);
        
        return buttonPanel;
    }
    
    private void setupEventHandlers() {
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateModElements();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
        
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
    }
    
    private void generateModElements() {
        String prompt = promptInputPanel.getPrompt();
        if (prompt.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a prompt describing what you want to generate.", 
                "Empty Prompt", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Disable generate button during processing
        generateButton.setEnabled(false);
        generateButton.setText("Generating...");
        
        // Start generation in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Starting AI mod generation...");
                    
                    // Get generation options
                    var options = optionsPanel.getGenerationOptions();
                    
                    // Start the generation process
                    core.generateModElements(prompt, options, new AIModGeneratorCore.GenerationCallback() {
                        @Override
                        public void onProgress(String message) {
                            publish(message);
                        }
                        
                        @Override
                        public void onElementGenerated(String elementType, String elementName, String details) {
                            publish("Generated " + elementType + ": " + elementName);
                            SwingUtilities.invokeLater(() -> {
                                outputPanel.addGeneratedElement(elementType, elementName, details);
                            });
                        }
                        
                        @Override
                        public void onComplete() {
                            publish("Generation complete!");
                        }
                        
                        @Override
                        public void onError(String error) {
                            publish("Error: " + error);
                        }
                    });
                    
                } catch (Exception e) {
                    publish("Error during generation: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    progressPanel.updateProgress(message);
                }
            }
            
            @Override
            protected void done() {
                // Re-enable generate button
                generateButton.setEnabled(true);
                generateButton.setText("Generate Mod Elements");
            }
        };
        
        worker.execute();
    }
    
    private void clearAll() {
        promptInputPanel.clearPrompt();
        outputPanel.clearOutput();
        progressPanel.clearProgress();
        optionsPanel.resetOptions();
    }
    
    private void showHelp() {
        String helpText = 
            "AI Mod Generator Help\n\n" +
            "This plugin allows you to generate Minecraft mod elements using natural language prompts.\n\n" +
            "How to use:\n" +
            "1. Enter a description of what you want to create in the prompt field\n" +
            "2. Select the types of elements you want to generate\n" +
            "3. Click 'Generate Mod Elements' to start the process\n\n" +
            "Example prompts:\n" +
            "• 'Create a magical sword that glows and deals extra damage to undead'\n" +
            "• 'Make a crafting table that can create infinite weapons and tools'\n" +
            "• 'Add a new ore that can be used to craft powerful armor'\n" +
            "• 'Create a potion that gives the player night vision and speed'\n\n" +
            "The AI will generate appropriate textures, sounds, and code for your mod elements.";
        
        JOptionPane.showMessageDialog(this, helpText, "AI Mod Generator Help", JOptionPane.INFORMATION_MESSAGE);
    }
}

