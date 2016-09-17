package com.neogenesis.pfaat.pdb;


import java.util.*;

import com.neogenesis.pfaat.*;


/**
 * A chain in a 3d protein structure.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/15 13:58:28 $ */
public class PdbChain extends PdbAtomContainer {
    private String name;
    private List residue_list = new ArrayList();
    private Map residue_name_map = new HashMap(17);
    private Map residue_offset_map = new HashMap(17);
    private Map secondary_structure_map = new HashMap(17);
    private Map atom2residue_map;

    // seconadry structure types
    public static final String SHEET = "Sheet";
    public static final String HELIX = "Helix";
    public static final String TURN = "Turn";

    public PdbChain(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PdbResidue getResidue(int i) {
        if (i<0 || i>=residue_list.size())
            return null;
        return (PdbResidue) residue_list.get(i);
    }

    public PdbResidue getResidue(String res_name) {
        return (PdbResidue) residue_name_map.get(res_name);
    }

    public PdbResidue getResidue(PdbAtom atom) {
        return (PdbResidue) atom2residue_map.get(atom);
    }

    public int getResidueCount() {
        return residue_list.size();
    }

    public int getSequenceAlignment(Sequence s) {
        int end = s.getRawLength() - residue_list.size();

        outer:
        for (int i = 0; i <= end; i++) {
            for (int k = residue_list.size() - 1; k >= 0; k--) {
                AminoAcid s_aa = s.getRawAA(i + k);

                if (s_aa.equals(AminoAcid.X)) // X matched anything
                    continue;
                AminoAcid c_aa = ((PdbResidue) residue_list.get(k)).getType();

                if (!s_aa.equals(c_aa))
                    continue outer;
            }
            return i;
        }
        return -1;
    }

    public AminoAcid[] getAAs() {
        AminoAcid[] aa = new AminoAcid[residue_list.size()];

        for (int i = aa.length - 1; i >= 0; i--) {
            aa[i] = ((PdbResidue) residue_list.get(i)).getType();
            if (aa[i] == null)
                aa[i] = AminoAcid.X;
        }
        return aa;
    }

    public int getOffset(PdbResidue residue) {
        Integer offset = (Integer) residue_offset_map.get(residue);

        return offset == null ? -1 : offset.intValue();
    }

    public String getSecondaryStructure(PdbResidue residue) {
        return (String) secondary_structure_map.get(residue);
    }

    public void setSecondaryStructure(PdbResidue residue, String ss) {
        secondary_structure_map.put(residue, ss);
    }

    public void addResidue(PdbResidue residue) throws Exception {
        if (residue_name_map.put(residue.getName(), residue) != null)
            throw new Exception("duplicate residue definition");
        residue_list.add(residue);
        residue_offset_map.put(residue, new Integer(residue_list.size() - 1));
    }

    public void addAtom(PdbAtom atom) throws Exception {
        throw new Exception("direct atom addition not supported");
    }

    public void initialize() throws Exception {
        atom2residue_map = new HashMap();
        for (int i = 0; i < residue_list.size(); i++) {
            PdbResidue residue = (PdbResidue) residue_list.get(i);

            residue.initialize();
            for (int j = 0; j < residue.getAtomCount(); j++) {
                PdbAtom a = residue.getAtom(j);

                super.addAtom(a);
                atom2residue_map.put(a, residue);
            }
        }
    }

}
