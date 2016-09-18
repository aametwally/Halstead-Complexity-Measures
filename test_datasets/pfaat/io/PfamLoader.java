package com.neogenesis.pfaat.io;


import java.io.*;
import java.util.*;
import java.awt.Color;
import org.apache.oro.text.perl.*;

import com.neogenesis.pfaat.colorscheme.ColorScheme;
import com.neogenesis.pfaat.*;


/**
 * Load <code>Alignment</code> objects in PFAM format (with Stockholm
 * extensions).
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:53 $ */
public class PfamLoader extends AlignmentLoader {
    private Perl5Util perl = new Perl5Util();
    // AlignmentLoader interface
    public Alignment loadAlignment(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;

        HashMap seq_map = new HashMap();
        HashMap ann_map = new HashMap();
        HashMap la_map = new HashMap();
        HashMap ca_map = new HashMap();
        HashMap color_map = new HashMap();
        HashMap group_map = new HashMap();
        HashMap startindex_map = new HashMap();
        HashMap ruler_map = new HashMap();

        ArrayList seq_list = new ArrayList();
        ArrayList existingGroupsList = new ArrayList();

        String alignment_name = "";
        boolean stockholm = false;

        // load the headers
        while ((line = r.readLine()) != null) {
            // stockholm format?
            if (perl.match("/^# STOCKHOLM 1.0/", line)) {
                stockholm = true;
                continue;
            }

            // stockholm comments
            if (stockholm) {
                if (perl.match("/^#=GH\\s+RULERANN\\s+(\\d+)\\s(\\S)\\s(\\d+)\\s(\\d+)\\s(\\d+)/",
                        line)) {
                    int col = Integer.parseInt(perl.group(1));
                    String letter = perl.group(2);

                    if (letter.charAt(0) == '-') letter = " ";
                    int red = Integer.parseInt(perl.group(3));
                    int green = Integer.parseInt(perl.group(4));
                    int blue = Integer.parseInt(perl.group(5));

                    ruler_map.put(new Integer(col),
                        new Alignment.RulerAnnotation(letter, new Color(red, green, blue), false));
                }

                if (perl.match("/^#=GS\\s+(\\S+)\\s+ANN\\s(.+)/", line)) {
                    String name = perl.group(1);
                    String ann = perl.group(2);

                    ann_map.put(name, ann);
                    continue;
                }
                if (perl.match("/^#=GS\\s+(\\S+)\\s+GROUP\\s+(\\S+)/", line)) {
                    String name = perl.group(1);
                    String group = perl.group(2);

                    existingGroupsList = (ArrayList) group_map.get(name);
                    if (existingGroupsList == null)
                        existingGroupsList = new ArrayList();
                    existingGroupsList.add(new String(group));
                    group_map.put(name, existingGroupsList);
                    continue;
                }
                if (perl.match("/^#=GS\\s+(\\S+)\\s+COLOR\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)/", line)) {
                    String name = perl.group(1);
                    int red = Integer.parseInt(perl.group(2));
                    int green = Integer.parseInt(perl.group(3));
                    int blue = Integer.parseInt(perl.group(4));

                    color_map.put(name, new Color(red, green, blue));
                    continue;
                }

                if (perl.match("/^#=GS\\s+(\\S+)\\s+COLANN\\s+(\\d+)\\s(\\S)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(.+)/",
                        line)) {
                    String name = perl.group(1);
                    int col = Integer.parseInt(perl.group(2)) - 1;
                    String symbol = perl.group(3);
                    int red = Integer.parseInt(perl.group(4));
                    int green = Integer.parseInt(perl.group(5));
                    int blue = Integer.parseInt(perl.group(6));
                    String ann = perl.group(7);
                    List ca = (List) ca_map.get(name);

                    if (ca == null) {
                        ca = new ArrayList();
                        ca_map.put(name, ca);
                    }
                    ca.add(new Sequence.ColumnAnnotation(col,
                            symbol,
                            new Color(red,
                                green,
                                blue),
                            ann));
                    continue;
                }

                if (perl.match("/^#=GR\\s+(\\S+)\\s+(\\S+)\\s(.+)/", line)) {
                    String name = perl.group(1);
                    String l_name = perl.group(2);
                    String ann = perl.group(3);
                    List la = (List) la_map.get(name);

                    if (la == null) {
                        la = new ArrayList();
                        la_map.put(name, la);
                    }
                    la.add(new Sequence.LineAnnotation(l_name, ann));
                    continue;
                }
            }

            // skip comments
            if (perl.match("/^[ #]/", line))
                continue;

            if (perl.match("/^(\\S+)\\s+(\\S+)/", line)) {
                String name = perl.group(1);
                String seq_data = perl.group(2);

                if (seq_map.put(name, seq_data) != null)
                    throw new Exception("duplicate sequence name");
                seq_list.add(name);
            }
        }

        // build the sequences
        Sequence[] seqs = new Sequence[seq_list.size()];

        for (int i = seq_list.size() - 1; i >= 0; i--) {
            String name = (String) seq_list.get(i);
            String seq_data = (String) seq_map.get(name);
            AminoAcid[] seq = new AminoAcid[seq_data.length()];

            for (int j = seq.length - 1; j >= 0; j--) {
                seq[j] =
                        AminoAcid.lookupByCode(seq_data.substring(j, j + 1));
                if (seq[j] == null)
                    throw new Exception("PfamLoader: unknown amino acid: " + seq_data.substring(j, j + 1));
            }
            seqs[i] = new Sequence(name, seq);
            List groupsList = (List) group_map.get(name);

            if (groupsList != null) {
                for (int m = 0; m < groupsList.size(); m++) {
                    seqs[i].addGroup(groupsList.get(m).toString());
                }
            }
            String ann = (String) ann_map.get(name);

            if (ann != null) seqs[i].setAnnotation(ann);
            List ca = (List) ca_map.get(name);

            if (ca != null) {
                for (Iterator iter = ca.iterator(); iter.hasNext();)
                    seqs[i].setColumnAnnotation((Sequence.ColumnAnnotation)
                        iter.next());
            }
            List la = (List) la_map.get(name);

            if (la != null) {
                for (Iterator iter = la.iterator(); iter.hasNext();)
                    seqs[i].addLineAnnotation((Sequence.LineAnnotation)
                        iter.next());
            }
            Color c = (Color) color_map.get(name);

            if (c != null)
                seqs[i].setColor(c);
            Object startindex = startindex_map.get(name);

            if (startindex != null && ((Integer) startindex).intValue() < 0)
                throw new Exception("Negative start index.");
            if (startindex != null) seqs[i].setStartIndex(((Integer) startindex).intValue());
        }

        return new Alignment(alignment_name, seqs, ruler_map);
    }

