package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.colorscheme.*;
                                             

/**
 * <code>Component</code> for displaying a single sequence.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class ConsensusComponent extends JPanel
    implements SequenceListener, DisplayPropertiesListener {
    public static final int CONSENSUS_NONE = 0;
    public static final int CONSENSUS_PID = 1;
    public static final int CONSENSUS_SIMILARITY = 2;
    public static final int CONSENSUS_INFORMATION_GAPSINCLUDED = 3;
    public static final int CONSENSUS_INFORMATION_GAPSEXCLUDED = 4;
    public static final int CONSENSUS_INFORMATION_GAPSRANDOMIZED = 5;
    public static final int CONSENSUS_PIDDIF = 6;
    
    // underlying sequence
    private ConsensusSequence consensus = null;
    private ConsensusSequence c3 = null;
    private ConsensusSequence c2 = null;
    // rendering properties
    private DisplayProperties props;
    // consensus type
    private int consensus_type = CONSENSUS_SIMILARITY;
    // is consensus intergroup comparison
    private boolean comparison = false;
    // minimum consensus length
    private int len;
    private double c3v, c2v;
    public ConsensusComponent(ConsensusSequence consensus,
        DisplayProperties props) {
        super();
        this.props = props;
        this.comparison = false;
        props.addListener(this);
        setConsensus(consensus);
    }
    
    public void setComparison(boolean b) {
        comparison = b;
    }

    public void setComparisonConsensuses(ConsensusSequence newc3, 
        ConsensusSequence newc2) {
        c3 = newc3;
        c2 = newc2;
    }
    
    // set the consensus type
    public void setConsensusType(int consensus_type) {
        if ((consensus_type != CONSENSUS_NONE) &&
            (consensus_type != CONSENSUS_PID) &&
            (consensus_type != CONSENSUS_SIMILARITY) &&
            (consensus_type != CONSENSUS_INFORMATION_GAPSINCLUDED) &&
            (consensus_type != CONSENSUS_INFORMATION_GAPSEXCLUDED) &&
            (consensus_type != CONSENSUS_INFORMATION_GAPSRANDOMIZED) &&
            (consensus_type != CONSENSUS_PIDDIF)) {
            this.consensus_type = CONSENSUS_NONE;
            ErrorDialog.showErrorDialog(this, "Illegal value for consensus type");
        } else {
            this.consensus_type = consensus_type;
        }
        revalidate();
        repaint();
    }
    
    // set the underlying sequence
    public void setConsensus(ConsensusSequence consensus) {
        if (this.consensus != null) this.consensus.removeListener(this);
        this.consensus = consensus;
        revalidate();
        repaint();
    }	
    
    // SequenceListener interface
    public void sequenceAAChanged(Sequence aaseq) {
        if (aaseq != consensus) 
            throw new RuntimeException("bound to incorrect Sequence");
        revalidate();
        repaint();
    }

    public void sequenceNameChanged(Sequence aaseq, String old_name) {}

    public void sequenceAnnotationChanged(Sequence aaseq) {}

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

    public void displayRenderGapsChanged(DisplayProperties dp) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        revalidate();
        repaint();
    }

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs) {}

    public Dimension getPreferredSize() {
        int height = props.getResidueHeight() * 3;
        int width = props.getResidueWidth() * consensus.length();

        return new Dimension(width, height);
    }

    // render the sequence
    public void paint(Graphics g) {
        g.setColor(Color.white);
        Dimension d = getSize();

        g.fillRect(0, 0, d.width, d.height);
	
        Rectangle clip = g.getClipBounds();

        len = comparison ? Math.min(c3.length(), c2.length()) : consensus.length();
        int residue_width = props.getResidueWidth();
        int residue_height = props.getResidueHeight();
        int block_height = 3 * residue_height;
        boolean render_gaps = props.isGapRendered();

        g.setFont(props.getFont());

        int start = clip.x / residue_width;

        if (start < 0)
            start = 0;
        int end = (clip.x + clip.width) / residue_width;

        if (end >= len) 
            end = len - 1;

        for (int i = end; i >= start; i--) {
            int x = i * residue_width;

            if (clip.intersects(x, 0, residue_width, block_height)) {
                AminoAcid aa = consensus.getAA(i);

                if (!aa.isGap()) {
                    double consensusValue = 0.0;
                    Color  consensusColor = Color.white;

                    switch (consensus_type) {
                    case CONSENSUS_PIDDIF:
                        consensusColor = getConsensusColor(CONSENSUS_PID);
                        break;

                    case CONSENSUS_SIMILARITY:
                        consensusValue = consensus.getSimilarity(i);
                        consensusColor = 
                                getConsensusColor(CONSENSUS_SIMILARITY);
                        break;

                    case CONSENSUS_PID:
                        consensusValue = consensus.getPID(i);
                        consensusColor = 
                                getConsensusColor(CONSENSUS_PID);
                        break;

                    case CONSENSUS_INFORMATION_GAPSRANDOMIZED:
                        consensusValue = consensus.getInformationGapsRandomized(i);
                        consensusColor = 
                                getConsensusColor(CONSENSUS_INFORMATION_GAPSRANDOMIZED);
                        break;

                    case CONSENSUS_INFORMATION_GAPSEXCLUDED:
                        consensusValue = consensus.getInformationGapsExcluded(i);
                        consensusColor = 
                                getConsensusColor(CONSENSUS_INFORMATION_GAPSEXCLUDED);
                        break;

                    case CONSENSUS_INFORMATION_GAPSINCLUDED:
                        consensusValue = consensus.getInformationGapsIncluded(i);
                        consensusColor = 
                                getConsensusColor(CONSENSUS_INFORMATION_GAPSINCLUDED);
                        break;

                    default:
                        ErrorDialog.showErrorDialog(this, "Illegal value for consensus type");
                        break;
                    }
                    if (comparison) {
                        consensusValue = 0;
                        for (int j = 0; j < AminoAcid.NUM_TRUE_AA; j++)
                            consensusValue += Math.abs(c2.getPID(i, j) - c3.getPID(i, j));
                        consensusValue /= 2;
                    }
                    boolean noConsensus = false;

                    if (c2 != null && c3 != null && 
                        ((c2.getAA(i).isGap() || c3.getAA(i).isGap())))
                        noConsensus = true;

                    if (!noConsensus) {
                        int height = (int) (2 * residue_height * Math.abs(consensusValue));

                        g.setColor(consensusColor);
                        g.fillRect(x, 2 * residue_height - height, 
                            residue_width, height);
                    }
                }
                if (!aa.isGap() || render_gaps) {
                    g.setColor(Color.black);
                    g.drawString(aa.getCode(), 
                        x + props.getFontXOffset(), 
                        2 * residue_height + props.getFontYOffset());
                }
            }
        }
    }

    public Color getConsensusColor(int consensus) {
        switch (consensus) {
        case CONSENSUS_NONE: 
            return Color.black;

        case CONSENSUS_PID:
            return Color.red;

        case CONSENSUS_SIMILARITY: 
            return Color.orange;

        case CONSENSUS_INFORMATION_GAPSINCLUDED:
            return Color.blue;

        case CONSENSUS_INFORMATION_GAPSEXCLUDED:
            return Color.green;

        case CONSENSUS_INFORMATION_GAPSRANDOMIZED:
            return Color.magenta;

        case CONSENSUS_PIDDIF:
            return Color.red;

        default:
            return Color.black;
        }
    }
}
    
