package com.neogenesis.pfaat.seqspace;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.*;


/**
 * Dialog for editing sequence data.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:38 $ */
public class SeqSpaceDialog extends JDialog implements ActionListener {
    private Sequence[] seqs;
    private DisplayProperties props;
    private JTextField dim_tf;
    private JRadioButton binary_button, blosum62_button;
    private JButton ok_button, cancel_button;
    private AlignmentFrame owner;

    public SeqSpaceDialog(AlignmentFrame owner, Sequence[] seqs, 
        DisplayProperties props) 
        throws Exception {
        super(owner, "Sequence Space Analysis");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        this.seqs = seqs;
        this.props = props;
        this.owner = owner;

        if (seqs.length < 3) 
            throw new Exception("At least 3 sequences must be used for "
                    + "sequence space analysis.");

        JPanel subpanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        subpanel.setLayout(gb);
        int row = 0;

        Utils.addToGridBag(subpanel, gb, 
            new JLabel(seqs.length + " sequences selected."),
            0, row, 2, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.CENTER);
        row++;

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Number of Dimensions: "),
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        dim_tf = new JTextField(Integer.toString(Math.min(10, seqs.length)),
                    3);
        Utils.addToGridBag(subpanel, gb, 
            dim_tf,
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Encoding: "),
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        ButtonGroup bg = new ButtonGroup();
        JPanel button_panel = new JPanel();

        binary_button = new JRadioButton("Binary", true);
        bg.add(binary_button);
        button_panel.add(binary_button);
        blosum62_button = new JRadioButton("Blosum62");
        bg.add(blosum62_button);
        button_panel.add(blosum62_button);
        Utils.addToGridBag(subpanel, gb, 
            button_panel,
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        getContentPane().add(subpanel, BorderLayout.CENTER);

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
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            int n_dims;

            try {
                n_dims = Integer.parseInt(dim_tf.getText());
            } catch (NumberFormatException exp) {
                ErrorDialog.showErrorDialog(this, 
                    "Number of dimensions must be an "
                    + "integer.");
                return;
            }
            if (n_dims < 3 || n_dims > seqs.length) {
                ErrorDialog.showErrorDialog(this, 
                    "Number of dimensions must be "
                    + "between 3 and " + seqs.length
                    + ".");
                return;
            }
		
            SeqSpaceWorker ssw = 
                new SeqSpaceWorker(owner,
                    seqs,
                    n_dims,
                    blosum62_button.isSelected(),
                    props);

            ssw.go();
            dispose();
        } else if (e.getSource() == cancel_button) 
            dispose();
    }

}
