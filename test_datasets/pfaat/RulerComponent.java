package com.neogenesis.pfaat;


import java.awt.*;
import javax.swing.*;

import com.neogenesis.pfaat.colorscheme.ColorScheme;


/**
 * Component for marking off residue offsets.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class RulerComponent extends JPanel
    implements AlignmentListener, DisplayPropertiesListener {
    // underlying alignment
    private Alignment alignment;
    // rendering properties
    private DisplayProperties props;
    // size
    private int size;

    public RulerComponent(Alignment alignment,
        DisplayProperties props) {
        super();
        this.props = props;
        props.addListener(this);
        this.alignment = alignment;
        size = alignment.maxLength();
        alignment.addListener(this);
        setBackground(Color.white);
    }

    private void resizeToAlignment() {
        int new_size = alignment.maxLength();

        if (new_size != size) {
            size = new_size;
            revalidate();
            repaint();
        }
    }

    // SequenceListener interface
    public void sequenceAAChanged(Sequence aaseq) {
        resizeToAlignment();
    }

    public void sequenceNameChanged(Sequence aaseq) {}

    public void sequenceAnnotationChanged(Sequence aaseq) {}

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {}

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {}

    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {}

    public void alignmentSeqInserted(Alignment align, int i) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        resizeToAlignment();
    }

    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        resizeToAlignment();
    }

    public void alignmentSeqSwapped(Alignment alignment, int i, int j) {}

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        resizeToAlignment();
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

    public Dimension getPreferredSize() {
        int height = props.getResidueHeight();
        int width = props.getResidueWidth() * size;

        return new Dimension(width, height);
    }

    // render the sequence
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int y = 0;
        int font_y = y + props.getFontYOffset() - 2;
        int residue_height = props.getResidueHeight();
        int residue_width = props.getResidueWidth();
        int line_height = residue_height 
            - props.getFontMetrics().getMaxAscent() 
            - props.getFontMetrics().getMaxDescent();
        int line_y_start = y + residue_height - line_height;
        FontMetrics fm = props.getFontMetrics();

        g.setFont(props.getFont());
        g.setColor(Color.black);
        for (int idx = size; idx > 0; idx--) {
            if (idx % 10 == 0) {
                int x = idx * residue_width - 1;

                g.drawLine(x, line_y_start, x, 
                    line_y_start + 2 * line_height);
                String str = Integer.toString(idx);
                int width = fm.stringWidth(str);

                g.drawString(str, x - width + 2, font_y);
            } else if (idx % 5 == 0) {
                int x = idx * residue_width - 1;

                g.drawLine(x, line_y_start, x, 
                    line_y_start + line_height);
            }
        }
    }
}

