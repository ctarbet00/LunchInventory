package LunchInventory;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.sql.ResultSet;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import javax.swing.JOptionPane;

/**
 *
 * @author Cole Tarbet
 */
public class MainFrame extends JFrame {

    protected SQLConnector databaseConn;
    private JTabbedPane tabbedPane;
    private JPanel mainInventoryPanel;
    private JPanel itemsPanel;
    private CustomNewItemPanel newItemPanel;
    private CustomAdminFunctionsPanel adminFunctionsPanel;
    private InventoryLabelsPanel inventoryLabelsPanel;
    private customReportsPanel reportsPanel;
    private JScrollPane inventorySPane;
    private File configFile;
    private String[] returnValues;
    private String filterCategory;
    private ResultSet allItemsRS;

    /**
     * Primary self-instantiating JFrame
     */
    public MainFrame() {
        setSize(1024, 768);
        setLocation(20, 20);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        filterCategory = "ALL";

        //reads program configuration from xml file
        String[] parameters = readConfig();

        try {
            databaseConn = new SQLConnector(parameters[0], parameters[1], parameters[2], parameters[3]);
        } catch (java.lang.NullPointerException e) {
            JOptionPane.showMessageDialog(new JFrame(), "Database connection failed!  Please verify configuration file.");
        }

        initComponents();
        
        validate();
        
    }

    /**
     * Initialize all components
     */
    private void initComponents() {
        inventoryLabelsPanel = new InventoryLabelsPanel();

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new GridLayout(0, 1));

        mainInventoryPanel = new JPanel();
        mainInventoryPanel.setLayout(new BorderLayout());

        mainInventoryPanel.add(inventoryLabelsPanel, BorderLayout.NORTH);
        mainInventoryPanel.add(itemsPanel, BorderLayout.CENTER);

        inventorySPane = new JScrollPane(mainInventoryPanel);

        newItemPanel = new CustomNewItemPanel();

        adminFunctionsPanel = new CustomAdminFunctionsPanel();

        reportsPanel = new customReportsPanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        tabbedPane.addTab("Inventory", inventorySPane);
        tabbedPane.addTab("Add Item", newItemPanel);
        tabbedPane.addTab("Admin Functions", adminFunctionsPanel);
        tabbedPane.addTab("Reports", reportsPanel);

        add(tabbedPane);

