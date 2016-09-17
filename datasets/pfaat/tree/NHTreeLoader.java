package com.neogenesis.pfaat.tree; 


import java.io.*;


/**
 * New Hampshire tree loader.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:50 $ */
public class NHTreeLoader {
    public String loadTree(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;
	
        StringBuffer sb = new StringBuffer();

        while ((line = r.readLine()) != null) 
            sb.append(line);

        return sb.toString();
    }
    
    // FileFilter interface
    public javax.swing.filechooser.FileFilter getFileFilter() { 
        return new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".nh") 
                        || f.getName().endsWith(".nhx");
                }

                public String getDescription() {
                    return "New Hampshire Tree (*.nh,*.nhx)";
                }
            };
    }
    
    public void saveNHTree(File f, String nh_string) throws Exception {
        Writer w = new BufferedWriter(new FileWriter(f));
        PrintWriter out = new PrintWriter(w);

        out.print(nh_string);
        w.close();
    }

}
