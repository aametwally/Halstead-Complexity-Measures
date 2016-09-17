package com.neogenesis.pfaat.j3d;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;


/**
 * Basic infrastructure for handling mouse events in J3D
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class BasicMouseBehavior extends Behavior {
    protected MouseState ms;
    protected WakeupCriterion[] mouse_events;
    protected WakeupOr mouse_criterion;

    /**
     * Create a basic mouse behavior is triggered by MouseState ms
     */
    public BasicMouseBehavior(MouseState ms) {
        this(ms, true, true, true);
    }

    /**
     * Create a mouse behavior with more specific wakeup criterea.
     */
    public BasicMouseBehavior(MouseState ms,
        boolean wakeup_on_press,
        boolean wakeup_on_drag,
        boolean wakeup_on_release) {
        this.ms = ms;
        ArrayList mouse_event_list = new ArrayList(3);

        if (wakeup_on_press)
            mouse_event_list.add(new 
                WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED));
        if (wakeup_on_drag)
            mouse_event_list.add(new 
                WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED));
        if (wakeup_on_release)
            mouse_event_list.add(new 
                WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED));
        mouse_events = new WakeupCriterion[mouse_event_list.size()];
        mouse_events = 
                (WakeupCriterion[]) mouse_event_list.toArray(mouse_events);
        mouse_criterion = new WakeupOr(mouse_events);
    }

    /**
     * Sets the MouseState trigger
     */
    public void setMouseStateTrigger(MouseState ms) {
        this.ms = ms;
    }

    public void processStimulus(Enumeration criteria) {
        WakeupCriterion wakeup;
        AWTEvent[] events;
        int id;

        while (criteria.hasMoreElements()) {
            wakeup = (WakeupCriterion) criteria.nextElement();
            if (wakeup instanceof WakeupOnAWTEvent) {
                events = ((WakeupOnAWTEvent) wakeup).getAWTEvent();
                for (int i = 0; i < events.length; i++) {
                    MouseEvent event = (MouseEvent) events[i];

                    if (matchesTrigger(event)) {
                        id = event.getID();
                        if (id == MouseEvent.MOUSE_PRESSED)
                            processPress(event);
                        if (id == MouseEvent.MOUSE_DRAGGED)
                            processDrag(event);
                        if (id == MouseEvent.MOUSE_RELEASED)
                            processRelease(event);
                    }
                }
            }
        }
        wakeupOn(mouse_criterion);
    }

    public boolean matchesTrigger(MouseEvent e) {
        return ms.isCompatible(e);
    }

    public void initialize() {
        wakeupOn(mouse_criterion);
    }

    public void processPress(MouseEvent e) {}

    public void processDrag(MouseEvent e) {}

    public void processRelease(MouseEvent e) {}

}
