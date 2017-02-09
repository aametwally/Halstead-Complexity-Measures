package com.neogenesis.pfaat.pdb;


import javax.media.j3d.*;
import java.util.*;

import com.neogenesis.pfaat.j3d.*;


/**
 * Space fill rendering.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:28 $ */
public class PdbSpaceFillRenderer implements PdbAtomContainerRenderer {
    private float fixed_radius;

    public PdbSpaceFillRenderer() {
        fixed_radius = -1.0f;
    }

    public PdbSpaceFillRenderer(float fixed_radius) {
        this.fixed_radius = fixed_radius;
    }

    public void render(PdbAtomContainer container,
        PdbColorScheme color_scheme,
        BranchGroup branch,
        List atom_list) {
        int lod = container.getAtomCount() > 10 ? 5 : 8;

        for (int i = container.getAtomCount() - 1; i >= 0; i--) {
            PdbAtom atom = container.getAtom(i);
            Appearance appearance = 
                PdbAppearances.getShinyAppearance(color_scheme.color(atom));
            float radius = fixed_radius > 0.0 
                ? fixed_radius 
                : atom.getVanDerWaalsRadius();

            branch.addChild(new SphereQuad(atom.getPosition(),
                    fixed_radius,
                    lod,
                    appearance));
            atom_list.add(atom);
        }
    }
}
