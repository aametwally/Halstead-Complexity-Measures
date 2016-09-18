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


public class RemoveGroupDialog extends JDialog implements ActionListener {
    private AlignmentFrame owner;
    private Alignment alignment;
    private DisplayProperties props;
    private JComboBox existingGroupCB;
    private JPanel selectionPanel; 
    
    private JButton ok_button, cancel_button;
    public RemoveGroupDialog(AlignmentFrame owner, Alignment alignment, 
        DisplayProperties props) {
        super(owner, "Remove from Group: " + alignment.getName());
	
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        contentPane.setLayout(new BorderLayout());
	
        this.owner = owner;
        this.alignment = alignment;
        this.props = props;
	
        JPanel overallPanel = new JPanel(new BorderLayout(1, 2));

        selectionPanel = new JPanel(new GridLayout(1, 2));
	
        Sequence[] seqs = props.getAllSelected();
        HashSet uniqueGroupNames = new HashSet();

        for (int i = 0; i < seqs.length; i++) {
            String[] groupNames = seqs[i].getGroupNames();

            if (groupNames != null)
                for (int j = 0; j < groupNames.length; j++)
                    uniqueGroupNames.add(new String(groupNames[j]));
        }
	
        existingGroupCB = new JComboBox(uniqueGroupNames.toArray());
	
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
            String groupName = existingGroupCB.getSelectedItem().toString();   
            Sequence[] seqs = props.getAllSelected();

            for (int i = 0; i < seqs.length; i++) {
                seqs[i].removeGroup(groupName);
            }
            owner.sequenceGroupChanged(seqs[0]);
            dispose();
            owner.getContentPane().repaint();
            owner.getContentPane().validate();
        } else if (e.getSource() == cancel_button)
            dispose();
        owner.getContentPane().repaint();
        owner.getContentPane().validate();
    }
}

