package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.text.*;

import com.neogenesis.pfaat.seqspace.*;
import com.neogenesis.pfaat.util.*;


/**
 * A frame for sequence comparisons.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:02 $ */
public class ComparisonFrame extends JFrame {
    private static final int FIELD_LENGTH = 10;
    private static final DecimalFormat FIELD_DF = new DecimalFormat("0.0000");
    private String adjustString(String s) {
        StringBuffer sb = new StringBuffer();

        if (s.length() > FIELD_LENGTH)
            sb.append(s.substring(0, FIELD_LENGTH));
        else
            sb.append(s);
        sb.append("\t");
        return sb.toString();
    }

    public ComparisonFrame(Sequence[] seqs, boolean use_blosum) {
        super("Sequence Comparison: " + (use_blosum ? "Blosum62" : "PID"));
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            }
        );
        getContentPane().setLayout(new BorderLayout());

        // compute the comparison matrix
        double[][] comparison = new double[seqs.length][seqs.length];
        SeqSpaceEncoder encoder = new Blosum62Encoder();

        for (int i = seqs.length - 1; i >= 0; i--) {
            for (int j = i; j >= 0; j--) {
                comparison[i][j] = comparison[j][i] = use_blosum
                                ? seqs[i].getScore(encoder, seqs[j])
                                : seqs[i].getPID(seqs[j]);
            }
        }

        // xih: in the case of blosum62 covert comparison scores to percentage similarity
        if (use_blosum) {
            // copy the diagonal value of the matrix to a temporary array
            double[] diag = new double[seqs.length];

            for (int i = seqs.length - 1; i >= 0; i--)
                diag[i] = comparison[i][i];

            for (int i = seqs.length - 1; i >= 0; i--) {
                for (int j = i; j >= 0; j--) {
                    comparison[j][i] = comparison[i][j] = comparison[i][j] / Math.sqrt(diag[i] * diag[j]);
                }
            }
        }

        JTextArea text_area = new JTextArea();

        text_area.setFont(new Font("Courier", Font.PLAIN, 10));
        text_area.setLineWrap(false);
        text_area.setTabSize(FIELD_LENGTH + 1);
        Utils.addClipboardBindings(text_area);
        // print header row
        text_area.append(adjustString(""));
        for (int i = 0; i < seqs.length; i++)
            text_area.append(adjustString(seqs[i].getName()));
        text_area.append("\n");
        // print data rows
        for (int i = 0; i < seqs.length; i++) {
            text_area.append(adjustString(seqs[i].getName()));
            for (int j = 0; j < seqs.length; j++) {
                text_area.append(adjustString(FIELD_DF.format(comparison[i][j])));
            }
            text_area.append("\n");
        }

        getContentPane().add(new JScrollPane(text_area), BorderLayout.CENTER);

        setSize(640, 580);
    }

}

