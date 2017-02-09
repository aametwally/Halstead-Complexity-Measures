package com.neogenesis.pfaat.pdb;


import javax.media.j3d.*;
import java.util.*;
import javax.vecmath.*;

import com.neogenesis.pfaat.j3d.*;


/**
 * Backbond style rendering.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public class PdbBackBoneRenderer implements PdbAtomContainerRenderer {
    private PdbWireFrameRenderer pathological_renderer = 
        new PdbWireFrameRenderer(3.0f);

    public void render(PdbAtomContainer container,
        PdbColorScheme color_scheme,
        BranchGroup branch,
        List atom_list) 
        throws Exception {
        if (container instanceof PdbStructure) {
            PdbStructure structure = (PdbStructure) container;

            for (int i = structure.getChainCount() - 1; i >= 0; i--)
                renderChain(structure.getChain(i), color_scheme,
                    branch, atom_list);
        } else if (container instanceof PdbChain)
            renderChain((PdbChain) container, color_scheme, branch, atom_list);
        else 
            throw new Exception("container type not supported "
                    + "for backbone rendering");
    }

    private void renderChain(PdbChain chain,
        PdbColorScheme color_scheme, 
        BranchGroup branch, 
        List atom_list) 
        throws Exception {
        ArrayList bb = new ArrayList();

        for (int i = 0; i < chain.getResidueCount(); i++) {
            PdbResidue r = chain.getResidue(i);
            PdbAtom c_alpha = r.getByLocation("CA");

            if (c_alpha != null) {
                bb.add(c_alpha);
                atom_list.add(c_alpha);
            } else {
                branch.addChild(createThinCurve(bb, color_scheme));
                pathological_renderer.render(r, 
                    color_scheme,
                    branch,
                    atom_list);
                bb.clear();
            }
        }
        branch.addChild(createThinCurve(bb, color_scheme));
    }
    
    private Shape3D createThinCurve(List bb, PdbColorScheme color_scheme) {
        int num_bb = bb.size();

        if (num_bb < 2) 
            return new Shape3D();
	
        Point3d[] points = new Point3d[num_bb];
        Color3f[] colors = new Color3f[num_bb];

        for (int k = 0; k < num_bb; k++) {
            PdbAtom atom = (PdbAtom) bb.get(k);

            points[k] = atom.getPosition();
            colors[k] = color_scheme.color(atom);
        }
        return new ThinCurve(points, colors, 8);
    }
}
