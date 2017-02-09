package com.neogenesis.pfaat.pdb;


import java.awt.Color;
import javax.vecmath.Color3f;


/**
 * Montone color scheme for proteins.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbMonotoneColorScheme implements PdbColorScheme {
    private Color3f color;

    public PdbMonotoneColorScheme(Color c) {
        color = new Color3f(c.getRed() / 255.0f, 
                    c.getGreen() / 255.0f,
                    c.getBlue() / 255.0f);
    }

    public void initialize(PdbStructure structure) {}

    public Color3f color(PdbAtom atom) {
        return color;
    }
}
    
