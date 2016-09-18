package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.*;


/**
 * Dialog for editing residue annotations.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/11/20 14:22:55 $ */
public class EditResAnnDialog extends JDialog implements ActionListener {
    private JTextField res_ann_tf, symbol_tf;
    private JLabel res_label, aln_idx, res_idx;
    private JButton ok_button, cancel_button, delete_button;
    private int idx;
    private JComboBox existing_ann_box;
    private ArrayList existing_annotations;
    private JColorChooser jcc;
    private Frame owner;
    private Sequence s;

    public EditResAnnDialog(Frame owner,
        DisplayProperties props,
        Alignment alignment,
        Sequence s,
        int idx) {
        super(owner, "Edit Residue Annotation: " + s.getName()
            + " Residue " + idx);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.s = s;
        this.idx = idx;
        this.owner = owner;
        Sequence.ColumnAnnotation ca = s.getColumnAnnotation(idx);

        JPanel subpanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        subpanel.setLayout(gb);

        int row = 0;

        Utils.addToGridBag(subpanel, gb,
            new JLabel("Sequence: "),
            0, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        Utils.addToGridBag(subpanel, gb,
            new JLabel(s.getName()),
            1, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        Utils.addToGridBag(subpanel, gb,
            new JLabel("Residue Index: "),
            0, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        res_idx = new JLabel(Integer.toString(s.getRawIndex(idx)));

        Utils.addToGridBag(subpanel, gb,
            res_idx,
            1, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        Utils.addToGridBag(subpanel, gb,
            new JLabel("Alignment Index: "),
            0, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        aln_idx = new JLabel(Integer.toString(idx));

        Utils.addToGridBag(subpanel, gb,
            aln_idx,
            1, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        Utils.addToGridBag(subpanel, gb,
            new JLabel("Residue: "),
            0, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        res_label = new JLabel(s.getAA(idx).getCode3());
        Utils.addToGridBag(subpanel, gb,
            res_label,
            1, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        /* update the residue label
        idx_tf.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {;}
            }
        );
        idx_tf.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent evt) {}

                public void focusLost(FocusEvent evt) {;}
            }
        );*/

        Utils.addToGridBag(subpanel, gb,
            new JLabel("Symbol: "),
            0, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.EAST);
        symbol_tf = new FixedWidthTextField(ca != null
                    ? ca.getSymbol()
                    : "*",
                    1);
        Utils.addToGridBag(subpanel, gb,
            symbol_tf,
            1, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        Utils.addToGridBag(subpanel, gb,
            new JLabel("Annotation: "),
            0, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.EAST);

        res_ann_tf = new FixedWidthTextField(ca != null ? ca.getAnnotation()
                    : "", 40);
        Utils.addToGridBag(subpanel, gb,
            res_ann_tf,
            1, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.WEST);
        row++;

        existing_annotations = alignment.getExistingColumnAnnotations();

        Utils.addClipboardBindings(res_ann_tf);
        Utils.addClipboardBindings(symbol_tf);

        if (existing_annotations.size() > 0) {
            ImageIcon[] images = new ImageIcon[existing_annotations.size()];

            for (int i = 0; i < images.length; i++) {
                CachedAnnotation this_ca = (CachedAnnotation) existing_annotations.get(i);
                Image im = this_ca.getAnnotationIcon(props);

                images[i] = new ImageIcon(im, this_ca.getAnnotation());
            }
            Utils.addToGridBag(subpanel, gb,
                new JLabel("Previous Annotations: "),
                0, row, 1, 1, 1, 1,
                GridBagConstraints.BOTH,
                GridBagConstraints.EAST);
            existing_ann_box = new JComboBox(images);
            existing_ann_box.setRenderer(new ComboBoxRenderer());
            existing_ann_box.addActionListener(this);
            Utils.addToGridBag(subpanel, gb,
                existing_ann_box,
                1, row, 1, 1, 1, 1,
                GridBagConstraints.BOTH,
                GridBagConstraints.WEST);
            row++;
        }

        jcc = new JColorChooser();
        jcc.setColor(ca != null ? ca.getColor() : Color.red);
        Utils.addToGridBag(subpanel, gb,
            jcc,
            0, row, 2, 1, 0, 1,
            GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        row++;

        getContentPane().add(subpanel, BorderLayout.CENTER);

        subpanel = new JPanel();
        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
        delete_button = new JButton("Delete");
        delete_button.addActionListener(this);
        subpanel.add(delete_button);
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        getContentPane().add(subpanel, BorderLayout.SOUTH);
        setSize(500, 700);
        setLocation(((Component) owner).getLocation());
        pack();
    }

    // set the proper residue label
    private void setResLabel() {
        int new_idx = -1;

        try {
            new_idx = Integer.parseInt(aln_idx.getText());
        } catch (NumberFormatException exp) {}
        if (new_idx < 0 || new_idx >= s.length())
            res_label.setText("Invalid Residue");
        else
            res_label.setText(s.getAA(new_idx).getCode3());
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button || e.getSource() == delete_button) {
            int new_idx;
            Vector idxRange;

            try {
                idxRange =
                        CollatedColumnAnnotation.parseRangeString(aln_idx.getText(), s.length());
            } catch (Exception exception) {
                ErrorDialog.showErrorDialog(this, "Invalid column range.");
                return;
            }

            String symbol = symbol_tf.getText();
            String ann = res_ann_tf.getText();

            if (symbol.length() != 1) {
                ErrorDialog.showErrorDialog(this, "Symbol must be a single character.");
                return;
            }
            for (int i = 0; i < idxRange.size(); i++) {
                new_idx = ((Integer) idxRange.get(i)).intValue();
                // res_label.setText(s.getAA(new_idx).getCode3());
                if (e.getSource() == delete_button || ann.length() < 1) {
                    s.deleteColumnAnnotation(new_idx);
                } else if (e.getSource() == ok_button)
                    s.setColumnAnnotation(new_idx, symbol, jcc.getColor(), ann);
            }
            dispose();
        } else if (e.getSource() == cancel_button)
            dispose();
        else if (e.getSource() == existing_ann_box
            && existing_ann_box != null) {
            CachedAnnotation ca =
                (CachedAnnotation)
                existing_annotations.get(existing_ann_box.getSelectedIndex());

            symbol_tf.setText(ca.getSymbol());
            res_ann_tf.setText(ca.getAnnotation());
            jcc.setColor(ca.getColor());
        }
        return;
    }

    // for rendering the combobox
    private class ComboBoxRenderer extends JLabel
        implements ListCellRenderer {
        public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            ImageIcon icon = (ImageIcon) value;

            setText(icon.getDescription());
            setIcon(icon);
            return this;
        }
    }

}
