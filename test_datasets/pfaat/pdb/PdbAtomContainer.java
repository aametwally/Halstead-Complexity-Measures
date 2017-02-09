package com.neogenesis.pfaat.pdb;


import java.util.*;
import javax.vecmath.Point3d;


/**
 * A group of atoms.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbAtomContainer {
    private List atoms = new ArrayList();
    private Map atom_name_map = new HashMap(11);

    public PdbAtom getAtom(int i) {
        return (PdbAtom) atoms.get(i);
    }

    public PdbAtom getAtom(String atom_name) {
        return (PdbAtom) atom_name_map.get(atom_name);
    }

    public int getAtomCount() {
        return atoms.size();
    }
    
    public void addAtom(PdbAtom atom) throws Exception {
        if (atom_name_map.put(atom.getName(), atom) != null) 
            throw new Exception("duplicate atom definition: "
                    + atom.getName());
        atoms.add(atom);
    }

    public void initialize() throws Exception {}

    public double getRadius(Point3d center) {
        double radius2 = 0.0;

        for (int i = getAtomCount() - 1; i >= 0; i--) {
            double x = center.distanceSquared(getAtom(i).getPosition());

            if (x > radius2) radius2 = x;
        }
        return Math.sqrt(radius2);
    }

    public Point3d getCenter() {
        Point3d center = new Point3d();

        for (int i = getAtomCount() - 1; i >= 0; i--) 
            center.add(getAtom(i).getPosition());
        center.scale(1.0 / getAtomCount());
        return center;
    }
}
    
