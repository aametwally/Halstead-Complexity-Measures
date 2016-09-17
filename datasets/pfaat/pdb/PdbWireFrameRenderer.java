package com.neogenesis.pfaat.pdb;


import javax.media.j3d.*;
import java.util.*;


/**
 * Wireframe rendering.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:28 $ */
public class PdbWireFrameRenderer implements PdbAtomContainerRenderer {
    private float line_width;
    private PdbSpaceFillRenderer single_mol_renderer;
    
    public PdbWireFrameRenderer(float line_width) {
        this.line_width = line_width;
        single_mol_renderer = new PdbSpaceFillRenderer();
    }
    
    public void render(PdbAtomContainer container,
        PdbColorScheme color_scheme,
        BranchGroup branch,
        List atom_list) {
        Set which_bonds = new HashSet();
        Set which_atoms = new HashSet();

        for (int i = container.getAtomCount() - 1; i >= 0; i--) {
            PdbAtom atom = container.getAtom(i);

            for (int j = atom.getBondCount() - 1; j >= 0; j--) {
                if (container.getAtom(atom.getBondedAtom(j).getName()) 
                    != null)
                    which_bonds.add(atom.getBond(j));
            }
        }
        if (which_bonds.size() > 0) {
            LineArray line = new LineArray(2 * which_bonds.size(), 
                    LineArray.COORDINATES 
                    | LineArray.COLOR_3);
            int count = 0;

            for (Iterator j = which_bonds.iterator(); j.hasNext();) {
                PdbBond bond = (PdbBond) j.next();
                PdbAtom first = bond.getFirst();
                PdbAtom second = bond.getSecond();

                which_atoms.add(first);
                which_atoms.add(second);
                line.setCoordinate(count * 2, first.getPosition());
                line.setCoordinate(count * 2 + 1, second.getPosition());
                line.setColor(count * 2, color_scheme.color(first));
                line.setColor(count * 2 + 1, color_scheme.color(second));
                count++;
            }
            Appearance app = PdbAppearances.getLineAppearance(line_width);

            branch.addChild(new Shape3D(line, app));
            atom_list.addAll(which_atoms);
        }
        // Jason doens't like spherical ball rendering
        // else 
        // single_mol_renderer.render(container, 
        // color_scheme,
        // branch,
        // atom_list);
    }

}
