package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;


/** Interface for callbacks from MouseNavigationBehaviors
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */

public interface MouseNavigationListener {
    public void transformChanged(Transform3D trans);
}
