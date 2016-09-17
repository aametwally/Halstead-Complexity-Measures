package com.neogenesis.pfaat;


import java.awt.*;
import java.util.*;
import javax.swing.*;

import com.neogenesis.pfaat.colorscheme.*;


/**
 * Component that displays which columns in an <code>Alignment</code>
 * have attached annotations.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class ColumnAnnotationComponent extends JPanel
    implements AlignmentListener, SequenceListener, DisplayPropertiesListener {
    // underlying alignment
    private Alignment alignment;
    // rendering properties
    private DisplayProperties props;
    // active columns
    private Set[] column_map = null;
    // maximum number of symbols per column
    private int max_symbols = 0;
    // size
    private int size = 0;

    private static class AnnotationDetails {
        public String symbol;
        public Color color;
        public AnnotationDetails(String symbol, Color color) {
            this.symbol = symbol;
            this.color = color;
        }

        public boolean equals(Object other) {
            if (other instanceof AnnotationDetails) {
                AnnotationDetails o = (AnnotationDetails) other;

                return symbol.equals(o.symbol) && color.equals(o.color);
            }
            return false;
        }

        public int hashCode() { 
            return symbol.hashCode() + 11 * color.hashCode();
        }
    }

    public ColumnAnnotationComponent(Alignment alignment,
        DisplayProperties props) {
        super();
        this.props = props;
        props.addListener(this);
        this.alignment = alignment;
        refreshSize(false);
        refreshColumns(false);
        alignment.addListener(this);
        for (int i = alignment.size() - 1; i >= 0; i--)
            alignment.getSequence(i).addListener(this);
        setBackground(Color.white);
    }

    private void refreshSize(boolean redraw) {
        int new_size = alignment.maxLength();

        if (size != new_size) {
            size = new_size;
            if (redraw) {
                revalidate();
                repaint();
            }
        }
    }

    private void refreshColumns(boolean redraw) {
        // build a list of columns with annotation
        Set[] new_column_map = new Set[size];
        int new_max_symbols = 0;

        for (int i = alignment.size() - 1; i >= 0; i--) {
            Sequence seq = alignment.getSequence(i);

            if (seq.getColumnAnnotationsCount() > 0) {
                Sequence.ColumnAnnotation[] ca = 
                    seq.getColumnAnnotations();

                for (int k = ca.length - 1; k >= 0; k--) {
                    int col = ca[k].getColumn();

                    if (new_column_map[col] == null) 
                        new_column_map[col] = new HashSet(3);
                    new_column_map[col].add(new AnnotationDetails(ca[k].getSymbol(),
                            ca[k].getColor()));
                    new_max_symbols = Math.max(new_max_symbols,
                                new_column_map[col].size());
                }
            }
        }
        // figure out if anything has changed
        boolean changed = false;

        if (column_map == null 
            || max_symbols != new_max_symbols
            || column_map.length != new_column_map.length) 
            changed = true;
        else {
            for (int i = column_map.length - 1; i >= 0; i--) {
                if (column_map[i] == null || new_column_map[i] == null) {
                    if (column_map[i] != new_column_map[i]) {
                        changed = true;
                        break;
                    }
                } else if (!column_map[i].equals(new_column_map[i])) {
                    changed = true;
                    break;
                }
            }
        }
			
        // set the map if things have changed
        if (changed) {
            column_map = new_column_map;
            max_symbols = new_max_symbols;
            if (redraw) {
                revalidate();
                repaint();
            }
        }	    
    }

    // SequenceListener interface
    public void sequenceAAChanged(Sequence aaseq) {
        refreshSize(true);
        refreshColumns(true);
    }

    public void sequenceNameChanged(Sequence aaseq, String old_name) {}

    public void sequenceAnnotationChanged(Sequence aaseq) {}

    public void sequenceGroupChanged(Sequence aaseq) {}

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {}

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {
        refreshColumns(true);
    }

    public void sequenceColorChanged(Sequence aaseq) {}

    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {}

    public void alignmentSeqInserted(Alignment align, int i) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        alignment.getSequence(i).addListener(this);
        refreshSize(true);
        refreshColumns(true);
    }

    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        aaseq.removeListener(this);
        refreshSize(true);
        refreshColumns(true);
    }

    public void alignmentSeqSwapped(Alignment alignment, int i, int j) {}

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {}

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

    public Dimension getPreferredSize() {
        int height = props.getResidueHeight() * max_symbols;
        int width = props.getResidueWidth() * size;

        return new Dimension(width, height);
    }

    // render the sequence
    public void paintComponent(Graphics g) {
        if (!props.showResAnnPanel()) return;
        super.paintComponent(g);
        if (max_symbols < 1) return;
        Rectangle clip = g.getClipBounds();

        int residue_height = props.getResidueHeight();
        int residue_width = props.getResidueWidth();
        int x_font_off = props.getFontXOffset();
        int y_font_off = props.getFontYOffset();
        int block_height = residue_height * max_symbols;

        int start = clip.x / residue_width;

        if (start < 1)
            start = 1;
        int end = (clip.x + clip.width) / residue_width;

        if (end >= column_map.length) 
            end = column_map.length - 1;
	
        g.setFont(props.getFont());
        for (int idx = end; idx >= start; idx--) {
            if (column_map[idx] != null) {
                int x = idx * residue_width;

                if (clip.intersects(x, 0, residue_width, block_height)) {
                    int j = 0;

                    for (Iterator i = column_map[idx].iterator(); 
                        i.hasNext();) {
                        AnnotationDetails ad = (AnnotationDetails) i.next();
                        int y = j * residue_height;

                        g.setColor(ad.color);
                        g.fillOval(x, y, residue_width - 1, residue_height - 1);
                        g.setColor(props.inverseRGB(ad.color));
                        g.drawString(ad.symbol,
                            x + x_font_off,
                            y + y_font_off);
                        j++;
                    }
                }
            }
        }
    }
}

