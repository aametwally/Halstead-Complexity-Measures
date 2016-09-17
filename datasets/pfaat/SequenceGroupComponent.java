package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.neogenesis.pfaat.colorscheme.*;


public class SequenceGroupComponent extends JPanel
    implements ActionListener, SequenceListener, DisplayPropertiesListener {
    // underlying sequence
    private Sequence sequence = null;
    // properties
    private DisplayProperties props;
    // text field
    private JTextField text_field;

    private JLabel groupLabel;
    
    public SequenceGroupComponent(Sequence sequence, 
        DisplayProperties props) {
        super();
        setBackground(Color.white);
        setLayout(new BorderLayout());

        this.props = props;
        props.addListener(this);
        groupLabel = new JLabel(); 
        groupLabel.setFont(props.getFont());
        groupLabel.setText(sequence.getFormattedGroupString());      

        setSequence(sequence);
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

    public void sequenceNameChanged(Sequence aaseq, String old_name) {}

    public void sequenceAnnotationChanged(Sequence aaseq) {
        if (aaseq != sequence) 
            throw new RuntimeException("bound to incorrect Sequence");
        revalidate();
        repaint();
    }

    public void sequenceGroupChanged(Sequence aaseq) { 
        revalidate();
        repaint();
    }

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {}

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {}

    public void sequenceColorChanged(Sequence aaseq) {}

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
        groupLabel.setFont(props.getFont());
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
    public Dimension getPreferredSize() {
        FontMetrics font_metrics = props.getFontMetrics();
        int height = props.getResidueHeight();

        if (props.getAnnView(sequence)) {
            Sequence.LineAnnotation[] la = sequence.getLineAnnotations();

            height *= 1 + la.length;
        }
        int width = font_metrics.stringWidth(sequence.getFormattedGroupString());

        return new Dimension(width, height);
    }
    
    // ActionListener interface
    public void actionPerformed(ActionEvent evt) {}

    public void paint(Graphics g) {
        g.setColor(Color.white);
        Dimension d = getSize();

        g.fillRect(0, 0, d.width, d.height);

        Font font = props.getFont();
        int x = props.getFontXOffset();
        int font_y = props.getFontYOffset();
        int residue_height = props.getResidueHeight();

        g.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
        g.setColor(Color.blue);
        g.drawString(sequence.getFormattedGroupString(), x, font_y);
    }
}

