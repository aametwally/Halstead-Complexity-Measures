package com.neogenesis.pfaat.swingx;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;


/**
 * Dialog for reporting errors/exceptions with stack trace, etc.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class ErrorDialog extends JDialog implements ActionListener {
    private JButton ok_button;

    public static void showErrorDialog(Component owner, String message,
        Throwable e) {
        ErrorDialog err = new ErrorDialog(getFrameForComponent(owner), 
                message, e);

        err.show();
    }

    public static void showErrorDialog(Component owner, String message) {
        ErrorDialog err = new ErrorDialog(getFrameForComponent(owner), 
                message, null);

        err.show();
    }

    private static Frame getFrameForComponent(Component c) {
        if (c == null) 
            return null;
        if (c instanceof Frame)
            return (Frame) c;
        return getFrameForComponent(c.getParent());
    }

    private ErrorDialog(Frame owner, String message, Throwable e) {
        super(owner, "Error", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        ConsolePane console = new ConsolePane();
        MutableAttributeSet a = new SimpleAttributeSet();

        StyleConstants.setForeground(a, new Color(58, 106, 122));
        PrintWriter out = console.createPrintWriter(a);

        if (message != null)
            out.println(message);
        if (e != null) {
            out.println("Exception: " + e.getMessage());
            e.printStackTrace(out);
        }
        out.close();

        getContentPane().add(console, BorderLayout.CENTER);

        JPanel subpanel = new JPanel();

        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
	
        getContentPane().add(subpanel, BorderLayout.SOUTH);
	
        setLocationRelativeTo(owner);
        setSize(320, 240);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok_button) 
            dispose();
    }
}
