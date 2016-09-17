package com.neogenesis.pfaat.pdb;


import javax.media.j3d.*;
import java.util.*;
import javax.vecmath.*;

import com.neogenesis.pfaat.j3d.*;


/**
 * Ribbon style renderer.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:28 $ */
public class PdbRibbonRenderer implements PdbAtomContainerRenderer {
    private PdbWireFrameRenderer pathological_renderer = 
        new PdbWireFrameRenderer(3.0f);
    private static final double RIBBON_HELIX_WIDTH = 2.0;
    private static final double RIBBON_SHEET_WIDTH = 3.0;
    private static final double RIBBON_DEFAULT_WIDTH = 0.75;

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
                    + "for ribbon rendering");
    }

    private void renderChain(PdbChain chain,
        PdbColorScheme color_scheme, 
        BranchGroup branch, 
        List atom_list) 
        throws Exception {
        Appearance app = PdbAppearances.getShinyAppearance();
        ArrayList bb = new ArrayList();
        List temp_atom_list = new ArrayList();
        Set atom_set = new HashSet();
        ArrayList ribbon_bb = new ArrayList();

        for (int i = 0; i < chain.getResidueCount(); i++) {
            PdbResidue r = chain.getResidue(i);
            PdbAtom c_alpha = r.getByLocation("CA");

            if (c_alpha == null) {
                branch.addChild(createRibbonCurve(bb, color_scheme, 
                        app, chain));
                pathological_renderer.render(r, 
                    color_scheme,
                    branch,
                    temp_atom_list);
                bb.clear();
            } else {
                bb.add(c_alpha);
                atom_set.add(c_alpha);
                String ss = chain.getSecondaryStructure(r);

                if (ss == PdbChain.SHEET || ss == PdbChain.HELIX) {
                    // draw ribbon up to now
                    branch.addChild(createRibbonCurve(bb, color_scheme, 
                            app, chain));
                    // set up for continuation
                    bb.clear();

                    // draw arrow for strand
                    ribbon_bb.clear();
                    ribbon_bb.add(c_alpha);
                    while (++i < chain.getResidueCount()) {
                        PdbResidue next = chain.getResidue(i);

                        if (chain.getSecondaryStructure(next) != ss) {
                            bb.add(ribbon_bb.get(ribbon_bb.size() - 1));
                            i--;
                            break;
                        } else {
                            PdbAtom c_a = next.getByLocation("CA");

                            if (c_a != null) {
                                ribbon_bb.add(c_a);
                                atom_set.add(c_a);
                            }
                        }
                    }
                    if (ss == PdbChain.SHEET)
                        branch.addChild(createArrowCurve(ribbon_bb, 
                                color_scheme, 
                                app,
                                RIBBON_SHEET_WIDTH));
                    else
                        branch.addChild(createArrowCurve(ribbon_bb, 
                                color_scheme, 
                                app,
                                RIBBON_HELIX_WIDTH));
                }
            }
            branch.addChild(createRibbonCurve(bb, color_scheme, app, chain));
        }

        atom_set.addAll(temp_atom_list);
        atom_list.addAll(atom_set);
    }

    private Shape3D createRibbonCurve(java.util.List bb,
        PdbColorScheme color_scheme, 
        Appearance app,
        PdbChain chain) 
        throws Exception {
        int num_bb = bb.size();

        if (num_bb < 2) return new Shape3D();
	
        Color3f[] colors = new Color3f[num_bb];
        Point3d[] points = new Point3d[num_bb];
        double[] widths = new double[num_bb];
        double[] orient = new double[num_bb];

        // orient = 0 means calc ribbon edge using normal
        // orient = 1 means calc ribbon edge using binormal
	
	
        for (int k = 0; k < num_bb; k++) {
            PdbAtom atom = (PdbAtom) bb.get(k);

            if (atom == null) 
                throw new Exception("PdbRibbonRenderer: empty atom in list: " 
                        + bb.toString());
            points[k] = atom.getPosition();
            colors[k] = color_scheme.color(atom);
            PdbResidue residue = chain.getResidue(atom);

            if (chain.getSecondaryStructure(residue) == PdbChain.HELIX) {
                widths[k] = RIBBON_HELIX_WIDTH;
                orient[k] = 1.0;
            } else {
                widths[k] = RIBBON_DEFAULT_WIDTH;
                orient[k] = 1.0;
            }
        }
        int knots = 8;

        return new Ribbon(points, colors, widths, orient, knots, app, false);
    }

    private Shape3D createArrowCurve(java.util.List bb,
        PdbColorScheme color_scheme, 
        Appearance app, 
        double width) {
        int num_bb = bb.size();

        if (num_bb < 2) return new Shape3D();
	
        Color3f[] colors = new Color3f[num_bb];
        Point3d[] points = new Point3d[num_bb];
        double[] widths = new double[num_bb];
        double[] orient = new double[num_bb];

        // orient = 0 means calc ribbon edge using normal
        // orient = 1 means calc ribbon edge using binormal
	
	
        for (int k = 0; k < num_bb; k++) {
            PdbAtom atom = (PdbAtom) bb.get(k);

            points[k] = atom.getPosition();
            colors[k] = color_scheme.color(atom);
            widths[k] = RIBBON_SHEET_WIDTH;
            orient[k] = 1.0;
        }
        int knots = 8;

        return new Ribbon(points, colors, widths, orient, knots, app, true);
    }

}
