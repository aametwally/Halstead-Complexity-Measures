package com.neogenesis.pfaat; 


import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;


/**
 * Jnet analysis loader.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class JnetLoader {
    private Perl5Util perl = new Perl5Util();

    public JnetAnalysis loadAnalysis(Reader ir) throws Exception {
        BufferedReader r = new BufferedReader(ir);
        String line;

        Map analysis_map = new HashMap();
        List analysis_list = new ArrayList();

        while ((line = r.readLine()) != null) {
            if (perl.match("/^\\s+(\\S+)\\s+: (.+)$/", line)) {
                String name = perl.group(1);
                String txt = perl.group(2);
                StringBuffer sb = (StringBuffer) analysis_map.get(name);

                if (sb == null) {
                    analysis_list.add(name);
                    sb = new StringBuffer();
                    analysis_map.put(name, sb);
                }
                sb.append(txt);
            }
        }
		    
        JnetAnalysis jnet = new JnetAnalysis();

        for (int i = 0; i < analysis_list.size(); i++) {
            String name = (String) analysis_list.get(i);

            jnet.addEntry(name, 
                ((StringBuffer) analysis_map.get(name)).toString());
        }
        return jnet;
    }

    // FileFilter interface
    public javax.swing.filechooser.FileFilter getFileFilter() { 
        return new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".jnet");
                }

                public String getDescription() {
                    return "Jnet (*.jnet)";
                }
            };
    }

}
