package com.neogenesis.pfaat;


import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.colorscheme.ColorScheme;
import com.neogenesis.pfaat.seqspace.*;

import org.apache.oro.text.perl.*;
import com.neogenesis.pfaat.*;


/**
 * An aligned amino acid sequence.
 *
 * @author $Author: xih $
 * @version $Revision: 1.9 $, $Date: 2002/10/11 18:28:02 $ */
public class Sequence {
    // sequence of amino acids
    private AminoAcid[] aligned_sequence;
    // sequence without gaps
    private AminoAcid[] raw_sequence;
    // offsets of aligned <-> raw
    private int[] raw2aligned, aligned2raw;
    // group memberships (e.g., amine)
    private HashSet groups;
    // sequence start index
    private int startindex;
    // name
    private String name;
    // sequence annotation
    private String annotation;
    // a list of line annotations
    private List line_annotations = new ArrayList();
    // a list of column annotations
    private List column_annotations = new ArrayList();
    // list of listeners
    private List listeners = new ArrayList();
    // an associated color
    private Color color;
    // an associated pdb file
    private File pdb_file;

    private static Perl5Util perl = new Perl5Util();
    // class for annotation of a line
    public static class LineAnnotation {
        private String name;
        private String annotation;

        public LineAnnotation(String name, String annotation) {
            this.name = name;
            this.annotation = annotation;
        }

        public String getName() {
            return name;
        }

        public String getAnnotation() {
            return annotation;
        }
    }


    // class for annotation of a residue
    public static class ColumnAnnotation {
        private int column;
        private String symbol;
        private Color color;
        private String annotation;

        public ColumnAnnotation(int column,
            String symbol,
            Color color,
            String annotation) {
            this.column = column;
            this.symbol = symbol;
            this.color = color;
            this.annotation = annotation;
        }

        public int getColumn() {
            return column;
        }

        public String getSymbol() {
            return symbol;
        }

        public Color getColor() {
            return color;
        }

        public String getAnnotation() {
            return annotation;
        }

        public boolean equals(Object other) {
            if (other instanceof ColumnAnnotation) {
                ColumnAnnotation o = (ColumnAnnotation) other;

                return ((symbol.equals(o.symbol)) &&
                        (color.equals(o.color)) &&
                        (annotation.equals(o.annotation)));
            }
            return false;
        }
    }

    /**
     * Constructors
     */

    public Sequence(String name, AminoAcid[] sequence) {
        initialize(name, sequence, null);
    }

    public Sequence(String name, AminoAcid[] sequence, HashSet groups) {
        initialize(name, sequence, groups);
    }

    protected Sequence() {}

    protected void initialize(String name, AminoAcid[] sequence) {
        initialize(name, sequence, null);
    }

    protected void initialize(String name, AminoAcid[] sequence, HashSet groups) {
        this.name = name;
        if (groups != null)
            this.groups = groups;
        else this.groups = new HashSet();
        annotation = null;
        this.startindex = 1;
        setAAInternal(sequence);
    }

    /**
     * Accessors
     */

    // length of the sequence
    public int length() {
        return aligned_sequence.length;
    }

    // amino acid at a given location
    public AminoAcid getAA(int idx) {
        return aligned_sequence[idx];
    }

    // all amino acids
    public AminoAcid[] getAllAA() {
        AminoAcid[] aa = new AminoAcid[aligned_sequence.length];

        System.arraycopy(aligned_sequence, 0, aa, 0, aligned_sequence.length);
        return aa;
    }

    // raw amino acids
    public AminoAcid[] getRawAA() {
        AminoAcid[] aa = new AminoAcid[raw_sequence.length];

        System.arraycopy(raw_sequence, 0, aa, 0, raw_sequence.length);
        return aa;
    }

    // get as a string
    public String toString() {
        return AminoAcid.aaToString(aligned_sequence);
    }

