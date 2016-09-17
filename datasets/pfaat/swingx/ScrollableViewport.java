package com.neogenesis.pfaat.swingx;


import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 * JViewport that can sync (horizontally or vertically) to scrollbars
 * or other viewports.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class ScrollableViewport extends JViewport {
    private JScrollBar hsb, vsb;
    private ScrollableViewport hmaster, vmaster;

    public ScrollableViewport() {
        super();
        addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    syncScrollbar();
                }
            }
        );
    }

    public void setHorizontalViewport(ScrollableViewport hmaster) {
        this.hmaster = hmaster;
        hmaster.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    syncViewport();
                }
            }
        );	
    }

    public void setVerticalViewport(ScrollableViewport vmaster) {
        this.vmaster = vmaster;
        vmaster.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    syncViewport();
                }
            }
        );	
    }

    public void setHorizontalScrollbar(JScrollBar hsb) {
        this.hsb = hsb;
        hsb.getModel().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    BoundedRangeModel model 
                        = (BoundedRangeModel) (e.getSource());
                    Point p = getViewPosition();

                    p.x = model.getValue();
                    setViewPosition(p);
                }
            }
        );
        syncScrollbar();
    }
			     
    public void setVerticalScrollbar(JScrollBar vsb) {
        this.vsb = vsb;
        vsb.getModel().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    BoundedRangeModel model 
                        = (BoundedRangeModel) (e.getSource());
                    Point p = getViewPosition();

                    p.y = model.getValue();
                    setViewPosition(p);
                }
            }
        );
        syncScrollbar();
    }
    
    private void syncViewport() {
        if (vmaster == null && hmaster == null)
            return;
	
        Point p = getViewPosition();

        if (hmaster != null)
            p.x = hmaster.getViewPosition().x;	
        if (vmaster != null)
            p.y = vmaster.getViewPosition().y;
        setViewPosition(p);
    }

    private void syncScrollbar() {
        if (vsb == null && hsb == null) 
            return;

        Dimension extentSize = getExtentSize();
        Dimension viewSize = getViewSize();
        Point viewPosition = getViewPosition();
        Component view = getView();

        if (vsb != null) {
            int extent = extentSize.height;
            int max = viewSize.height;
            int value = Math.max(0, Math.min(viewPosition.y, max - extent));

            vsb.setValues(value, extent, 0, max);
            if (view instanceof Scrollable) {
                Scrollable s = (Scrollable) view;
                Rectangle viewRect = new Rectangle(viewPosition, extentSize);
                int ui = 
                    s.getScrollableUnitIncrement(viewRect,
                        SwingConstants.VERTICAL,
                        1);

                vsb.setUnitIncrement(ui);
                int bi = 
                    s.getScrollableBlockIncrement(viewRect,
                        SwingConstants.VERTICAL,
                        1);

                vsb.setBlockIncrement(bi);
            }
        }
        if (hsb != null) {
            int extent = extentSize.width;
            int max = viewSize.width;
            int value = Math.max(0, Math.min(viewPosition.x, max - extent));

            hsb.setValues(value, extent, 0, max);
            if (view instanceof Scrollable) {
                Scrollable s = (Scrollable) view;
                Rectangle viewRect = new Rectangle(viewPosition, extentSize);
                int ui = 
                    s.getScrollableUnitIncrement(viewRect,
                        SwingConstants.HORIZONTAL,
                        1);

                hsb.setUnitIncrement(ui);
                int bi = 
                    s.getScrollableBlockIncrement(viewRect,
                        SwingConstants.HORIZONTAL,
                        1);

                hsb.setBlockIncrement(bi);
            }	
        }
    }
}

