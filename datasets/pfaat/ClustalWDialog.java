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


public class ClustalWDialog extends JDialog {
    private AlignmentFrame owner;
    private Alignment alignment;
    private DisplayProperties props;
    private ClustalWAlgorithm cwAlg;

    private static final int STANDARD_ALIGN = 0;
    private static final int PROFILE_ALIGN = 1;
    private static final int PROFILE_PROFILE_ALIGN = 2;
    private int alignmentType = STANDARD_ALIGN;

    private JCheckBox removeGapsCB;
    private boolean removeGaps;

    private JCheckBox modelSetCB;
    private JButton modelSetButton;
    private Sequence[] modelSet = null;

    private JCheckBox alignmentSetCB;
    private JButton alignmentSetButton;
    private Sequence[] alignmentSet = null;

    public ClustalWDialog(AlignmentFrame owner, Alignment alignment,
        DisplayProperties props) {
        super(owner, "ClustalW Alignment");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        this.owner = owner;
        this.alignment = alignment;
        this.props = props;

        JPanel tPanel;

        tPanel = new JPanel();
        tPanel.setLayout(new BorderLayout()); {
            JPanel rbPanel = new JPanel();

            rbPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            rbPanel.setLayout(new GridLayout(3, 1)); {
                ButtonGroup alignBG = new ButtonGroup();

                JRadioButton standardAlignmentRB = new JRadioButton("Standard Alignment", true);

                standardAlignmentRB.setActionCommand("" + (STANDARD_ALIGN));
                standardAlignmentRB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            alignmentType = STANDARD_ALIGN;
                            modelSetButton.setEnabled(false);
                        }
                    }
                );
                alignBG.add(standardAlignmentRB);
                rbPanel.add(standardAlignmentRB);

                JRadioButton profileAlignmentRB = new JRadioButton("Alignment to Profile", false);

