package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.colorscheme.ColorScheme;


/**
 * <code>Component</code> for displaying a single analysis entry.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public class AnalysisComponent extends JPanel 
    implements DisplayPropertiesListener {
    // underlying analysis string
    private String analysis;
    // rendering properties
    private DisplayProperties props;

    public AnalysisComponent(String analysis,
        DisplayProperties props) {
        super();
        this.analysis = analysis;
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

    public void displayHighlightsChanged(DisplayProperties dp, Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp, Sequence[] seqs) {}

    // sizes
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() { 
        Dimension d = getPreferredSize();

        d.width = Integer.MAX_VALUE;
        return d;
    }

    public Dimension getPreferredSize() {
        int height = props.getResidueHeight();
        int width = props.getResidueWidth() * analysis.length();

        return new Dimension(width, height);
    }

    private class RenderIterator {
        private int residue_height;
        private int residue_width;

        private int current_idx;
        private int start_idx, end_idx;
        private int current_x;

        public RenderIterator(Rectangle clip) {
            residue_height = props.getResidueHeight();
            residue_width = props.getResidueWidth();	

            int len = analysis.length();
	    
            start_idx = 
                    (int) Math.floor(((float) clip.x) / ((float) residue_width)) 
                    - 1;
            if (start_idx < 0)
                start_idx = 0;
            end_idx = 
                    (int) Math.floor(((float) clip.x + clip.width) 
                        / ((float) residue_width)) 
                    + 1;
            if (end_idx >= len) 
                end_idx = len - 1;
        }

        public boolean hasNext() {
            return current_idx < end_idx;
        }

        public void reset() { 
            current_idx = start_idx - 1;
            current_x = current_idx * residue_width;
        }

        public int getIndex() {
            return current_idx;
        }

        public int getX() {
            return current_x;
        }

        public int getStartIndex() {
            return start_idx;
        }

        public int getEndIndex() {
            return end_idx;
        }

        public int getStartX() {
            return start_idx * residue_width;
        }

        public void next() { 
            current_idx++;
            current_x += residue_width;
        }
    
        private String getString() {
            return analysis.substring(current_idx, current_idx + 1);
        }
    }

    // render the sequence
    public void paint(Graphics g) {
        g.setColor(Color.white);
        Dimension d = getSize();

        g.fillRect(0, 0, d.width, d.height);

        // figure out the range to render
        Rectangle clip = g.getClipBounds();
        int residue_width = props.getResidueWidth();
        int residue_height = props.getResidueHeight();
        RenderIterator ri = new RenderIterator(clip);

        // draw text
        int font_x = props.getFontXOffset();
        int font_y = props.getFontYOffset();

        g.setFont(props.getFont());
        g.setColor(Color.black);
        for (ri.reset(); ri.hasNext();) {
            ri.next();	
            String str = ri.getString();

            if (!str.equals(" ")) 
                g.drawString(str,
                    ri.getX() + font_x,
                    font_y);
        }
    }
}

