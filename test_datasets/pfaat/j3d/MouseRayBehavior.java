package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import java.awt.event.*;
import java.util.*;
import javax.vecmath.*;


/**
 * Basic class for support generating rays from mouseclicks 
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class MouseRayBehavior extends BasicMouseBehavior {
    private Set listeners = new HashSet();
    private BasicCanvas canvas;
    private Transform3D motion = new Transform3D();
    private Transform3D root_trans = new Transform3D();
    private Point3d eye_pos = new Point3d();
    private Point3d mouse_pos = new Point3d();
    private Vector3d mouse_vec = new Vector3d();
    private PickRay ray1 = new PickRay();
    private PickRay ray2 = new PickRay();
    private PickRay ray3 = new PickRay();
    private PickRay ray4 = new PickRay();
    private int last_x = 0;
    private int last_y = 0;
    private int region_threshold;

    public MouseRayBehavior(MouseState ms, BasicCanvas canvas, 
        int region_threshold) {
        super(ms, true, false, true);
        this.region_threshold = region_threshold;
        this.canvas = canvas;
    }

    public void processPress(MouseEvent e) {
        last_x = e.getX();
        last_y = e.getY();
    }

    private void initializePickRay() {
        canvas.getImagePlateToVworld(motion);
        canvas.getRootTransformGroup().getTransform(root_trans);
        root_trans.invert();
    }

    private void computePickRay(int x, int y, PickRay ray) {
        canvas.getPixelLocationInImagePlate(x, y, mouse_pos);
        if (canvas.getView().getProjectionPolicy() ==
            View.PARALLEL_PROJECTION) {
            // Correct for the parallel projection: keep the eye's z
            // coordinate, but make x,y be the same as the mouse, this
            // simulates the eye being at "infinity"
            eye_pos.x = mouse_pos.x;
            eye_pos.y = mouse_pos.y;
        } else
            canvas.getCenterEyeInImagePlate(eye_pos);

        motion.transform(eye_pos);
        motion.transform(mouse_pos);
        mouse_vec.sub(mouse_pos, eye_pos);
        // mouse_vec.normalize();
	
        // adjust by scene transformation
        root_trans.transform(mouse_vec);
        root_trans.transform(eye_pos);
        ray.set(eye_pos, mouse_vec);
    }

    public void processRelease(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        initializePickRay();
        if (region_threshold < 0 
            || (Math.abs(x - last_x) < region_threshold 
                && Math.abs(y - last_y) < region_threshold)) {
            computePickRay(x, y, ray1);
            for (Iterator i = listeners.iterator(); i.hasNext();) {
                MouseRayListener listener = (MouseRayListener) i.next();

                listener.rayFired(ray1);
            }	    
        } else {
            int min_x, min_y, max_x, max_y;

            if (x < last_y) {
                min_x = x;
                max_x = last_x;
            } else {
                min_x = last_x;
                max_x = x;
            }
            if (y < last_y) {
                min_y = y;
                max_y = last_y;
            } else {
                min_y = last_y;
                max_y = y;
            }

            computePickRay(min_x, min_y, ray1);
            computePickRay(max_x, min_y, ray2);
            computePickRay(max_x, max_y, ray3);
            computePickRay(min_x, max_y, ray4);
            for (Iterator i = listeners.iterator(); i.hasNext();) {
                MouseRayListener listener = (MouseRayListener) i.next();

                listener.rayFired(ray1, ray2, ray3, ray4);
            }	    
        }
    }

    /** add a callback which get notified each time the transofrm is modified
     */
    public void addListener(MouseRayListener listener) {
        listeners.add(listener);
    }

    /** remove callback from the listners list
     */
    public void removeListener(MouseRayListener listener) {
        listeners.remove(listener);
    }

}
