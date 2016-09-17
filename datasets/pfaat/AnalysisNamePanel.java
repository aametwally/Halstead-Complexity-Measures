package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;


/**
 * Display component for alignment analysis names.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public class AnalysisNamePanel extends JPanel {

    public AnalysisNamePanel() {
        super();
        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        return getLayout().minimumLayoutSize(this);
    }
    
}