    /**
     * returns the sequence in fasta format in unaligned form
     * Added by Giles Day 11/02/2002
     * @param com.neogenesis.pfaat.Sequence
     * @return String
     */
    public static String toRawFasta(com.neogenesis.pfaat.Sequence seq) {
        // System.out.println("Sequence.toFasta")
        String s = seq.toRawString();
        int len = s.length();
        int width = 60;
        int pos1 = 0;
        int pos2 = pos1 + width;
        StringBuffer sb = new StringBuffer(">" + seq.name + "\n");

        while (pos2 < len) {
            sb.append(s.substring(pos1, pos2) + "\n");
            pos1 = pos2;
            pos2 += 60;
        }
        sb.append(s.substring(pos1, len));

        return sb.toString();
    }

    /**
     * returns the sequence in fasta format in unaligned form
     * Added by Giles Day 11/02/2002
     * @param String
     * @param com.neogenesis.pfaat.Sequence
     * @return String
     */
    public static String toRawFasta(String id, String s) throws Exception {

        int len = s.length();
        int width = 60;
        int pos1 = 0;
        int pos2 = pos1 + width;
        StringBuffer sb = new StringBuffer(">" + id + "\n");

        while (pos2 < len) {
            sb.append(s.substring(pos1, pos2) + "\n");
            pos1 = pos2;
            pos2 += 60;
        }
        sb.append(s.substring(pos1, len));

        return sb.toString();
    }

    /**
     * returns the sequence in fasta format in aligned form
     * Added by Giles Day 11/02/2002
     * @param com.neogenesis.pfaat.Sequence
     * @return String
     */
    public static String toFasta(com.neogenesis.pfaat.Sequence seq) throws Exception {
        // System.out.println("Sequence.toFasta")
        String s = seq.toString();
        int len = s.length();
        int width = 60;
        int pos1 = 0;
        int pos2 = pos1 + width;
        StringBuffer sb = new StringBuffer(">" + seq.name + "\n");

        while (pos2 < len) {
            sb.append(s.substring(pos1, pos2) + "\n");
            pos1 = pos2;
            pos2 += 60;
        }
        sb.append(s.substring(pos1, len));

        return sb.toString();
    }

    /**
     * returns the sequence in fasta format in aligned form
     * Added by Giles Day 11/02/2002
     * @param id
     * @param sequence
     * @return fasta
     */
    public static String toFasta(String id, String s) throws Exception {
        int len = s.length();
        int width = 60;
        int pos1 = 0;
        int pos2 = pos1 + width;
        StringBuffer sb = new StringBuffer(">" + id + "\n");

        while (pos2 < len) {
            sb.append(s.substring(pos1, pos2) + "\n");
            pos1 = pos2;
            pos2 += 60;
        }
        sb.append(s.substring(pos1, len));

        return sb.toString();
    }

    // get raw sequence as a string
    public String toRawString() {
        return AminoAcid.aaToString(raw_sequence);
    }

    // name
    public String getName() {
        return name;
    }

    // get the annotation
    public String getAnnotation() {
        return annotation;
    }

    // get the line annotations
    public LineAnnotation[] getLineAnnotations() {
        LineAnnotation[] la = new LineAnnotation[line_annotations.size()];

        la = (LineAnnotation[]) line_annotations.toArray(la);
        return la;
    }

    // get one line annotation
    public LineAnnotation getLineAnnotation(int idx) {
        return (LineAnnotation) line_annotations.get(idx);
    }

    // get the number of line annotation
    public int getLineAnnotationsCount() {
        return line_annotations.size();
    }

    // get the column annotations
    public ColumnAnnotation[] getColumnAnnotations() {
        ColumnAnnotation[] ca =
            new ColumnAnnotation[column_annotations.size()];

        ca = (ColumnAnnotation[]) column_annotations.toArray(ca);
        return ca;
    }

    // get the number of column annotations
    public int getColumnAnnotationsCount() {
        return column_annotations.size();
    }

    // get the column annotation for a specific column
    public ColumnAnnotation getColumnAnnotation(int column) {
        // binary search for position
        int idx = findColumnAnnotationIndex(column);

        return idx >= 0
            ? (ColumnAnnotation) column_annotations.get(idx)
            : null;
    }

    public int getRawLength() {
        return raw_sequence.length;
    }

    public AminoAcid getRawAA(int idx) {
        return raw_sequence[idx];
    }

    // get the aligned index of a raw index
    public int getStartIndex() {
        return startindex;
    }

