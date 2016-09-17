package com.neogenesis.pfaat.seqspace;


import java.text.DecimalFormat;
import javax.swing.*;

import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.*;


public class SeqSpaceWorker extends ProgressWorker {
    private SeqSpace ss;
    private DisplayProperties props;
    private AlignmentFrame owner;

    public SeqSpaceWorker(AlignmentFrame owner,
        Sequence[] seqs, int n_dims, 
        boolean blosum62, DisplayProperties props) {
        super();
        this.props = props;
        this.owner = owner;
        ss = new SeqSpace(seqs, n_dims, blosum62);
        setTotalIterations(ss.getTotalIterations());
    }

    public Object construct() throws Exception {
        ss.compute(this);
        return ss;
    }

    public void finished(Object o) {
        double time = getElapsedTime() / 1000.0;
        DecimalFormat df = new DecimalFormat("0.00");

        JOptionPane.showMessageDialog(owner,
            "SeqSpace analysis complete. "
            + "(" + getInfo() + ") in "
            + df.format(time) + " (secs)",
            "Done!",
            JOptionPane.INFORMATION_MESSAGE);
        if (o != null) {
            SeqSpaceFrame frame = new SeqSpaceFrame(owner, 
                    (SeqSpace) o, 
                    props);

            frame.show();
        }
    }

    public String getNote() {
        return "Performing SeqSpace analysis (" + getInfo() + ").";
    }

    public String getErrorMessage() {
        return "SeqSpace analysis error.";
    }

    public String getInfo() {
        return ss.getNumSequences() + " sequences, "
            + ss.getNumResidues() + " residues, "
            + ss.getNumDimensions() + " dimensions";
    }

}

