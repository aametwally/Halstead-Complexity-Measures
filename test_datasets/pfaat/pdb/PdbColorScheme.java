package com.neogenesis.pfaat.pdb;


import javax.vecmath.Color3f;


/**
 * Color scheme for protein rendering.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public interface PdbColorScheme {
    public void initialize(PdbStructure structure);
    public Color3f color(PdbAtom atom);
}
