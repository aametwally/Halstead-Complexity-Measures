package com.neogenesis.pfaat.j3d;


import java.awt.event.MouseEvent;
import javax.media.j3d.PickRay;


/** Interface for callbacks from MouseRayBehaviors
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public interface MouseRayListener {
    public void rayFired(PickRay ray);
    public void rayFired(PickRay ray1, 
        PickRay ray2,
        PickRay ray3,
        PickRay ray4);
}
