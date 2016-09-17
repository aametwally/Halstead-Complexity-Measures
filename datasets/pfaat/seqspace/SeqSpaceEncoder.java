package com.neogenesis.pfaat.seqspace;


import com.neogenesis.pfaat.AminoAcid;


/**
 * Interface for encoding an AminoAcid as a vector for SeqSpace analysis.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:38 $ */
public interface SeqSpaceEncoder {
    // get the score between two AAs, generally the dot 
    // product of their respective encodings
    public double getPairScore(AminoAcid aa1, AminoAcid aa2);
}
