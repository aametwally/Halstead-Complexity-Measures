package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.text.*;

import com.neogenesis.pfaat.seqspace.*;
import com.neogenesis.pfaat.print.*;
import com.neogenesis.pfaat.util.*;


/**
 * A frame for sequence comparisons.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class PairwiseAlignmentFrame extends JFrame {
    public PairwiseAlignmentFrame(Sequence[] seqs) {
        super("Pairwise Alignment");
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            }
        );
        getContentPane().setLayout(new BorderLayout());
	
        int max_name_len = 0;

        for (int i = seqs.length - 1; i >= 0; i--) 
            max_name_len = Math.max(seqs[i].getName().length(), max_name_len);
	    
        JTextArea text_area = new JTextArea();

        text_area.setFont(new Font("Courier", Font.PLAIN, 10));
        text_area.setLineWrap(false);
        text_area.setTabSize(max_name_len + 1);
        Utils.addClipboardBindings(text_area);
        SeqSpaceEncoder encoder = new Pam250Encoder();
        DecimalFormat percent_df = new DecimalFormat("0.00%");
        StringBuffer text_sb = new StringBuffer();

        for (int i = 0; i < seqs.length; i++) { 
            for (int j = i + 1; j < seqs.length; j++) {
                text_sb.append(seqs[i].getName() + "\t");
                text_sb.append(seqs[i].toString() + "\n\t");
                int len_i = seqs[i].length();
                int len_j = seqs[j].length();
                int len = Math.max(len_i, len_j);

                for (int k = 0; k < len; k++) {
                    AminoAcid aa_i = k < len_i ? seqs[i].getAA(k) 
                        : AminoAcid.GAP;
                    AminoAcid aa_j = k < len_j ? seqs[j].getAA(k)
                        : AminoAcid.GAP;

                    if (aa_i.equals(aa_j) && !aa_i.isGap())
                        text_sb.append("|");
                    else if (encoder.getPairScore(aa_i, aa_j) > 0
                        && !(aa_i.isGap() && aa_j.isGap()))
                        text_sb.append(".");
                    else
                        text_sb.append(" ");
                }
                text_sb.append("\n");
                text_sb.append(seqs[j].getName() + "\t");
                text_sb.append(seqs[j].toString() + "\n");
                text_sb.append("Percentage Identity = "
                    + percent_df.format(seqs[i].getPID(seqs[j]))
                    + "\n\n");
            }
        }
        text_area.append(text_sb.toString());

        getContentPane().add(new JScrollPane(text_area), BorderLayout.CENTER);
	
        setSize(640, 580);
    }

}
	
