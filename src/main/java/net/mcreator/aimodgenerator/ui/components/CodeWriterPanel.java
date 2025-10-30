package net.mcreator.aimodgenerator.ui.components;

import net.mcreator.aimodgenerator.core.AIModGeneratorCoreNew;
import net.mcreator.workspace.Workspace;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;

/**
 * Panel for direct code writing to MCreator workspace files
 * Allows users to generate and write code directly into mod elements
 */
public class CodeWriterPanel extends JPanel {
    
    private final AIModGeneratorCoreNew aiCore;
    private final Workspace workspace;
    
    // UI Components
    private JComboBox<String> elementTypeCombo;
    private JTextField elementNameField;
    private JTextArea descriptionArea;
    private JComboBox<String> forgeVersionCombo;
    private JCheckBox writeToWorkspaceCheck;
    private JTextArea codeOutputArea;
    private JButton generateCodeButton;
    private JButton writeToFileButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public CodeWriterPanel(AIModGeneratorCoreNew aiCore, Workspace workspace) {
        this.aiCore = aiCore;
        this.workspace = workspace;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Element type selection
        String[] elementTypes = {
            "Item", "Block", "Recipe", "Enchantment", "Procedure", 
            "Biome", "Dimension", "Structure", "Command", "GUI"
        };
        elementTypeCombo = new JComboBox<>(elementTypes);
        elementTypeCombo.setToolTipText("Select the type of mod element to generate");
        
        // Element name input
        elementNameField = new JTextField(20);
        elementNameField.setToolTipText("Enter the name for your mod element");
        
        // Description input
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setToolTipText("Describe what you want this element to do");
        descriptionArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Forge version selection
        String[] forgeVersions = {
            "1.20.1", "1.20.0", "1.19.4", "1.19.2", "1.18.2"
        };
        forgeVersionCombo = new JComboBox<>(forgeVersions);
        forgeVersionCombo.setSelectedItem("1.20.1");
        forgeVersionCombo.setToolTipText("Select target Forge version");
        
        // Write to workspace option
        writeToWorkspaceCheck = new JCheckBox("Write directly to MCreator workspace", true);
        writeToWorkspaceCheck.setToolTipText("Automatically create the mod element in your workspace");
        
        // Code output area
        codeOutputArea = new JTextArea(15, 50);
        codeOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        codeOutputArea.setEditable(false);
        codeOutputArea.setBackground(new Color(248, 248, 248));
        codeOutputArea.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Buttons
        generateCodeButton = new JButton("Generate Code with Gemini AI");
        generateCodeButton.setBackground(new Color(66, 133, 244));
        generateCodeButton.setForeground(Color.WHITE);
        generateCodeButton.setToolTipText("Generate code using Google Gemini AI");
        
        writeToFileButton = new JButton("Write to MCreator File");
        writeToFileButton.setEnabled(false);
        writeToFileButton.setToolTipText("Write the generated code to a MCreator file");
        
        // Progress and status
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        statusLabel = new JLabel("Ready to generate code");
        statusLabel.setForeground(new Color(60, 60, 60));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Direct Code Writer - Gemini AI Integration"));
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Element type row
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Element Type:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(elementTypeCombo, gbc);
        
        gbc.gridx = 2;
        inputPanel.add(new JLabel("Forge Version:"), gbc);
        gbc.gridx = 3;
        inputPanel.add(forgeVersionCombo, gbc);
        
        // Element name row
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Element Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(elementNameField, gbc);
        
        // Description row
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        inputPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Options row
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(writeToWorkspaceCheck, gbc);
        
        // Button row
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(generateCodeButton, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        inputPanel.add(writeToFileButton, gbc);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Code output panel
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(new TitledBorder("Generated Code"));
        outputPanel.add(new JScrollPane(codeOutputArea), BorderLayout.CENTER);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);
        outputPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(outputPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        generateCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateCode();
            }
        });
        
        writeToFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeCodeToFile();
            }
        });
        
        // Enable/disable write button based on code output
        codeOutputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateWriteButton(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateWriteButton(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateWriteButton(); }
            
            private void updateWriteButton() {
                writeToFileButton.setEnabled(!codeOutputArea.getText().trim().isEmpty());
            }
        });
    }
    
    private void generateCode() {
        String elementType = (String) elementTypeCombo.getSelectedItem();
        String elementName = elementNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String forgeVersion = (String) forgeVersionCombo.getSelectedItem();
        boolean writeToWorkspace = writeToWorkspaceCheck.isSelected();
        
        // Validation
        if (elementName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an element name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a description.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Start generation
        generateCodeButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        statusLabel.setText("Generating code with Gemini AI...");
        codeOutputArea.setText("Generating code, please wait...");
        
        CompletableFuture<String> future = aiCore.generateDirectCode(
            elementType, elementName, description, forgeVersion, writeToWorkspace
        );
        
        future.thenAccept(code -> {
            SwingUtilities.invokeLater(() -> {
                codeOutputArea.setText(code);
                generateCodeButton.setEnabled(true);
                progressBar.setVisible(false);
                
                if (code.startsWith("// Error")) {
                    statusLabel.setText("Code generation failed");
                    statusLabel.setForeground(Color.RED);
                } else {
                    statusLabel.setText("Code generated successfully with Gemini AI");
                    statusLabel.setForeground(new Color(0, 150, 0));
                    
                    if (writeToWorkspace) {
                        statusLabel.setText("Code generated and written to workspace");
                    }
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                codeOutputArea.setText("Error generating code: " + throwable.getMessage());
                generateCodeButton.setEnabled(true);
                progressBar.setVisible(false);
                statusLabel.setText("Code generation failed");
                statusLabel.setForeground(Color.RED);
            });
            return null;
        });
    }
    
    private void writeCodeToFile() {
        String code = codeOutputArea.getText();
        String elementName = elementNameField.getText().trim();
        String elementType = (String) elementTypeCombo.getSelectedItem();
        
        if (code.isEmpty() || code.startsWith("// Error")) {
            JOptionPane.showMessageDialog(this, "No valid code to write.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // File chooser for manual file writing
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Generated Code");
        fileChooser.setSelectedFile(new java.io.File(elementName + ".java"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            
            try {
                java.nio.file.Files.write(fileToSave.toPath(), code.getBytes());
                statusLabel.setText("Code saved to: " + fileToSave.getAbsolutePath());
                statusLabel.setForeground(new Color(0, 150, 0));
                
                JOptionPane.showMessageDialog(this, 
                    "Code saved successfully!\n\nFile: " + fileToSave.getAbsolutePath() + 
                    "\n\nYou can now import this into your MCreator project.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                statusLabel.setText("Failed to save code");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "Failed to save file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Clears all input fields and output
     */
    public void clearAll() {
        elementNameField.setText("");
        descriptionArea.setText("");
        codeOutputArea.setText("");
        elementTypeCombo.setSelectedIndex(0);
        forgeVersionCombo.setSelectedItem("1.20.1");
        writeToWorkspaceCheck.setSelected(true);
        statusLabel.setText("Ready to generate code");
        statusLabel.setForeground(new Color(60, 60, 60));
        writeToFileButton.setEnabled(false);
    }
    
    /**
     * Sets the element type programmatically
     */
    public void setElementType(String elementType) {
        elementTypeCombo.setSelectedItem(elementType);
    }
    
    /**
     * Sets the element name programmatically
     */
    public void setElementName(String elementName) {
        elementNameField.setText(elementName);
    }
    
    /**
     * Sets the description programmatically
     */
    public void setDescription(String description) {
        descriptionArea.setText(description);
    }
}

