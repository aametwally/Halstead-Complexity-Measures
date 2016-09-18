package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.colorscheme.*;


/**
 * <code>Component</code> for displaying a single sequence.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class SequenceComponent extends JPanel
    implements SequenceListener, 
        DisplayPropertiesListener, 
        ColorSchemeListener {
    // underlying sequence
    private Sequence sequence = null;
    // rendering properties
    private DisplayProperties props;
    // rendering colors
    private Color[] foreground_color, background_color;
    public SequenceComponent(Sequence sequence, 
        DisplayProperties props) {
        super();
        this.props = props;
        props.addListener(this);
        props.getColorScheme().addListener(this);
        foreground_color = background_color = null;
        setSequence(sequence);
    }

    // set the underlying sequence
    public void setSequence(Sequence sequence) {
        if (this.sequence != null) this.sequence.removeListener(this);
        this.sequence = sequence;
        this.sequence.addListener(this);
        foreground_color = background_color = null;
        revalidate();
        repaint();
    }	

    // SequenceListener interface
    public void sequenceAAChanged(Sequence aaseq) {
        if (aaseq != sequence) 
            throw new RuntimeException("bound to incorrect Sequence");
        foreground_color = background_color = null;
        revalidate();
        repaint();
    }

    public void sequenceNameChanged(Sequence aaseq, String old_name) {}

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

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {
        if (aaseq != sequence)
            throw new RuntimeException("bound to incorrect Sequence");	
        revalidate();
        repaint();
    }

    public void sequenceColorChanged(Sequence aaseq) { 
        if (aaseq != sequence)
            throw new RuntimeException("bound to incorrect Sequence");	
        foreground_color = null;
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
        boolean select) {}

    public void displayColorSchemeChanged(DisplayProperties dp, 
        ColorScheme old) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        old.removeListener(this);
        props.getColorScheme().addListener(this);
        foreground_color = background_color = null;
        revalidate();
        repaint();
    }

    public void displayFontChanged(DisplayProperties dp) { 
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        revalidate();
        repaint();
    }

    public void displayRenderGapsChanged(DisplayProperties dp) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        revalidate();
        repaint();
    }

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp, Sequence seq) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        if (sequence == seq) {
            revalidate();
            repaint();
        }
    }

    public void displayHighlightsChanged(DisplayProperties dp, Sequence[] seqs) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        for (int i = 0; i < seqs.length; i++) {
            if (sequence == seqs[i]) {
                revalidate();
                repaint();
                return;
            }
        }
    }

    // ColorSchemeListener interface
    public void colorSchemeChanged(ColorScheme cs) {
        if (props.getColorScheme() != cs)
            throw new RuntimeException("bound to incorrect ColorScheme");
        foreground_color = background_color = null;
        revalidate();
        repaint();
    }

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

        if (props.getAnnView(sequence))
            height *= 1 + sequence.getLineAnnotationsCount();
        int width = props.getResidueWidth() * sequence.length();

        return new Dimension(width, height);
    }

    public int findColumn(int x, int y) {
        int idx = x / props.getResidueWidth();

        return idx >= sequence.length() ? -1 : idx;
    }

    // upper left hand corner of a particular residue
    public Point getResiduePosition(int res_idx) {
        return new Point(res_idx * props.getResidueWidth(), 0);
    }
    
    private class RenderIterator {
        private int residue_height;
        private int residue_width;
        private int current_idx;
        private int start_idx, end_idx;
        private int current_x;
        private int[] highlights;
        private int h_idx;
        private boolean this_res_highlighted = false;
        private Color this_seq_color;
        private Sequence.ColumnAnnotation[] ca;
        private int ca_idx;
        private Sequence.ColumnAnnotation this_res_ca = null;
        private ColorScheme cs;
        private boolean render_gaps;

        public RenderIterator(Rectangle clip) {
            residue_height = props.getResidueHeight();
            residue_width = props.getResidueWidth();	
            render_gaps = props.isGapRendered();

            int len = sequence.length();
	    
            start_idx = 
                    (int) Math.floor(((float) clip.x) / ((float) residue_width)) 
                    - 1;
            if (start_idx < 1)
                start_idx = 1;
            end_idx = 
                    (int) Math.floor(((float) clip.x + clip.width) 
                        / ((float) residue_width)) 
                    + 1;
            if (end_idx >= len) 
                end_idx = len - 1;

            this_seq_color = sequence.getColor();
            highlights = props.getHighlights(sequence);
            ca = sequence.getColumnAnnotationsCount() > 0
                    ? sequence.getColumnAnnotations()
                    : null;

            if (background_color == null) 
                background_color = new Color[len];
            if (foreground_color == null)
                foreground_color = new Color[len];
            cs = props.getColorScheme();
        }

        public boolean hasNext() {
            return current_idx < end_idx;
        }

        public void reset() { 
            current_idx = start_idx - 1;
            current_x = current_idx * residue_width;
            if (highlights != null)
                h_idx = 0;
            if (ca != null)
                ca_idx = 0;
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
            if (highlights != null) {
                while (h_idx < highlights.length 
                    && highlights[h_idx] < current_idx)
                    h_idx++;
                this_res_highlighted = 
                        h_idx < highlights.length 
                        && highlights[h_idx] == current_idx;
            }
            if (ca != null) {
                while (ca_idx < ca.length 
                    && ca[ca_idx].getColumn() < current_idx)
                    ca_idx++;
                this_res_ca = ca_idx < ca.length
                        && ca[ca_idx].getColumn() == current_idx
                        ? ca[ca_idx] : null;
            }
        }
    
        public Sequence.ColumnAnnotation getColumnAnnotation() {
            return this_res_ca;
        }

        public Color getBackgroundColor() {
            Color c;

            if (this_res_highlighted) {
                if (this_seq_color != null)
                    c = this_seq_color;
                else {
                    c = foreground_color[current_idx];
                    if (c == null) { 
                        c = cs.getForegroundColor(sequence, current_idx);
                        foreground_color[current_idx] = c;
                    }
                }
            } else {
                c = background_color[current_idx];
                if (c == null) {
                    c = cs.getBackgroundColor(sequence, current_idx);
                    background_color[current_idx] = c;
                }
            }
            return c;
        }

        private Color getForegroundColor() {
            Color c;

            if (this_res_highlighted) {
                c = background_color[current_idx];
                if (c == null) {
                    c = cs.getBackgroundColor(sequence, current_idx);
                    background_color[current_idx] = c;
                }
            } else {
                if (this_seq_color != null)
                    c = this_seq_color;
                else {
                    c = foreground_color[current_idx];
                    if (c == null) { 
                        c = cs.getForegroundColor(sequence, current_idx);
                        foreground_color[current_idx] = c;
                    }
                }
            }
            return c;
        }

        private String getAACode() {
            AminoAcid aa = sequence.getAA(current_idx);

            if (!aa.isGap() || render_gaps)
                return aa.getCode();
            return " ";
        }
    }

    // render the sequence
    public void paint(Graphics g) {
        g.setColor(Color.white);
        Sequence.ColumnAnnotation ca = null;
        Color caColor = Color.white;   
        Dimension d = getSize();

        g.fillRect(0, 0, d.width, d.height);

        // figure out the range to render
        Rectangle clip = g.getClipBounds();
        int residue_width = props.getResidueWidth();
        int residue_height = props.getResidueHeight();
        RenderIterator ri = new RenderIterator(clip);

        // debugging
        // System.out.println(sequence.getName() + " " + ri.getStartIndex()
        // + " " + ri.getEndIndex());

        // draw the backgrounds
        // try to draw as few boxes as possible
        if (props.isFastRender()) {
            Color last_color = null;
            int min_x = 0;
            int last_x = 0;
	    
            for (ri.reset(); ri.hasNext();) {
                ri.next();
                int x = ri.getX();
                Color color = ri.getBackgroundColor();

                if (last_color == null)
                    min_x = x;
                else if (!color.equals(last_color)) {
                    if (!last_color.equals(Color.white)) {
                        g.setColor(last_color);
                        g.fillRect(min_x, 
                            0, 
                            (last_x - min_x) + residue_width, 
                            residue_height);
                    }
                    min_x = x;
                }
		
                if (!ri.hasNext()) {
                    if (!color.equals(Color.white)) {
                        g.setColor(color);
                        g.fillRect(min_x, 
                            0, 
                            (x - min_x) + residue_width, 
                            residue_height);
                    }
                }
		
                last_x = x;
                last_color = color;
            }
        } else {
            for (ri.reset(); ri.hasNext();) {
                ri.next();
                g.setColor(ri.getBackgroundColor());
                g.fillRect(ri.getX(), 
                    0, 
                    residue_width, 
                    residue_height);
            }
        }
		
        // draw column annotations
        if (sequence.getColumnAnnotationsCount() > 0 && props.showResAnn()) {
            for (ri.reset(); ri.hasNext();) {
                ri.next();
                ca = ri.getColumnAnnotation();
                if (ca != null) {
                    g.setColor(ca.getColor());
                    g.fillOval(ri.getX(), 0, 
                        residue_width - 1, residue_height - 1);
                }
            }
        } else ca = null;

        // draw residue text
        int font_x = props.getFontXOffset();
        int font_y = props.getFontYOffset();
        boolean render_gaps = props.isGapRendered();

        g.setFont(props.getFont());
        if (props.isFastRender()) {
            Color last_color = null;
            int min_x = 0;
            StringBuffer sb = new StringBuffer();

            for (ri.reset(); ri.hasNext();) {
                ri.next();
                int x = ri.getX();

                ca = ri.getColumnAnnotation();
                if (ca != null) caColor = ca.getColor();
                Color color = ca == null || !props.showResAnn() ? ri.getForegroundColor() : props.inverseRGB(caColor);  

                if (last_color == null) 
                    min_x = x;
                else if (!color.equals(last_color)) {
                    g.setColor(last_color);
                    g.drawString(sb.toString(), min_x + font_x,
                        font_y);
                    sb = new StringBuffer();
                    min_x = x;
                }
	    
                if (!ri.hasNext()) {
                    sb.append(ri.getAACode());
                    g.setColor(last_color);
                    g.drawString(sb.toString(), min_x + font_x,
                        font_y);
                }

                sb.append(ri.getAACode());
                last_color = color;
            }
        } else {
            for (ri.reset(); ri.hasNext();) {
                ri.next();	
                String aa = ri.getAACode();

                if (!aa.equals(" ")) {
                    ca = ri.getColumnAnnotation();
                    if (ca != null) caColor = ca.getColor();
                    Color color = ca == null | !props.showResAnn() ? ri.getForegroundColor() : props.inverseRGB(caColor); 

                    g.setColor(color);
                    g.drawString(aa, ri.getX() + font_x, font_y);
                }
            }
        }

        // draw line annotations
        if (props.getAnnView(sequence)) {
            Sequence.LineAnnotation[] la = sequence.getLineAnnotations();

            g.setColor(Color.black);
            if (props.isFastRender()) {
                int start = ri.getStartIndex();

                for (int k = 0; k < la.length; k++) {
                    int y = (k + 1) * residue_height;

                    font_y = y + props.getFontYOffset();
                    String ann = la[k].getAnnotation();
                    int end = Math.min(ann.length(), ri.getEndIndex() + 1);

                    if (start < end)
                        g.drawString(ann.substring(start, end),
                            start * residue_width + font_x,
                            font_y);
                }
            } else {
                for (int k = 0; k < la.length; k++) {
                    int y = (k + 1) * residue_height;

                    font_y = y + props.getFontYOffset();
                    String ann = la[k].getAnnotation();
                    int max_idx = ann.length() - 1;

                    for (ri.reset(); ri.hasNext();) {
                        ri.next();
                        int idx = ri.getIndex();

                        if (idx > max_idx)
                            break;
                        g.drawString(ann.substring(idx, idx + 1), 
                            ri.getX() + font_x,
                            font_y);
                    }
                }
            }
        }
    }
}

