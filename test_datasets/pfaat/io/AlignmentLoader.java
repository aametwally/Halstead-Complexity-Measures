package com.neogenesis.pfaat.io;


import java.io.*;
import javax.swing.JFileChooser;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.colorscheme.ColorScheme;


/**
 * Abstract class for loading and saving <code>Alignment</code> objects.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:53 $ */
public abstract class AlignmentLoader {
    public abstract Alignment loadAlignment(Reader r) throws Exception;

    public abstract void saveAlignment(Writer w, Alignment a) throws Exception;

    public abstract javax.swing.filechooser.FileFilter getFileFilter();

    private static AlignmentLoader[] loaders = {
            new TabLoader(),
            new ClustalLoader(),
            new FSALoader(),
            new MSFLoader(),
            new PfamLoader()
        };
    private static javax.swing.filechooser.FileFilter[] filters;
    static {
        filters = new javax.swing.filechooser.FileFilter[loaders.length];
        for (int i = loaders.length - 1; i >= 0; i--)
            filters[i] = loaders[i].getFileFilter();
    }

    public static void addFileFilters(JFileChooser fc) {
        for (int i = 0; i < filters.length; i++)
            fc.addChoosableFileFilter(new DirectoryFileFilter(filters[i]));
    }

    public static AlignmentLoader getAlignmentLoader(File f) throws Exception {
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].accept(f))
                return loaders[i];
        }
        throw new Exception("unknown alignment file type for "
                + f.getAbsolutePath());
    }

    public static Alignment loadAlignmentFile(File f) throws Exception {
        AlignmentLoader l = getAlignmentLoader(f);
        Reader r = new BufferedReader(new FileReader(f));
        Alignment a = l.loadAlignment(r);

        r.close();
        if (a.getName() == null || a.getName().length() == 0)
            a.setName(f.getName());
        // a.setChanged(false);
        return a;
    }

    public static String getColorSchemeHint(File f) throws Exception {
        AlignmentLoader l = getAlignmentLoader(f);

        if (l instanceof PfamLoader) {
            Reader r = new BufferedReader(new FileReader(f));
            String hint = ((PfamLoader) l).loadColorSchemeHint(r);

            r.close();
            return hint;
        }
        return null;
    }

    public static void saveAlignmentFile(File f, ColorScheme cs, Alignment a)
        throws Exception {
        AlignmentLoader l = getAlignmentLoader(f);
        Writer w = new BufferedWriter(new FileWriter(f));

        if (l instanceof PfamLoader)
            ((PfamLoader) l).saveAlignment(w, a, cs);
        else
            l.saveAlignment(w, a);
        w.close();
    }
}