    public int getAlignedIndex(int raw_idx) {
        return raw2aligned[raw_idx];
    }

    public int getRawIndex(int aligned_idx) {
        return (aligned2raw[aligned_idx] == -1 ? -1 :
                aligned2raw[aligned_idx] + this.getStartIndex());
    }

    // make sure a shift would only delete gaps
    public boolean gapsShiftedOnly(int idx, int shift) {
        if (shift >= 0)
            return true;
        if (idx + shift < 0
            || idx < 0
            || idx >= aligned_sequence.length)
            return false;
        for (int i = idx + shift; i < idx; i++) {
            if (aligned2raw[i] != -1)
                return false;
        }
        return true;
    }

    // get the color
    public Color getColor() {
        return color;
    }

    // get the pdb file
    public File getPDBFile() {
        return pdb_file;
    }

    // generates line annotation from secondary structure
    // annotations in PDB file
    public String getLineAnnFromPDBFile() {
        if (pdb_file == null) return null;
        StringBuffer la = new StringBuffer();
        int idx;
        int start = 0;
        int end = 0;
        String molecule1 = new String();
        String molecule2 = new String();
        char annChar = 'X';

        for (int i = 0; i <= length(); i++)  la.append(' ');
        try {
            FileInputStream fis = new FileInputStream(pdb_file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            String line;

            // PDB examples

            // HELIX   38  38 LEU B  315  GLU B  317  5     3
            // 1   2   3 4    5    6 7    8  9    10

            // SHEET    1   A 4 ASP A  94  GLU A  99  0
            // 1   2 3   4 5   6    7 8   9 10

            // TURN     1 S1A GLY A  16  GLN A  18     SURFACE
            // 1   2   3 4   5    6 7   8           9


            while ((line = dis.readLine()) != null) {
                annChar = 'X';
                if (perl.match("/HELIX\\s+(\\d+)\\s+(\\d+)\\s(\\S+)\\s(\\S)\\s+(\\d+)\\s+(\\S+)\\s(\\S)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)/",
                        line)) {
                    annChar = 'H';
                    start = Integer.parseInt(perl.group(5));
                    end = Integer.parseInt(perl.group(8));

                    molecule1 = perl.group(4);
                    molecule2 = perl.group(7);
                } else if (perl.match("/SHEET\\s+(\\d+)\\s+(\\S)\\s(\\d+)\\s(\\S+)\\s(\\S)\\s+(\\d+)\\s+(\\S+)\\s(\\S)\\s+(\\d+)\\s+(\\d+)/",
                        line)) {
                    annChar = 'S';
                    start = Integer.parseInt(perl.group(6));
                    end = Integer.parseInt(perl.group(9));;

                    molecule1 = perl.group(5);
                    molecule2 = perl.group(8);
                } else if (perl.match("/TURN\\s+(\\d+)\\s(\\S+)\\s(\\S+)\\s(\\S)\\s+(\\d+)\\s+(\\S+)\\s(\\S)\\s+(\\d+)\\s+(\\S+)/",
                        line)) {
                    annChar = 'T';
                    start = Integer.parseInt(perl.group(6));
                    end = Integer.parseInt(perl.group(8));;
                    molecule1 = perl.group(4);
                    molecule2 = perl.group(7);
                }

                if (annChar != 'X') {
                    for (int i = start; i <= end; i++) {
                        idx = i >= 0 && i < raw2aligned.length ? getAlignedIndex(i) : -1;
                        if (idx > 0 && idx < la.length() &&
                            molecule1.charAt(0) == 'A' &&
                            molecule2.charAt(0) == 'A')
                            la.setCharAt(idx, annChar);
                    }
                }
            }
        } catch (IOException e) {// ErrorDialog.showErrorDialog(this, "Unable to insert amino acid.", e);
        }
        if (la.length() < length())
            for (int i = 0; i < length() - la.length(); i++)
                la.append(' ');
        return la.toString();
    }

    public static String getSequenceFromPDBFile(File f)
        throws IOException {
        StringBuffer aa = new StringBuffer();
        String molecule = new String();
        String code;
        String num;

        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);

        String line;

        // example
        // SEQRES   3 A  255  ALA PHE TYR GLU GLN GLY TYR ASP PRO ILE MET MET GLU
        // ATOM     30  CA  GLU A   5      47.438   3.458 -26.710  1.00 47.26           C
        while ((line = dis.readLine()) != null) {
            if (perl.match("/^ATOM.{9}CA.{2}(\\w{3}).[A|\\s]\\s*\\d+[A|\\s]/", line)) { // only look at chain A
                String residue = perl.group(1);

                aa.append((AminoAcid.lookupByCode3(residue)).getCode());
            }

            /*
             StringTokenizer st = new StringTokenizer(line);
             if(st.hasMoreTokens()) {

             code = st.nextToken();
             if (code.equals("SEQRES")) {
             num = st.nextToken();
             molecule = st.nextToken();
             num = st.nextToken();
             if (molecule.equals("A")) {
             while (st.hasMoreTokens()) {
             aa.append((AminoAcid.lookupByCode3(st.nextToken())).getCode());
             }
             }
             }*/
        }
        dis.close();
        return aa.toString();
    }

