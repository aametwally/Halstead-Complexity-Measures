package com.neogenesis.pfaat;


import java.awt.*;
import java.util.*;
import com.neogenesis.pfaat.*;


public class CollatedLineAnnotation {
    private int index, annotationIndex, start, end;
    private String name, symbol;
    public CollatedLineAnnotation(
        int index, int annotationIndex, int start, int end, String name, String symbol) { 
        this.index = index;
        this.annotationIndex = annotationIndex;
        this.start = start;
        this.end = end;
        this.name = name;
        this.symbol = symbol;
    }

    public int getIndex() {
        return index;
    }

    public int getAnnotationIndex() {
        return annotationIndex;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }
}
	
