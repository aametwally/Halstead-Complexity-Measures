package com.neogenesis.pfaat.seqspace;


import java.util.Arrays;

import com.neogenesis.pfaat.*;


/**
 * SeqSpaceEncoder implementation for a binary encoding matrix.
 * Scores generally represent pairwise identity, with some exceptions
 * (e.g. X, B, Z).
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:38 $ */
public class BinaryEncoder implements SeqSpaceEncoder {
    private static double[][] encodings;
    private static double[][] score_matrix;

    // the special cases
    // B is weighted Q + E
    private static final double B_WEIGHT_GLU = 0.61;
    private static final double B_WEIGHT_GLN = 0.39;
    // Z is weighted D + B
    private static final double Z_WEIGHT_ASP = 0.54;
    private static final double Z_WEIGHT_ASN = 0.42;

    static {
        encodings = new double[AminoAcid.NUM_AA][AminoAcid.NUM_TRUE_AA];

        for (int aa_idx = AminoAcid.NUM_AA - 1; aa_idx >= 0; aa_idx--) {
            AminoAcid aa = AminoAcid.lookupByIndex(aa_idx);

            Arrays.fill(encodings[aa_idx], 0.0);
            if (aa.isTrueAA())
                encodings[aa_idx][aa_idx] = 1.0;
            else if (aa.equals(AminoAcid.B)) {
                encodings[aa_idx][AminoAcid.GLU.getIndex()] = B_WEIGHT_GLU;
                encodings[aa_idx][AminoAcid.GLN.getIndex()] = B_WEIGHT_GLN;
            } else if (aa.equals(AminoAcid.Z)) {
                encodings[aa_idx][AminoAcid.ASP.getIndex()] = Z_WEIGHT_ASP;
                encodings[aa_idx][AminoAcid.ASN.getIndex()] = Z_WEIGHT_ASN;
            } // X is some of everythings
            else if (aa.equals(AminoAcid.X)) {
                for (int k = AminoAcid.NUM_TRUE_AA - 1; k >= 0; k--)
                    encodings[aa_idx][k] = 1.0 / AminoAcid.NUM_TRUE_AA;
            } else if (aa.isGap())
                continue;
            else
                throw new RuntimeException("no BinaryEncoding representation "
                        + "for " + aa.getCode());
        }

        score_matrix = new double[AminoAcid.NUM_AA][AminoAcid.NUM_AA];
        for (int i = AminoAcid.NUM_AA - 1; i >= 0; i--) {
            for (int j = i; j >= 0; j--) {
                double score = 0.0;

                for (int k = AminoAcid.NUM_TRUE_AA - 1; k >= 0; k--)
                    score += encodings[i][k] * encodings[j][k];
                score_matrix[i][j] = score_matrix[j][i] = score;
            }
        }
    }

    public double getPairScore(AminoAcid aa1, AminoAcid aa2) {
        return score_matrix[aa1.getIndex()][aa2.getIndex()];
    }
    
}
