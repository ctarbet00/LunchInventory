package LunchInventory;

/**
 *
 * @author ctarbet
 */

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridLayout;
import javax.swing.BorderFactory;


public class ReportsPanel extends JPanel {
    private final JLabel instructionsLabel;
    private final JButton hsReportAllButton;
    private final JButton hsReportLowButton;
    private final JButton elReportAllButton;
    private final JButton elReportLowButton;
    
    ReportsPanel(){
        setVisible(true);
        setLayout(new GridLayout(5,1));
        setBorder(BorderFactory.createEmptyBorder(150,150,150,150));

        instructionsLabel = new JLabel("Choose to report all items or only items that have low inventory.");

        hsReportAllButton = new JButton("Report HS All");
        hsReportLowButton = new JButton("Report HS Low");
        elReportAllButton = new JButton("Report EL All");
        elReportLowButton = new JButton("Report EL Low");

        hsReportAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportHSAllButtonActionPerformed(evt);
            }
        });

        hsReportLowButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportHSLowButtonActionPerformed(evt);
            }
        });

        elReportAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportELAllButtonActionPerformed(evt);
            }
        });

        elReportLowButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportELLowButtonActionPerformed(evt);
            }
        });

        add(instructionsLabel);
        add(hsReportAllButton);
        add(hsReportLowButton);
        add(elReportAllButton);
        add(elReportLowButton);
    }

    protected void reportHSAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        System.out.println("reportAllButtonActionPerformed");
    }

    protected void reportHSLowButtonActionPerformed(java.awt.event.ActionEvent evt) {
       System.out.println("reportLowButtonActionPerformed");
    }

    protected void reportELAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        System.out.println("reportAllButtonActionPerformed");
    }

    protected void reportELLowButtonActionPerformed(java.awt.event.ActionEvent evt) {
       System.out.println("reportLowButtonActionPerformed");
    }
}
