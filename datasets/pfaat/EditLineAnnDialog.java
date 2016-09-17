package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


/**
 * Dialog for editing line annotations.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class EditLineAnnDialog extends JDialog implements ActionListener {
    private FixedWidthTextField old_seq, ann_name, ann_text;
    private JButton ok_button, cancel_button;
    private Sequence s;
    private int idx;
		
    public EditLineAnnDialog(Frame owner, Sequence s, int idx) {
        super(owner, "Edit Line Annotation: " + s.getName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.s = s;
        this.idx = idx;
        JPanel subpanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        subpanel.setLayout(gb);

        String seq = s.toString();

        Sequence.LineAnnotation la = idx >= 0 ? s.getLineAnnotation(idx) 
            : null;

        Utils.addToGridBag(subpanel, gb, 		     
            new JLabel("Annotation Name: "),
            0, 0, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);

        ann_name = new FixedWidthTextField(la != null ? la.getName() : "",
                    la != null ? Math.max(la.getName().length(),
                        25)
                    : 25);
        Utils.addToGridBag(subpanel, gb, 
            ann_name,
            1, 0, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);
	
        StringBuffer laText = new StringBuffer(la == null ? "" : la.getAnnotation());
        StringBuffer newLaText = new StringBuffer("");

        for (int i = 0; i < seq.length(); i++) newLaText.append(' ');
        for (int i = 0; i < laText.length(); i++) 
            newLaText.setCharAt(i, laText.charAt(i));
		
        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Annotation Text: "),
            0, 1, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        ann_text = new FixedWidthTextField(newLaText.toString(), seq.length());

        Utils.addToGridBag(subpanel, gb, 
            ann_text,
            1, 1, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Sequence: "),
            0, 2, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        old_seq = new FixedWidthTextField(seq, seq.length());
        old_seq.setEditable(false);
        Utils.addToGridBag(subpanel, gb, 
            old_seq,
            1, 2, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);
	
        Utils.addClipboardBindings(old_seq);
        Utils.addClipboardBindings(ann_name);
        Utils.addClipboardBindings(ann_text);

        JScrollPane jsp = 
            new JScrollPane(subpanel, 
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        jsp.getHorizontalScrollBar().setUnitIncrement(ann_text.getCharWidth());

        getContentPane().add(jsp, BorderLayout.CENTER);

        subpanel = new JPanel();
        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        getContentPane().add(subpanel, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        pack();
        Dimension d = getSize();

        setSize(600, d.height);
    }

    public StringBuffer trimEnd(StringBuffer sb) {
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') { 
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            String name = ann_name.getText();

            if (name.length() < 1) {
                ErrorDialog.showErrorDialog(this, 
                    "Annotation must have a name.");
                return;
            }
            String ann = trimEnd(new StringBuffer(ann_text.getText())).toString();
	   
            if (idx >= 0) {
                if (ann.length() < 1)
                    s.deleteLineAnnotation(idx);
                else 
                    s.setLineAnnotation(idx, name, ann);
            } else {
                if (ann.length() < 1) {
                    ErrorDialog.showErrorDialog(this, 
                        "Annotation cannot be empty.");
                    return;
                }
                s.addLineAnnotation(name, ann);
            }
            dispose();
        } else if (e.getSource() == cancel_button) 
            dispose();
    }

}
