package com.neogenesis.pfaat.j3d;


import java.awt.event.*;


/**
 * Encapsulates the basic state of a mouse event
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $
 */
public class MouseState {
    public int alt, ctrl, shift;
    public int button1, button2, button3;
    public int num_clicks;
    
    // each state can be true, false, or unspecified
    public static final int UNSPECIFIED = -1;
    public static final int FALSE = 0;
    public static final int TRUE = 1;
      
    /**
     * Creates a MouseState object
     *@param shift Specifies if shift key is pressed
     *@param ctrl Specifies if ctrl key is pressed
     *@param alt Specifies if alt key is pressed
     *@param button[1..3] Specifies which mouse button was pressed
     *@param num_clicks Specifies the number of clicks
     */
    public MouseState(int shift, int ctrl, int alt, 
        int button1, int button2, int button3,
        int num_clicks) {
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
        this.button1 = button1;
        this.button2 = button2;
        this.button3 = button3;
        this.num_clicks = num_clicks;
    }

    /**
     *Default MouseState: shift,ctrl,alt = false; num_clicks = 1;
     *which_button = 1;
     */

    public MouseState() {
        this(FALSE, FALSE, FALSE, TRUE, TRUE, TRUE, 1);
    }

    // determine if the MouseState is compatible with a MouseEvent
    public boolean isCompatible(MouseEvent e) {
        if (shift != UNSPECIFIED && (shift != FALSE) != e.isShiftDown())
            return false;
        if (ctrl != UNSPECIFIED && (ctrl != FALSE) != e.isControlDown())
            return false;
        if (alt != UNSPECIFIED && (alt != FALSE) != e.isAltDown())
            return false;
        if (num_clicks != UNSPECIFIED && num_clicks != e.getClickCount())
            return false;
        int flags = e.getModifiers();

        if (button1 != UNSPECIFIED 
            && (button1 != FALSE)
            != ((flags & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK))
            return false;
        if (button2 != UNSPECIFIED 
            && (button2 != FALSE)
            != ((flags & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK))
            return false;
        if (button3 != UNSPECIFIED 
            && (button3 != FALSE)
            != ((flags & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK))
            return false;
        return true;
    }

    public String toString() {
        return "shift=" + shift
            + ",alt=" + alt
            + ",ctrl=" + ctrl
            + ",num_clicks=" + num_clicks
            + ",button1=" + button1
            + ",button2=" + button2
            + ",button3=" + button3;
    }
}

