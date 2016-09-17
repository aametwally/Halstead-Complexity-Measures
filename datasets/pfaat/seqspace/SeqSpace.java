package com.neogenesis.pfaat.seqspace;


import java.io.*;
import java.util.*;

import Jama.*;
import org.apache.oro.text.perl.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.*;


/**
 * SeqSpace alignment analysis.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:38 $ */
public class SeqSpace {
    private Sequence[] seqs;
    private int n_dims, max_length;
    private SeqSpaceEncoder encoder;
    private SymmetricEigenvalueDecomposition sed;

    private Dimension[] dimensions;
    private SequenceCoordinates[] seq_coords;
    private ResidueCoordinates[] res_coords;
    private double total_variance;
    private static final double THRESHOLD = 1e-10;

    public static class Dimension {
        private String name;
        private double variance;

        public Dimension(String name, double variance) {
            this.name = name;
            this.variance = variance;
        }

        public Dimension(Dimension dim) {
            this.name = dim.name;
            this.variance = dim.variance;
        }

        public String getName() {
            return (name);
        }

        public double getVariance() {
            return (variance);
        }
    }


    public static class Coordinates {
        private double[] coords;

        public Coordinates(double[] coords) {
            this.coords = coords;
        }

        public Coordinates(Coordinates c) {
            this.coords = new double[c.coords.length];
            for (int i = c.coords.length; --i >= 0;) {
                this.coords[i] = c.coords[i];
            }
        }

        public double[] getCoordinates() {
            return coords;
        }

        public void addCoord(double value) {
            double[] new_coords = new double[coords.length + 1];

            System.arraycopy(coords, 0, new_coords, 0, coords.length);
            new_coords[new_coords.length - 1] = value;
            coords = new_coords;
        }
    }


    public static class SequenceCoordinates extends Coordinates {
        private String seq_name;

        public SequenceCoordinates(String seq_name, double[] coords) {
            super(coords);

            this.seq_name = seq_name;
        }

        public SequenceCoordinates(SequenceCoordinates sc) {
            super((Coordinates) sc);

            this.seq_name = sc.seq_name;
        }

        public String getSequenceName() {
            return seq_name;
        }
    }


    public static class ResidueCoordinates extends Coordinates {
        private int offset;
        private AminoAcid aa;

        public ResidueCoordinates(int offset, AminoAcid aa, double[] coords) {
            super(coords);

            this.offset = offset;
            this.aa = aa;
        }

        public ResidueCoordinates(ResidueCoordinates rc) {
            super((Coordinates) rc);

            this.offset = rc.offset;
            this.aa = rc.aa;
        }

        public String getName() {
            return (offset + 1) + "-" + aa.getCode3();
        }

        public int getIndex() {
            return offset;
        }

        public AminoAcid getAA() {
            return aa;
        }
    }

    public SeqSpace(Sequence[] seqs, int n_dims, boolean blosum62) {
        this.seqs = seqs;
        this.n_dims = n_dims;
        if (blosum62)
            this.encoder = new Blosum62Encoder();
        else
            this.encoder = new BinaryEncoder();
        this.sed = new SymmetricEigenvalueDecomposition(seqs.length);
        max_length = 0;
        for (int k = seqs.length - 1; k >= 0; k--) {
            int l = seqs[k].length();

            if (l > max_length)
                max_length = l;
        }
    }

    public SeqSpace(SeqSpace ss) {
        this(ss.seqs, ss.n_dims, (ss.encoder instanceof Blosum62Encoder));

        this.dimensions = new Dimension[ss.dimensions.length];
        for (int i = ss.dimensions.length; --i >= 0;) {
            this.dimensions[i] = new Dimension(ss.dimensions[i]);
        }
        this.seq_coords = new SequenceCoordinates[ss.seq_coords.length];
        for (int i = ss.seq_coords.length; --i >= 0;) {
            this.seq_coords[i] = new SequenceCoordinates(ss.seq_coords[i]);
        }
        this.res_coords = new ResidueCoordinates[ss.res_coords.length];
        for (int i = ss.res_coords.length; --i >= 0;) {
            this.res_coords[i] = new ResidueCoordinates(ss.res_coords[i]);
        }
        this.total_variance = ss.total_variance;
    }

    public int getNumSequences() {
        return (seqs.length);
    }

    public int getNumResidues() {
        return (max_length);
    }

    public int getNumDimensions() {
        return (n_dims);
    }

