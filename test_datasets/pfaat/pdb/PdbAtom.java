package com.neogenesis.pfaat.pdb;


import java.util.*;
import javax.vecmath.Point3d;


/**
 * An atom in R^3.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbAtom {
    private String name;
    private Point3d position;
    private String type;
    private List bonds = new ArrayList();

    public PdbAtom(String name, Point3d position, String type) {
        this.name = name;
        this.position = position;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Point3d getPosition() {
        return position;
    }

    public void setPosition(Point3d newPos) {
        position = newPos;
    };
    public String getType() {
        return type;
    }

    public int getBondCount() {
        return bonds.size();
    }

    public PdbBond getBond(int i) {
        return (PdbBond) bonds.get(i);
    }

    public PdbAtom getBondedAtom(int i) {
        PdbBond bond = getBond(i);
        PdbAtom a = bond.getFirst();

        return a == this ? a : bond.getSecond();
    }

    public boolean isBondedTo(PdbAtom other) {
        for (int i = bonds.size() - 1; i >= 0; i--) {
            if (getBondedAtom(i) == other)
                return true;
        }
        return false;
    }

    public void addBond(PdbBond bond) throws Exception {
        if (bond.getFirst() != this && bond.getSecond() != this)
            throw new Exception("attempt to add invalid bond");
        bonds.add(bond);
    }

    public float getVanDerWaalsRadius() {
        if (type.equals("C"))
            return 1.53f;
        if (type.equals("N"))
            return 1.48f;
        if (type.equals("O"))
            return 1.36f;
        if (type.equals("S"))
            return 1.7f;
        if (type.equals("P"))
            return 1.75f;
        if (type.equals("H"))
            return 1.08f;
        if (type.equals("Br"))
            return 1.8f;
        if (type.equals("Cl"))
            return 1.65f;
        if (type.equals("F"))
            return 1.3f;
        if (type.equals("I"))
            return 2.05f;
        if (type.equals("Na"))
            return 0.95f;
        if (type.equals("K"))
            return 1.33f;
        if (type.equals("Ca"))
            return 0.99f;
        if (type.equals("Li"))
            return 0.6f;
        return 1.5f;
    }

}
