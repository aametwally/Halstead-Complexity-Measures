package com.neogenesis.pfaat.pdb;


import java.util.*;

import com.neogenesis.pfaat.*;


/**
 * Residue in 3d protein.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbResidue extends PdbAtomContainer {
    private String name, raw_type;
    private AminoAcid type;
    private Map location2atom_map = new HashMap(23);

    public PdbResidue(String name, 
        String raw_type,
        AminoAcid type) {
        this.name = name;
        this.raw_type = raw_type;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getRawType() {
        return raw_type;
    }

    public AminoAcid getType() {
        return type;
    }

    public PdbAtom getByLocation(String location) { 
        return (PdbAtom) location2atom_map.get(location);
    }

    public void addAtom(PdbAtom atom) throws Exception {
        throw new Exception("direct atom addition not supported");
    }

    public void addAtom(PdbAtom atom, String location) throws Exception {
        // ignores duplicates for now
        location2atom_map.put(location, atom);
        super.addAtom(atom);
    }

}
