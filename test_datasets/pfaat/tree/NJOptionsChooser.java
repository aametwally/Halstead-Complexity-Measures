// ///////////
// BETE BEGIN
// ///////////
package com.neogenesis.pfaat.tree;


import java.io.*;
import java.util.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.seqspace.*;
import com.neogenesis.pfaat.print.*;
import com.neogenesis.pfaat.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Displays dialog allowing the user to choose NJ options
 *
 * @author $Author: xih $
 * @version $Revision: 1.7 $, $Date: 2002/10/11 18:31:50 $ */
public class NJOptionsChooser extends JDialog
    implements ActionListener {
    // dialog is modal...only one dialog around at a time
    private static NJOptionsChooser dialog;
    private static NJOptions value;

    // buttons
    JButton cancelButton;
    final JButton setButton;

    // the parameters
    private JList method;
    private JCheckBox useWeightedColumns;
    private JCheckBox skipGappyColumns;
    private JTextField gapCutoffPercent, includeColumns, excludeColumns;
    private JRadioButton all_column_button, exclude_column_button, include_column_button;
    private JCheckBox bootStrapping;
    private JTextField numBootStrapIterations;

    // show modal dialog and return options or null if canel button pressed
    public static NJOptions showDialog(Component comp, String title) {
        // initialize if we never have been
        if (dialog == null) {
            Frame frame = JOptionPane.getFrameForComponent(comp);

            dialog = new NJOptionsChooser(frame, title);
        }
        // default to the same as last time
        dialog.setValue(value);
        dialog.setLocationRelativeTo(comp);
        dialog.setVisible(true);

        return value;
    }

    // set the value we are editing
    private void setValue(NJOptions options) {
        // setup defaults
        if (options == null) {
            method.setSelectedIndex(0);
            useWeightedColumns.setSelected(false);
            skipGappyColumns.setSelected(false);
            gapCutoffPercent.setText("" + NJOptions.DefaultGapCutoffPercent);
            gapCutoffPercent.disable();
            bootStrapping.setSelected(false);
            numBootStrapIterations.setText("" + NJOptions.DefaultNumBootStrapIterations);
            numBootStrapIterations.disable();

            return;
        }

        // save this
        value = options;

        // setup UI from values
        method.setSelectedIndex(options.method);
        useWeightedColumns.setSelected(options.useWeightedColumns);
        skipGappyColumns.setSelected(options.skipGappyColumns);
        gapCutoffPercent.setText("" + options.gapCutoffPercent);
        gapCutoffPercent.setEnabled(options.skipGappyColumns);
        bootStrapping.setSelected(options.bootStrapping);
        numBootStrapIterations.setText("" + options.numBootStrapIterations);
        numBootStrapIterations.setEnabled(options.bootStrapping);
    }

    // display warning
    private void doWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    // read the values back from the UI
    // if invalid give warning and return null
    private NJOptions validateValue() {
        // get values from UI
        NJOptions options = new NJOptions();

        // get  values from ui
        options.method = method.getSelectedIndex();
        options.useWeightedColumns = useWeightedColumns.isSelected();
        options.skipGappyColumns = skipGappyColumns.isSelected();
        options.bootStrapping = bootStrapping.isSelected();

        // validate gap cutoff
        String s = gapCutoffPercent.getText();

        try {
            options.gapCutoffPercent = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            doWarning("Gap Cutoff Percent must be a numeric value");
            return null;
        }

        if (options.gapCutoffPercent < 0.0f || options.gapCutoffPercent > 100.0f) {
            doWarning("Gap Cutoff Percent must be: (0 <= value <= 100)");
            return null;
        }

        // validate num boot iter
        s = numBootStrapIterations.getText();
        try {
            options.numBootStrapIterations = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            doWarning("Number of Bootstrap Iterations must be a numeric value");
            return null;
        }

        if (options.numBootStrapIterations <= 0) {
            doWarning("Number of Bootstrap Iterations must be greater than zero");
            return null;
        }

        // get the options for excluded column
        try {
            if (exclude_column_button.isSelected()) {
                NJOptions.getExcludedColumns(options, excludeColumns.getText());
            } else if (include_column_button.isSelected()) {
                NJOptions.getIncludedColumns(options, includeColumns.getText());
            }
        } catch (Exception e) {
            doWarning("columns should be specified as (start1-end1,start2-end2) and end < 7500");
            return null;
        }

        // return this
        return options;
    }

    // private constructor...only one dialog around at a time
    private NJOptionsChooser(Frame frame, String title) {
        super(frame, title, true);

        // buttons
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        setButton = new JButton("OK");
        setButton.addActionListener(this);
        getRootPane().setDefaultButton(setButton);

        // Main part of dialog...

        // controls
        method = new JList(NJOptions.getMethodTypes());
        useWeightedColumns = new JCheckBox();
        skipGappyColumns = new JCheckBox();
        skipGappyColumns.addActionListener(this);
        gapCutoffPercent = new JTextField();
        bootStrapping = new JCheckBox();
        bootStrapping.addActionListener(this);
        numBootStrapIterations = new JTextField();

        // Layout the param panel
        JPanel paramPanel = new JPanel();

        paramPanel.setPreferredSize(new Dimension(300, 300));
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));
        paramPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // method
        JPanel panel = new JPanel();

        paramPanel.add(panel);
        panel.add(new JLabel("Similarity Matrix:"));
        // JScrollPane scrollPane = new JScrollPane();
        // paramPanel.add(scrollPane);
        // scrollPane.add(method);
        paramPanel.add(method);

        ButtonGroup bg = new ButtonGroup();
        JPanel button_panel = new JPanel();

        paramPanel.add(button_panel);
        // set layout of this panel
        button_panel.setLayout(new GridLayout(3, 2));
        all_column_button = new JRadioButton("Include all columns", true);
        include_column_button = new JRadioButton("Include columns: ", false);
        exclude_column_button = new JRadioButton("Exclude columns: ", false);
        Color c = new JLabel("").getForeground();

        all_column_button.setForeground(c);
        include_column_button.setForeground(c);
        exclude_column_button.setForeground(c);

        bg.add(all_column_button);
        bg.add(include_column_button);
        bg.add(exclude_column_button);

        button_panel.add(all_column_button);
        includeColumns = new JTextField("e.g. 200-300,400-500");
        excludeColumns = new JTextField("e.g. 100-200,300-400");
        button_panel.add(all_column_button);
        button_panel.add(new JLabel(""));
        button_panel.add(include_column_button);
        button_panel.add(includeColumns);
        button_panel.add(exclude_column_button);
        button_panel.add(excludeColumns);

        // column for calculationi
        // Todo: unspecified columns will be ignored


        // column ignored for calculation
        // Todo: specified columns will be ignored

        // separator
        paramPanel.add(new JLabel(""));
        paramPanel.add(new JSeparator());

        // column weights
        panel = new JPanel();
        paramPanel.add(panel);
        panel.add(useWeightedColumns);
        panel.add(new JLabel("Use Global Column Conservation Weighting"));

        // gappy columns
        panel = new JPanel();
        paramPanel.add(panel);
        panel.add(skipGappyColumns);
        panel.add(new JLabel("Skip Col. with more than "));
        panel.add(gapCutoffPercent);
        panel.add(new JLabel("% gaps."));

        // boot strapping
        panel = new JPanel();
        paramPanel.add(panel);
        panel.add(bootStrapping);
        panel.add(new JLabel("Boot strapping:"));
        panel.add(numBootStrapIterations);
        panel.add(new JLabel("iterations."));

        // Lay out main area
        Box mainPane = new Box(BoxLayout.Y_AXIS);

        mainPane.add(paramPanel);

        // Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();

        contentPane.add(mainPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }

    // any menu item or button pressed
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            value = null;
            dialog.setVisible(false);
        } else if (e.getSource() == setButton) {
            value = validateValue();
            if (value != null)
                dialog.setVisible(false);
        } else if (e.getSource() == skipGappyColumns) {
            gapCutoffPercent.setEnabled(skipGappyColumns.isSelected());
        } else if (e.getSource() == bootStrapping) {
            numBootStrapIterations.setEnabled(bootStrapping.isSelected());
        }
    }
}

// /////////
// BETE END
// /////////