    public Dimension[] getDimensions() {
        return (dimensions);
    }

    public double getVariance(int dim) {
        return (dimensions[dim].getVariance());
    }

    public double getTotalVariance() {
        return (total_variance);
    }

    public SequenceCoordinates[] getSeqCoords() {
        return (seq_coords);
    }

    public ResidueCoordinates[] getResCoords() {
        return (res_coords);
    }

    public Sequence getSequence(int i) {
        return (seqs[i]);
    }

    public int getTotalIterations() {
        return seqs.length + sed.getTotalIterations() + 1 + max_length;
    }

    public void compute(IterationListener il) throws Exception {
        // construct the comparision matrix
        Matrix comparison = new Matrix(seqs.length, seqs.length);
        double[][] C = comparison.getArray();

        for (int i = seqs.length - 1; i >= 0; i--) {
            for (int j = i; j >= 0; j--) {
                double score = seqs[i].getScore(encoder, seqs[j]);

                C[i][j] = score;
                C[j][i] = score;
            }
            if (il != null) il.finishedThisIteration();
        }
		
        // compute the eigenvalues
        sed.compute(comparison, il);

        // calculate the protein coordinates
        double[] variance = sed.getEigenvalues();

        total_variance = 0.0;
        for (int i = variance.length - 1; i >= 0; i--) {
            if (variance[i] < -THRESHOLD)
                throw new Exception("negative eigenvalues in "
                        + "comparison matrix");
            else if (variance[i] > 0.0)
                total_variance += variance[i];
            else
                variance[i] = 0.0;
        }

        ArrayList dimensionList = new ArrayList();
        int cnt = 0;

        for (int i = variance.length - 1; cnt < n_dims; i--, cnt++) {
            dimensionList.add(new Dimension("Dimension " + (cnt + 1), variance[i]));
        }
        dimensions = (Dimension[]) dimensionList.toArray(new Dimension[] {}
                );

        Matrix eigenvectors = sed.getV();
        double[][] V = eigenvectors.getArray();
        double[][] coords = new double[seqs.length][n_dims];

        cnt = 0;
        for (int p = variance.length - 1; cnt < n_dims; p--, cnt++) {
            double sqlamdba = Math.sqrt(variance[p]);

            for (int k = seqs.length - 1; k >= 0; k--) 
                coords[k][cnt] = V[k][p] * sqlamdba;
        }
        seq_coords = new SequenceCoordinates[seqs.length];
        for (int k = seqs.length - 1; k >= 0; k--) 
            seq_coords[k] = new SequenceCoordinates(seqs[k].getName(),
                        coords[k]);
        if (il != null) il.finishedThisIteration();

        // calculate residue coordinates
        Set residues = new HashSet(43);
        List res_coords_list = new ArrayList();
        double[] across_all = new double[seqs.length];
        double[][] ev_by_row = eigenvectors.transpose().getArray();

        for (int r = 0; r < max_length; r++) {
            for (int k = seqs.length - 1; k >= 0; k--) {
                if (seqs[k].length() > r)
                    residues.add(seqs[k].getAA(r));
            }
	    
            if (residues.size() > 0) {
                for (Iterator i = residues.iterator(); i.hasNext();) {
                    AminoAcid aa = (AminoAcid) i.next();
                    double[] coord = new double[n_dims];

                    for (int k = seqs.length - 1; k >= 0; k--) 
                        across_all[k] = r < seqs[k].length() 
                                ? encoder.getPairScore(aa, seqs[k].getAA(r))
                                : encoder.getPairScore(aa, AminoAcid.GAP);
                    cnt = 0;
                    for (int p = variance.length - 1; cnt < n_dims; p--, cnt++)
                        coord[cnt] = dot(across_all, ev_by_row[p]);
		    
                    res_coords_list.add(new ResidueCoordinates(r,
                            aa,
                            coord));
                }
                residues.clear();
            }
	    
            if (il != null) il.finishedThisIteration();
	    
        }

        res_coords = new ResidueCoordinates[res_coords_list.size()];
        res_coords = 
                (ResidueCoordinates[]) res_coords_list.toArray(res_coords);
    }

    // dot product of two vectors
    private static double dot(double[] a, double[] b) {
        double dot = 0.0;

        for (int i = a.length - 1; i >= 0; i--) 
            dot += a[i] * b[i];
        return dot;
    }

