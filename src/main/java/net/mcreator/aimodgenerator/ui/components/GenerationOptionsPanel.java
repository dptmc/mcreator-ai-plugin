package net.mcreator.aimodgenerator.ui.components;

import net.mcreator.aimodgenerator.core.GenerationOptions;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel for configuring generation options
 * Allows users to select what types of mod elements to generate
 */
public class GenerationOptionsPanel extends JPanel {
    
    // Element type checkboxes
    private JCheckBox itemsCheckBox;
    private JCheckBox blocksCheckBox;
    private JCheckBox recipesCheckBox;
    private JCheckBox enchantmentsCheckBox;
    private JCheckBox proceduresCheckBox;
    private JCheckBox entitiesCheckBox;
    private JCheckBox texturesCheckBox;
    private JCheckBox soundsCheckBox;
    private JCheckBox generateModelsCheckbox;
    
    // Entity-specific options
    private JCheckBox generateBlockbenchModelsCheckbox;
    private JCheckBox uploadImageConversionCheckbox;
    private JButton uploadImageButton;
    private JLabel uploadedImageLabel;
    private String uploadedImagePath;
    
    // Advanced options
    private JCheckBox webSearchCheckBox;
    private JCheckBox generateLoreCheckBox;
    private JCheckBox balancedStatsCheckBox;
    
    // Quality settings
    private JComboBox<String> qualityComboBox;
    private JSlider creativitySlider;
    
    public GenerationOptionsPanel() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Element type checkboxes
        itemsCheckBox = new JCheckBox("Items", true);
        blocksCheckBox = new JCheckBox("Blocks", true);
        recipesCheckBox = new JCheckBox("Recipes", true);
        enchantmentsCheckBox = new JCheckBox("Enchantments", false);
        proceduresCheckBox = new JCheckBox("Procedures", false);
        entitiesCheckBox = new JCheckBox("Entities", false);
        texturesCheckBox = new JCheckBox("Textures", true);
        soundsCheckBox = new JCheckBox("Sounds", false);
        generateModelsCheckbox = new JCheckBox("Blockbench Model Ideas", false);
        
        // Entity-specific options
        generateBlockbenchModelsCheckbox = new JCheckBox("Generate Blockbench Models", false);
        uploadImageConversionCheckbox = new JCheckBox("Convert Uploaded Image to Entity", false);
        uploadImageButton = new JButton("Upload Character Image");
        uploadedImageLabel = new JLabel("No image uploaded");
        uploadedImagePath = null;
        
        // Advanced options
        webSearchCheckBox = new JCheckBox("Use Web Search", true);
        generateLoreCheckBox = new JCheckBox("Generate Lore", true);
        balancedStatsCheckBox = new JCheckBox("Balanced Stats", true);
        
        // Quality settings
        qualityComboBox = new JComboBox<>(new String[]{"Fast", "Balanced", "High Quality"});
        qualityComboBox.setSelectedIndex(1); // Default to Balanced
        
        creativitySlider = new JSlider(0, 100, 70);
        creativitySlider.setMajorTickSpacing(25);
        creativitySlider.setMinorTickSpacing(10);
        creativitySlider.setPaintTicks(true);
        creativitySlider.setPaintLabels(true);
        
