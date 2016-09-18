// ///////////
// BETE BEGIN
// ///////////
package com.neogenesis.pfaat;


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
 * Displays dialog displaying progress
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class ProgressDialog extends JDialog
    implements ActionListener {
    // buttons
    final JButton cancelButton;

    // we have a progress bar and at label
    private JLabel progressLabel;
    private JProgressBar progressBar;

    // this is true only if we have hit cancel button and requested cancel
    private boolean requestCancel = false;

    // create and open window and provide initial label and value
    public static ProgressDialog createDialog(Component comp, String title) {
        // initialize
        Frame frame = JOptionPane.getFrameForComponent(comp);
        ProgressDialog dialog = new ProgressDialog(frame, title);

        dialog.setLocationRelativeTo(comp);
        dialog.setVisible(true);

        return dialog;
    }

    // show given progress
    public void setProgress(String label, int value, int max) {
        // we can use this as an opportunity to let the calling worker
        // thread yield a bit so that the UI can be updated
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {}

        progressLabel.setText(label + ": ");
        progressBar.setValue(getValue(value, max));
    }

    // hide dialog
    public void closeDialog() {
        setVisible(false);
    }

    // private constructor...
    private ProgressDialog(Frame frame, String title) {
        super(frame, title, false);

        // buttons
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        getRootPane().setDefaultButton(cancelButton);

        // controls
        progressLabel = new JLabel("Progress: ");
        progressBar = new JProgressBar(0, 100);

        // Layout the param panel
        JPanel paramPanel = new JPanel();

        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.X_AXIS));
        paramPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        paramPanel.add(progressLabel);
        paramPanel.add(progressBar);

        // Lay out main area
        Box mainPane = new Box(BoxLayout.Y_AXIS);

        mainPane.add(paramPanel);

        // Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        // buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();

        contentPane.add(mainPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }

    // any menu item or button pressed
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelButton) {
            // just flag that we want to cancel
            requestCancel = true;
        }
    }

    // has a cancel been requested?
    public boolean cancelRequested() {
        // we can use this as an opportunity to let the calling worker
        // thread yield a bit so that the UI can be updated
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {}

        return requestCancel;
    }

    // utility for scaling value that ranges from 0...(max-1) to
    // a progress value between 0 and 100
    private static int getValue(int value, int max) {
        int v = value * 100 / (max - 1);

        return v;
    }
}

// /////////
// BETE END
// /////////
