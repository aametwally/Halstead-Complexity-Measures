package com.neogenesis.pfaat.colorscheme;


import java.awt.Color;
import java.io.*;
import java.util.*;

import org.apache.oro.text.perl.*;

import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.*;


/**
 * Color scheme based on residue in sequence and PID versus consensus.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:34 $ */
public class DefaultColorScheme extends ResidueColorScheme {
    public DefaultColorScheme(ConsensusSequence cons) {
        // construct the color table
        super(cons, 0.0,
            new Color[] {
                
                /* color_table[AminoAcid.ALA.getIndex()]*/ new Color(255, 255, 255),
                
                /* color_table[AminoAcid.ARG.getIndex()]*/ new Color(100, 100, 255),
                
                /* color_table[AminoAcid.ASN.getIndex()]*/ new Color(0, 255, 0),
                
                /* color_table[AminoAcid.ASP.getIndex()]*/ new Color(255, 0, 0),
                
                /* color_table[AminoAcid.CYS.getIndex()]*/ new Color(102, 102, 0),
                
                /* color_table[AminoAcid.GLN.getIndex()]*/ new Color(0, 255, 0),
                
                /* color_table[AminoAcid.GLU.getIndex()]*/ new Color(255, 0, 0),
                
                /* color_table[AminoAcid.GLY.getIndex()]*/ new Color(255, 255, 255),

                /* color_table[AminoAcid.HIS.getIndex()]*/ new Color(255, 153, 255),
                
                /* color_table[AminoAcid.ILE.getIndex()]*/ new Color(255, 255, 255),
                
                /* color_table[AminoAcid.LEU.getIndex()]*/ new Color(255, 255, 255),
                
                /* color_table[AminoAcid.LYS.getIndex()]*/ new Color(100, 100, 255),
                
                /* color_table[AminoAcid.MET.getIndex()]*/ new Color(255, 255, 255),
                
                /* color_table[AminoAcid.PHE.getIndex()]*/ new Color(255, 204, 0),
                
                /* color_table[AminoAcid.PRO.getIndex()]*/ new Color(204, 0, 204),
                
                /* color_table[AminoAcid.SER.getIndex()]*/ new Color(0, 255, 255),
                
                /* color_table[AminoAcid.THR.getIndex()]*/ new Color(0, 255, 255),
                
                /* color_table[AminoAcid.TRP.getIndex()]*/ new Color(255, 204, 0),
                
                /* color_table[AminoAcid.TYR.getIndex()]*/ new Color(255, 204, 0),
                
                /* color_table[AminoAcid.VAL.getIndex()]*/ new Color(255, 255, 255),
                
                /* color_table[AminoAcid.B.getIndex()]*/  new Color(255, 255, 255),

                /* color_table[AminoAcid.Z.getIndex()]*/ new Color(255, 255, 255),
                
                /* color_table[AminoAcid.X.getIndex()]*/ new Color(255, 255, 255),
                
                /* color_table[AminoAcid.GAP.getIndex()]*/ new Color(255, 255, 255)
            },
            "default");
    }

}
