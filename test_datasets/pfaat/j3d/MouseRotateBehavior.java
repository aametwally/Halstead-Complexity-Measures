package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import java.awt.event.*;
import javax.vecmath.*;


/** MouseRotate Behavior
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class MouseRotateBehavior extends MouseNavigationBehavior {
    double x_angle, y_angle;
    double x_factor = .0075;
    double y_factor = .0075;
    Vector3d translation = new Vector3d();
    Transform3D old_trans = new Transform3D();
    Transform3D rot_x = new Transform3D();
    Transform3D rot_y = new Transform3D();

    public MouseRotateBehavior(MouseState ms, TransformGroup tg) {
        super(ms, tg);
    }

    public void processDrag(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int dx = x - last_x;
        int dy = y - last_y;

        x_angle = dy * y_factor;
        y_angle = dx * x_factor;

        rot_x.rotX(x_angle);
        rot_y.rotY(y_angle);
		    
        tg.getTransform(old_trans);
        Matrix4d mat = new Matrix4d();

        // Remember old matrix
        old_trans.get(mat);
        // Translate to origin
        old_trans.setTranslation(new Vector3d(0.0, 0.0, 0.0));
        if (invert) {
            old_trans.mul(old_trans, rot_x);
            old_trans.mul(old_trans, rot_y);
        } else {
            old_trans.mul(rot_x, old_trans);
            old_trans.mul(rot_y, old_trans);
        }

        // Set old translation back
        translation.set(mat.m03, mat.m13, mat.m23);
        old_trans.setTranslation(translation);

        // Update xform
        tg.setTransform(old_trans);
        // notify lsiteners
        notifyListeners(old_trans);
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
