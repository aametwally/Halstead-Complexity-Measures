package com.neogenesis.pfaat;


import java.awt.*;
import java.util.*;
import com.neogenesis.pfaat.*;


public class CollatedColumnAnnotation {
    private int index;
    private String symbol;
    private Color color;
    private String annotation;
    private Vector range;
    public CollatedColumnAnnotation( 
        int index,
        String symbol, 
        Color color,
        String annotation, 
        Vector range) { 
        this.index = index;
        this.symbol = symbol;
        this.color = color;
        this.annotation = annotation; 
        this.range = range;
    }

    public int getIndex() {
        return index;
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

    public Vector getRange() {
        return range;
    }		
		
    public String getFormattedRange() {
        // returns comma-delimited, dash-ranged string	
        String formattedRange = "";
        Vector tempRange = new Vector();

        for (int i = 0; i < range.size(); i++)
            tempRange.add(Integer.toString(((Integer) range.get(i)).intValue()));		
        int l, c, r;	

        for (int i = 1; i < range.size() - 1; i++) {
            l = ((Integer) range.get(i - 1)).intValue();
            c = ((Integer) range.get(i)).intValue();
            r = ((Integer) range.get(i + 1)).intValue();
            if ((c + 1 == r) && (c - 1 == l)) tempRange.set(i, new String("-"));
        }
        String mid, right;

        for (int i = 1; i < tempRange.size() - 1; i++) {
            mid = (String) tempRange.get(i);
            right = (String) tempRange.get(i + 1);
            if (mid.equals("-") && right.equals("-"))
                tempRange.set(i, new String("*"));
        }
			
        while (tempRange.remove("*")) {};
			
        int j = 0;

        while (j < (tempRange.size() - 1)) {
            mid = (String) tempRange.get(j);
            right = (String) tempRange.get(j + 1);
            if (!mid.equals("-") && !right.equals("-")) {
                tempRange.add(j + 1, ",");
                j += 2;
            } else j += 1;
        }
			 
        for (int i = 0; i < tempRange.size(); i++)
            formattedRange += (String) tempRange.get(i);
        return formattedRange;
    }
		
    public void addColumnIndex(int idx) {
        range.addElement(new Integer(idx));
    }
		
    public boolean equals(Object other) {
        CollatedColumnAnnotation o = (CollatedColumnAnnotation) other;

        return ((symbol.equals(o.symbol)) &&
                (color.equals(o.color)) &&
                (annotation.equals(o.annotation)));
    }

    public static Vector parseRangeString(String range, int maxLength) throws Exception { 
        // returns vector of ints from comma-delimited, dash-ranged string		
        Vector unformattedRange = new Vector();		
        StringBuffer tempNewRange = new StringBuffer();
        String newRange = "";
        String admissableChars = "0123456789,-";

        if (range.length() == 0) return null;
        for (int i = 0; i < range.length(); i++)
            if (range.charAt(i) != ' ') tempNewRange.append(range.charAt(i));		
        newRange = tempNewRange.toString();
        for (int i = 0; i < newRange.length(); i++)
            if (admissableChars.indexOf(newRange.charAt(i)) == -1) return null;
        StringTokenizer rangeTokens = new StringTokenizer(newRange, ",");
        StringTokenizer subrangeTokens; 
        String temp;

        while (rangeTokens.hasMoreTokens()) {
            temp = (String) rangeTokens.nextToken();
            if (temp.indexOf("-") == -1)
                unformattedRange.add(new Integer(Integer.parseInt(temp)));
            else {
                // parse int range
                subrangeTokens = new StringTokenizer(temp, "-");
                int start = Integer.parseInt(subrangeTokens.nextToken());
                int stop = Integer.parseInt(subrangeTokens.nextToken());

                if (stop < start) return null;
                for (int i = start; i <= stop; i++)
                    unformattedRange.add(new Integer(i));
            }
        }
        for (int i = 0; i < unformattedRange.size(); i++)
            if (((Integer) unformattedRange.get(i)).intValue() > maxLength) 
                return null;
        return unformattedRange;
    }

    public void setSymbol(String Symbol) {
        this.symbol = symbol;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setRange(Vector range) {
        this.range = range;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}

