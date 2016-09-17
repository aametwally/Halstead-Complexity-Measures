package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.seqspace.*;


public class RemoveRedundantSequencesDialog extends JDialog implements ActionListener {
    private JButton ok_button, cancel_button;
    private Alignment alignment;
    private DisplayProperties props;
    private AlignmentFrame owner;
    private FixedWidthTextField pidtf;
    private Sequence[] seqs;
    public RemoveRedundantSequencesDialog(AlignmentFrame owner, 
        Alignment alignment,	
        DisplayProperties props) {
        super(owner, "Remove Redundant Sequences: " + alignment.getName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
	
        this.owner = owner;
        this.props = props;
        this.alignment = alignment;
	
        seqs = alignment.getAllSequences();
	
        JPanel panel = new JPanel();
        JPanel toppanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        panel.setLayout(gb);

        pidtf = new FixedWidthTextField("", 4);

        toppanel.add(new JLabel("Select percentage identity threshhold:"));
        toppanel.add(pidtf);
        toppanel.add(new JLabel("%"));
	
        JPanel buttonpanel = new JPanel();

        ok_button = new JButton("Remove Sequences");
        ok_button.addActionListener(this);
        buttonpanel.add(ok_button);

        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        buttonpanel.add(cancel_button);

        int row = 0;

        Utils.addToGridBag(panel, gb, 
            toppanel,
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.WEST);

        row++;
	
        Utils.addToGridBag(panel, gb, 
            buttonpanel,
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
	
        getContentPane().add(panel, BorderLayout.NORTH);

        setLocationRelativeTo(owner);
        pack();
    }
     
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            String textnum = pidtf.getText();
            double cutoff;

            try {
                cutoff = Double.parseDouble(textnum) / 100.0;
            } catch (NumberFormatException exp) {
                ErrorDialog.showErrorDialog(this, 
                    "Please specify a number between 0 and 100");
                return;
            }

            if (cutoff < 0 || cutoff > 100) {
                ErrorDialog.showErrorDialog(this, 
                    "Please specify a number between 0 and 100.");
                return;
            }
	    
            HashSet deletes = new HashSet();

            for (int i = seqs.length - 1; i >= 0; i--) {
                for (int j = i; j >= 0; j--) {
                    if (seqs[i].getPID(seqs[j]) < cutoff) 
                        deletes.add(new Integer(j));
                }
            }
	    
            String[] deleteSeqNames = new String[deletes.size()];
            int m = 0;

            for (Iterator i = deletes.iterator(); i.hasNext();) {
                deleteSeqNames[m] = 
                        alignment.getSequence(Integer.parseInt(i.next().toString())).getName();
                m++;
            }
	    
            JTextArea seqsta = new JTextArea(5, 20);
	    
            for (int i = 0; i < deleteSeqNames.length; i++)
                seqsta.append(deleteSeqNames[i] + "\n");
	    
            JPanel mssgpanel = new JPanel();

            GridBagLayout gb2 = new GridBagLayout();

            mssgpanel.setLayout(gb2);
	    
            int row = 0;

            Utils.addToGridBag(mssgpanel, gb2, 
                new JLabel("Remove " + deletes.size() + " sequences?"),	
                0, row, 1, 1, 1, 1, 
                GridBagConstraints.BOTH, GridBagConstraints.NORTH);
            row++;
	    
            Utils.addToGridBag(mssgpanel, gb2, 
                new JScrollPane(seqsta, 
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),
                0, row, 1, 1, 1, 1, 
                GridBagConstraints.BOTH, GridBagConstraints.SOUTH);	    
	    
            if (JOptionPane.showConfirmDialog(owner,
                    mssgpanel,
                    "Remove Redundant Sequences",
                    JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {
		
                for (int i = 0; i < deleteSeqNames.length; i++)
                    alignment.deleteSequence(alignment.getSequence(deleteSeqNames[i]));
                dispose();
            } else 
                return; 
        } else if (e.getSource() == cancel_button) 
            dispose();
	
    }
}
