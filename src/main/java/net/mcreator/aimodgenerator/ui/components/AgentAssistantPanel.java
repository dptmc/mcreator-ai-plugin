package net.mcreator.aimodgenerator.ui.components;

import net.mcreator.aimodgenerator.core.AIModGeneratorCoreNew;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;

/**
 * Panel for AI-powered agent assistance and tutorial system
 * Provides intelligent help and guidance for MCreator users
 */
public class AgentAssistantPanel extends JPanel {
    
    private final AIModGeneratorCoreNew aiCore;
    
    // UI Components
    private JComboBox<String> assistanceTypeCombo;
    private JComboBox<String> userLevelCombo;
    private JTextArea questionArea;
    private JTextArea responseArea;
    private JButton askAssistantButton;
    private JButton clearButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JList<String> quickHelpList;
    
    // Quick help topics
    private final String[] quickHelpTopics = {
        "How to create a new item in MCreator",
        "How to make a custom block with special properties",
        "How to create recipes for my items",
        "How to add enchantments to weapons",
        "How to create procedures and triggers",
        "How to make entities and mobs",
        "How to create custom biomes",
        "How to add sound effects to my mod",
        "How to create custom textures",
        "How to test my mod in Minecraft",
        "How to export and share my mod",
        "How to fix common MCreator errors",
        "How to optimize mod performance",
        "How to add compatibility with other mods",
        "How to create complex crafting systems"
    };
    
    public AgentAssistantPanel(AIModGeneratorCoreNew aiCore) {
        this.aiCore = aiCore;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Assistance type selection
        String[] assistanceTypes = {
            "General Help", "Tutorial", "Troubleshooting", "Best Practices", 
            "Code Review", "Project Planning", "Learning Path"
        };
        assistanceTypeCombo = new JComboBox<>(assistanceTypes);
        assistanceTypeCombo.setToolTipText("Select the type of assistance you need");
        
        // User level selection
        String[] userLevels = {
            "Beginner", "Intermediate", "Advanced", "Expert"
        };
        userLevelCombo = new JComboBox<>(userLevels);
        userLevelCombo.setToolTipText("Select your experience level with MCreator");
        
        // Question input
        questionArea = new JTextArea(4, 40);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setToolTipText("Ask your question or describe what you need help with");
        questionArea.setBorder(BorderFactory.createLoweredBevelBorder());
        questionArea.setText("How can I create a magical sword that shoots fireballs when right-clicked?");
        
        // Response area
        responseArea = new JTextArea(12, 40);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEditable(false);
        responseArea.setBackground(new Color(248, 248, 248));
        responseArea.setBorder(BorderFactory.createLoweredBevelBorder());
        responseArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Buttons
        askAssistantButton = new JButton("Ask Gemini AI Assistant");
        askAssistantButton.setBackground(new Color(66, 133, 244));
        askAssistantButton.setForeground(Color.WHITE);
        askAssistantButton.setToolTipText("Get AI-powered assistance from Google Gemini");
        
        clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear all fields");
        
        // Progress and status
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        statusLabel = new JLabel("Ready to assist you with MCreator");
        statusLabel.setForeground(new Color(60, 60, 60));
        
        // Quick help list
        quickHelpList = new JList<>(quickHelpTopics);
        quickHelpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        quickHelpList.setToolTipText("Click on a topic for quick help");
        quickHelpList.setBorder(BorderFactory.createLoweredBevelBorder());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("AI Agent Assistant - Powered by Gemini"));
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Settings row
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Assistance Type:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(assistanceTypeCombo, gbc);
        
        gbc.gridx = 2;
        inputPanel.add(new JLabel("Your Level:"), gbc);
        gbc.gridx = 3;
        inputPanel.add(userLevelCombo, gbc);
        
        // Question row
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Your Question:"), gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(new JScrollPane(questionArea), gbc);
        
