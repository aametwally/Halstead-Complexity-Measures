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


/**
 * Dialog for extracting profile from expression
 **/

public class ExtractProfileDialog extends JDialog implements ActionListener {
    private AlignmentFrame owner;
    private Alignment alignment;
    private DisplayProperties props;
    private JPanel selectionPanel; 
    private JRadioButton jrb1, jrb2, jrb3, jrb4;
    private JTextArea textarea;
    private JScrollBar hsb;
    private JButton ok_button, cancel_button;

    private static final int SELCOLS = 0;
    private static final int ALLCOLS = 1;
    private static final int SELSEQS = 0;
    private static final int ALLSEQS = 1;
  
    public ExtractProfileDialog(AlignmentFrame owner, Alignment alignment, 
        DisplayProperties props) {
        super(owner, "Extract Regular Expression: " + alignment.getName());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        contentPane.setLayout(new BorderLayout());

        this.owner = owner;
        this.alignment = alignment;
        this.props = props;
		
        JPanel overallPanel = new JPanel();

        overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
        JPanel selectionPanel = new JPanel();

        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        ButtonGroup bg = new ButtonGroup();

        jrb1 = new JRadioButton("selected columns, selected sequences", false);
        jrb2 = new JRadioButton("selected columns, all sequences", false);
        jrb3 = new JRadioButton("all columns, selected sequences", false);
        jrb4 = new JRadioButton("all columns, all sequences", true);
    
        bg.add(jrb1);
        bg.add(jrb2);
        bg.add(jrb3);
        bg.add(jrb4);
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1));

        buttonPanel.add(new JLabel("Selection Scope"));
        buttonPanel.add(jrb1);
        buttonPanel.add(jrb2);
        buttonPanel.add(jrb3);
        buttonPanel.add(jrb4);
        selectionPanel.add(buttonPanel);
	  
        JPanel subpanel = new JPanel();

        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.X_AXIS));
        ok_button = new JButton("Extract Regular Expression");
        ok_button.addActionListener(this);
        ok_button.setAlignmentX(Component.LEFT_ALIGNMENT);
        subpanel.add(ok_button);
		
        cancel_button = new JButton("Close");
        cancel_button.addActionListener(this);
        cancel_button.setAlignmentX(Component.LEFT_ALIGNMENT);
        subpanel.add(cancel_button);
    
        JPanel textpanel = new JPanel(new GridLayout(2, 1));

        textpanel.add(new JLabel("Regular Expression:"));
        textarea = new JTextArea(1, 30);
        textarea.setFont(new Font("Courier", Font.PLAIN, 12));
        Utils.addClipboardBindings(textarea);
        JScrollPane scrollpane = 
            new JScrollPane(textarea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        hsb = scrollpane.getHorizontalScrollBar();
        textpanel.add(scrollpane);
    
        overallPanel.add(selectionPanel, BorderLayout.NORTH);
        overallPanel.add(subpanel, BorderLayout.WEST);
        overallPanel.add(textpanel, BorderLayout.SOUTH);
        contentPane.add(overallPanel);
        setSize(150, 400);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            if (jrb1.isSelected()) {
                // select cols, select seqs
                if (!alignment.isColumnSelected()) {
                    ErrorDialog.showErrorDialog(owner, 
                        "Please select at least one column.");
                    return;
                } else if (!props.isSequenceSelected()) {
                    ErrorDialog.showErrorDialog(owner, 
                        "Please select at least one sequence.");
                    return;
                } else { 
                    textarea.setText(ExtractProfile(SELCOLS, SELSEQS));
                    hsb.setValue(0);
                    return; 
                }
            } else if (jrb2.isSelected()) {
                // select cols, all seqs
                if (!alignment.isColumnSelected()) {
                    ErrorDialog.showErrorDialog(owner, 
                        "Please select at least one column.");
                    return;
                } else { 
                    textarea.setText(ExtractProfile(SELCOLS, ALLSEQS));
                    hsb.setValue(0);
                    return; 
                }
            } else if (jrb3.isSelected()) {
                // all cols, select seqs
                if (!props.isSequenceSelected()) {
                    ErrorDialog.showErrorDialog(owner, 
                        "Please select at least one sequence.");
                    return;
                } else { 
                    textarea.setText(ExtractProfile(ALLCOLS, SELSEQS));
                    hsb.setValue(0);
                    return; 
                }
            } else if (jrb4.isSelected()) {
                // all cols, all seqs
                textarea.setText(ExtractProfile(ALLCOLS, ALLSEQS));
                hsb.setValue(0);
                return;
            }
        } else if (e.getSource() == cancel_button) {
            dispose();
            owner.getContentPane().validate();
            owner.getContentPane().repaint();
        }
    }

    public String ExtractProfile(int colScope, int seqScope) {
        StringBuffer aasb = new StringBuffer("");
        String[] globalAA = new String[alignment.size()];
        StringBuffer rawString;
        HashSet aaSet;

        for (int row = 0; row < alignment.size(); row++) 
            globalAA[row] = alignment.getSequence(row).toString();
          
        for (int i = 1; i < alignment.maxLength(); i++) { 
            aaSet = new HashSet();
            if ((colScope == SELCOLS && alignment.isColumnSelected(i)) || colScope == ALLCOLS) {
                for (int row = 0; row < alignment.size(); row++) 
                    if ((seqScope == SELSEQS && props.isSequenceSelected(row)) || seqScope == ALLSEQS) 
                        if (row < globalAA.length && i < globalAA[row].length()) 
                            aaSet.add(String.valueOf(globalAA[row].charAt(i)));
            
                // remove all commas and whitespaces
                rawString = new StringBuffer(aaSet.toString().replace(',', ' ').replace('-', '.'));
                for (int m = rawString.length() - 1; m >= 0; m--) 
                    if (rawString.charAt(m) == ' ') rawString.deleteCharAt(m);
          
                // remove brackets from [singlechar]
                if (rawString.length() == 3) {
                    rawString.deleteCharAt(0);
                    rawString.deleteCharAt(1);
                }
          
                aasb.append(new String(rawString.toString()));
            }
        }
        return aasb.toString();
    }
}
