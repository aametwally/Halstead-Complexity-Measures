package com.neogenesis.pfaat.colorscheme;


import java.awt.Color;
import java.util.*;

import com.neogenesis.pfaat.*;  


/**
 * Color scheme based on PID.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:34 $ */
public class PIDColorScheme implements ColorScheme, SequenceListener {

    private static Color[] colors = {    
            new Color(100, 100, 255),
            new Color(153, 153, 255),
            new Color(204, 204, 255),
            Color.white
        };
    private static double[] thresholds = { 
            0.80,
            0.60,
            0.40,
            -1.0,
        }; 

    private ConsensusSequence cons;

    private static final Color match = new Color(154, 154, 255);
    private static final Color mis = new Color(204, 204, 255);

    public PIDColorScheme(ConsensusSequence cons) {
        this.cons = cons;
        cons.addListener(this);
    }

    public String getName() {
        return "PID";
    }

    public Color getForegroundColor(Sequence s, int res_idx) { 
        return Color.black; 
    }

    public Color getBackgroundColor(Sequence s, int res_idx) {
        AminoAcid s_aa = s.getAA(res_idx);

        if (!s_aa.isGap() && res_idx < cons.length()) {
            double pid = cons.getPID(res_idx, s_aa);

            for (int k = 0; k < thresholds.length; k++) {
                if (pid > thresholds[k]) 
                    return colors[k];
            }
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
