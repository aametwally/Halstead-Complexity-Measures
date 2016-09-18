package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


/**
 * Dialog for comparing groups of sequences
 **/

public class GroupComparisonDialog extends JDialog implements ActionListener {
    private AlignmentFrame owner;
    private Alignment alignment;
    private DisplayProperties props;
    private JList jlist1;
    private JList jlist2;
    private JButton ok_button, cancel_button;
    public GroupComparisonDialog(AlignmentFrame owner, Alignment alignment, 
        DisplayProperties props) {
        super(owner, "Group Comparison: " + alignment.getName());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        contentPane.setLayout(new BorderLayout());

        this.owner = owner;
        this.alignment = alignment;
        this.props = props;
		
        JPanel overallPanel = new JPanel();
        JPanel selectionPanel = new JPanel(new GridLayout(1, 2));

        jlist1 = new JList(alignment.getAllSequenceNames());
        jlist2 = new JList(alignment.getAllSequenceNames());
        JPanel selectionPanel1 = new JPanel();
        JPanel selectionPanel2 = new JPanel();
        JScrollPane sp1 = new JScrollPane(jlist1);
        JScrollPane sp2 = new JScrollPane(jlist2);

        selectionPanel1.add(new JLabel("Group 1"), BorderLayout.NORTH);
        selectionPanel1.add(sp1, BorderLayout.NORTH);
        selectionPanel2.add(new JLabel("Group 2"), BorderLayout.NORTH);
        selectionPanel2.add(sp2, BorderLayout.NORTH);
        selectionPanel.add(selectionPanel1, BorderLayout.WEST);
        selectionPanel.add(selectionPanel2, BorderLayout.EAST);
        overallPanel.add(selectionPanel, BorderLayout.NORTH);
		
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
        pack(); 
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            int[] idxs1 = jlist1.getSelectedIndices();
            int[] idxs2 = jlist2.getSelectedIndices();

            if ((idxs1.length < 2) || (idxs2.length < 2)) {
                ErrorDialog.showErrorDialog(owner, 
                    "At least 2 sequences must be selected from each group.");
                return;
            }
            JPanel scroll_grid_panel = new JPanel();
            ScrollableGridLayout layout = 
                new ScrollableGridLayout(scroll_grid_panel, 5, 2, 0, 0);

            layout.setRowFixed(0, true);
            layout.setColumnFixed(0, true);
            ScrollableViewport seqvp1 = new ScrollableViewport();

            seqvp1.setBackingStoreEnabled(false);
            seqvp1.putClientProperty("EnableWindowBlit", null);
            seqvp1.setBackground(Color.white);
            layout.setPosition(seqvp1, 0, 0);
            seqvp1.setView(new JLabel("foo1"));
            scroll_grid_panel.add(seqvp1);
      
            ScrollableViewport seqvp2 = new ScrollableViewport();

            seqvp2.setBackingStoreEnabled(false);
            seqvp2.putClientProperty("EnableWindowBlit", null);
            seqvp2.setBackground(Color.white);
            layout.setPosition(seqvp2, 2, 0);
            seqvp2.setView(new JLabel("foo2"));
            scroll_grid_panel.add(seqvp2);
            scroll_grid_panel.setLayout(layout);
            JDialog f = new JDialog(this, "Group Comparison: " + alignment.getName());

        } else if (e.getSource() == cancel_button)
            dispose();
    }
}	

