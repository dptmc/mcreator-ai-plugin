package net.mcreator.aimodgenerator.ui.components;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying generated mod elements and their details
 * Shows a list of generated elements with preview capabilities
 */
public class OutputDisplayPanel extends JPanel {
    
    private JTable elementsTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsTextArea;
    private JLabel previewLabel;
    private JSplitPane splitPane;
    
    private List<GeneratedElement> generatedElements;
    
    public OutputDisplayPanel() {
        this.generatedElements = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Create table for generated elements
        String[] columnNames = {"Type", "Name", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        elementsTable = new JTable(tableModel);
        elementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        elementsTable.setRowHeight(25);
        elementsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        elementsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        elementsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        elementsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        
        // Custom renderer for status column
        elementsTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        
        // Details text area
        detailsTextArea = new JTextArea(8, 30);
        detailsTextArea.setEditable(false);
        detailsTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsTextArea.setBackground(new Color(248, 249, 250));
        detailsTextArea.setText("Select an element to view details...");
        
        // Preview label for textures/images
        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        previewLabel.setPreferredSize(new Dimension(128, 128));
        previewLabel.setText("No Preview");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Left panel with elements table
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Generated Elements"));
        
        JScrollPane tableScrollPane = new JScrollPane(elementsTable);
        tableScrollPane.setPreferredSize(new Dimension(320, 200));
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Right panel with details and preview
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        
        // Details section
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new TitledBorder("Details"));
        
        JScrollPane detailsScrollPane = new JScrollPane(detailsTextArea);
        detailsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);
        
        // Preview section
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(new TitledBorder("Preview"));
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        
        rightPanel.add(detailsPanel, BorderLayout.CENTER);
        rightPanel.add(previewPanel, BorderLayout.SOUTH);
        
        // Create split pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.4);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Table selection handler
        elementsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = elementsTable.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < generatedElements.size()) {
                    GeneratedElement element = generatedElements.get(selectedRow);
                    displayElementDetails(element);
                } else {
                    clearDetails();
                }
            }
        });
        
        // Double-click to open element in MCreator
        elementsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = elementsTable.getSelectedRow();
                    if (selectedRow >= 0 && selectedRow < generatedElements.size()) {
                        GeneratedElement element = generatedElements.get(selectedRow);
                        openElementInMCreator(element);
                    }
                }
            }
        });
    }
    
    /**
     * Adds a generated element to the display
     */
    public void addGeneratedElement(String type, String name, String details) {
        GeneratedElement element = new GeneratedElement(type, name, details, "Generated");
        generatedElements.add(element);
        
        Object[] rowData = {type, name, "Generated"};
        tableModel.addRow(rowData);
        
        // Auto-select the newly added element
        int newRowIndex = tableModel.getRowCount() - 1;
        elementsTable.setRowSelectionInterval(newRowIndex, newRowIndex);
    }
    
    /**
     * Updates the status of an element
     */
    public void updateElementStatus(int index, String status) {
        if (index >= 0 && index < generatedElements.size()) {
            generatedElements.get(index).setStatus(status);
            tableModel.setValueAt(status, index, 2);
        }
    }
    
    /**
     * Clears all output
     */
    public void clearOutput() {
        generatedElements.clear();
        tableModel.setRowCount(0);
        clearDetails();
    }
    
    private void displayElementDetails(GeneratedElement element) {
        StringBuilder details = new StringBuilder();
        details.append("Type: ").append(element.getType()).append("\n");
        details.append("Name: ").append(element.getName()).append("\n");
        details.append("Status: ").append(element.getStatus()).append("\n\n");
        details.append("Details:\n");
        details.append(element.getDetails());
        
        detailsTextArea.setText(details.toString());
        detailsTextArea.setCaretPosition(0);
        
        // Update preview if applicable
        updatePreview(element);
    }
    
    private void updatePreview(GeneratedElement element) {
        // For now, just show element type
        // In a full implementation, this would show texture previews
        if (element.getType().equals("Item") || element.getType().equals("Block")) {
            previewLabel.setText("<html><center>" + element.getType() + "<br>Preview<br>(Texture would appear here)</center></html>");
        } else {
            previewLabel.setText("No Preview Available");
        }
    }
    
    private void clearDetails() {
        detailsTextArea.setText("Select an element to view details...");
        previewLabel.setText("No Preview");
    }
    
    private void openElementInMCreator(GeneratedElement element) {
        // This would open the element in MCreator's editor
        JOptionPane.showMessageDialog(this, 
            "Opening " + element.getName() + " in MCreator editor...\n(This would be implemented to actually open the element)", 
            "Open Element", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Custom cell renderer for status column
     */
    private static class StatusCellRenderer extends JLabel implements TableCellRenderer {
        
        public StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            String status = (String) value;
            setText(status);
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                
                // Set color based on status
                switch (status) {
                    case "Generated":
                        setForeground(new Color(76, 175, 80)); // Green
                        break;
                    case "Error":
                        setForeground(new Color(244, 67, 54)); // Red
                        break;
                    case "Processing":
                        setForeground(new Color(255, 152, 0)); // Orange
                        break;
                    default:
                        setForeground(table.getForeground());
                        break;
                }
            }
            
            return this;
        }
    }
    
    /**
     * Data class for generated elements
     */
    public static class GeneratedElement {
        private String type;
        private String name;
        private String details;
        private String status;
        
        public GeneratedElement(String type, String name, String details, String status) {
            this.type = type;
            this.name = name;
            this.details = details;
            this.status = status;
        }
        
        // Getters and setters
        public String getType() { return type; }
        public String getName() { return name; }
        public String getDetails() { return details; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}

