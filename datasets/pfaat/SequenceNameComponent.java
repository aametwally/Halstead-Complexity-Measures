package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.neogenesis.pfaat.colorscheme.ColorScheme;


/**
 * <code>Component</code> for displaying a single sequence name with
 * line annotation names..
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class SequenceNameComponent extends JPanel
    implements SequenceListener, DisplayPropertiesListener {
    // underlying sequence
    private Sequence sequence = null;
    // properties
    private DisplayProperties props;
    private Color sequence_color = null;
    
    public SequenceNameComponent(Sequence sequence, 
        DisplayProperties props) {
        super();
        setBackground(Color.white);

        this.props = props;
        props.addListener(this);
        setSequence(sequence);
        sequence_color = sequence.getColor();
        if (sequence_color == null ||
            sequence_color == Color.white) {
            sequence_color = Color.black;
        }
    }

    // set the underlying sequence
    public void setSequence(Sequence sequence) {
        if (this.sequence != null) this.sequence.removeListener(this);
        this.sequence = sequence;
        this.sequence.addListener(this);
        revalidate();
        repaint();
    }	

    // SequenceListener interface
    public void sequenceAAChanged(Sequence aaseq) {}

    public void sequenceNameChanged(Sequence aaseq, String old_name) {
        if (aaseq != sequence) 
            throw new RuntimeException("bound to incorrect Sequence");
        revalidate();
        repaint();
    }

    public void sequenceAnnotationChanged(Sequence aaseq) {}

    public void sequenceGroupChanged(Sequence aaseq) {}

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {
        if (aaseq != sequence)
            throw new RuntimeException("bound to incorrect Sequence");	
        if (props.getAnnView(sequence)) {
            revalidate();
            repaint();
        }
    }

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {}

    public void sequenceColorChanged(Sequence aaseq) {
        if (aaseq != sequence) {
            throw new RuntimeException("bound to incorrect Sequence");
        }
        sequence_color = aaseq.getColor();
        if (sequence_color == null ||
            sequence_color == Color.white) {
            sequence_color = Color.black;
        }
        revalidate();
        repaint();
    }

    // DisplayPropertiesListener interface
    public void displayAnnViewChanged(DisplayProperties dp, 
        Sequence seq,
        boolean show) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        if (seq == sequence) {
            revalidate();
            repaint();
        }
    }

    public void displaySeqSelectChanged(DisplayProperties dp, 
        Sequence seq,
        boolean select) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        if (seq == sequence) {
            revalidate();
            repaint();
        }
    }

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
        int width = font_metrics.stringWidth("+" + sequence.getName());

        if (props.getAnnView(sequence)) {
            Sequence.LineAnnotation[] la = sequence.getLineAnnotations();

            height *= 1 + la.length;
            int offset = props.getResidueWidth() * 2;

            for (int cnt = la.length - 1; cnt >= 0; cnt--) 
                width = Math.max(width, 
                            offset + 
                            font_metrics.stringWidth(la[cnt].getName()));
        }
        return new Dimension(width, height);
    }

    public int findLineAnnotation(int x, int y) {
        if (!props.getAnnView(sequence)) return -1;
        int la_cnt = sequence.getLineAnnotationsCount();

        if (la_cnt < 1) return -1;
        return (int) Math.floor(((double) y)
                / ((double) props.getResidueHeight())) - 1;
    }
	
    public void paint(Graphics g) {
        boolean selected = props.getSeqSelect(sequence);

        g.setColor(selected ? sequence_color : Color.white);
        Dimension d = getSize();

        g.fillRect(0, 0, d.width, d.height);
	
        Font font = props.getFont();
        int x = 0;
        int y = 0;
        int font_y = y + props.getFontYOffset();
        int residue_height = props.getResidueHeight();

        g.setFont(font);
        g.setColor(selected ? Color.white : sequence_color);
        String str;
        boolean show_annotation = props.getAnnView(sequence);

        if (sequence.getLineAnnotationsCount() > 0) 
            str = (show_annotation ? "-" : "+") + sequence.getName();
        else 
            str = sequence.getName();
        g.drawString(str, x, font_y);
	  
        if (show_annotation) {
            x = 2 * props.getResidueWidth();
            Sequence.LineAnnotation[] la = sequence.getLineAnnotations();

            for (int k = 0; k < la.length; k++) {
                y = (k + 1) * residue_height;
                font_y = y + props.getFontMetrics().getMaxAscent();
                g.drawString(la[k].getName(), x, font_y);
            }
        }
    }
    
}

