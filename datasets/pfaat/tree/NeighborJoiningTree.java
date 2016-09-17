package com.neogenesis.pfaat.tree;


import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.seqspace.*;
import com.neogenesis.pfaat.print.*;


/**
 * Computes a tree by neighbor-joining
 *
 * @author $Author: xih $
 * @version $Revision: 1.13 $, $Date: 2002/10/11 18:31:50 $ */
public class NeighborJoiningTree {
    private Node rootoriginal;
    private Node root;
    private double[]    columnWeight; // weight for each column
    private String NHXString;

    private NJOptions options;
    private Sequence[]  seqs;       // input aligned sequences

    // the constructor just takes options...it does no processing
    public NeighborJoiningTree(Sequence[] seqs, NJOptions options) {
        // just save this
        this.seqs = seqs;
        this.options = options;
    }

    public NJOptions getOptions() {
        return options;
    }

    public Sequence[] getSeqs() {
        return seqs;
    }

    // call this to do the work
    public void calcNJTree(ProgressDialog progress) throws Exception {
        if (seqs.length < 1)
            throw new Exception("At least one sequence required for tree analysis.");

        for (int i = 0; i < seqs.length; i++) {
            String name = seqs[i].getName();

            if (name.indexOf(' ') != -1
                || name.indexOf(':') != -1
                || name.indexOf(';') != -1
                || name.indexOf('(') != -1
                || name.indexOf(')') != -1
                || name.indexOf('[') != -1
                || name.indexOf(']') != -1)
                throw new Exception("Invalid name for sequence " + name
                        + ": name cannot contain spaces, :, ;, (, ), [, or ]");
        }

        // save input
        this.options = options;

        // calculate the column weights
        calcWeights(seqs);

        // adjust weights according to the gap cutoff
        adjustWeightsByGapCutoff(seqs);

        // set weight to 0 for columns to be ignored. Do this after calcWeights and adjustWeights!
        excludeColumns();

        // COPY the orig weights
        double[] originalWeights = new double[columnWeight.length];

        for (int i = 0; i < originalWeights.length; i++)
            originalWeights[i] = columnWeight[i];

        // Get Distances
        double[][] distances = new double[seqs.length][seqs.length];
        Node[] nodesoriginal = new Node[seqs.length];
        Node[] nodesboot = new Node[seqs.length];

        distances = calcDistMatrix(seqs);
        nodesoriginal = calcTree(seqs, distances);
        rootoriginal = root;
        NHXString = rootoriginal.toString();

        // if we are not doing bootstrapping...then we are done
        if (!options.bootStrapping)
            return;

        if (progress != null)
            progress.setProgress("Bootstrapping...", 0, options.numBootStrapIterations);

        // Bootstrap
        short numboot = (short) options.numBootStrapIterations;
        double nodeVotes[] = new double[seqs.length];

        for (int i = 0; i < seqs.length; i++) nodeVotes[i] = 0.0;

        for (int boot = 0; boot < numboot; boot++) {
            for (int i = 0; i < columnWeight.length; i++)
                columnWeight[i] = 1.0;
            for (int i = 0; i < columnWeight.length; i++)
                columnWeight[(int) Math.floor(Math.random() * columnWeight.length)]++;
            for (int i = 0; i < columnWeight.length; i++) {
                columnWeight[i] *= originalWeights[i];
                // columnWeight[i] = originalWeights[i]*Math.random();
                // System.out.println(columnWeight[i]);
            }
            distances = calcDistMatrix(seqs);
            nodesboot = calcTree(seqs, distances);
            for (int i = 0; i < seqs.length; i++)
                if (nodesoriginal[i] != null)
                    for (int j = 0; j < seqs.length; j++) {
                        if (nodesoriginal[i].equals(nodesboot[j])) nodeVotes[i]++;
                    }

            // System.out.println("Completed Boostrap loop number " + boot);
            if (progress != null)
                progress.setProgress("Bootstrapping...", boot,
                    options.numBootStrapIterations);

            // canceled?
            if (progress != null && progress.cancelRequested()) {
                NHXString = null;
                return;
            }

        }

        for (int i = 0; i < seqs.length; i++) {
            nodeVotes[i] = nodeVotes[i] / (double) numboot * 100.0;
            // System.out.println("Node " + i + " Bootstrap = " + nodeVotes[i] + "%");
        }

        StringBuffer newNHXString = new StringBuffer(NHXString);

        for (int i = 0; i < seqs.length; i++) {
            if (i == 0 || i == 1)
                continue;
            String findString = nodesoriginal[i].toString();
            int insertposition = newNHXString.toString().indexOf(findString);
            int offset = findString.lastIndexOf(':');

            insertposition += offset;
            int insertVal = (int) Math.round(nodeVotes[i]);

            newNHXString.insert(insertposition, insertVal);
        }
        NHXString = newNHXString.toString();
    }

