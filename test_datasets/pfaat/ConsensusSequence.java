package com.neogenesis.pfaat;

import java.util.*;

import com.neogenesis.pfaat.seqspace.*;

/**
 * A consensus sequence generated from an <code>Alignment</code>.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/11/19 17:18:29 $ */
public class ConsensusSequence extends Sequence {
    private Sequence[] seqs;
    private int[][] freq_table;
    private double[] similarity_table;
    private double[] information_gapsrandomized;
    private double[] information_gapsexcluded;
    private double[] information_gapsincluded;
    private int[] num_residues;
    private Random seed = new Random();

    private Alignment alignment;
    private SeqSpaceEncoder blosum62 = new Blosum62Encoder();
    private Sequence seq;

    public double log2(double x) {
	return Math.log(x)/Math.log(2.0);
    }
    public int randomInt(int low, int high){
      return (seed.nextInt() & Integer.MAX_VALUE) % (high-low+1) + low;
    }
    // constructor
    public ConsensusSequence(Alignment alignment, Sequence[] seqs) {
	this.seqs = seqs;
  this.alignment = alignment;
	initialize("Consensus", null);
	recalc();
    }

    public void setSeqs(Sequence[] newSeqs) { seqs = newSeqs; }

    public void recalc() {
	generateFrequencyTable();
	generateInformationTable();
	AminoAcid[] seq = new AminoAcid [freq_table.length];
	for (int k = seq.length - 1; k >= 0; k--) {
	    seq[k] = AminoAcid.GAP;
	    int max_cnt = 0;
	    for (int i = freq_table[k].length - 1; i >= 0; i--) {
		if (freq_table[k][i] >= max_cnt) {
		    max_cnt = freq_table[k][i];
		    seq[k] = AminoAcid.lookupByIndex(i);
		}
	    }
	}
	generateSimilarityTable(seq);
	setAA(seq);
    }

    public Sequence[] getSeqs() { return seqs; }

    public AminoAcid getUniformAA(int idx) {
	boolean uniform = true;
	AminoAcid aa = seqs[0].getAA(1);
	AminoAcid aa0 = aa;
	for (int j = 0; j < seqs.length; j++) {
	    aa = seqs[j].getAA(idx);
	    if (j==0) aa0 = aa;
	    if (!aa.equals(aa0)) uniform = false;
	}
	if (uniform) return aa0;
	else return null;
    }

    public double getPID(int idx, AminoAcid aa) {
	return ((double) freq_table[idx][aa.getIndex()])
	    / ((double) num_residues[idx]);
    }

    public double getPID(int idx) {
	return ((double) freq_table[idx][getAA(idx).getIndex()])
	    / ((double) num_residues[idx]);
    }

    public double getPID(int idx, int j) {
	return ((double) freq_table[idx][j])
	    / ((double) num_residues[idx]);
    }

    public double getSimilarity(int idx) {
	return similarity_table[idx];
    }

    public double getInformationGapsRandomized(int idx) {
      return information_gapsrandomized[idx];
    }

    public double getInformationGapsExcluded(int idx) {
      return information_gapsexcluded[idx];
    }

    public double getInformationGapsIncluded(int idx) {
      return information_gapsincluded[idx];
    }

    private void generateSimilarityTable(AminoAcid[] cons) {
	similarity_table = new double [cons.length];
	double max = -1e10;
	double min = 1e10;
	// compute a weighted average of consensus vs. seq
	// blosum62 scores
	for (int idx = similarity_table.length - 1; idx >= 0; idx--) {
	    double score = 0.0;
	    AminoAcid c_aa = cons[idx];
	    int[] this_freq = freq_table[idx];
	    for (int k = freq_table[idx].length - 1; k >= 0; k--)
		score += blosum62.getPairScore(c_aa,
					       AminoAcid.lookupByIndex(k))
		    * this_freq[k];
	    score /= num_residues[idx];

	    if (max < score) max = score;
	    if (min > score) min = score;
	    similarity_table[idx] = score;
	}
	// normalize to [0, 1]
	double scale = 1.0 / (max - min);
	for (int idx = similarity_table.length - 1; idx >= 0; idx--)
	    similarity_table[idx] = (similarity_table[idx] - min) * scale;
    }

    private void generateFrequencyTable() {
	freq_table = new int [alignment.maxLength()][AminoAcid.NUM_AA];
	for (int k = freq_table.length - 1; k >= 0; k--)
	    Arrays.fill(freq_table[k], 0);
	num_residues = new int [freq_table.length];
	Arrays.fill(num_residues, 0);

	for (int i = seqs.length - 1; i >= 0; i--) {
	    seq = seqs[i];
	    for (int k = seq.length() - 1; k >= 0; k--) {
		freq_table[k][seq.getAA(k).getIndex()]++;
		num_residues[k]++;
	    }
	}
    }

