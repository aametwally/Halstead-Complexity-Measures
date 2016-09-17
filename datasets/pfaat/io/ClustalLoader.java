package com.neogenesis.pfaat.io;


import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;

import com.neogenesis.pfaat.*; 


/**
 * Load <code>Alignment</code> objects in fasta format.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:53 $ */
public class ClustalLoader extends AlignmentLoader {
    private static final int MIN_NAME_FIELD_WIDTH = 10;
    private static final String SEPARATOR = "      ";
    private static final int SEQ_FIELD_WIDTH = 60;

    private Perl5Util perl = new Perl5Util();

    private class InnerSequence {
        public String name;
        public StringBuffer sequence;

        public InnerSequence(String name) {
            this.name = name;
            this.sequence = new StringBuffer();
        }

        public Sequence getSequence() 
            throws Exception {
            AminoAcid[] seq = new AminoAcid[sequence.length()];

            for (int j = seq.length - 1; j >= 0; j--) {
                seq[j] = 
                        AminoAcid.lookupByCode(sequence.charAt(j));
                if (seq[j] == null)
                    throw new Exception("ClustalLoader: unknown amino acid");
            }
            return (new Sequence(name, seq));
        }
    }

    // AlignmentLoader interface
    public Alignment loadAlignment(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;

        HashMap seq_map = new HashMap();
        ArrayList seq_list = new ArrayList();

        String alignment_name = "";

        // load the sequence data
        while ((line = r.readLine()) != null) {
            if (perl.match("/^CLUSTAL/", line))
                continue; // match header
            if (perl.match("/^\\s*$/", line))
                continue; // skip blank lines

            if (perl.match("/^([^\\s]+)\\s+([^\\s]+)\\s*$/", line)) {
                String name = perl.group(1);
                InnerSequence sequence = (InnerSequence) seq_map.get(name);

                if (sequence == null) {
                    sequence = new InnerSequence(name);
                    seq_map.put(name, sequence);
                    seq_list.add(sequence);
                }
                sequence.sequence.append(perl.group(2));
            }
        }

        // build and check the sequences
        Sequence[] seqs = new Sequence[seq_list.size()];

        for (int i = 0; i < seq_list.size(); i++) {
            InnerSequence sequence = (InnerSequence) seq_list.get(i);

            seqs[i] = sequence.getSequence();
        }

        r.close();

        return new Alignment(alignment_name, seqs);
    }

    public void saveAlignment(Writer w, Alignment a) throws Exception {
        PrintWriter out = new PrintWriter(w);
	
        int maxNameLength = 0;
        InnerSequence[] seqs = new InnerSequence[a.size()];

        for (int i = 0; i < a.size(); i++) {
            Sequence s = a.getSequence(i);

            seqs[i] = new InnerSequence(s.getName());
            if (seqs[i].name.length() > maxNameLength)
                maxNameLength = seqs[i].name.length();

            s.shiftAA(1, -1); // account for offset in alignment display
            for (int pos = 0; pos < s.length(); pos++)
                seqs[i].sequence.append(s.getAA(pos).getCode());
            s.shiftAA(0, 1); // account for offset in alignment display
        }

        // pad names
        if (maxNameLength < MIN_NAME_FIELD_WIDTH)
            maxNameLength = MIN_NAME_FIELD_WIDTH;
        for (int i = 0; i < seqs.length; i++) {
            if (seqs[i].name.length() != maxNameLength) {
                StringBuffer newName = new StringBuffer(seqs[i].name);

                while (newName.length() != maxNameLength)
                    newName.append(' ');
                seqs[i].name = newName.toString();
            }
        }
	
        // pad sequences
        int maxSequenceLength = a.maxLength() - 1;

        if (maxSequenceLength != a.minLength())
            for (int i = 0; i < seqs.length; i++)
                while (seqs[i].sequence.length() < maxSequenceLength)
                    seqs[i].sequence.append('-');
	
        out.println("CLUSTAL");
        out.println("");
        out.println("");
        for (int i = 0; i < maxSequenceLength; i += SEQ_FIELD_WIDTH) {
            for (int j = 0; j < seqs.length; j++) {
                String subseq = 
                    seqs[j].sequence.substring(i, 
                        ((i + SEQ_FIELD_WIDTH > maxSequenceLength) ? 
                            maxSequenceLength :
                            i + SEQ_FIELD_WIDTH));

                out.println(seqs[j].name + SEPARATOR + subseq);
            }
            out.println("");
        }

        out.flush();
    }

    // FileFilter interface
    public javax.swing.filechooser.FileFilter getFileFilter() { 
        return new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return (perl.match("/.*\\.aln$/",
                                f.getName()));
                }

                public String getDescription() {
                    return ("CLUSTAL (*.aln)");
                }
            };
    }
} 