    // extracts aa sequence from PDB file
    public String getAlignmentFromPDBFile() {
        if (pdb_file == null) return null;

        String seq = null;

        try {
            seq = getSequenceFromPDBFile(pdb_file);
        } catch (IOException ex) {// ErrorDialog.showErrorDialog(this, "Unable to get sequence from alignment file", ex);
        }
        return seq;
    }

    // get the alignment to a raw sequence which is a strict subset
    public int getRawAlignment(AminoAcid[] o_aa) {
        int end = getRawLength() - o_aa.length;

        outer:
        for (int i = 0; i <= end; i++) {
            for (int k = o_aa.length - 1; k >= 0; k--) {
                AminoAcid s_aa = getRawAA(i + k);

                if (!s_aa.equals(o_aa[k]))
                    continue outer;
            }
            return i;
        }
        return -1;
    }

    // group sequence group names
    public String[] getGroupNames() {
        if (groups.isEmpty()) return null;
        String[] groupArray = new String[groups.size()];
        int m = 0;

        for (Iterator i = groups.iterator(); i.hasNext();)
            groupArray[m++] = i.next().toString();
        return groupArray;
    }

    public HashSet getGroups() {
        return groups;
    }

    public String getFormattedGroupString() {
        StringBuffer grouptxt = new StringBuffer("");
        String groups[] = getGroupNames();

        if (getGroupNames() != null) {
            for (int j = 0; j < groups.length; j++) {
                grouptxt.append(groups[j]);
                if (j != groups.length - 1) grouptxt.append(":");
            }
            return grouptxt.toString();
        } else return "";
    }

    public void addGroup(String newGroup) {
        groups.add(newGroup);
    }

    public void createGroups(HashSet groupSet) {
        groups = groupSet;
    }

    public void removeGroup(String removedGroup) {
        groups.remove(new String(removedGroup));
    }

    // get percentage identity vs. another sequence
    public double getPID(Sequence other) {
        Sequence min, max;

        if (other.length() > length()) {
            min = this;
            max = other;
        } else {
            min = other;
            max = this;
        }
        int match_count = 0;
        int mis_count = 0;
        int end = min.length() - 1;

        for (int i = end; i >= 0; i--) {
            AminoAcid min_aa = min.getAA(i);
            AminoAcid max_aa = max.getAA(i);

            if (min_aa.equals(max_aa)) {
                if (!min_aa.isGap())
                    match_count++;
            } else if (!(min_aa.isGap() && max_aa.isGap()))
                mis_count++;
        }
        for (int i = max.length() - 1; i > end; i--)
            if (!max.getAA(i).isGap())
                mis_count++;
        return ((double) match_count) / ((double) match_count + mis_count);
    }

    // get score vs. another sequence
    public double getScore(SeqSpaceEncoder encoder, Sequence other) {
        Sequence min, max;

        if (other.length() > length()) {
            min = this;
            max = other;
        } else {
            min = other;
            max = this;
        }
        double score = 0.0;
        int end = min.length() - 1;

        for (int i = end; i >= 0; i--)
            score += encoder.getPairScore(min.getAA(i), max.getAA(i));
        for (int i = max.length() - 1; i > end; i--)
            score += encoder.getPairScore(AminoAcid.GAP, max.getAA(i));
        return score;
    }

