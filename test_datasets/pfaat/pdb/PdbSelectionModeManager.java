package com.neogenesis.pfaat.pdb;


import java.awt.event.ActionListener;

import com.neogenesis.pfaat.swingx.*;


/**
 * Manage display modes for selection.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:28 $ */
public class PdbSelectionModeManager extends GroupManager {
    public static final String SPACE_FILL = "Space Fill";
    public static final String WIREFRAME = "Wireframe";
    
    public PdbSelectionModeManager(String initial, 
        ActionListener action_listener) {
        super("Selection Display Mode", action_listener);
        addItem(SPACE_FILL, new PdbSpaceFillRenderer());
        addItem(WIREFRAME, new PdbWireFrameRenderer(3.0f));
        setSelectionKey(initial);
    }
}
