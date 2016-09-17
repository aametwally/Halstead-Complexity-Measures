package com.neogenesis.pfaat.pdb;


/**
 * Bond object.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbBond {
    public static final int COVALENT_BOND = 0;
    public static final int HYDROGEN_BOND = 1;
    public static final int SALT_BRIDGE = 2;

    private int type;
    private PdbAtom first, second;

    public PdbBond(int type, PdbAtom first, PdbAtom second) {
        this.type = type;
        this.first = first;
        this.second = second;
    }

    public PdbAtom getFirst() {
        return first;
    }

    public PdbAtom getSecond() {
        return second;
    }

    public int getType() {
        return type;
    }

}
