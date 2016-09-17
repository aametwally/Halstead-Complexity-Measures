package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.apache.oro.text.perl.*;
import org.apache.oro.text.regex.*; 
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


/**
 * Dialog for residue searching.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class FindDialog extends JDialog implements ActionListener {
    private JTextField search_tf;
    private JCheckBox regexp_box, gaps_box;
    private JButton search_button, find_next_button, cancel_button;
    private Alignment alignment;
    private DisplayProperties props;
    private Perl5Util perl = new Perl5Util();
    private AlignmentFrame owner;
    private LinkedList match_list = new LinkedList();

    public FindDialog(AlignmentFrame owner, 
        DisplayProperties props,
        Alignment alignment) {
        super(owner, "Find: " + alignment.getName());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.owner = owner;
        this.props = props;
        this.alignment = alignment;

        JPanel subpanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        subpanel.setLayout(gb);

        int row = 0;

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Search String: "),
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        search_tf = new FixedWidthTextField("", 25);
        Utils.addClipboardBindings(search_tf);
        Utils.addToGridBag(subpanel, gb, 
            search_tf,
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;
	
        regexp_box = new JCheckBox("Perl Regular Expression", false);
        Utils.addToGridBag(subpanel, gb, 
            regexp_box,
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        gaps_box = new JCheckBox("Include Gaps", false);
        Utils.addToGridBag(subpanel, gb, 
            gaps_box,
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        row++;

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Amino acids must be in upper case, "
                + "with gaps represented by \"-\"."),
            0, row, 2, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        row++;

        getContentPane().add(subpanel, BorderLayout.CENTER);

        subpanel = new JPanel();
        search_button = new JButton("Find");
        search_button.addActionListener(this);
        subpanel.add(search_button);
        find_next_button = new JButton("Find Next");
        find_next_button.addActionListener(this);
        subpanel.add(find_next_button);
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        getContentPane().add(subpanel, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        pack();
    }

    private static class Match {
        public int seq_idx, res_idx;
        public Match(int seq_idx, int res_idx) {
            this.seq_idx = seq_idx;
            this.res_idx = res_idx;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == search_button 
            || (e.getSource() == find_next_button && match_list.isEmpty())) {
            boolean is_regexp = regexp_box.isSelected();
            boolean search_gaps = gaps_box.isSelected();
            String search_string = search_tf.getText();

            // sanity checks on the search string
            if (search_string.length() < 1) {
                ErrorDialog.showErrorDialog(this, 
                    "Please enter a search string.");
                return;
            }

            StringBuffer sb = new StringBuffer("/");

            if (!is_regexp) {
                for (int i = 0; i < search_string.length(); i++) {
                    String c = search_string.substring(i, i + 1);
                    AminoAcid aa = AminoAcid.lookupByCode(c);

                    if (aa == null) {
                        ErrorDialog.showErrorDialog(this, 
                            c + " is not an amino "
                            + "acid character.");
                        return;
                    }
                    if (!search_gaps && aa.isGap()) {
                        ErrorDialog.showErrorDialog(this, 
                            "Gap detected in "
                            + "search string. Please "
                            + "use the Search Gaps "
                            + "option.");
                        return;
                    }
                    sb.append(aa.getCode());
                }
            } else
                sb.append(search_string);
            sb.append("/");
            search_string = sb.toString();
	    
            // perform the search
            props.clearHighlights();
            match_list.clear();
            try {
                for (int i = 0; i < alignment.size(); i++) {
                    Sequence seq = alignment.getSequence(i);
                    PatternMatcherInput line = 
                        new PatternMatcherInput(search_gaps 
                            ? seq.toString() 
                            : seq.toRawString());

                    while (perl.match(search_string, line)) {
                        int offset = perl.beginOffset(0);

                        if (!search_gaps) 
                            offset = seq.getAlignedIndex(offset);
                        match_list.add(new Match(i, offset));
                        props.setSeqHighlight(seq, 
                            offset,
                            true);
                    }
                }
            } catch (MalformedPerl5PatternException exp) {
                ErrorDialog.showErrorDialog(this, 
                    "Malformed Perl5 "
                    + "expression",
                    exp);
                return;
            }

            if (match_list.isEmpty()) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, "No matches found for expression: "
                    + search_string + " .");
            } else {
                Match first = (Match) match_list.removeFirst();

                owner.setAlignmentPanelPosition(first.seq_idx, 
                    first.res_idx);
            }
        } else if (e.getSource() == find_next_button) {
            if (match_list.isEmpty()) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                Match first = (Match) match_list.removeFirst();

                owner.setAlignmentPanelPosition(first.seq_idx, 
                    first.res_idx);
            }
        } else if (e.getSource() == cancel_button) 
            dispose();
    }

}
