package com.neogenesis.pfaat.pdb;


import java.util.List;
import javax.media.j3d.BranchGroup;


/**
 * A renderer for a group of atoms.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:29 $ */
public interface PdbAtomContainerRenderer {
    public void render(PdbAtomContainer container,
        PdbColorScheme color_scheme,
        BranchGroup bg,
        List atom_list) 
        throws Exception;
}
