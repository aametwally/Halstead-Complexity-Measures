package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;
import java.lang.Character;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


public class MutualInformationDialog extends JDialog implements ActionListener {
    private AlignmentFrame owner;
    private Alignment alignment;
    private DisplayProperties props;
    private File file;
    private DataInputStream dis;
 
    private JButton ok_button, cancel_button;
    public MutualInformationDialog(AlignmentFrame owner, Alignment alignment, 
        DisplayProperties props, File file) {
        super(owner, "Mutual Information: " + alignment.getName());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        contentPane.setLayout(new BorderLayout());

        this.owner = owner;
        this.alignment = alignment;
        this.props = props;
        this.file = file;
    
        String line = null;
        ArrayList seqNamesList = new ArrayList();
        ArrayList itemsList = new ArrayList();
        HashSet itemsHashSet = new HashSet();
        StringTokenizer st = new StringTokenizer("");
        String dummy;

        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            dis = new DataInputStream(bis);
            while ((line = dis.readLine()) != null) {
                st = new StringTokenizer(line, " \t");
                while (st.hasMoreTokens()) {
                    seqNamesList.add(st.nextToken());
                    itemsList.add(dummy = st.nextToken());
                    itemsHashSet.add(dummy);
                }
            }
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(owner, "Unable to open file stream: ", e);
        } finally {
            if (dis != null) try {
                    dis.close();
                } catch (IOException e) {}
        }
	
        int size = seqNamesList.size();
	
        // error checks
        if (size != itemsList.size()) {
            ErrorDialog.showErrorDialog(owner, 
                "Invalid mutual information file. Sequence name list and " + 
                "item list are not the same length");
            return;
        }
	
        // are all sequence in alignment
        boolean inAlignment = false;
        String[] alignmentSeqNames = alignment.getAllSequenceNames();

        for (int j = 0; j < size; j++) {
            inAlignment = false;
            for (int i = 0; i < alignmentSeqNames.length; i++) {
                if (((String) seqNamesList.get(j)).equals(alignmentSeqNames[i])) inAlignment = true;
            }
            if (!inAlignment) {
                ErrorDialog.showErrorDialog(owner, 
                    "Sequence '" + (String) seqNamesList.get(j) + "' not found in alignment.");
                return;
            }
        }
        String aas = new String();
        int index = 0;
        float[][] freq_table = new float[alignment.minLength()][AminoAcid.NUM_AA];

        for (int i = 0; i < size; i++) {
            aas = alignment.getSequence((String) seqNamesList.get(i)).toString().toUpperCase();
            for (int k = 0; k < alignment.minLength(); k++) {
                freq_table[k][AminoAcid.lookupByCode(String.valueOf(aas.charAt(k))).getIndex()] += (1 / (float) size);
            }
        }
	
        float[] item_freq_table = new float[size];

        for (int i = 0; i < size; i++) 
            item_freq_table[i] += (float) matches(itemsList.get(i), itemsHashSet) / (float) itemsHashSet.size();
	
        float[] info_table = new float[alignment.minLength()];
        int idx;

        for (int k = 0; k < info_table.length; k++)
            for (int i = 0; i < size; i++) {
                aas = alignment.getSequence((String) seqNamesList.get(i)).toString().toUpperCase();
                idx = AminoAcid.lookupByCode(String.valueOf(aas.charAt(k))).getIndex();
                info_table[k] += freq_table[k][idx] * log2(freq_table[k][idx] / item_freq_table[i]);
            }
	
        JPanel overallPanel = new JPanel();
        JPanel subpanel = new JPanel();

        cancel_button = new JButton("Close");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);

        overallPanel.add(subpanel, BorderLayout.SOUTH);
        contentPane.add(overallPanel, BorderLayout.NORTH);
        setSize(150, 400);
        pack(); 
    }

    public double log2(double x) {
        return Math.log(x) / Math.log(2.0);
    }

    public int matches(Object obj, Set s) {
        int m = 0;

        for (Iterator i = s.iterator(); i.hasNext();) {
            if (((String) i.next()).equals(obj)) m++;
        }
        return m;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancel_button) {
            dispose();
        }
    }
}
