package com.neogenesis.pfaat;


import java.util.*;


/**
 * Sort an alignment based on a comparator.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public class AlignmentSorter {
    private Alignment alignment;
    private Comparator comparator;

    public AlignmentSorter(Alignment alignment, Comparator comparator) {
        this.alignment = alignment;
        this.comparator = comparator;
    }

    public void sort() throws Exception {
        Sequence[] seqs = alignment.getAllSequences();

        Arrays.sort(seqs, comparator);
	
        for (int i = seqs.length - 1; i >= 0; i--) {
            int orig_idx = alignment.getIndex(seqs[i]);

            if (orig_idx != i)
                alignment.swapSequence(i, orig_idx);
        }
    }
}
