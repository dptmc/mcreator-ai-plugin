package net.mcreator.aimodgenerator;

import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.PluginLoader;
import net.mcreator.plugin.events.ui.ModElementGUIEvent;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.action.ActionRegistry;
import net.mcreator.ui.action.BasicAction;
import net.mcreator.workspace.Workspace;
import net.mcreator.aimodgenerator.ui.AIModGeneratorDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Main plugin class for AI Mod Generator
 * Provides AI-powered mod element generation through natural language prompts
 */
public class AIModGeneratorPlugin extends JavaPlugin {
    
    public static final String PLUGIN_ID = "ai-mod-generator";
    
    @Override
    public void preInit() {
        // Register the AI Mod Generator action in the Tools menu
        ActionRegistry.registerAction(PLUGIN_ID + "_open", new BasicAction(
            ActionRegistry.ActionType.NORMAL,
            "AI Mod Generator",
            "Generate mod elements using AI and natural language prompts",
            e -> openAIModGenerator(e)
        ) {
            @Override
            public boolean isEnabled() {
                return MCreator.getApplication() != null && 
                       MCreator.getApplication().getWorkspace() != null;
            }
        });
    }
    
    @Override
    public void init() {
        // Plugin initialization complete
        System.out.println("AI Mod Generator Plugin initialized successfully");
    }
    
    /**
     * Opens the AI Mod Generator dialog
     */
    private void openAIModGenerator(ActionEvent e) {
        MCreator mcreator = MCreator.getApplication();
        if (mcreator != null && mcreator.getWorkspace() != null) {
            AIModGeneratorDialog dialog = new AIModGeneratorDialog(mcreator, mcreator.getWorkspace());
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, 
                "Please open a workspace first to use the AI Mod Generator.", 
                "No Workspace", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
}