    // calculate Tree
    private Node[] calcTree(Sequence[] seqs, double[][] distances) {

        Node[] nodes = new Node[seqs.length];
        Node[] nodeskeep = new Node[seqs.length];

        for (int i = seqs.length - 1; i >= 0; i--)
            nodes[i] = new Node(seqs[i], i);

        double[] uniqueness = new double[seqs.length];
        int cluster_count = nodes.length;

        while (cluster_count > 2) {
            // calculate uniqueness
            for (int i = nodes.length - 1; i >= 0; i--) {
                if (nodes[i] == null) continue;
                double sum = 0.0;

                for (int j = nodes.length - 1; j >= 0; j--) {
                    if (nodes[j] == null || i == j) continue;
                    sum += distances[i][j];
                }
                uniqueness[i] = sum / ((double) cluster_count - 2);
            }

            // figure out which 2 to merge
            int min_i = -1;
            int min_j = -1;
            double min_dist = Double.MAX_VALUE;

            for (int i = nodes.length - 1; i >= 1; i--) {
                if (nodes[i] == null) continue;
                for (int j = i - 1; j >= 0; j--) {
                    if (nodes[j] == null) continue;
                    double dist = distances[i][j]
                        - uniqueness[i] - uniqueness[j];

                    if (dist < min_dist) {
                        min_dist = dist;
                        min_i = i;
                        min_j = j;
                    }
                }
            }

            // merge
            Node new_node = new Node(nodes[min_j], nodes[min_i]);

            nodeskeep[cluster_count - 1] = new_node;
            double branch1 = 0.5 * (distances[min_i][min_j] + uniqueness[min_j] - uniqueness[min_i]);
            double branch2 = 0.5 * (distances[min_i][min_j] + uniqueness[min_i] - uniqueness[min_j]);

            // Take partial care of negative branch length quirk in NJ if problem is small enough
            if (branch1 < 0 || branch2 < 0) {
                branch1 = Math.abs(branch1);
                branch2 = Math.abs(branch2);
                branch1 = distances[min_i][min_j] * branch1 / (branch1 + branch2);
                branch2 = distances[min_i][min_j] - branch1;
            }
            nodes[min_j].setParent(new_node, branch1);
            nodes[min_i].setParent(new_node, branch2);
            nodes[min_j] = nodes[min_i] = null;

            // adjust distances
            double d = distances[min_i][min_j];

            for (int k = nodes.length - 1; k >= 0; k--) {
                if (nodes[k] == null) continue;
                distances[min_j][k] = distances[k][min_j] =
                                0.5 * (distances[min_i][k] + distances[min_j][k] - d);
            }
            nodes[min_j] = new_node;
            cluster_count--;
        }

        // if 2 are left, merge them
        if (cluster_count == 2) {
            int other = -1;

            for (int i = nodes.length - 1; i >= 0; i--) {
                if (nodes[i] != null) {
                    other = i;
                    break;
                }
            }
            Node new_node = new Node(nodes[0], nodes[other]);

            nodeskeep[cluster_count - 1] = new_node;
            double d = 0.5 * distances[0][other];

            nodes[0].setParent(new_node, d);
            nodes[other].setParent(new_node, d);
            root = new_node;
        } else if (cluster_count == 1) {
            // only one left
            root = nodes[0];
            nodeskeep[cluster_count - 1] = nodes[0];
        }

        return nodeskeep;
    }

    // calculate Distance Matrix
    private double[][] calcDistMatrix(Sequence[] seqs) {

        double[][] distances = new double[seqs.length][seqs.length];

        if (options.method != options.PercentageIdentity) {
            SeqSpaceEncoder encoder = new Blosum62Encoder();

            if (options.method == options.Blosum50) encoder = new Blosum50Encoder();
            // if (options.method == options.PhysicoChemical) encoder = new AAPhysicoChemicalEncoder();
            double[] seq_score = new double[seqs.length];

            for (int i = seqs.length - 1; i >= 0; i--) {
                distances[i][i] = 0.0;
                seq_score[i] = seqs[i].getScore(encoder, seqs[i], columnWeight);
            }
            for (int i = seqs.length - 1; i >= 1; i--) {
                for (int j = i - 1; j >= 0; j--) {
                    double s = seq_score[i] + seq_score[j]
                        - 2.0 * seqs[i].getScore(encoder, seqs[j], columnWeight);

                    distances[i][j] = distances[j][i] = s;
                }
            }
        } else {
            for (int i = seqs.length - 1; i >= 0; i--) {
                for (int j = i; j >= 0; j--)
                    distances[i][j] = distances[j][i] =
                                    100.0 * (seqs[i].getPID(seqs[i], columnWeight) - seqs[i].getPID(seqs[j], columnWeight));
            }
        }

        return distances;
    }

