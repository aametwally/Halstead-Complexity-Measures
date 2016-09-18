package com.neogenesis.pfaat;


import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ChoosePdbChainDialog extends JDialog implements ActionListener {
    private String selectedChain = null;
    private JComboBox select_pdb_chain;
    private JButton button_ok, button_cancel;
    public ChoosePdbChainDialog(Frame owner, String[] names) {
        super(owner, "Add Chain to Alignment");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // getContentPane().setLayout(new BoxLayout(), );

        // build GUI
        // components
        select_pdb_chain = new JComboBox(names);
        button_ok = new JButton("OK");
        button_cancel = new JButton("Cancel");
        button_ok.addActionListener(this);
        button_cancel.addActionListener(this);

        /* Layout the param panel
         JPanel paramPanel = new JPanel();
         paramPanel.setPreferredSize(new Dimension(100,50));
         paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));
         paramPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
         */
        // selection
        JPanel panel = new JPanel();

        panel.add(new JLabel("Choose chain:"));
        panel.add(select_pdb_chain);

        // buttons
        // Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(button_ok);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(button_cancel);

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();

        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        setModal(true);
        pack();
        setSize(200, 100);
    }

    public String getSelectedChain() {
        return selectedChain;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button_cancel) {
            selectedChain = null;
            dispose();
        } else if (e.getSource() == button_ok) {
            selectedChain = (String) select_pdb_chain.getSelectedItem();
            dispose();
        }
    }

}
