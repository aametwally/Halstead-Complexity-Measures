package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import com.neogenesis.pfaat.colorscheme.*;
import com.neogenesis.pfaat.util.*;


/**
 * <code>Component</code> for displaying a single sequence annotation.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class SequenceAnnComponent extends JPanel
    implements MouseListener, 
        SequenceListener, 
        DisplayPropertiesListener {
    // underlying sequence
    private Sequence sequence = null;
    // properties
    private DisplayProperties props;

    private class EditDialog extends JDialog {
        private boolean hit_OK = false;
        private JLabel seqNameLabel;
        private String annotation;
        private JTextField annTF;
        private JPanel center_panel = new JPanel();
        private JPanel button_panel = new JPanel();
	
        public EditDialog(String ann) {
            super((Frame) null, "Edit Sequence Annotation", true);
            getContentPane().setLayout(new BorderLayout());

            getContentPane().add(center_panel, BorderLayout.CENTER);
            center_panel.setLayout(new GridLayout(2, 1));
            seqNameLabel = new JLabel("Sequence: " + sequence.getName());
            this.annotation = ann;
            annTF = new JTextField(annotation, 20);
            Utils.addClipboardBindings(annTF);
            center_panel.add(seqNameLabel);
            center_panel.add(annTF);

            getContentPane().add(button_panel, BorderLayout.SOUTH);
            JButton ok_button = new JButton("OK");
            JButton cancel_button = new JButton("Cancel");

            ok_button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        annotation = annTF.getText();
                        dispose();
                    }
                }
            );
            cancel_button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        dispose();
                    }
                }
            );
            button_panel.add(ok_button);
            button_panel.add(cancel_button);

            setSize(getPreferredSize());
        }

        public Dimension getPreferredSize() {
            int w = 200;
            int h = 120;
	    
            Dimension snld = seqNameLabel.getSize();

            if (snld.width > w)
                w = snld.width;
            if (w > 400)
                w = 400;

            return (new Dimension(w, h));
        }

        public void show() {
            super.show();
        }
	
        public String getAnnotation() {
            return (annotation);
        }
    }
    
    public SequenceAnnComponent(Sequence sequence, 
        DisplayProperties props) {
        super();
        setBackground(Color.white);
        setLayout(new BorderLayout());

        this.props = props;
        props.addListener(this);

        addMouseListener(this);

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

    // start editing
    private void edit() {
        removeAll();
        props.setSeqAnnEditing(true);
        props.setRulerEditing(false);
        props.setCursorHidden(true);

        EditDialog ed = new EditDialog(sequence.getAnnotation());

        ed.setLocation(getLocationOnScreen());
        ed.show();
	
        String ann = ed.getAnnotation();

        if (ann != null)
            if (!ann.equals(sequence.getAnnotation())) 
                sequence.setAnnotation(ann.length() > 0 ? ann : "");
        revalidate();
        repaint();
        props.setSeqAnnEditing(false);
        props.setCursorHidden(false);
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

    public void sequenceGroupChanged(Sequence aaseq) {}

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
        int width = 2 * props.getResidueWidth();
        String ann = sequence.getAnnotation();

        if (ann != null) {
            width += font_metrics.stringWidth(ann) + 2;
            width = Math.min(width, 25 * props.getResidueWidth());
        }
        return new Dimension(width, height);
    }
    
    public void paint(Graphics g) {
        g.setColor(Color.white);
        Dimension d = getSize();

        g.fillRect(0, 0, d.width, d.height);

        String ann = sequence.getAnnotation();

        if (ann == null) return;

        Font font = props.getFont();
        int x = props.getFontXOffset();
        int font_y = props.getFontYOffset();
        int residue_height = props.getResidueHeight();

        g.setFont(font);
        g.setColor(Color.black);
        g.drawString(ann, x, font_y);
    }

    // MouseListener interface
    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) { 
        if (e.getClickCount() >= 2)
            edit();
    }

    public void mouseReleased(MouseEvent e) {}
}

