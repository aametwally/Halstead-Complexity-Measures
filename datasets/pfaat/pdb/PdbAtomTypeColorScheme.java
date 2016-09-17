package com.neogenesis.pfaat.pdb;

import java.awt.Color;
import javax.vecmath.Color3f;


/**
 * Color scheme for protein rendering.
 *
 * @author $Author: xih $
 * @version $Revision: 1.1 $, $Date: 2002/10/15 13:58:28 $ */

public class PdbAtomTypeColorScheme implements PdbColorScheme
{

    public void initialize(PdbStructure structure) {}
    public static Color3f grey = new Color3f(125/255.0f, 125/255.0f, 125/255.0f);
    public static Color3f red = new Color3f(1.0f, 0.0f, 0.0f); //red
    public static Color3f blue = new Color3f(0.0f, 0.0f, 1.0f); //blue
    public static Color3f lightGreen = new Color3f(0.0f, 1.0f, 0.0f); //lightGreen
    public static Color3f brown = new Color3f(255/255.0f, 165/255.0f, 0.0f);
    public static Color3f cyan = new Color3f(0.0f, 1.0f, 1.0f);
    public Color3f color(PdbAtom atom) {
        String atomType = atom.getType();
        if (atomType.equalsIgnoreCase("C"))
            return grey;//grey
        else if (atomType.equalsIgnoreCase("N"))
             return blue;
        else if (atomType.equalsIgnoreCase("O"))
             return red;
        else if (atomType.equalsIgnoreCase("Cl") ||
                 atomType.equalsIgnoreCase("Br") ||
                 atomType.equalsIgnoreCase("I") )
            return lightGreen;
        else if (atomType.equalsIgnoreCase("S"))
            return brown;
        else
            return grey;
    }
}