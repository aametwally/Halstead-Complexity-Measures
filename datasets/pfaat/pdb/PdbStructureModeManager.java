package com.neogenesis.pfaat.pdb;


import java.awt.event.ActionListener;

import com.neogenesis.pfaat.swingx.*;


/**
 * Manage protein display.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:28 $ */
public class PdbStructureModeManager extends GroupManager {
    public static final String SPACE_FILL = "Space Fill";
    public static final String WIREFRAME = "Wireframe";
    public static final String BACKBONE = "Backbone";
    public static final String RIBBON = "Ribbon";
    
    public PdbStructureModeManager(String initial, 
        ActionListener action_listener) {
        super("Structure Display Mode", action_listener);
        addItem(SPACE_FILL, new PdbSpaceFillRenderer());
        addItem(WIREFRAME, new PdbWireFrameRenderer(1.0f));
        addItem(BACKBONE, new PdbBackBoneRenderer());
        addItem(RIBBON, new PdbRibbonRenderer());
        setSelectionKey(initial);
    }
}