    // save as a text file
    public void save(File f) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
	
        out.print("Dimensions " + getTotalVariance());
        for (int i = 0; i < dimensions.length; i++) {
            out.print(", " + dimensions[i].getName() + " (" + dimensions[i].getVariance() + ")");
        }
        out.println("");

        for (int i = 0; i < seq_coords.length; i++) {
            out.print("Sequence, "
                + seq_coords[i].getSequenceName());
            double[] v = seq_coords[i].getCoordinates();

            for (int j = 0; j < v.length; j++)
                out.print(", " + v[j]);
            out.println("");
        }
	
        for (int i = 0; i < res_coords.length; i++) {
            out.print("Residue, "
                + res_coords[i].getName());
            double[] v = res_coords[i].getCoordinates();

            for (int j = 0; j < v.length; j++)
                out.print(", " + v[j]);
            out.println("");
        }
        out.close();
    }

    public void writeTemplate(File f) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
	
        out.println("Dimensions, <name> [, <name> [, <name> [...]]]");

        for (int i = 0; i < seq_coords.length; i++) {
            out.println("Sequence, " + seq_coords[i].getSequenceName() + ", <value> [, <value> [, <value> [...]]]");
        }

        out.close();
    }

    public void loadData(File f) throws Exception {
        Perl5Util perl = new Perl5Util();

        HashMap seqMap = new HashMap();

        for (int i = seq_coords.length; --i >= 0;) {
            seqMap.put(seq_coords[i].getSequenceName(), seq_coords[i]);
        }

        ArrayList udList = new ArrayList();

        BufferedReader in = new BufferedReader(new FileReader(f));
        String line = null;

        while ((line = in.readLine()) != null) {
            if (perl.match("/^Dimensions(.*)$/", line)) {
                String valueLine = perl.group(1).trim();

                while (valueLine.length() > 0) {
                    if (perl.match("/^,([^,]*)(.*)$/", valueLine)) {
                        udList.add(new Dimension(perl.group(1).trim(), 0.0));
                        valueLine = perl.group(2).trim();
                    } else if (valueLine.length() > 0) {
                        throw new Exception("User Dimension file-format error");
                    }
                }
                Dimension[] new_dimensions = new Dimension[dimensions.length + udList.size()];

                System.arraycopy(dimensions, 0, new_dimensions, 0, dimensions.length);
                System.arraycopy((Dimension[]) udList.toArray(new Dimension[] {}
                    ), 0, new_dimensions, dimensions.length, udList.size());
                dimensions = new_dimensions;
            } else if (perl.match("/^Sequence[^,]*,([^,]*)(.*)$/", line)) {
                SequenceCoordinates sc = (SequenceCoordinates) seqMap.get(perl.group(1).trim());
                String valueLine = perl.group(2).trim();

                for (int i = 0; i < udList.size(); i++) {
                    if (perl.match("/^,([^,]*)(.*)$/", valueLine)) {
                        sc.addCoord(Double.parseDouble(perl.group(1).trim()));
                        valueLine = perl.group(2).trim();
                    } else {
                        throw new Exception("User Dimension file-format error");
                    }
                }
                if (valueLine.length() > 0) {
                    throw new Exception("User Dimension file-format error");
                }
            } else {
                throw new Exception("User Dimension file-format error");
            }
        }
        in.close();

        int udListLength = udList.size();

        for (int i = 0; i < udListLength; i++) {
            for (int j = res_coords.length; --j >= 0;) {
                ResidueCoordinates rc = res_coords[j];
                int index = rc.getIndex();
                AminoAcid aa = rc.getAA();
                int rcCoordCum = 0;
                int rcCoordNumSeqs = 0;

                for (int k = seq_coords.length; --k >= 0;) {
                    if (seqs[k].getAA(index).equals(aa)) {
                        double[] seqCoords = seq_coords[k].getCoordinates();

                        rcCoordCum += seqCoords[seqCoords.length - udListLength + i];
                        rcCoordNumSeqs++;
                    }
                }
                if (rcCoordNumSeqs > 0) {
                    rc.addCoord(rcCoordCum / rcCoordNumSeqs);
                } else {
                    rc.addCoord(0.0);
                }
            }
        }
	
        /*
         for (int i = 0; i < udList.size(); i++) {
         for (int j = res_coords.length; --j >= 0; ) {
         res_coords[j].addCoord(0.0);
         }
         }
         */
    }
}