        refresh();
    }

    /**
     * Renew the GUI after database modifications are made
     */
    private void refresh() {
        //remove all the inventory panels
        itemsPanel.removeAll();

        allItemsRS = databaseConn.getAllItemsByCategory(filterCategory);

        try {
            //create new panels for every inventory item
            while (allItemsRS.next()) {
                itemsPanel.add(new CustomInventoryPanel(
                        allItemsRS.getInt("id"),
                        allItemsRS.getInt("hsqty"),
                        allItemsRS.getInt("elqty"),
                        allItemsRS.getString("itemName"),
                        new DecimalFormat("$#,###,###,##0.00").format(allItemsRS.getDouble("price")),
                        allItemsRS.getString("categoryName")));
            }
        } catch (SQLException e) {
            System.out.println("Exception in refresh: " + e.getMessage());
        }
        inventoryLabelsPanel.refreshCategories();
        setVisible(true);
    }

    /**
     * Imports items from a .CSV file
     * does not handle " " in fields
     * does not expect field names
     */
    private void importFile() {
        File inputFile = new File("file.txt");

        StringBuilder hsqty = new StringBuilder(), elqty = new StringBuilder(), name = new StringBuilder(), price = new StringBuilder(), category = new StringBuilder();

        try {
            Scanner reader = new Scanner(inputFile);

            for (; reader.hasNext();) {
                hsqty.append(reader.next());
                elqty.append(reader.next());
                name.append(reader.next());
                price.append(reader.next());
                category.append(reader.next());

                hsqty.deleteCharAt(hsqty.indexOf(","));
                elqty.deleteCharAt(hsqty.indexOf(","));
                name.deleteCharAt(name.indexOf(","));
                price.deleteCharAt(name.indexOf(","));

                databaseConn.insertItem(hsqty.toString(), elqty.toString(), name.toString(), price.toString(), category.toString());

                hsqty.delete(0, hsqty.length());
                elqty.delete(0, hsqty.length());
                name.delete(0, name.length());
                category.delete(0, category.length());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Exception in importFile: " + e + " -- " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Exception in importFile: " + e + " -- " + e.getMessage());
        }
        refresh();
    }

    /**
     * Reads SQL server information from the XML configuration file config.xml from program directory.
     *
     */
    private String[] readConfig() {
        try {
            configFile = new File("config.xml");
            if(!configFile.exists()){
                writeConfig();
            }
            Scanner input = new Scanner(configFile);

            StringBuilder buffer = new StringBuilder("");
            returnValues = new String[4];

            for (; input.hasNext();) {
                buffer.append(input.nextLine());
            }
            returnValues[0] = buffer.subSequence(buffer.indexOf("<serverip>") + 10, buffer.indexOf("</serverip>")).toString();
            returnValues[1] = buffer.subSequence(buffer.indexOf("<databasename>") + 14, buffer.indexOf("</databasename>")).toString();
            returnValues[2] = buffer.subSequence(buffer.indexOf("<username>") + 10, buffer.indexOf("</username>")).toString();
            returnValues[3] = buffer.subSequence(buffer.indexOf("<password>") + 10, buffer.indexOf("</password>")).toString();
            return returnValues;
        } catch (FileNotFoundException e) {
            System.out.println("Exception in readConfig() " + e.getMessage());
        }
        return null;
    }
    /**
     * writeConfig
     */
    private void writeConfig() {
        try {
            // Stream to write file
            FileOutputStream fout;

            // Open an output stream
            fout = new FileOutputStream("config.xml");

            // Print a line of text
            PrintStream output = new PrintStream(fout);

            output.println(
                    "<?xml version=\"1.0\" ?>\n"
                    + "<javainventory>\n"
                    + "<serverip>127.0.0.1</serverip>\n"
                    + "<databasename>FILL</databasename>\n"
                    + "<username>THESE</username>\n"
                    + "<password>DATAS</password>\n"
                    + "</javainventory>");
            fout.close();

        } catch (IOException e) {
            System.out.println("Problem writing file." + e.getMessage());
        }
    }
    
    /**
     * CustomNewItemPanel
     */
    private class CustomNewItemPanel extends NewItemPanel {

        CustomNewItemPanel() {
            super();
            ResultSet rs = databaseConn.getDistinctCategories();
            try {
                categoryComboBox.removeAllItems();
               categoryComboBox.setMaximumRowCount(25);
                while (rs.next()) {
                    categoryComboBox.addItem(rs.getString("categoryName"));
                }
            } catch (java.sql.SQLException e) {
                System.out.println("SQLException in CustomNewItemPanel" + e.getMessage());
            }
        }

        @Override
        protected void categoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
            try{
                categoryTField.setText(categoryComboBox.getSelectedItem().toString());
            } catch (Exception e){
                System.out.println(e.getMessage());
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        @Override
        protected void addButtonActionPerformed(java.awt.event.ActionEvent evt) {
            try{
                databaseConn.insertItem(hsQtyTField.getText(), elQtyTField.getText(), nameTField.getText(), priceTField.getText(), categoryTField.getText());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(new JFrame(), "Exception while adding item. " + "\n" + e + "\n" + e.getMessage());
            }
            refresh();
        }

        @Override
        protected void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
            hsQtyTField.setText("0");
            elQtyTField.setText("0");
            nameTField.setText("");
            categoryTField.setText("");
        }
    }

    /**
     * CustomAdminFunctionsPanel
     */
    private class CustomAdminFunctionsPanel extends AdminFunctionsPanel {

        CustomAdminFunctionsPanel() {
            super();
        }

        @Override
        protected void createTablesButtonActionPerformed(java.awt.event.ActionEvent evt) {
            databaseConn.createTables();
            refresh();
        }

        @Override
        protected void importFileButtonActionPerformed(java.awt.event.ActionEvent evt) {
            importFile();
        }
    }

    /**
     * CustomInventoryPanel
     */
    private class CustomInventoryPanel extends inventoryContentPanel {

        CustomInventoryPanel() {
            super();
        }

        /** Creates new form inventoryContentPanel */
        CustomInventoryPanel(int id, int hsqty, int elqty, String name, String price, String category) {
            super();
            ID = id;
            hsQtyTField.setText(Integer.toString(hsqty));
            elQtyTField.setText(Integer.toString(elqty));
            nameTField.setText(name);
            priceTField.setText(price);
            categoryIDField.setText(category);
        }

        @Override
        protected void hsAddButtonActionPerformed(java.awt.event.ActionEvent evt) {
            databaseConn.updateQTY(ID, Integer.parseInt(inputTField.getText()), 1);
            refresh();
        }

        @Override
        protected void hsSubButtonActionPerformed(java.awt.event.ActionEvent evt) {
            databaseConn.updateQTY(ID, Integer.parseInt(inputTField.getText()) * -1, 1);
            refresh();
        }

        @Override
        protected void elAddButtonActionPerformed(java.awt.event.ActionEvent evt) {
            databaseConn.updateQTY(ID, Integer.parseInt(inputTField.getText()), 2);
            refresh();
        }

        @Override
        protected void elSubButtonActionPerformed(java.awt.event.ActionEvent evt) {
            databaseConn.updateQTY(ID, Integer.parseInt(inputTField.getText()) * -1, 2);
            refresh();
        }

        @Override
        protected void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
            int option = JOptionPane.showConfirmDialog(
                    new JFrame(),
                    "Remove this type of item from the database completely?",
                    "Confim",
                    JOptionPane.YES_NO_OPTION);
            if (option == 0) {
                if(databaseConn.deleteItem(ID)) {
                    filterCategory = "ALL";
                }
                refresh();
            }
        }

        @Override
        protected void priceTFieldActionPerformed(java.awt.event.ActionEvent evt) {
            databaseConn.updatePrice(ID, priceTField.getText());
            refresh();
        }

        @Override
        protected void nameTFieldActionPerformed(java.awt.event.ActionEvent evt) {
            databaseConn.updateName(ID, nameTField.getText());
            refresh();
        }
    }

    /**
     * inventoryLabelsPanel
     */
    public class InventoryLabelsPanel extends javax.swing.JPanel {

        private int ID;
        private javax.swing.JLabel valueLabel;
        private javax.swing.JComboBox categoryComboBox;
        private javax.swing.JLabel hsLabel;
        private javax.swing.JLabel elLabel;
        private javax.swing.JLabel nameLabel;
        private javax.swing.JLabel priceLabel;
        private boolean flag;
        private String previousSelection;

        public InventoryLabelsPanel() {
            super();
            initComponents();
            flag = false;
            previousSelection = "ALL";
        }

        private void initComponents() {

            valueLabel = new JLabel();
            hsLabel = new JLabel();
            elLabel = new JLabel();
            nameLabel = new JLabel();
            priceLabel = new JLabel();
            categoryComboBox = new javax.swing.JComboBox();

            categoryComboBox.setMaximumRowCount(25);

            valueLabel.setText("Value");
            hsLabel.setText("High School");
            elLabel.setText("Elementary");
            nameLabel.setText("Name");
            priceLabel.setText("Price");

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup()
                    .addComponent(valueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(hsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(elLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(priceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(categoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(12, 12, 12)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(valueLabel).addComponent(hsLabel).addComponent(elLabel).addComponent(nameLabel).addComponent(priceLabel).addComponent(categoryComboBox)).addContainerGap()));

            categoryComboBox.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    categoryComboBoxActionPerformed(evt);
                }
            });
        }

        private void categoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
            if (!flag) {
                filterCategory = categoryComboBox.getSelectedItem().toString();
                previousSelection = categoryComboBox.getSelectedItem().toString();
            }
            refresh();
        }

        private void refreshCategories() {
            ResultSet rs = databaseConn.getDistinctCategories();
            try {
                flag = true;
                categoryComboBox.removeAllItems();
                flag = false;
                categoryComboBox.addItem("ALL");
                while (rs.next()) {
                    categoryComboBox.addItem(rs.getString("categoryName"));
                }
                categoryComboBox.setSelectedItem(previousSelection);
            } catch (java.sql.SQLException e) {
            }
        }
    }

    /**
     * customReportsPanel
     */
    public class customReportsPanel extends ReportsPanel {

        private final ReportGenerator reportGenerator;

        customReportsPanel() {
            reportGenerator = new ReportGenerator();
        }

        @Override
        protected void reportHSAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
            reportGenerator.generateReport(allItemsRS, false, true);
        }

        @Override
        protected void reportHSLowButtonActionPerformed(java.awt.event.ActionEvent evt) {
            reportGenerator.generateReport(allItemsRS, true, true);
        }

        @Override
        protected void reportELAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
            reportGenerator.generateReport(allItemsRS, false, false);
        }

        @Override
        protected void reportELLowButtonActionPerformed(java.awt.event.ActionEvent evt) {
            reportGenerator.generateReport(allItemsRS, true, false);
        }
    }
}


