package com.neogenesis.pfaat.srs;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;


/**
 * Interface for querying SRS.
 *
 * @author $Author: xih $
 * @version $Revision: 1.4 $, $Date: 2002/10/11 18:30:55 $ */
public class SRSQueryFrame extends JFrame {
    private JTextField id_tf;
    private ConsolePane console_pane;
    private SRSServer server = new SRSServer();
    private AlignmentFrame owner;

    public SRSQueryFrame(AlignmentFrame owner) {
        super("SRS Query");
        this.owner = owner;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        JPanel subpanel;

        subpanel = new JPanel();
        subpanel.setLayout(new BorderLayout());
        subpanel.add(new JLabel("ID: "), BorderLayout.WEST);
        id_tf = new JTextField(50);
        subpanel.add(id_tf);
        getContentPane().add(subpanel, BorderLayout.NORTH);

        console_pane = new ConsolePane();
        getContentPane().add(console_pane, BorderLayout.CENTER);

        subpanel = new JPanel();
        JButton query_button = new JButton("Query");

        query_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        String id = id_tf.getText();

                        if (id == null || (id = id.trim()).length() < 1) {
                            ErrorDialog.showErrorDialog(SRSQueryFrame.this,
                                "Please enter "
                                + "a sequence ID.");
                            return;
                        }

                        console_pane.clear();
                        MutableAttributeSet a = new SimpleAttributeSet();

                        StyleConstants.setForeground(a, new Color(58, 106, 122));
                        PrintWriter out = console_pane.createPrintWriter(a);

                        server.printQueryResults(id, out);
                        out.close();
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(SRSQueryFrame.this,
                            "SRS query failed. Suggestions: \n" +
                            "1) Check if SRS server is available online. \n" +
                            "2) If running behind the firewall, check the proxy setting in pfaat.properties file in pfaat install directory",
                            exp);

                    }
                }
            }
        );
        subpanel.add(query_button);
        if (owner.getAlignment() != null) {
            JButton import_button = new JButton("Import Sequence");

            import_button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        String id = id_tf.getText();

                        if (id == null || (id = id.trim()).length() < 1) {
                            ErrorDialog.showErrorDialog(SRSQueryFrame.this,
                                "Please enter "
                                + "a sequence ID.");
                            return;
                        }

                        try {
                            SRSEntry entry =
                                new SRSEntry(server.getQueryReader(id));
                            Sequence sequence =
                                new Sequence(entry.getID(), entry.getAAs());

                            SRSQueryFrame.this.owner.getAlignment().insertSequence(0, sequence);
                            SRSQueryFrame.this.owner.setAlignmentPanelPosition(0, 0);
                            sequence.insertAA(AminoAcid.GAP, 0);
                        } catch (Exception exp) {
                            ErrorDialog.showErrorDialog(SRSQueryFrame.this,
                                "Unable to import "
                                + "SRS entry. Does "
                                + "entry exist?",
                                exp);
                        }
                    }
                }
            );
            subpanel.add(import_button);
        }
        JButton close_button = new JButton("Close");

        close_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            }
        );
        subpanel.add(close_button);

        getContentPane().add(subpanel, BorderLayout.SOUTH);

        setSize(600, 400);
    }
}