        // Button row
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(askAssistantButton, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        inputPanel.add(clearButton, gbc);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Response panel
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(new TitledBorder("AI Assistant Response"));
        responsePanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        responsePanel.add(statusPanel, BorderLayout.SOUTH);
        
        contentPanel.add(responsePanel, BorderLayout.CENTER);
        
        // Quick help panel
        JPanel quickHelpPanel = new JPanel(new BorderLayout());
        quickHelpPanel.setBorder(new TitledBorder("Quick Help Topics"));
        quickHelpPanel.add(new JScrollPane(quickHelpList), BorderLayout.CENTER);
        quickHelpPanel.setPreferredSize(new Dimension(250, 0));
        
        contentPanel.add(quickHelpPanel, BorderLayout.EAST);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        askAssistantButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                askAssistant();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAll();
            }
        });
        
        // Quick help selection
        quickHelpList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedTopic = quickHelpList.getSelectedValue();
                if (selectedTopic != null) {
                    questionArea.setText(selectedTopic);
                    assistanceTypeCombo.setSelectedItem("Tutorial");
                }
            }
        });
        
        // Enter key in question area
        questionArea.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke("ctrl ENTER"), "askAssistant");
        questionArea.getActionMap().put("askAssistant", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                askAssistant();
            }
        });
    }
    
    private void askAssistant() {
        String question = questionArea.getText().trim();
        String assistanceType = (String) assistanceTypeCombo.getSelectedItem();
        String userLevel = (String) userLevelCombo.getSelectedItem();
        
        if (question.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a question or select a quick help topic.", 
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Start assistance
        askAssistantButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Consulting Gemini AI Assistant...");
        responseArea.setText("Thinking... Please wait while I analyze your question and provide assistance.");
        
        // Determine which method to use based on assistance type
        CompletableFuture<String> future;
        if (assistanceType.equals("Tutorial")) {
            future = aiCore.provideTutorialAssistance(question, userLevel.toLowerCase());
        } else {
            // Format the question with context
            String contextualQuestion = String.format(
                "Assistance Type: %s\nUser Level: %s\nQuestion: %s", 
                assistanceType, userLevel, question
            );
            future = aiCore.provideAgentAssistance(contextualQuestion, userLevel.toLowerCase());
        }
        
        future.thenAccept(response -> {
            SwingUtilities.invokeLater(() -> {
                responseArea.setText(response);
                askAssistantButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText("Response ready - Powered by Gemini AI");
                statusLabel.setForeground(new Color(0, 150, 0));
                
                // Scroll to top of response
                responseArea.setCaretPosition(0);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                responseArea.setText("I apologize, but I encountered an error while processing your request:\n\n" + 
                                   throwable.getMessage() + 
                                   "\n\nPlease try rephrasing your question or check your internet connection.");
                askAssistantButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText("Error occurred during assistance");
                statusLabel.setForeground(Color.RED);
            });
            return null;
        });
    }
    
    private void clearAll() {
        questionArea.setText("");
        responseArea.setText("");
        assistanceTypeCombo.setSelectedIndex(0);
        userLevelCombo.setSelectedIndex(0);
        quickHelpList.clearSelection();
        statusLabel.setText("Ready to assist you with MCreator");
        statusLabel.setForeground(new Color(60, 60, 60));
    }
    
    /**
     * Sets a predefined question
     */
    public void setQuestion(String question) {
        questionArea.setText(question);
    }
    
    /**
     * Sets the assistance type
     */
    public void setAssistanceType(String type) {
        assistanceTypeCombo.setSelectedItem(type);
    }
    
    /**
     * Sets the user level
     */
    public void setUserLevel(String level) {
        userLevelCombo.setSelectedItem(level);
    }
    
    /**
     * Adds a custom quick help topic
     */
    public void addQuickHelpTopic(String topic) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String existingTopic : quickHelpTopics) {
            model.addElement(existingTopic);
        }
        model.addElement(topic);
        quickHelpList.setModel(model);
    }
    
    /**
     * Gets the current response text
     */
    public String getResponse() {
        return responseArea.getText();
    }
    
    /**
     * Shows a helpful tip dialog
     */
    public void showTip() {
        String tip = "💡 Pro Tip: Be specific in your questions for better assistance!\n\n" +
                    "Instead of: 'How do I make an item?'\n" +
                    "Try: 'How do I create a magical sword that deals extra damage to undead mobs?'\n\n" +
                    "The AI assistant works best with detailed, specific questions!";
        
        JOptionPane.showMessageDialog(this, tip, "AI Assistant Tips", JOptionPane.INFORMATION_MESSAGE);
    }
}

