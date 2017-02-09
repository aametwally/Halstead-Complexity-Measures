package com.neogenesis.pfaat.colorscheme;


import java.awt.Color;
import java.util.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.seqspace.*;


/**
 * Color scheme for Blosum62.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:34 $ */
public class Blosum62ColorScheme implements ColorScheme, SequenceListener {

    private ConsensusSequence cons;
    private static final Color match = new Color(154, 154, 255);
    private static final Color mis = new Color(204, 204, 255);
    private SeqSpaceEncoder blosum62 = new Blosum62Encoder();

    public Blosum62ColorScheme(ConsensusSequence cons) {
        this.cons = cons;
        cons.addListener(this);
    }

    public String getName() {
        return "Blosum62";
    }

    public Color getForegroundColor(Sequence s, int res_idx) { 
        return Color.black; 
    }

    public Color getBackgroundColor(Sequence s, int res_idx) {
        AminoAcid s_aa = s.getAA(res_idx);

        if (!s_aa.isGap()) {
            AminoAcid c_aa = res_idx < cons.length() ? cons.getAA(res_idx) 
                : AminoAcid.GAP;

            if (s_aa.equals(c_aa))
                return match;
            if (blosum62.getPairScore(c_aa, s_aa) > 0)
                return mis;
        }
        return Color.white;
    }

    // list of listeners
    private List listeners = new ArrayList();
    public void addListener(ColorSchemeListener l) {
        listeners.add(l);
    }

    public void removeListener(ColorSchemeListener l) {
        listeners.remove(l);
    }

    // SequenceListener interface
    public void sequenceNameChanged(Sequence aaseq, String old_name) {}

    public void sequenceAAChanged(Sequence aaseq) {
        if (aaseq != cons)
            throw new RuntimeException("bound to incorrect consensus");
        for (Iterator it = listeners.iterator(); it.hasNext();) 
            ((ColorSchemeListener) it.next()).colorSchemeChanged(this);	
    }

    public void sequenceAnnotationChanged(Sequence aaseq) {}

    public void sequenceGroupChanged(Sequence aaseq) {}

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {}

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {}

    public void sequenceColorChanged(Sequence aaseq) {}

}
