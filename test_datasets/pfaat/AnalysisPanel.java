package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;


/**
 * Display component for alignment analysis data.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public class AnalysisPanel extends JPanel implements Scrollable {
    // display properties
    protected DisplayProperties props;

    public AnalysisPanel(DisplayProperties props) {
        super();
        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.props = props;
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

    // Scrollable interface
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, 
        int orientation, 
        int direction) {
        return orientation == SwingConstants.VERTICAL
            ? visibleRect.height : visibleRect.width;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, 
        int orientation, 
        int direction) {
        return orientation == SwingConstants.VERTICAL
            ? props.getResidueHeight() : props.getResidueWidth();
    }

}

