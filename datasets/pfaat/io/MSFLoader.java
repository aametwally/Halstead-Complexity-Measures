package com.neogenesis.pfaat.io;


import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;

import com.neogenesis.pfaat.*; 


/**
 * Load <code>Alignment</code> objects in MSF format.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:53 $ */
public class MSFLoader extends AlignmentLoader {
    private Perl5Util perl = new Perl5Util();

    // AlignmentLoader interface

    private static class SeqInfo {
        public String name;
        public int len, check;
        public StringBuffer sequence_data = new StringBuffer();
        public boolean is_annotation = false;

        public SeqInfo(String name, int len, int check) {
            this.name = name;
            this.len = len;
            this.check = check;
        }
    }

    public Alignment loadAlignment(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;

        HashMap seq_map = new HashMap();
        ArrayList seq_list = new ArrayList();

        String alignment_name = "";

        // load the headers
        while ((line = r.readLine()) != null) {
            if (perl.match("/^\\/\\//", line)) 
                break;

            if (perl.match("/^\\s*Name:\\s+(\\S+)\\s+.*Len:\\s+(\\d+)\\s+.*Check:\\s+(\\d+)/",
                    line)) {
                String name = perl.group(1);
                int len = Integer.valueOf(perl.group(2)).intValue();
                int check = Integer.valueOf(perl.group(3)).intValue();
                SeqInfo si = new SeqInfo(name, len, check);

                seq_list.add(name);
                seq_map.put(name, si);
            } else if (perl.match("/^\\s*(\\S+)\\s+MSF:/", line)) {
                alignment_name = perl.group(1);
            }
        }
	
        if (line == null)
            throw new Exception("no sequence data in MSF file");
	
        // count the number of annotation lines
        int annotation_cnt = 0;

        // load the sequence data
        while ((line = r.readLine()) != null) {
            if (perl.match("/^\\/\\//", line)) 
                continue;

            // ignore numbering lines
            if (perl.match("/^\\s*\\d+\\s+\\d+\\s*$/", line)) 
                continue;

            if (perl.match("/^\\s*(\\S+)\\s+(\\S+.*)/", line)) {
                String name = perl.group(1);
                SeqInfo si = (SeqInfo) seq_map.get(name);

                if (si == null)
                    throw new Exception("sequence data present "
                            + "for an undefined sequence "
                            + name + ": " + line);
                String data = perl.group(2);
                Vector seqs = perl.split(data);

                for (Iterator i = seqs.iterator(); i.hasNext();) {
                    String s = (String) i.next();

                    if (!si.is_annotation 
                        && !AminoAcid.isAminoAcidSequence(s)) {
                        si.is_annotation = true;
                        annotation_cnt++;
                    }
                    si.sequence_data.append(s);
                }
            }
        }

        // build and check the sequences
        // TODO - verify the checksums
        Sequence[] seqs = new Sequence[seq_list.size() - annotation_cnt];
        int cnt = 0;

        for (int i = 0; i < seq_list.size(); i++) {
            String name = (String) seq_list.get(i);
            SeqInfo si = (SeqInfo) seq_map.get(name);

            if (si.sequence_data.length() != si.len) 
                System.err.println("MSFLoader: size mismatch for sequence " 
                    + name + ", expecting "
                    + si.len + " residues, got "
                    + si.sequence_data.length());

            if (si.is_annotation) {
                if (cnt == 0) 
                    System.err.println("MSFLoader: annotation " + si.name 
                        + " does not come after a sequence,"
                        + " ignoring");
                else 
                    seqs[cnt - 1].addLineAnnotation(si.name,
                        si.sequence_data.toString());
            } else {
                AminoAcid[] seq = new AminoAcid[si.sequence_data.length()];

                for (int j = seq.length - 1; j >= 0; j--) {
                    seq[j] = 
                            AminoAcid.lookupByCode(si.sequence_data.substring(j, j + 1));
                    if (seq[j] == null)
                        throw new Exception("MSFLoader: unknown amino acid");
                }
                seqs[cnt++] = new Sequence(si.name, seq);
            }
        }
        if (cnt != seqs.length)
            throw new RuntimeException("MSFLoader: internal error"
                    + " parsing annotations");

        r.close();

        return new Alignment(alignment_name, seqs);
    }

    public void saveAlignment(Writer w, Alignment a) throws Exception {
        PrintWriter out = new PrintWriter(w);
	
        String a_name = a.getName();

        if (a_name == null || a_name.length() < 1)
            a_name = "alignment";
        if (perl.match("/\\s/", a_name))
            throw new Exception("alignment name " + 
                    a_name + " contains whitespace");
        int maxlen = a.maxLength();

        // header
        out.print(a_name);
        out.print(" MSF: ");
        out.print(maxlen);
        out.println(" Type: P Check: 9999 ..");
        out.println("");

        // sequence names
        for (int i = 0; i < a.size(); i++) {
            Sequence s = a.getSequence(i);

            s.shiftAA(1, -1); // account for offset in alignment display
            String name = s.getName();

            if (perl.match("/\\s/", name))
                throw new Exception("sequence name " + 
                        name + " contains whitespace");
            out.print(" Name: ");
            out.print(name);
            out.print(" Len: ");
            out.print(s.length());
            out.println(" Check: 9999 Weight: 1.00");
            s.shiftAA(0, 1); // account for offset in alignment display
        }
	
        out.println("");
        out.println("//");
        out.println("");
	
        // sequence data
        for (int start = 0; start < maxlen; start += 50) {
            for (int i = 0; i < a.size(); i++) {
                Sequence s = a.getSequence(i);		

                s.shiftAA(1, -1);
                int end = Math.min(start + 50, s.length());

                if (start >= end)
                    continue;
		
                out.print(s.getName());
                out.print(" ");
		
                for (int pos = start; pos < end; pos++) {
                    out.print(s.getAA(pos).getCode());
                    if (pos % 10 == 0 && pos % 50 != 0)
                        out.print(" ");
                }
                out.println("");
                s.shiftAA(0, 1);
            }

            out.println("");
        }
        out.flush();
    }

    // FileFilter interface
    public javax.swing.filechooser.FileFilter getFileFilter() { 
        return new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return perl.match("/.*\\.msf$/", 
                            f.getName());
                }

                public String getDescription() {
                    return "MSF (*.msf)";
                }
            };
    }
} 