        // Add tooltips
        itemsCheckBox.setToolTipText("Generate new items (swords, tools, food, etc.)");
        blocksCheckBox.setToolTipText("Generate new blocks (ores, decorative blocks, etc.)");
        recipesCheckBox.setToolTipText("Generate crafting recipes for created items/blocks");
        enchantmentsCheckBox.setToolTipText("Generate new enchantments");
        proceduresCheckBox.setToolTipText("Generate custom procedures and functions");
        texturesCheckBox.setToolTipText("Generate textures for items and blocks using AI");
        soundsCheckBox.setToolTipText("Generate sound effects using AI");
        webSearchCheckBox.setToolTipText("Use web search to gather relevant information");
        generateLoreCheckBox.setToolTipText("Generate lore and descriptions for items");
        balancedStatsCheckBox.setToolTipText("Ensure generated items have balanced stats");
        qualityComboBox.setToolTipText("Generation quality vs speed trade-off");
        creativitySlider.setToolTipText("How creative vs realistic the AI should be");
    }
    
    private void setupLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(250, 400));
        
        // Element types section
        JPanel elementTypesPanel = new JPanel();
        elementTypesPanel.setLayout(new BoxLayout(elementTypesPanel, BoxLayout.Y_AXIS));
        elementTypesPanel.setBorder(new TitledBorder("Generate"));
        elementTypesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        elementTypesPanel.add(itemsCheckBox);
        elementTypesPanel.add(blocksCheckBox);
        elementTypesPanel.add(recipesCheckBox);
        elementTypesPanel.add(enchantmentsCheckBox);
        elementTypesPanel.add(proceduresCheckBox);
        elementTypesPanel.add(Box.createVerticalStrut(5));
        elementTypesPanel.add(new JSeparator());
        elementTypesPanel.add(Box.createVerticalStrut(5));
        elementTypesPanel.add(texturesCheckBox);
        elementTypesPanel.add(soundsCheckBox);
        
        // Advanced options section
        JPanel advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        advancedPanel.setBorder(new TitledBorder("Advanced"));
        advancedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        advancedPanel.add(webSearchCheckBox);
        advancedPanel.add(generateLoreCheckBox);
        advancedPanel.add(balancedStatsCheckBox);
        
        // Quality settings section
        JPanel qualityPanel = new JPanel();
        qualityPanel.setLayout(new BoxLayout(qualityPanel, BoxLayout.Y_AXIS));
        qualityPanel.setBorder(new TitledBorder("Quality"));
        qualityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel qualityComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qualityComboPanel.add(new JLabel("Mode: "));
        qualityComboPanel.add(qualityComboBox);
        qualityPanel.add(qualityComboPanel);
        
        qualityPanel.add(Box.createVerticalStrut(10));
        qualityPanel.add(new JLabel("Creativity:"));
        qualityPanel.add(creativitySlider);
        
        // Add all sections
        add(elementTypesPanel);
        add(Box.createVerticalStrut(10));
        add(advancedPanel);
        add(Box.createVerticalStrut(10));
        add(qualityPanel);
        add(Box.createVerticalGlue());
    }
    
    private void setupEventHandlers() {
        // Enable/disable related options based on selections
        texturesCheckBox.addActionListener(e -> {
            if (!texturesCheckBox.isSelected() && !soundsCheckBox.isSelected()) {
                // If no assets are being generated, disable some quality options
                creativitySlider.setEnabled(itemsCheckBox.isSelected() || blocksCheckBox.isSelected());
            } else {
                creativitySlider.setEnabled(true);
            }
        });
        
        soundsCheckBox.addActionListener(e -> {
            if (!texturesCheckBox.isSelected() && !soundsCheckBox.isSelected()) {
                creativitySlider.setEnabled(itemsCheckBox.isSelected() || blocksCheckBox.isSelected());
            } else {
                creativitySlider.setEnabled(true);
            }
        });
    }
    
    /**
     * Gets the current generation options
     * @return GenerationOptions object with current settings
     */
    public GenerationOptions getGenerationOptions() {
        GenerationOptions options = new GenerationOptions();
        
        // Element types
        options.setGenerateItems(itemsCheckBox.isSelected());
        options.setGenerateBlocks(blocksCheckBox.isSelected());
        options.setGenerateRecipes(recipesCheckBox.isSelected());
        options.setGenerateEnchantments(enchantmentsCheckBox.isSelected());
        options.setGenerateProcedures(proceduresCheckBox.isSelected());
        options.setGenerateTextures(texturesCheckBox.isSelected());
        options.setGenerateSounds(soundsCheckBox.isSelected());
        
        // Advanced options
        options.setUseWebSearch(webSearchCheckBox.isSelected());
        options.setGenerateLore(generateLoreCheckBox.isSelected());
        options.setBalancedStats(balancedStatsCheckBox.isSelected());
        
        // Quality settings
        String quality = (String) qualityComboBox.getSelectedItem();
        options.setQualityMode(quality);
        options.setCreativityLevel(creativitySlider.getValue());
        
        return options;
    }
    
    /**
     * Resets all options to default values
     */
    public void resetOptions() {
        itemsCheckBox.setSelected(true);
        blocksCheckBox.setSelected(true);
        recipesCheckBox.setSelected(true);
        enchantmentsCheckBox.setSelected(false);
        proceduresCheckBox.setSelected(false);
        texturesCheckBox.setSelected(true);
        soundsCheckBox.setSelected(false);
        
        webSearchCheckBox.setSelected(true);
        generateLoreCheckBox.setSelected(true);
        balancedStatsCheckBox.setSelected(true);
        
        qualityComboBox.setSelectedIndex(1);
        creativitySlider.setValue(70);
    }
    
    /**
     * Enables or disables all options
     * @param enabled Whether options should be enabled
     */
    public void setOptionsEnabled(boolean enabled) {
        Component[] components = {
            itemsCheckBox, blocksCheckBox, recipesCheckBox, enchantmentsCheckBox,
            proceduresCheckBox, texturesCheckBox, soundsCheckBox,
            webSearchCheckBox, generateLoreCheckBox, balancedStatsCheckBox,
            qualityComboBox, creativitySlider
        };
        
        for (Component component : components) {
            component.setEnabled(enabled);
        }
    }
}

