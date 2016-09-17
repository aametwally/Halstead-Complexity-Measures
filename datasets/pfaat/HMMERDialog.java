package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.border.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.io.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


public class HMMERDialog extends JDialog {
    private AlignmentFrame owner;
    private Alignment alignment;
    private DisplayProperties props;
    private HMMERAlgorithm hmmAlg;

    private JCheckBox modelSetCB;
    private Sequence[] modelSet = null;

    private JCheckBox alignmentSetCB;
    private Sequence[] alignmentSet = null;

    private JCheckBox withaliSetCB;
    private Sequence[] withaliSet = null;

    public HMMERDialog(AlignmentFrame owner, Alignment alignment,
        DisplayProperties props) {
        super(owner, "HMMER Alignment");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.owner = owner;
        this.alignment = alignment;
        this.props = props;

        JPanel tPanel;

        tPanel = new JPanel();
        tPanel.setLayout(new GridLayout(4, 1)); {
            JPanel setPanel;

            setPanel = new JPanel();
            setPanel.setLayout(new FlowLayout()); {
                JLabel label = new JLabel("Model Sequences");

                setPanel.add(label);

                JButton modelSetButton = new JButton("Set");

                modelSetButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            modelSet = HMMERDialog.this.props.getAllSelected();
                            if (modelSet == null) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "No sequences chosen");
                                modelSet = null;
                                return;
                            }
                            if (modelSet.length < 1) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "Must select at least one sequence");
                                modelSet = null;
                                return;
                            }
                            modelSetCB.setSelected(true);
                        }
                    }
                );
                setPanel.add(modelSetButton);

                modelSetCB = new JCheckBox();
                modelSetCB.setEnabled(false);
                setPanel.add(modelSetCB);
            }
            tPanel.add(setPanel);

            setPanel = new JPanel();
            setPanel.setLayout(new FlowLayout()); {
                JLabel label = new JLabel("Sequences to be Aligned");

                setPanel.add(label);

                JButton alignmentSetButton = new JButton("Set");

                alignmentSetButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            alignmentSet = HMMERDialog.this.props.getAllSelected();
                            if (alignmentSet == null) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "No sequences chosen");
                                alignmentSet = null;
                                return;
                            }
                            if (alignmentSet.length < 1) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "Must select at least one sequence");
                                alignmentSet = null;
                                return;
                            }
                            alignmentSetCB.setSelected(true);
                        }
                    }
                );
                setPanel.add(alignmentSetButton);

                alignmentSetCB = new JCheckBox();
                alignmentSetCB.setEnabled(false);
                setPanel.add(alignmentSetCB);
            }
            tPanel.add(setPanel);

            setPanel = new JPanel();
            setPanel.setLayout(new FlowLayout()); {
                JLabel label = new JLabel("Preserved (--withali) Sequences");

                setPanel.add(label);

                JButton withaliSetButton = new JButton("Set");

                withaliSetButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            withaliSet = HMMERDialog.this.props.getAllSelected();
                            if (withaliSet == null) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "No sequences chosen");
                                withaliSet = null;
                                return;
                            }
                            if (withaliSet.length < 1) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "Must select at least one sequence");
                                withaliSet = null;
                                return;
                            }
                            withaliSetCB.setSelected(true);
                        }
                    }
                );
                setPanel.add(withaliSetButton);

                withaliSetCB = new JCheckBox();
                withaliSetCB.setEnabled(false);
                setPanel.add(withaliSetCB);
            }
            tPanel.add(setPanel);

            JPanel buttonPanel = new JPanel();

            buttonPanel.setLayout(new FlowLayout()); {
                JButton okButton = new JButton("OK");

                okButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (alignmentSet == null) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "No alignment set chosen");
                                HMMERDialog.this.dispose();
                                return;
                            }

                            if (modelSet == null) {
                                ErrorDialog.showErrorDialog(HMMERDialog.this.owner,
                                    "No profile set chosen");
                                HMMERDialog.this.dispose();
                                return;
                            }

                            if (withaliSet == null) {
                                hmmAlg = HMMERAlgorithm.createProfileAlgorithm(HMMERDialog.this.owner,
                                            modelSet,
                                            alignmentSet);
                            } else {
                                hmmAlg = HMMERAlgorithm.createWithaliAlgorithm(HMMERDialog.this.owner,
                                            modelSet,
                                            alignmentSet,
                                            withaliSet);
                            }

                            JOptionPane.showMessageDialog(HMMERDialog.this.owner,
                                "HMMER will be running at background. It may run for a long time for large alignments.\n Once it is finished a new alignment frame will be displayed.",
                                "HMMER Info",
                                JOptionPane.INFORMATION_MESSAGE);
                            new HMMERWorker(HMMERDialog.this.owner, HMMERDialog.this.props, hmmAlg).start();
                            HMMERDialog.this.dispose();
                        }// end of actionPerformed()
                    }
                );
                buttonPanel.add(okButton);

                JButton cancelButton = new JButton("Cancel");

                cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            HMMERDialog.this.dispose();
                        }
                    }
                );
                buttonPanel.add(cancelButton);
            }
            tPanel.add(buttonPanel);
        }
        getContentPane().add(tPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        pack();
    }
}