    // ///////////
    // BETE BEGIN
    // ///////////

    // NOTE: all changes added by phdana are marked with the above
    // BETE BEGIN notice even tho, stricly speaking, the
    // changes in this file are _not_ for bete trees
    // but for neighbor joining trees.

    // get percentage identity vs. another sequence (using column weights)
    public double getPID(Sequence other, double[] columnWeight) {
        Sequence min, max;

        if (other.length() > length()) {
            min = this;
            max = other;
        } else {
            min = other;
            max = this;
        }
        double match_count = 0.0;
        double mis_count = 0.0;
        int end = min.length() - 1;

        for (int i = end; i >= 0; i--) {
            AminoAcid min_aa = min.getAA(i);
            AminoAcid max_aa = max.getAA(i);

            if (min_aa.equals(max_aa)) {
                if (!min_aa.isGap())
                    match_count = match_count + columnWeight[i];
            } else if (!(min_aa.isGap() && max_aa.isGap()))
                mis_count = mis_count + columnWeight[i];
        }
        for (int i = max.length() - 1; i > end; i--)
            if (!max.getAA(i).isGap())
                mis_count = mis_count + columnWeight[i];
        return ((double) match_count) / ((double) match_count + mis_count);
    }

    // get score vs. another sequence (using column weights)
    public double getScore(SeqSpaceEncoder encoder, Sequence other,
        double[] columnWeight) {
        Sequence min, max;

        if (other.length() > length()) {
            min = this;
            max = other;
        } else {
            min = other;
            max = this;
        }
        double score = 0.0;
        int end = min.length() - 1;

        for (int i = end; i >= 0; i--)
            score += columnWeight[i] *
                    encoder.getPairScore(min.getAA(i), max.getAA(i));
        for (int i = max.length() - 1; i > end; i--)
            score += columnWeight[i] *
                    encoder.getPairScore(AminoAcid.GAP, max.getAA(i));
        return score;
    }

    // ///////////
    // BETE BEGIN
    // ///////////


    /**
     * Mutators
     */

    public void removeGaps() {
        for (int i = 1; i < length();) {
            if (aligned_sequence[i].equals(AminoAcid.GAP)) {
                try {
                    deleteAA(i);
                } catch (Exception e) {// indicates invalid shift specification (will not happen)
                }
            } else {
                i++;
            }
        }
    }

    public void setStartIndex(int newStartIndex) {
        this.startindex = newStartIndex;
    }

    // set the name
    public void setName(String name) throws Exception {
        String old_name = this.name;

        this.name = name;
        if ((name == old_name) ||
            (name != null && name.equals(old_name)))
            return; // no notification necessary
        int idx = listeners.size() - 1;

        try {
            while (idx >= 0) {
                ((SequenceListener) listeners.get(idx)).sequenceNameChanged(this, old_name);
                idx--;
            }
        } catch (Exception e) {
            this.name = old_name;
            while (++idx < listeners.size())
                ((SequenceListener) listeners.get(idx)).sequenceNameChanged(this, name);
            throw e;
        }
    }

    // set the amino acid sequence
    // rebuilds raw2aligned indexes, etc.
    // does not alter line or column annotations!!
    private void setAAInternal(AminoAcid[] sequence) {
        // need logic to check if length has changed
        this.aligned_sequence = sequence;

        if (aligned_sequence != null) {
            int gap_cnt = 0;

            for (int i = aligned_sequence.length - 1; i >= 0; i--) {
                if (aligned_sequence[i].isGap())
                    gap_cnt++;
            }
            raw_sequence = new AminoAcid[aligned_sequence.length - gap_cnt];
            aligned2raw = new int[aligned_sequence.length];
            raw2aligned = new int[raw_sequence.length];
            int aligned_cnt = 0;
            int raw_cnt = 0;

            while (aligned_cnt < aligned_sequence.length) {
                AminoAcid aa = aligned_sequence[aligned_cnt];

                if (aa.isGap()) {
                    aligned2raw[aligned_cnt] = -1;
                } else {
                    raw_sequence[raw_cnt] = aa;
                    raw2aligned[raw_cnt] = aligned_cnt;
                    aligned2raw[aligned_cnt] = raw_cnt;
                    raw_cnt++;
                }
                aligned_cnt++;
            }
        } else {
            raw_sequence = null;
            aligned2raw = null;
            raw2aligned = null;
        }

        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((SequenceListener) it.next()).sequenceAAChanged(this);
    }

