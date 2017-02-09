package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.swingx.*;


/**
 * Dialog for seting the start index of a sequence.
 **/
public class StartIndexDialog extends JDialog implements ActionListener {
    private JButton ok_button, cancel_button;
    private Sequence s;
    private JTextField newStartIndextf;
    private Frame owner;
    public StartIndexDialog(Frame owner, Sequence s) {
        super(owner, "Set Start Index: " + s.getName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.s = s;
        this.owner = owner;
        JPanel IndexPanel = new JPanel();

        IndexPanel.add(new JLabel(
                "Previous Start Index: " + s.getStartIndex() + "       New Start Index: "));
        newStartIndextf = new JTextField("  ", 4);
        IndexPanel.add(newStartIndextf);
        getContentPane().add(IndexPanel, BorderLayout.NORTH);
        setLocationRelativeTo(owner);
        JPanel subpanel = new JPanel();

        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
	
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        getContentPane().add(subpanel, BorderLayout.SOUTH);

        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            try {
                int newStartIndex
                    = Integer.parseInt(newStartIndextf.getText().trim());

                if (newStartIndex < 0)
                    ErrorDialog.showErrorDialog(owner, "Start index must be positive.");
                else {
                    s.setStartIndex(newStartIndex);
                    dispose();
                }
            } catch (NumberFormatException exception) {
                ErrorDialog.showErrorDialog(owner, "Invalid start index.");
            }
        } else if (e.getSource() == cancel_button) 
            dispose();
    }

}