    // calculate column weights
    private void calcWeights(Sequence[] seqs) throws Exception {
        int seqlen;

        seqlen = seqs[0].length();
        columnWeight = new double[seqlen];

        // if we are not using weights...then just set all weights to 1.0
        if (!options.useWeightedColumns) {
            for (int i = 0; i < seqlen; i++)
                columnWeight[i] = 1.0;
            return;
        }

        double[] p = new double[AminoAcid.NUM_AA];
        int aaindex;
        double log20 = Math.log(20);

        for (int i = 0; i < seqlen; i++) {
            int count = 0;

            for (int l = 0; l < AminoAcid.NUM_TRUE_AA; l++) {
                p[l] = 0.0;
            }
            for (int k = 0; k < seqs.length; k++) {
                aaindex = seqs[k].getAA(i).getIndex();
                if (aaindex < AminoAcid.NUM_TRUE_AA) {
                    p[aaindex]++; // Exclude Gaps. Only count 20 amino acids.
                    count++;
                }
            }
            for (int l = 0; l < AminoAcid.NUM_TRUE_AA; l++) {
                if (count != 0)
                    p[l] /= count;
                if (count != 0 && p[l] != 0.0)
                    columnWeight[i] -= p[l] * Math.log(p[l]) / log20;
            }
            columnWeight[i] = 1.0 - columnWeight[i];
            if (columnWeight[i] > 1.0 || columnWeight[i] <= 0.0)
                throw new Exception("Error in the Column Weights");
        }
    }

    // adjust weights according to the gap cutoff
    private void adjustWeightsByGapCutoff(Sequence[] seqs) {
        int seqlen;

        seqlen = seqs[0].length();
        int numseqs = seqs.length;

        // if we are not skipping gappy columns...nothing to do!
        if (!options.skipGappyColumns)
            return;

        for (int i = 0; i < seqlen; i++) {
            int count = 0;

            for (int k = 0; k < seqs.length; k++) {
                if (seqs[k].getAA(i).getIndex() == AminoAcid.GAP.getIndex())
                    count++;
            }
            // express this as a percent
            float percent = (float) count * 100.0f / (float) numseqs;

            // if the percent is above the cutoff then
            // SKIP this column by setting it's weight to zero
            if (percent >= options.gapCutoffPercent)
                columnWeight[i] = 0.0;
        }
    }

    public void excludeColumns() {
        for (int i = 0; i < columnWeight.length; i++) {
            if (!options.includedColumns[i])
                columnWeight[i] = 0.0;
        }

    }

    public String toString() {
        return NHXString;
        // return rootoriginal.toString();
    }

    private static class Node {
        private Sequence sequence;
        private double length;
        private Node parent, child_a, child_b;
        public short[] seqlist;

        public Node(Sequence sequence, int seqid) {
            this.sequence = sequence;
            this.seqlist = new short[1];
            seqlist[0] = (short) seqid;
        }

        public Node(Node child_a, Node child_b) {
            this.child_a = child_a;
            this.child_b = child_b;
            mergeSeqlist();
        }

        private void mergeSeqlist() {
            int dima = child_a.seqlist.length;
            int dimb = child_b.seqlist.length;
            int dim = dima + dimb;
            short seqid_a;
            short seqid_b;

            this.seqlist = new short[dim];
            short count1 = 0;
            short count2 = 0;

            for (short i = 0; i < dim; i++) {
                if (count1 < dima)
                    seqid_a = child_a.seqlist[count1];
                else
                    seqid_a = Short.MAX_VALUE;
                if (count2 < dimb)
                    seqid_b = child_b.seqlist[count2];
                else
                    seqid_b = Short.MAX_VALUE;
                if (seqid_a < seqid_b) {
                    this.seqlist[i] = child_a.seqlist[count1];
                    count1++;
                } else {
                    this.seqlist[i] = child_b.seqlist[count2];
                    count2++;
                }
            }
        }

        public void setParent(Node parent, double length) {
            // ////
            // BETE
            // ////
            // if its a very small negative (or positive) number
            // then force it to be zero
            if (Math.abs(length) < 1E-10)
                length = 0.0;
            // ////
            // BETE
            // ////

            this.parent = parent;
            this.length = length;
            if (length < 0.0 || Double.isNaN(length))
                throw new RuntimeException("invalid branch length");
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();

            if (sequence != null)
                sb.append(sequence.getName());
            else {
                sb.append("(");
                sb.append(child_a.toString());
                sb.append(",");
                sb.append(child_b.toString());
                sb.append(")");
            }
            if (parent != null) {
                sb.append(":");
                sb.append(length);
            }
            return sb.toString();
        }

        public boolean equals(Node node) {
            if (node == null) return false;
            short listsize1 = (short) this.seqlist.length;
            short listsize2 = (short) node.seqlist.length;

            if (listsize1 != listsize2) return false;
            else {
                for (int i = 0; i < listsize1; i++)
                    if (this.seqlist[i] != node.seqlist[i]) return false;
                    else return true;
            }
            return false;
        }
    }

}
