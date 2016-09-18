package com.neogenesis.pfaat.io;


import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;

import com.neogenesis.pfaat.*; 


/**
 * Load <code>Alignment</code> objects in fasta format.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:53 $ */
public class FSALoader extends AlignmentLoader {
    private Perl5Util perl = new Perl5Util();

    // AlignmentLoader interface
    public Alignment loadAlignment(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;
        String name = "";
        String sequence = "";

        HashMap seq_map = new HashMap();
        ArrayList seq_list = new ArrayList();

        String alignment_name = "";

        // load the sequence data
        while ((line = r.readLine()) != null) {
            if (perl.match("/^>([^\\s]*).*/", line)) {
                name = perl.group(1);
                seq_list.add(name);
                sequence = "";
                seq_map.put(name, sequence);
            } else {
                sequence = 
                        sequence.concat(line.toUpperCase().replace('.', '-'));
                seq_map.put(name, sequence);
            }
        }

        // build and check the sequences
        Sequence[] seqs = new Sequence[seq_list.size()];

        for (int i = 0; i < seq_list.size(); i++) {
            name = (String) seq_list.get(i);
            sequence = (String) seq_map.get(name);
            AminoAcid[] seq = new AminoAcid[sequence.length()];

            for (int j = seq.length - 1; j >= 0; j--) {
                seq[j] = 
                        AminoAcid.lookupByCode(sequence.substring(j, j + 1));
                if (seq[j] == null)
                    throw new Exception("FSALoader: unknown amino acid");
            }
            seqs[i] = new Sequence(name, seq);
        }

        r.close();

        return new Alignment(alignment_name, seqs);
    }

    public void saveAlignment(Writer w, Alignment a) throws Exception {
        PrintWriter out = new PrintWriter(w);
	
        // sequence data
        for (int i = 0; i < a.size(); i++) {
            Sequence s = a.getSequence(i);

            s.shiftAA(1, -1); // account for offset in alignment display
            out.println(">" + s.getName());
            for (int pos = 0; pos < s.length(); pos++) {
                String aa = s.getAA(pos).getCode();

                if (aa.equals("-")) aa = ".";
                out.print(aa);
                if ((((pos + 1) % 60) == 0) || ((pos + 1) == s.length())) {
                    out.println("");
                }
            }
            s.shiftAA(0, 1); // account for offset in alignment display
        }
        out.flush();
    }

    // FileFilter interface
    public javax.swing.filechooser.FileFilter getFileFilter() { 
        return new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return (perl.match("/.*\\.fa$/", f.getName()) ||
                            perl.match("/.*\\.tfa$/", f.getName()) ||
                            perl.match("/.*\\.ufa$/", f.getName()) ||
                            perl.match("/.*\\.afa$/", f.getName()) ||
                            perl.match("/.*\\.fsa$/", f.getName()));
                }

                public String getDescription() {
                    return "fasta (*.fa, *.tfa, *.ufa, *.afa, *.fsa)";
                }
            };
    }
} 
