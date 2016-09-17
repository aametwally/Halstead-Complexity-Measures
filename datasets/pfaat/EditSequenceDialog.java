package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


/**
 * Dialog for editing sequence data.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class EditSequenceDialog extends JDialog implements ActionListener {
    private FixedWidthTextField seq_name, old_seq, new_seq;
    private JButton ok_button, cancel_button;
    private StringBuffer newSeq;
    private Sequence s;
    private Alignment a;
    private int idx;

    public EditSequenceDialog(Frame owner, Sequence s) {
        super(owner, "Edit Sequence");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.s = s;
        this.a = null;
        this.idx = -1;

        buildUI();

        setLocationRelativeTo(owner);
        pack();
        Dimension d = getSize();

        setSize(600, d.height);
    }

    public EditSequenceDialog(Frame owner, Sequence s, Alignment a, int idx) {
        super(owner, "Insert New Sequence");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.s = s;
        this.a = a;
        this.idx = idx;

        buildUI();

        setLocationRelativeTo(owner);
        pack();
        Dimension d = getSize();

        setSize(600, d.height);
    }

    private void buildUI() {
        JPanel subpanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        subpanel.setLayout(gb);

        int row = 0;

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Name:"),
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        seq_name = new FixedWidthTextField(s != null ? s.getName() : "", 
                    s != null 
                    ? Math.max(s.getName().length(),
                        25)
                    : 25);
        Utils.addToGridBag(subpanel, gb, 
            seq_name,
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        row++;

        String seq;

        if (s != null) {
            Utils.addToGridBag(subpanel, gb, 
                new JLabel("Original:"),
                0, row, 1, 1, 1, 1, 
                GridBagConstraints.NONE, 
                GridBagConstraints.EAST);
            seq = s.toString();
            old_seq = new FixedWidthTextField(seq, seq.length());
            old_seq.setEditable(false);
            Utils.addToGridBag(subpanel, gb, 
                old_seq,
                1, row, 1, 1, 1, 1, 
                GridBagConstraints.NONE, 
                GridBagConstraints.WEST);
            row++;
			
            if (idx == -1)
                newSeq = new StringBuffer(seq);
            else {
                newSeq = new StringBuffer("");
                for (int i = 0; i < seq.length(); i++) newSeq.append('-');
            }
        } else {
            seq = "";
            old_seq = null;
        }

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Sequence:"),
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        new_seq = new FixedWidthTextField(newSeq.toString(), 
                    a != null ? Math.max(seq.length(), 
                        a.maxLength())
                    : seq.length());
        Utils.addToGridBag(subpanel, gb, 
            new_seq,
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        row++;

        Utils.addClipboardBindings(seq_name);
        Utils.addClipboardBindings(old_seq);
        Utils.addClipboardBindings(new_seq);

        JScrollPane jsp = 
            new JScrollPane(subpanel, 
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        jsp.getHorizontalScrollBar().setUnitIncrement(new_seq.getCharWidth());

        getContentPane().add(jsp, BorderLayout.CENTER);

        subpanel = new JPanel();
        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        getContentPane().add(subpanel, BorderLayout.SOUTH);

    }
		
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            String name = seq_name.getText();

            if (name.length() < 1) {
                ErrorDialog.showErrorDialog(this, 
                    "Sequence must have a name.");
                return;
            }
            String data = new_seq.getText();

            if (data.length() < 1) {
                ErrorDialog.showErrorDialog(this, 
                    "Sequence must have data.");
                return;
            }	    
            AminoAcid[] aa = AminoAcid.stringToAA(data);

            if (idx == -1) {
                try {
                    s.setName(name);
                    s.setAA(aa);
                } catch (Exception exp) {
                    ErrorDialog.showErrorDialog(this, "Unable to edit "
                        + "sequence.",
                        exp);
                    return;
                }
            } else {
                boolean isSeqNameInAlignment = false;

                for (int i = 0; i < a.size(); i++)
                    if (a.getSequence(i).getName().equals(name))
                        isSeqNameInAlignment = true;
                if (isSeqNameInAlignment) {
                    ErrorDialog.showErrorDialog(this, "Sequence name "
                        + name + " already in alignment");
                    return;
                } else {	
                    Sequence seq = new Sequence(name, aa);

                    try {
                        a.insertSequence(idx, seq);
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(this, "Unable to insert new "
                            + "sequence.",
                            exp);
                        return;
                    }
                }
            }
            dispose();
        } else if (e.getSource() == cancel_button) 
            dispose();
    }

}
