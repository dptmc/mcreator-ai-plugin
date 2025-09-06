package net.mcreator.aimodgenerator.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panel for natural language prompt input
 * Provides a rich text area with helpful features for prompt entry
 */
public class PromptInputPanel extends JPanel {
    
    private JTextArea promptTextArea;
    private JLabel characterCountLabel;
    private JButton exampleButton;
    
    private static final String[] EXAMPLE_PROMPTS = {
        "Create a magical sword that glows in the dark and deals extra damage to undead creatures",
        "Make a crafting table that can create infinite weapons and tools using basic materials",
        "Add a new emerald ore that spawns rarely and can be used to craft powerful armor",
        "Create a speed potion that lasts longer and gives jump boost as well",
        "Design a block that generates energy and can power nearby machines",
        "Make an enchantment that allows tools to repair themselves over time",
        "Create a food item that gives multiple beneficial effects when eaten",
        "Add a new dimension with unique blocks, items, and creatures"
    };
    
    public PromptInputPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        promptTextArea = new JTextArea(6, 50);
        promptTextArea.setLineWrap(true);
        promptTextArea.setWrapStyleWord(true);
        promptTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        promptTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        promptTextArea.setBackground(new Color(248, 249, 250));
        
        // Add placeholder text
        promptTextArea.setText("Describe what you want to create... (e.g., 'Create a magical sword that glows and deals fire damage')");
        promptTextArea.setForeground(Color.GRAY);
        
        characterCountLabel = new JLabel("0 characters");
        characterCountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        characterCountLabel.setForeground(Color.GRAY);
        
        exampleButton = new JButton("Random Example");
        exampleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        exampleButton.setPreferredSize(new Dimension(120, 25));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));
        
        // Main text area with scroll pane
        JScrollPane scrollPane = new JScrollPane(promptTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Bottom panel with character count and example button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(characterCountLabel, BorderLayout.WEST);
        bottomPanel.add(exampleButton, BorderLayout.EAST);
        
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Handle placeholder text
        promptTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (promptTextArea.getForeground() == Color.GRAY) {
                    promptTextArea.setText("");
                    promptTextArea.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (promptTextArea.getText().trim().isEmpty()) {
                    promptTextArea.setText("Describe what you want to create... (e.g., 'Create a magical sword that glows and deals fire damage')");
                    promptTextArea.setForeground(Color.GRAY);
                }
            }
        });
        
        // Update character count
        promptTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateCharacterCount();
            }
        });
        
        // Example button handler
        exampleButton.addActionListener(e -> {
            String example = EXAMPLE_PROMPTS[(int) (Math.random() * EXAMPLE_PROMPTS.length)];
            promptTextArea.setText(example);
            promptTextArea.setForeground(Color.BLACK);
            updateCharacterCount();
        });
    }
    
    private void updateCharacterCount() {
        String text = promptTextArea.getText();
        if (promptTextArea.getForeground() == Color.GRAY) {
            characterCountLabel.setText("0 characters");
        } else {
            int count = text.length();
            characterCountLabel.setText(count + " characters");
            
            // Change color based on length
            if (count > 1000) {
                characterCountLabel.setForeground(Color.RED);
            } else if (count > 500) {
                characterCountLabel.setForeground(Color.ORANGE);
            } else {
                characterCountLabel.setForeground(Color.GRAY);
            }
        }
    }
    
    /**
     * Gets the current prompt text
     * @return The prompt text, or empty string if placeholder is showing
     */
    public String getPrompt() {
        if (promptTextArea.getForeground() == Color.GRAY) {
            return "";
        }
        return promptTextArea.getText().trim();
    }
    
    /**
     * Sets the prompt text
     * @param prompt The prompt to set
     */
    public void setPrompt(String prompt) {
        promptTextArea.setText(prompt);
        promptTextArea.setForeground(Color.BLACK);
        updateCharacterCount();
    }
    
    /**
     * Clears the prompt text
     */
    public void clearPrompt() {
        promptTextArea.setText("Describe what you want to create... (e.g., 'Create a magical sword that glows and deals fire damage')");
        promptTextArea.setForeground(Color.GRAY);
        updateCharacterCount();
    }
    
    /**
     * Focuses the prompt text area
     */
    public void focusPrompt() {
        promptTextArea.requestFocusInWindow();
    }
}