                profileAlignmentRB.setActionCommand("" + (PROFILE_ALIGN));
                profileAlignmentRB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            alignmentType = PROFILE_ALIGN;
                            modelSetButton.setEnabled(true);
                        }
                    }
                );
                alignBG.add(profileAlignmentRB);
                rbPanel.add(profileAlignmentRB);

                JRadioButton profileProfileAlignmentRB = new JRadioButton("Profile/Profile Alignment", false);

                profileProfileAlignmentRB.setActionCommand("" + (PROFILE_PROFILE_ALIGN));
                profileProfileAlignmentRB.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            alignmentType = PROFILE_PROFILE_ALIGN;
                            modelSetButton.setEnabled(true);
                        }
                    }
                );
                alignBG.add(profileProfileAlignmentRB);
                rbPanel.add(profileProfileAlignmentRB);
            }
            tPanel.add(rbPanel, BorderLayout.CENTER);

            JPanel cbPanel = new JPanel();

            cbPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            cbPanel.setLayout(new GridLayout(1, 1)); {
                removeGapsCB = new JCheckBox("Remove Gaps", false);
                removeGapsCB.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent evt) {
                            switch (evt.getStateChange()) {
                            case ItemEvent.SELECTED:
                                removeGaps = true;
                                break;

                            case ItemEvent.DESELECTED:
                                removeGaps = false;
                                break;
                            }
                        }
                    }
                );
                cbPanel.add(removeGapsCB);
            }
            tPanel.add(cbPanel, BorderLayout.SOUTH);
        }
        getContentPane().add(tPanel, BorderLayout.NORTH);

        tPanel = new JPanel();
        tPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        tPanel.setLayout(new GridLayout(2, 1)); {
            JPanel setPanel;

            setPanel = new JPanel();
            setPanel.setLayout(new FlowLayout()); {
                JLabel label = new JLabel("Model Sequences");

                setPanel.add(label);

                modelSetButton = new JButton("Set");
                modelSetButton.setEnabled(false);
                modelSetButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            modelSet = ClustalWDialog.this.props.getAllSelected();
                            if (modelSet == null) {
                                ErrorDialog.showErrorDialog(ClustalWDialog.this.owner,
                                    "No sequences chosen");
                                modelSet = null;
                                return;
                            }
                            if (modelSet.length < 1) {
                                ErrorDialog.showErrorDialog(ClustalWDialog.this.owner,
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

                alignmentSetButton = new JButton("Set");
                alignmentSetButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            alignmentSet = ClustalWDialog.this.props.getAllSelected();
                            if (alignmentSet == null) {
                                ErrorDialog.showErrorDialog(ClustalWDialog.this.owner,
                                    "No sequences chosen");
                                alignmentSet = null;
                                return;
                            }
                            if ((alignmentType == PROFILE_ALIGN) ||
                                (alignmentType == PROFILE_PROFILE_ALIGN)) {
                                if (alignmentSet.length < 1) {
                                    ErrorDialog.showErrorDialog(ClustalWDialog.this.owner,
                                        "Must select at least one sequence");
                                    alignmentSet = null;
                                    return;
                                }
                            } else {
                                if (alignmentSet.length < 2) {
                                    ErrorDialog.showErrorDialog(ClustalWDialog.this.owner,
                                        "Must select at least two sequences");
                                    alignmentSet = null;
                                    return;
                                }
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
        }
        getContentPane().add(tPanel, BorderLayout.CENTER);

        tPanel = new JPanel();
        tPanel.setLayout(new FlowLayout()); {
            JButton okButton = new JButton("OK");

            okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (alignmentSet == null) {
                            ErrorDialog.showErrorDialog(ClustalWDialog.this.owner,
                                "No alignment set chosen");
                            ClustalWDialog.this.dispose();
                            return;
                        }

                        if (removeGaps)
                            alignmentSet = removeGaps(alignmentSet);

                        if ((alignmentType == PROFILE_ALIGN) ||
                            (alignmentType == PROFILE_PROFILE_ALIGN)) {
                            if (modelSet == null) {
                                ErrorDialog.showErrorDialog(ClustalWDialog.this.owner,
                                    "No profile set chosen");
                                ClustalWDialog.this.dispose();
                                return;
                            }
                        }

                        switch (alignmentType) {
                        case STANDARD_ALIGN:
                            cwAlg = ClustalWAlgorithm.createStandardAlgorithm(ClustalWDialog.this.owner,
                                        alignmentSet);
                            break;

                        case PROFILE_ALIGN:
                            cwAlg = ClustalWAlgorithm.createProfileAlgorithm(ClustalWDialog.this.owner,
                                        modelSet,
                                        alignmentSet);
                            break;

                        case PROFILE_PROFILE_ALIGN:
                            cwAlg = ClustalWAlgorithm.createProfileProfileAlgorithm(ClustalWDialog.this.owner,
                                        modelSet,
                                        alignmentSet);
                            break;
                        }

                        JOptionPane.showMessageDialog(ClustalWDialog.this.owner,
                            "ClustalW will be running at background. It may run for a long time for large alignments.\n Once it is finished a new alignment frame will be displayed.",
                            "ClustalW Info",
                            JOptionPane.INFORMATION_MESSAGE);
                        new ClustalWWorker(ClustalWDialog.this.owner, ClustalWDialog.this.props, cwAlg).start();
                        ClustalWDialog.this.dispose();
                    }// end of actionPerformed()
                }
            );
            tPanel.add(okButton);

            JButton cancelButton = new JButton("Cancel");

            cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        ClustalWDialog.this.dispose();
                    }
                }
            );
            tPanel.add(cancelButton);
        }
        getContentPane().add(tPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(owner);
        pack();
    }

    private Sequence[] removeGaps(Sequence[] sequences) {
        Sequence[] degappedSequences = new Sequence[sequences.length];

        for (int i = sequences.length; --i >= 0;) {
            degappedSequences[i] = new Sequence(sequences[i].getName(),
                        sequences[i].getAllAA());
            degappedSequences[i].removeGaps();
        }

        return (degappedSequences);
    }
}

