package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


public class GroupDialog extends JDialog implements ActionListener {
    private AlignmentFrame owner;
    private Alignment alignment;
    private DisplayProperties props;
    private JComboBox existingGroupCB;
    private JPanel selectionPanel; 
    private JRadioButton newGroupButton, existingGroupButton;
    
    private JButton ok_button, cancel_button;
    public GroupDialog(AlignmentFrame owner, Alignment alignment, 
        DisplayProperties props) {
        super(owner, "Add Selection to Group: " + alignment.getName());
	
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        contentPane.setLayout(new BorderLayout());
	
        this.owner = owner;
        this.alignment = alignment;
        this.props = props;
	
        JPanel overallPanel = new JPanel(new BorderLayout(3, 1));
        JPanel selectionPanel = new JPanel();

        selectionPanel.add(new JLabel("Group Name:"));
        String[] groupNames = alignment.getAllGroupNames();

        existingGroupCB = groupNames == null ? new JComboBox() : new JComboBox(groupNames);
        existingGroupCB.setEditable(true);
        selectionPanel.add(existingGroupCB);
	
        JPanel subpanel = new JPanel();

        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
	
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        overallPanel.add(selectionPanel, BorderLayout.NORTH);
        overallPanel.add(subpanel, BorderLayout.SOUTH);
        contentPane.add(overallPanel, BorderLayout.NORTH);
        setSize(150, 400);
        setLocationRelativeTo(owner);
        pack(); 
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            String groupName = existingGroupCB.getSelectedItem().toString().trim();      

            if (groupName.length() < 1) {
                ErrorDialog.showErrorDialog(owner, 
                    "Please enter a group name.");
                return;
            } else if (groupName.indexOf(":") != -1) {
                ErrorDialog.showErrorDialog(owner, 
                    "The group name cannot contains colons. Please enter a different group name.");
                return;
            } else if (groupName.equals(alignment.getBackgroundString())) {
                ErrorDialog.showErrorDialog(owner, 
                    "Group name '" + alignment.getBackgroundString() + "' is reserved. Please enter a different group name.");
                return;
            } else {
                Sequence[] seqs = props.getAllSelected();

                for (int i = 0; i < seqs.length; i++) {
                    seqs[i].addGroup(groupName);
                }
                owner.sequenceGroupChanged(seqs[0]);
                dispose();
                owner.getContentPane().validate();
                owner.getContentPane().repaint();
            }
        } else if (e.getSource() == cancel_button) {
            dispose();
            owner.getContentPane().validate();
            owner.getContentPane().repaint();
        }
    }
}

