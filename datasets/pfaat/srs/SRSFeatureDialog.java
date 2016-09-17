package com.neogenesis.pfaat.srs;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.*;


/**
 * Dialog for importing SRS features.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:55 $ */
public class SRSFeatureDialog extends JDialog {
    private Sequence sequence;
    private SRSEntry entry;
    private JTable table;
    private String[] symbols;

    public SRSFeatureDialog(Frame owner, Sequence sequence, SRSEntry entry) {
        super(owner, "SRS Annotation: " + sequence.getName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.sequence = sequence;
        this.entry = entry;
        symbols = new String[entry.getFeatureCount()];
        Arrays.fill(symbols, "");

        table = new JTable(new FeatureTableModel());
        JScrollPane jsp = new JScrollPane(table);

        getContentPane().add(jsp, BorderLayout.CENTER);

        JPanel subpanel = new JPanel();
        JButton import_button = new JButton("Import");

        import_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Sequence s = SRSFeatureDialog.this.sequence;
                    String[] aa_sym = 
                        new String[s.length()];
                    int max = -1;

                    for (int i = 0; i < symbols.length; i++) {
                        if (symbols[i].length() != 1)
                            continue;
                        SRSEntry.Feature f = SRSFeatureDialog.this.entry.getFeature(i);

                        for (int k = f.getStart(); k <= f.getEnd(); k++) 
                            aa_sym[s.getAlignedIndex(k)] = symbols[i];
                        max = Math.max(s.getAlignedIndex(f.getEnd()), max);
                    }
                    if (max < 0)
                        return;
                    StringBuffer sb = new StringBuffer();

                    for (int i = 0; i <= max; i++)
                        sb.append(aa_sym[i] == null ? " " : aa_sym[i]);
                    s.addLineAnnotation("SRS", sb.toString());
                }

            }
        );
        subpanel.add(import_button);
        JButton cancel_button = new JButton("Cancel");

        cancel_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            }
        );
        subpanel.add(cancel_button);

        getContentPane().add(subpanel, BorderLayout.SOUTH);

        pack();
    }

    private class FeatureTableModel extends AbstractTableModel {
        public String getColumnName(int col) {
            switch (col) {
            case 0:
                return "Symbol";

            case 1:
                return "Type";

            case 2:
                return "Start";

            case 3:
                return "End";

            default:
                return "Description";
            }
        }

        public int getRowCount() {
            return symbols.length;
        }

        public int getColumnCount() {
            return 5;
        }

        public Class getColumnClass(int col) { 
            return getValueAt(0, col).getClass();
        }

        public Object getValueAt(int row, int col) {
            SRSEntry.Feature f = entry.getFeature(row);

            switch (col) {
            case 0:
                return symbols[row];

            case 1:
                return f.getType();

            case 2:
                return new Integer(f.getStart() + 1);

            case 3:
                return new Integer(f.getEnd() + 1);

            default:
                return f.getDescription();
            }
        }

        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        public void setValueAt(Object value, int row, int col) {
            switch (col) {
            case 0:
                String symbol = (String) value;

                if (symbol.length() > 1)
                    ErrorDialog.showErrorDialog(SRSFeatureDialog.this,
                        "Symbol must be a single "
                        + "character.");
                else {
                    symbols[row] = symbol;
                    fireTableCellUpdated(row, col);
                }
                break;
            }
        }
    }

}