    public void insertAA(AminoAcid aa, int idx) {
        try {
            shiftAA(idx, 1);
        } catch (Exception e) {// ErrorDialog.showErrorDialog(this, "Unable to insert amino acid.", e);
        }
        aligned_sequence[idx] = aa;
    }

    // set the amino acid sequence
    // delete column annotations which are out of range
    public void setAA(AminoAcid[] sequence) {
        ArrayList old_ca = new ArrayList();

        for (int i = column_annotations.size() - 1; i >= 0; i--) {
            ColumnAnnotation ca =
                (ColumnAnnotation) column_annotations.get(i);

            if (ca.getColumn() >= sequence.length) {
                old_ca.add(ca);
                column_annotations.remove(i);
            } else
                break;
        }

        // set the new sequence and perform notifications
        setAAInternal(sequence);
        if (old_ca.size() > 0) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                SequenceListener l = (SequenceListener) it.next();

                for (int j = old_ca.size() - 1; j >= 0; j--) {
                    ColumnAnnotation ca =
                        (ColumnAnnotation) column_annotations.get(j);

                    l.sequenceColumnAnnotationsChanged(this, ca.getColumn());
                }
            }
        }
    }

    // shift the sequence
    // shift column and line annotations as well
    public void shiftAA(int idx, int shift) throws Exception {
        if (idx + shift < 0
            || idx < 0
            || idx > aligned_sequence.length)
            throw new Exception("invalid amino acid shift specified");
        if (shift == 0)
            return;

        // need logic to check if length has changed
        AminoAcid[] new_aa = new AminoAcid[aligned_sequence.length + shift];

        if (shift > 0) {
            System.arraycopy(aligned_sequence, 0, new_aa, 0, idx);
            Arrays.fill(new_aa, idx, idx + shift, AminoAcid.GAP);
            if (idx < aligned_sequence.length)
                System.arraycopy(aligned_sequence, idx, new_aa, idx + shift,
                    aligned_sequence.length - idx);
        } else {
            System.arraycopy(aligned_sequence, 0, new_aa, 0, idx + shift);
            if (idx < aligned_sequence.length)
                System.arraycopy(aligned_sequence, idx, new_aa, idx + shift,
                    new_aa.length - (idx + shift));
        }

        // adjust column annotations
        Set changed_column_set = new HashSet(11);
        int shift_start_idx = idx + shift;

        for (int i = column_annotations.size() - 1; i >= 0; i--) {
            ColumnAnnotation ca =
                (ColumnAnnotation) column_annotations.get(i);

            if (ca.getColumn() >= idx) {
                ColumnAnnotation new_ca =
                    new ColumnAnnotation(ca.getColumn() + shift,
                        ca.getSymbol(),
                        ca.getColor(),
                        ca.getAnnotation());

                changed_column_set.add(new Integer(ca.getColumn()));
                changed_column_set.add(new Integer(new_ca.getColumn()));

                column_annotations.set(i, new_ca);
            } // delete overwritten annotations
            else if (ca.getColumn() >= shift_start_idx) {
                changed_column_set.add(new Integer(ca.getColumn()));
                column_annotations.remove(i);
            } else
                break;
        }

        // adjust line annotations
        boolean line_annotations_changed = false;

        for (int i = line_annotations.size() - 1; i >= 0; i--) {
            LineAnnotation la = (LineAnnotation) line_annotations.get(i);
            String ann = la.getAnnotation();

            if (shift > 0 && ann.length() > idx) {
                StringBuffer sb = new StringBuffer();

                sb.append(ann.substring(0, idx));
                for (int k = 0; k < shift; k++)
                    sb.append(" ");
                sb.append(ann.substring(idx, ann.length()));
                line_annotations.set(i, new LineAnnotation(la.getName(),
                        sb.toString()));
                line_annotations_changed = true;
            } else if (shift < 0 && ann.length() > idx + shift) {
                StringBuffer sb = new StringBuffer();

                sb.append(ann.substring(0, idx + shift));
                if (ann.length() > idx)
                    sb.append(ann.substring(idx, ann.length()));
                line_annotations.set(i, new LineAnnotation(la.getName(),
                        sb.toString()));
                line_annotations_changed = true;
            }
        }

        // set the new sequence and perform notifications
        setAAInternal(new_aa);
        if (changed_column_set.size() > 0) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                SequenceListener l = (SequenceListener) it.next();

                for (Iterator jt = changed_column_set.iterator();
                    jt.hasNext();) {
                    int column = ((Integer) jt.next()).intValue();

                    l.sequenceColumnAnnotationsChanged(this, column);
                }
            }
        }
        if (line_annotations_changed) {
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                SequenceListener l = (SequenceListener) it.next();
                // l.sequenceLineAnnotationsChanged(this);
            }
        }
    }

    // pad a sequence with gaps to get to a certain size
    public void padWithGaps(int length) throws Exception {
        shiftAA(aligned_sequence.length, length - aligned_sequence.length);
    }

    // delete a particular column
    public void deleteAA(int idx) throws Exception {
        shiftAA(idx + 1, -1);
    }

    // set the annotation
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((SequenceListener) it.next()).sequenceAnnotationChanged(this);
    }

    // add a line annotation
    public void addLineAnnotation(String name, String ann) {
        addLineAnnotation(new LineAnnotation(name, ann));
    }

    public void addLineAnnotation(LineAnnotation la) {
        line_annotations.add(la);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((SequenceListener) it.next()).sequenceLineAnnotationsChanged(this);
    }

    // delete a line annotation
    public void deleteLineAnnotation(int idx) {
        line_annotations.remove(idx);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((SequenceListener) it.next()).sequenceLineAnnotationsChanged(this);
    }

    // set a line annotation
    public void setLineAnnotation(int idx, String name, String ann) {
        line_annotations.set(idx, new LineAnnotation(name, ann));
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((SequenceListener) it.next()).sequenceLineAnnotationsChanged(this);
    }

    // set the annotation on a particular column
    public void setColumnAnnotation(int column,
        String symbol,
        Color color,
        String ann) {
        setColumnAnnotation(new ColumnAnnotation(column, symbol, color, ann));
    }

    // delete a column annotation
    public void deleteColumnAnnotation(int column) {
        setColumnAnnotation(new ColumnAnnotation(column, null, null, null));
    }

    public void setColumnAnnotation(ColumnAnnotation ca) {
        // binary search for position
        int idx = findColumnAnnotationIndex(ca.getColumn());
        boolean changed = false;

        if (idx >= 0) {
            if (ca.getAnnotation() == null)
                column_annotations.remove(idx);
            else
                column_annotations.set(idx, ca);
            changed = true;
        } else if (ca.getAnnotation() != null) {
            int ins = -(idx + 1);

            column_annotations.add(ins, ca);
            changed = true;
        }

        if (changed) {
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((SequenceListener) it.next()).sequenceColumnAnnotationsChanged(this, ca.getColumn());
        }
    }

    // binary search for a column annotation position
    private int findColumnAnnotationIndex(int column) {
        int low = 0;
        int high = column_annotations.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int midVal = ((ColumnAnnotation) column_annotations.get(mid)).getColumn();

            if (midVal < column)
                low = mid + 1;
            else if (midVal > column)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    // set the color
    public void setColor(Color color) {
        if (this.color == null || color == null || !color.equals(this.color)) {
            this.color = color;
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((SequenceListener) it.next()).sequenceColorChanged(this);
        }
    }

    // set the pdb file
    public void setPDBFile(File f) {
        pdb_file = f;
    }

    public void addListener(SequenceListener l) {
        listeners.add(l);
    }

    public void removeListener(SequenceListener l) {
        listeners.remove(l);
    }

}