    public String loadColorSchemeHint(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;
        String hint = null;

        boolean stockholm = false;

        while ((line = r.readLine()) != null) {
            // stockholm format?
            if (perl.match("/^# STOCKHOLM 1.0/", line)) {
                stockholm = true;
                continue;
            }

            if (stockholm && perl.match("/^#=GF COLORSCHEME (.+)/", line))
                return perl.group(1);
        }

        return null;
    }

    public void saveAlignment(Writer w, Alignment a) throws Exception {
        saveAlignment(w, a, null);
    }

    public void saveAlignment(Writer w, Alignment a, ColorScheme cs)
        throws Exception {
        PrintWriter out = new PrintWriter(w);

        out.println("# STOCKHOLM 1.0");

        String cs_name = cs.getName();

        if (cs_name == null || cs_name.length() < 1)
            throw new Exception("color scheme does not have a name");
        out.println("#=GF COLORSCHEME " + cs_name);
        for (int i = 0; i < a.rulerAnnotationsSize(); i++) {
            if (a.getRulerAnnotation(i).getLetter() != null) {
                Alignment.RulerAnnotation rann = a.getRulerAnnotation(i);

                out.print("#=GH ");
                out.print("RULERANN ");
                out.print(i + " ");
                if (rann.getLetter().equals(" "))
                    out.print("-" + " ");
                else
                    out.print(rann.getLetter() + " ");
                out.print(rann.getColor().getRed() + " ");
                out.print(rann.getColor().getGreen() + " ");
                out.print(rann.getColor().getBlue());
                out.println();
            }
        }
        for (int i = 0; i < a.size(); i++) {
            Sequence s = a.getSequence(i);

            s.shiftAA(1, -1); // account for offset in alignment display
            String name = s.getName();

            if (perl.match("/\\s/", name))
                throw new Exception("sequence name " +
                        name + " contains whitespace");
            out.print(name);
            out.print(" ");
            out.println(s.toString());

            String ann = s.getAnnotation();

            if (ann != null && ann.length() > 0) {
                out.print("#=GS ");
                out.print(name);
                out.print(" ANN ");
                out.println(ann);
            }
            Color color = s.getColor();

            if (color != null) {
                out.print("#=GS ");
                out.print(name);
                out.print(" COLOR ");
                out.print(color.getRed());
                out.print(" ");
                out.print(color.getGreen());
                out.print(" ");
                out.println(color.getBlue());
            }

            if (s.getGroupNames() != null) {
                String[] groupNames = s.getGroupNames();

                for (int m = 0; m < groupNames.length; m++) {
                    out.print("#=GS ");
                    out.print(name);
                    out.print(" GROUP ");
                    out.println(groupNames[m]);
                }
            }

            Sequence.ColumnAnnotation[] ca = s.getColumnAnnotations();

            if (ca.length > 0) {
                for (int k = 0; k < ca.length; k++) {
                    out.print("#=GS ");
                    out.print(name);
                    out.print(" COLANN ");
                    out.print(ca[k].getColumn() + 1);
                    out.print(" ");
                    if (ca[k].getSymbol().length() != 1)
                        throw new Exception("Column Annotation Symbol "
                                + "must have length 1");
                    out.print(ca[k].getSymbol());
                    out.print(" ");
                    Color c = ca[k].getColor();

                    out.print(c.getRed());
                    out.print(" ");
                    out.print(c.getGreen());
                    out.print(" ");
                    out.print(c.getBlue());
                    out.print(" ");
                    out.println(ca[k].getAnnotation());
                }
            }
            Sequence.LineAnnotation[] la = s.getLineAnnotations();

            if (la.length > 0) {
                for (int k = 0; k < la.length; k++) {
                    out.print("#=GR ");
                    out.print(name);
                    out.print(" ");
                    String l_name = la[k].getName();

                    if (perl.match("/\\s/", l_name))
                        throw new Exception("line annotation name " +
                                l_name + " contains whitespace");
                    out.print(l_name);
                    out.print(" ");
                    out.println(la[k].getAnnotation());
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
                    return perl.match("/.*\\.pfam$/",
                            f.getName());
                }

                public String getDescription() {
                    return "PFAM (*.pfam)";
                }
            };
    }
}
