package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import java.awt.event.*;
import java.util.*;


/**
 * Basic class for support mouse navigation on a canvas3D
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */


public abstract class MouseNavigationBehavior extends BasicMouseBehavior {
    protected int last_x, last_y;
    TransformGroup tg;
    boolean invert = false;
    Set listeners = new HashSet();
    Transform3D old_trans = new Transform3D(); // temp storage
  
    /** Create a MouseNavigationBehavior which modifes TransformGroup tg
     */
    public MouseNavigationBehavior(MouseState ms, TransformGroup tg) {
        super(ms, true, true, false);
        this.tg = tg;
    }

    /** add a callback which get notified each time the transofrm is modified
     */
    public void addListener(MouseNavigationListener listener) {
        listeners.add(listener);
    }

    /** remove callback from the listners list
     */
    public void removeListener(MouseNavigationListener listener) {
        listeners.remove(listener);
    }

    /** Sets whether to invert the normal behavior */
    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    protected void updateTransform(Transform3D trans) {
        tg.getTransform(old_trans);
        if (invert)
            old_trans.mul(old_trans, trans);
        else
            old_trans.mul(trans, old_trans);
        tg.setTransform(old_trans);
        notifyListeners(old_trans);
    }

    protected void notifyListeners(Transform3D trans) {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            MouseNavigationListener listener = (MouseNavigationListener) i.next();

            listener.transformChanged(trans);
        }
    }

    public void  processPress(MouseEvent e) {
        setLastXY(e.getX(), e.getY());
    }

    protected void setLastXY(int x, int y) {
        last_x = x;
        last_y = y;
    }
}
