package com.neogenesis.pfaat.colorscheme;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


/**
 * Dialog for user defined color schemes.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:34 $ */
public class ColorSchemeDialog extends JDialog
    implements ActionListener, ChangeListener {
    private JButton[] set_color_buttons;
    private JColorChooser jcc;
    private JButton ok_button, apply_button, cancel_button;
    private JButton load_button, save_button;
    private DoubleSlider pid_slider;
    private JLabel pid_label;
    private String name = "";
    private ColorSchemeManager csm;

    private static final Color[] DEFAULT_COLOR_TABLE = {
            Color.pink,          // A
            Color.blue,          // R
            Color.green,         // N
            Color.red,           // D
            Color.yellow,        // C
            Color.green,         // Q
            Color.red,           // E
            Color.magenta,       // G
            Color.red,           // H
            Color.pink,          // I
            Color.pink,          // L
            Color.blue,          // K
            Color.pink,          // M
            Color.orange,        // F
            Color.magenta,       // P
            Color.green,         // S
            Color.green,         // T
            Color.orange,        // W
            Color.orange,        // Y
            Color.pink,          // V
            Color.white,         // B
            Color.white,         // Z
            Color.white,         // X
            Color.white          // GAP
        };

    public ColorSchemeDialog(AlignmentFrame owner,
        DisplayProperties props,
        ColorSchemeManager csm) {
        super(owner, "User Defined Color Scheme");
        BoxLayout layout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);

        getContentPane().setLayout(layout);
        this.csm = csm;

        JPanel subpanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        subpanel.setLayout(gb);

        set_color_buttons = new JButton[AminoAcid.NUM_AA];
        int cols = 4;
        int rows = AminoAcid.NUM_AA / cols;

        if (rows * cols != AminoAcid.NUM_AA)
            throw new RuntimeException("number of amino acids has changed");
        int cnt = 0;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                AminoAcid aa = AminoAcid.lookupByIndex(cnt);

                set_color_buttons[cnt] = new JButton(aa.getCode3());
                set_color_buttons[cnt].addActionListener(this);
                Utils.addToGridBag(subpanel, gb,
                    set_color_buttons[cnt],
                    x, y, 1, 1, 1, 1,
                    GridBagConstraints.BOTH,
                    GridBagConstraints.CENTER);
                cnt++;
            }
        }
        getContentPane().add(subpanel);

        subpanel = new JPanel();
        subpanel.add(new JLabel("PID Threshold: "));
        pid_slider = new DoubleSlider(0.0, 1.0, "0%");
        pid_slider.addChangeListener(this);
        subpanel.add(pid_slider);
        pid_label = new JLabel("");
        subpanel.add(pid_label);
        getContentPane().add(subpanel);

        jcc = new JColorChooser();
        jcc.setPreviewPanel(new JPanel());
        getContentPane().add(jcc);

        subpanel = new JPanel();
        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
        apply_button = new JButton("Apply");
        apply_button.addActionListener(this);
        subpanel.add(apply_button);
        load_button = new JButton("Load");
        load_button.addActionListener(this);
        subpanel.add(load_button);
        save_button = new JButton("Save");
        save_button.addActionListener(this);
        subpanel.add(save_button);
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);
        getContentPane().add(subpanel);

        // in applet mode no user-defined scheme can be saved
        if (owner.isAppletMode()) {
            save_button.setVisible(false);
            load_button.setVisible(false);
        }

        ColorScheme cs = props.getColorScheme();

        if (cs instanceof ResidueColorScheme)
            setColorScheme((ResidueColorScheme) cs);
        else
            setColorScheme(new ResidueColorScheme(null,
                    0.0,
                    DEFAULT_COLOR_TABLE,
                    ""));

        setLocationRelativeTo(owner);
        pack();
    }

    private void setColorScheme(ResidueColorScheme cs) {
        Color[] color_table = cs.getColorTable();

        for (int i = set_color_buttons.length - 1; i >= 0; i--)
            set_color_buttons[i].setBackground(color_table[i]);
        pid_slider.setDouble(cs.getPID());
        setLabels();
    }

    private ResidueColorScheme getColorScheme() {
        Color[] color_table = new Color[AminoAcid.NUM_AA];

        for (int i = color_table.length - 1; i >= 0; i--)
            color_table[i] = set_color_buttons[i].getBackground();
        return new ResidueColorScheme(null,
                pid_slider.getDouble(),
                color_table,
                name);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == pid_slider)
            setLabels();
    }

    private void setLabels() {
        DecimalFormat df = new DecimalFormat("0%");
        double x = pid_slider.getDouble();

        pid_label.setText(df.format(x));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button || e.getSource() == apply_button) {
            csm.setUserDefinedColorScheme(getColorScheme());
            if (e.getSource() == ok_button)
                dispose();
        } else if (e.getSource() == cancel_button)
            dispose();
        else if (e.getSource() == load_button) {
            JFileChooser fc =
                new JFileChooser(PathManager.getColorSchemeDirectory());

            fc.addChoosableFileFilter(new
                DirectoryFileFilter(ResidueColorScheme.getFileFilter()));
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            if (fc.showDialog(this, "Load") == JFileChooser.APPROVE_OPTION) {
                try {
                    ResidueColorScheme cs =
                        new ResidueColorScheme(null, fc.getSelectedFile());

                    setColorScheme(cs);
                } catch (Exception exp) {
                    ErrorDialog.showErrorDialog(this,
                        "Unable to load color scheme: ",
                        exp);
                }
            }
        } else if (e.getSource() == save_button) {
            JFileChooser fc =
                new JFileChooser(PathManager.getLocalPath());

            fc.addChoosableFileFilter(new
                DirectoryFileFilter(ResidueColorScheme.getFileFilter()));
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            if (fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
                try {
                    name = Utils.getBaseFileName(fc.getSelectedFile());
                    ResidueColorScheme cs = getColorScheme();

                    cs.save(fc.getSelectedFile());
                    csm.refreshColorSchemes();
                } catch (Exception exp) {
                    ErrorDialog.showErrorDialog(this,
                        "Unable to save color scheme: ",
                        exp);
                }
            }
        } else {
            for (int i = set_color_buttons.length - 1; i >= 0; i--) {
                if (e.getSource() == set_color_buttons[i]) {
                    set_color_buttons[i].setBackground(jcc.getColor());
                    return;
                }
            }
        }
    }
}
