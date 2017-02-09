package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.util.*;


/**
 * Dialog for coloring a sequence.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class ColorSequenceDialog extends JDialog implements ActionListener {
    private JButton ok_button, cancel_button;
    private Sequence s;
    private JColorChooser jcc;
    private int idx;

    public ColorSequenceDialog(Frame owner, Sequence s) {
        super(owner, "Color Sequence");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.s = s;

        buildUI();

        setLocationRelativeTo(owner);
        pack();
    }

    private void buildUI() {
        JPanel subpanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        subpanel.setLayout(gb);

        int row = 0;

        Utils.addToGridBag(subpanel, gb, 
            new JLabel("Name: "),
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        Utils.addToGridBag(subpanel, gb, 
            new JLabel(s.getName()),
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        row++;

        jcc = new JColorChooser();
        jcc.setColor(s.getColor() != null ? s.getColor() : Color.black);
        Utils.addToGridBag(subpanel, gb, 
            jcc,
            0, row, 2, 1, 1, 1, 
            GridBagConstraints.BOTH, GridBagConstraints.CENTER);
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

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) {
            Color c = jcc.getColor();

            s.setColor(c);
            dispose();
        } else if (e.getSource() == cancel_button) 
            dispose();
    }

}
