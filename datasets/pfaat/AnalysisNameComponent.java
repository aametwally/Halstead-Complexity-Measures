package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.neogenesis.pfaat.colorscheme.ColorScheme;


/**
 * <code>Component</code> for displaying a single analysis entry name.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public class AnalysisNameComponent extends JPanel 
    implements DisplayPropertiesListener {
    // the name
    private String name;
    // properties
    private DisplayProperties props;
    
    public AnalysisNameComponent(String name,
        DisplayProperties props) {
        super();
        setBackground(Color.white);

        this.name = name;
        this.props = props;
        props.addListener(this);
    }

    // DisplayPropertiesListener interface
    public void displayAnnViewChanged(DisplayProperties dp, 
        Sequence seq,
        boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp, 
        Sequence seq,
        boolean select) {}

    public void displayColorSchemeChanged(DisplayProperties dp, 
        ColorScheme old) {}

    public void displayFontChanged(DisplayProperties dp) { 
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        revalidate();
        repaint();
    }

    public void displayRenderGapsChanged(DisplayProperties dp) {}

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs) {}

    // default size
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() { 
        Dimension d = getPreferredSize();

        d.width = Integer.MAX_VALUE;
        return d;
    }

    public Dimension getPreferredSize() {
        FontMetrics font_metrics = props.getFontMetrics();
        int height = props.getResidueHeight();
        int width = font_metrics.stringWidth(name);

        return new Dimension(width, height);
    }

    public void paint(Graphics g) {
        g.setColor(Color.white);
        Dimension d = getSize();

        g.fillRect(0, 0, d.width, d.height);
        Font font = props.getFont();
        int x = 0;
        int y = 0;
        int font_y = y + props.getFontYOffset();
        int residue_height = props.getResidueHeight();

        g.setFont(font);
        g.setColor(Color.black);
        g.drawString(name, x, font_y);
    }
    
}

