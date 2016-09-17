package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;


public class CachedAnnotation {
    private String symbol;
    private String annotation;
    private Color color;

    public CachedAnnotation(String symbol, String annotation, Color color) { 
        this.symbol = symbol; 
        this.annotation = annotation;
        this.color = color;
    }

    public int hashCode() {
        return 27 * color.hashCode() 
            + 13 * symbol.hashCode() 
            + annotation.hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof CachedAnnotation) {
            CachedAnnotation o = (CachedAnnotation) other;

            return symbol.equals(o.symbol) 
                && color.equals(o.color)
                && annotation.equals(o.annotation);
        }
        return false;
    }
	
    // getters
    public String getSymbol() {
        return symbol;
    }

    public String getAnnotation() {
        return annotation;
    }

    public Color getColor() {
        return color;
    }	
	
    public Image getAnnotationIcon(DisplayProperties props) {
        int residue_width = props.getResidueWidth();
        int residue_height = props.getResidueHeight();
        int x_font_off = props.getFontXOffset();
        int y_font_off = props.getFontYOffset();
	  
        Image im = new BufferedImage(residue_width, residue_height, BufferedImage.TYPE_INT_RGB);
        Graphics g = im.getGraphics();

        g.setColor(Color.white);
        g.fillRect(0, 0, residue_width, residue_height);
        g.setColor(this.getColor());
        g.fillOval(0, 0, residue_width - 1, residue_height - 1);
        g.setColor(Color.black);
        g.drawString(this.getSymbol(), x_font_off, y_font_off);
        g.dispose();
        return im;
    }
}
