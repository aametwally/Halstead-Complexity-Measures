package com.neogenesis.pfaat;


import java.util.*;


/**
 * Jnet analysis file.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public class JnetAnalysis {
    private static class Entry {
        private String name, data;
        public Entry(String name, String data) { 
            this.name = name;
            this.data = data; 
        }

        public String getName() {
            return name;
        }

        public String getData() {
            return data;
        }
    }
    
    private List entries = new ArrayList();

    // accessors
    public int getEntryCount() {
        return entries.size();
    }

    public String getName(int i) {
        return ((Entry) entries.get(i)).getName();
    }

    public String getData(int i) {
        return ((Entry) entries.get(i)).getData();
    }

    // mutators
    public void addEntry(String name, String data) {
        entries.add(new Entry(name, " " + data)); // account for seq offset
    }
}
