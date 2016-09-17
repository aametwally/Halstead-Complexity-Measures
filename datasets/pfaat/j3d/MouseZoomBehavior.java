package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import java.awt.event.*;
import javax.vecmath.*;


/** Mouse Zoom Behavior
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ 
 */
public class MouseZoomBehavior extends MouseNavigationBehavior {
    double z_factor = 0.8;
    Vector3d translation = new Vector3d();
    Transform3D trans = new Transform3D();

    public MouseZoomBehavior(MouseState ms, TransformGroup tg) {
        super(ms, tg);
    }

    public void processDrag(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int dx = x - last_x;
        int dy = y - last_y;

        translation.z = dy * z_factor;
        trans.setTranslation(translation);
        updateTransform(trans);
        setLastXY(x, y);
    }

    /**
     * Return the z-axis movement multipler.
     **/
    public double getFactor() {
        return z_factor;
    }

    /**
     * Set the z-axis movement multipler with factor.
     **/
    public void setFactor(double factor) {
        z_factor = factor;
    }
}
