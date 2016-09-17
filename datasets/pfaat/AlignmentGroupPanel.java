package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.pdb.*;
import com.neogenesis.pfaat.srs.*;  


public class AlignmentGroupPanel extends JPanel
    implements AlignmentListener {
    // underlying alignment
    protected Alignment alignment;
    // underlying display properties
    protected DisplayProperties props;

    public AlignmentGroupPanel(Alignment alignment, 
        DisplayProperties props) {
        super();
        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.props = props;
        this.alignment = alignment;
        alignment.addListener(this);
        for (int i = 0; i < alignment.size(); i++) {
            add(new SequenceGroupComponent(alignment.getSequence(i), props));      
        }
    }

    public Dimension getPreferredSize() {
        return getLayout().preferredLayoutSize(this);
    }

    public void stopEdit() {}
    
    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {}

    public void alignmentSeqInserted(Alignment align, int i) {
        if (alignment != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        add(new SequenceGroupComponent(alignment.getSequence(i), props), i);
        revalidate();
        repaint();
    }

    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq) {
        if (alignment != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        remove(i);
        revalidate();
        repaint();
    }

    public void alignmentSeqSwapped(Alignment alignment, int i, int j) {
        if (alignment != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");

        int min, max;

        if (i < j) {
            min = i;
            max = j;
        } else if (i > j) {
            min = j;
            max = i;
        } else
            return;

        Component min_c = getComponent(min);
        Component max_c = getComponent(max);

        remove(max);
        remove(min);
        add(max_c, min);
        add(min_c, max);
    }

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {}

}
