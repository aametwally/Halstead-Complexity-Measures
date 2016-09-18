package com.neogenesis.pfaat.srs;


import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;

import com.neogenesis.pfaat.*;


/**
 * An SRS annotated sequence.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:55 $ */
public class SRSEntry {
    private String id;
    private Feature[] features;
    private AminoAcid[] aa;

    private static Perl5Util perl = new Perl5Util();

    // constructor
    public SRSEntry(Reader ir) throws Exception {
        BufferedReader r;

        if (ir instanceof BufferedReader)
            r = (BufferedReader) ir;
        else 
            r = new BufferedReader(ir);
	
        String line;
        List f_list = new ArrayList();

        while ((line = r.readLine()) != null) {
            if (perl.match("/^ID\\s+(\\S+)/", line))
                id = perl.group(1);
            else if (perl.match("/^FT\\s+(\\S+)\\s+([0-9]+)\\s+([0-9]+)/",
                    line)) {
                String f_type = perl.group(1);
                int f_start = Integer.parseInt(perl.group(2)) - 1;
                int f_end = Integer.parseInt(perl.group(3)) - 1;
                String f_desc = line.substring(perl.endOffset(0), 
                        line.length()).trim();

                f_list.add(new Feature(f_type, f_start, f_end, f_desc));
            } else if (perl.match("/^SQ\\s+SEQUENCE\\s+([0-9]+)/", line)) {
                int seq_length = Integer.parseInt(perl.group(1));
                StringBuffer aa_buf = new StringBuffer();

                while ((line = r.readLine()) != null 
                    && !line.startsWith("//")) {
                    line = perl.substitute("s/\\s//g", line);
                    aa_buf.append(line);
                }
                if (aa_buf.length() != seq_length)
                    throw new Exception("sequence length incorrect");
                aa = AminoAcid.stringToAA(aa_buf.toString());
            }
        }

        if (id == null)
            throw new Exception("id unspecified in SRS entry");
        if (aa == null)
            throw new Exception("sequence unspecified in SRS entry");
        features = (Feature[]) f_list.toArray(new Feature[0]);
    }

    // accessors
    public String getID() {
        return id;
    }

    public AminoAcid[] getAAs() {
        return aa;
    }

    public int getFeatureCount() {
        return features.length;
    }

    public Feature getFeature(int i) {
        return features[i];
    }

    public static class Feature {
        private String type, description;
        private int start, end;
        public Feature(String type, int start, int end, String description) {
            this.type = type;
            this.start = start;
            this.end = end;
            this.description = description == null ? "" : description;
        }

        public String getType() {
            return type;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getDescription() {
            return description;
        }
    }
}