    private void generateInformationTable() {
	double gapsexcluded_probability;
	double gapsincluded_probability;
	double entropy_gapsexcluded=0;
	double entropy_gapsincluded=0;
	double maxentropy_gapsexcluded=0;
	double maxentropy_gapsincluded=0;

	information_gapsexcluded = new double [alignment.maxLength()];
	information_gapsincluded = new double [alignment.maxLength()];

	for (int i = freq_table.length - 1; i >= 0; i--)
        {
	  entropy_gapsexcluded=0;
	  entropy_gapsincluded=0;
  	  for (int k = AminoAcid.NUM_AA - 1; k >= 0; k--)
          {
	    if ( (freq_table[i][k] > 0) && (num_residues[i] > 0) )
            {

              gapsincluded_probability = (double)freq_table[i][k] / (double)num_residues[i];
              entropy_gapsincluded-=gapsincluded_probability*log2(gapsincluded_probability);

             // gapsexcluded_probability = (double)freq_table[i][k] / (double)(num_residues[i] - 1);

            // The line above is what Neogenesis gave us. There is an obvious problem here since
            // if we sum the above gapsexcluded_probability over all the AAcids save the GAP one we clearly
            // will not get 1.  To correct we need to count the number of gaps in the column i and subtract from
            // the total number of residues in that
            // column. Here is the fix (I have rearranged the order of some lines to help with reading) :

            if ( k < (AminoAcid.NUM_AA - 1) )
              {
		gapsexcluded_probability = (double)freq_table[i][k] / (double)(num_residues[i] - freq_table[i][AminoAcid.NUM_AA-1]);
                entropy_gapsexcluded-=gapsexcluded_probability*log2(gapsexcluded_probability);
              }

	    }
	  }
	  maxentropy_gapsexcluded = log2( (double)(AminoAcid.NUM_AA-1) ); // Fixed an obvious error that was here. Note this formula still treats B and X etc as real amino acids!
	  maxentropy_gapsincluded = log2( (double)(AminoAcid.NUM_AA));    // Made the same fix here. This is fine now. Much better than before. - SS

	 information_gapsexcluded[i] = 1.0-entropy_gapsexcluded/maxentropy_gapsexcluded;
	 information_gapsincluded[i] = 1.0-entropy_gapsincluded/maxentropy_gapsincluded;
	}

	// calculate randomized information table
        // This codes needs to be checked properly - SS
	double gapsrandomized_probability=0;
	double entropy_gapsrandomized=0;
	double maxentropy_gapsrandomized=0;
	int[] randomized_frequencies;

	information_gapsrandomized = new double [alignment.maxLength()];
	randomized_frequencies = new int [AminoAcid.NUM_AA-1];

	int random_index;

 	for (int i = freq_table.length - 1; i >= 0; i--)
        {
	  System.arraycopy(freq_table[i], 0,randomized_frequencies,0,AminoAcid.NUM_AA-1);

          //For every GAP increment the count of a randomly chosen Amino Acid
 	  for (int j = freq_table[i][AminoAcid.NUM_AA-1]-1; j >=0; j--)
          {
 	    random_index=randomInt(0,AminoAcid.NUM_AA-2);
 	    randomized_frequencies[random_index]++;
 	  }

	  entropy_gapsrandomized=0;
 	  for (int k = randomized_frequencies.length - 1; k >= 0; k--)
          {
 	    if ( (randomized_frequencies[k] > 0) && (num_residues[i] > 0) )
            {
 	      gapsrandomized_probability = (double)randomized_frequencies[k] / (double)(num_residues[i]); // Since we have replaced every GAP by a random AA there should still be a total of num_residues[i] AA in col i
 	      entropy_gapsrandomized-=gapsrandomized_probability*log2(gapsrandomized_probability);
 	    }
	  }

 	information_gapsrandomized[i] = 1.0-entropy_gapsrandomized/maxentropy_gapsexcluded;
        }
  }

    ConsensusSequence join(ConsensusSequence cs3, ConsensusSequence cs2) {
	ConsensusSequence csj;
	Sequence[] seqs3 = cs3.getSeqs();
	Sequence[] seqs2 = cs2.getSeqs();
	Sequence[] seqsjoint = new Sequence[seqs3.length + seqs2.length];
	for (int i = 0; i < seqs3.length; i++)
	    seqsjoint[i] = seqs3[i];
	for (int i = 0; i < seqs2.length; i++)
	    seqsjoint[i+seqs3.length] = seqs2[i];
	csj = new ConsensusSequence(alignment, seqsjoint);
	return csj;
    }
}
