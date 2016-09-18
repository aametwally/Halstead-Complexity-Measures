package com.neogenesis.pfaat.colorscheme;


import java.awt.Color;
import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;

import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.*;        


/**
 * Color scheme based on residue in sequence and PID versus consensus.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:34 $ */
public class ResidueColorScheme implements ColorScheme, SequenceListener {
    private ConsensusSequence cons;
    private double pid_threshold;
    protected Color[] color_table;
    private String name;
    private static Perl5Util perl = new Perl5Util();
    
    public ResidueColorScheme(ConsensusSequence cons,
        double pid_threshold,
        Color[] color_table,
        String name) {
        this.cons = cons;
        if (cons != null)
            cons.addListener(this);
        this.pid_threshold = pid_threshold;
        this.color_table = color_table;
        this.name = name;
    }

    public ResidueColorScheme(ConsensusSequence cons, File f) throws Exception {
        this.cons = cons;
        load(f);
    }

    public String getName() {
        return name;
    }

    public Color[] getColorTable() {
        return color_table;
    }

    public double getPID() {
        return pid_threshold;
    }

    public Color getForegroundColor(Sequence s, int res_idx) { 			
        return Color.black; 
    }

    public Color getBackgroundColor(Sequence s, int res_idx) {
        AminoAcid s_aa = s.getAA(res_idx);

        if (pid_threshold > 0.0) {
            if (cons != null && res_idx < cons.length())
                return cons.getPID(res_idx, s_aa) >= pid_threshold
                    ? color_table[s_aa.getIndex()]
                    : Color.white;
        } else
            return color_table[s_aa.getIndex()];
        return Color.white;
    }

    public void load(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        Properties p = new Properties();

        p.load(in);
        in.close();

        color_table = new Color[AminoAcid.NUM_AA];
        for (int i = 0; i < color_table.length; i++) {
            String rgb = p.getProperty(AminoAcid.lookupByIndex(i).getCode3());

            if (rgb == null)
                throw new Exception("no color table entry for "
                        + AminoAcid.lookupByIndex(i).getCode3());

            Vector v = perl.split(rgb);

            color_table[i] = new Color(Integer.parseInt((String) v.get(0)),
                        Integer.parseInt((String) v.get(1)),
                        Integer.parseInt((String) v.get(2)));
				       
        }
        pid_threshold = 
                Double.parseDouble(p.getProperty("PID"));
        name = Utils.getBaseFileName(f);
    }

    public void save(File f) throws Exception {
        PrintStream out = new PrintStream(new FileOutputStream(f));

        for (int i = 0; i < color_table.length; i++) {
            Color c = color_table[i];

            out.println(AminoAcid.lookupByIndex(i).getCode3() + " = " 
                + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
        }
        out.println("PID = " + pid_threshold);
        out.close();
    }

    public static javax.swing.filechooser.FileFilter getFileFilter() { 
        return new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return perl.match("/.*\\.csm$/", 
                            f.getName());
                }

                public String getDescription() {
                    return "Color Schemes (*.csm)";
                }
            };
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
