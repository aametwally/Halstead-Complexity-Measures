package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import java.awt.event.*;
import javax.vecmath.*;


/** MouseTranslation Behavior
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class MouseTranslateBehavior extends MouseNavigationBehavior {
    double x_factor = 0.2;
    double y_factor = 0.2;
    Vector3d translation = new Vector3d();
    Transform3D trans = new Transform3D();

    public MouseTranslateBehavior(MouseState ms, TransformGroup tg) {
        super(ms, tg);
    }

    public void processDrag(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int dx = x - last_x;
        int dy = y - last_y;

        translation.x = dx * x_factor;
        translation.y = -dy * y_factor;
        trans.setTranslation(translation);
        updateTransform(trans);
        setLastXY(x, y);
    }

    /**
     * Return the x-axis movement multipler.
     **/
    public double getXFactor() {
        return x_factor;
    }

    /**
     * Return the y-axis movement multipler.
     **/
    public double getYFactor() {
        return y_factor;
    }

    /**
     * Set the x-axis amd y-axis movement multipler with factor.
     **/
    public void setFactor(double factor) {
        x_factor = y_factor = factor;
    }

    /**
     * Set the x-axis amd y-axis movement multipler with xFactor and yFactor
     * respectively.
     **/
    public void setFactor(double x_factor, double y_factor) {
        this.x_factor = x_factor;
        this.y_factor = y_factor;
    }

}
