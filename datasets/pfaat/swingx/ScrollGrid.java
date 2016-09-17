package com.neogenesis.pfaat.swingx;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


/**
 * Like JScrollPane, but has a bottom component.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class ScrollGrid extends JScrollPane 
    implements ChangeListener {

    /**
     * The column footer child.  Default is <code>null</code>.
     * @see #setColumnFooter
     */
    protected JViewport columnFooter;

    private static final String COLUMN_FOOTER = "COLUMN_FOOTER";

    public ScrollGrid() {
        super();
        setLayout(new ScrollGridLayout());
    }

    /**
     * Returns the bounds of the viewport's border.
     *
     * @return a <code>Rectangle</code> object specifying the viewport border
     */
    public Rectangle getViewportBorderBounds() {
        Rectangle borderR = super.getViewportBorderBounds();

        /* If there's a visible column footer remove the space it 
         * needs from the bottem of borderR.  
         */
        JViewport colFoot = getColumnFooter();

        if ((colFoot != null) && (colFoot.isVisible())) {
            borderR.height -= colFoot.getHeight();
        }
        return borderR;
    }

    /**
     * Returns the column footer.
     * @return the <code>columnFooter</code> property
     * @see #setColumnFooter
     */
    public JViewport getColumnFooter() {
        return columnFooter;
    }

    /**
     * Removes the old columnFooter, if it exists.  If the new columnFooter
     * isn't <code>null</code>, sync the x coordinate of the its viewPosition 
     * with the viewport (if there is one) and then add it to the scrollpane.
     * <p>
     * Most applications will find it more convenient to use 
     * <code>setRowFooterView</code>
     * to add a row footer component and its viewport to the scrollpane.
     * 
     * @see #getColumnFooter
     * @see #setColumnFooterView
     * 
     * @beaninfo
     *        bound: true
     *  description: The column footer child for this scrollpane
     *    attribute: visualUpdate true
     */
    public void setColumnFooter(JViewport columnFooter) {
        JViewport old = getColumnFooter();

        this.columnFooter = columnFooter;	
        if (columnFooter != null) {
            add(columnFooter, COLUMN_FOOTER);
        } else if (old != null) {
            remove(old);
        }
        firePropertyChange("columnFooter", old, columnFooter);

        revalidate();
        repaint();
    }

    /**
     * Creates a column-footer viewport if necessary, sets
     * its view, and then adds the column-footer viewport
     * to the scrollpane.  For example:
     * <pre>
     * JScrollPane scrollpane = new JScrollPane();
     * scrollpane.setViewportView(myBigComponentToScroll);
     * scrollpane.setColumnFooterView(myBigComponentsColumnFooter);
     * </pre>
     * 
     * @see #setColumnFooter
     * @see JViewport#setView
     * 
     * @param view the component to display as the column footer
     */
    public void setColumnFooterView(Component view) {
        if (getColumnFooter() == null) {
            setColumnFooter(createViewport());
        }
        getColumnFooter().setView(view);
    }

    // have the footer track the main viewport
    public void setViewport(JViewport viewport) {
        JViewport old = getViewport();

        if (old != null)
            old.removeChangeListener(this);
        if (viewport != null)
            viewport.addChangeListener(this);
        super.setViewport(viewport);
    }

    public void stateChanged(ChangeEvent e) {
        JViewport colFoot = getColumnFooter();

        if (colFoot != null) {
            Point p = colFoot.getViewPosition();

            p.x = viewport.getViewPosition().x;
            colFoot.setViewPosition(p);
        }
    }

    /**
     * Returns a string representation of this <code>ScrollGrid</code>.
     * This method 
     * is intended to be used only for debugging purposes, and the 
     * content and format of the returned string may vary between      
     * implementations. The returned string may be empty but may not 
     * be <code>null</code>.
     * 
     * @return  a string representation of this <code>JScrollPane</code>.
     */
    protected String paramString() {
        return super.paramString() + ",columnFooter=" 
            + (columnFooter != null ? columnFooter.toString() : "");
    }

    public static class ScrollGridLayout extends ScrollPaneLayout {
        protected JViewport colFoot;

        public void syncWithScrollPane(JScrollPane sp) {
            super.syncWithScrollPane(sp);
            colFoot = ((ScrollGrid) sp).getColumnFooter();
        }

        /**
         * Adds the specified component to the layout. The layout is
         * identified using one of:
         * <ul>
         * <li>JScrollPane.VIEWPORT
         * <li>JScrollPane.VERTICAL_SCROLLBAR
         * <li>JScrollPane.HORIZONTAL_SCROLLBAR
         * <li>JScrollPane.ROW_HEADER
         * <li>JScrollPane.COLUMN_HEADER
         * <li>JScrollPane.LOWER_LEFT_CORNER
         * <li>JScrollPane.LOWER_RIGHT_CORNER
         * <li>JScrollPane.UPPER_LEFT_CORNER
         * <li>JScrollPane.UPPER_RIGHT_CORNER
         * </ul>
         *
         * @param s the component identifier
         * @param comp the the component to be added
         */
        public void addLayoutComponent(String s, Component c) {
            if (s.equals(COLUMN_FOOTER)) {
                colFoot = (JViewport) addSingletonComponent(colFoot, c);
            } else
                super.addLayoutComponent(s, c);
        }

        /**
         * Removes the specified component from the layout.
         *   
         * @param c the component to remove
         */
        public void removeLayoutComponent(Component c) {
            if (c == colFoot) {
                colFoot = null;
            } else
                super.removeLayoutComponent(c);
        }

        /**
         * Returns the JViewport object that is the column header.
         * @return the JViewport object that is the column header
         * @see JScrollPane#getColumnFooter
         */
        public JViewport getColumnFooter() {
            return colFoot;
        }

        public Dimension preferredLayoutSize(Container parent) {
            Dimension d = super.preferredLayoutSize(parent);

            if ((colFoot != null) && colFoot.isVisible()) 
                d.height += colFoot.getPreferredSize().height;
            return d;
        }

        public Dimension minimumLayoutSize(Container parent) {
            Dimension d = super.minimumLayoutSize(parent);

            if ((colFoot != null) && colFoot.isVisible()) {
                Dimension size = colHead.getMinimumSize();

                d.width = Math.max(d.width, size.width);
                d.height += size.height;
            }
            return d;
        }

        public void layoutContainer(Container parent) {
            // this seems to be necessary
            syncWithScrollPane((JScrollPane) parent);

            /* Sync the (now obsolete) policy fields with the
             * JScrollPane.
             */
            JScrollPane scrollPane = (JScrollPane) parent;

            vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
            hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();
	    
            Rectangle availR = scrollPane.getBounds();

            availR.x = availR.y = 0;
	    
            Insets insets = parent.getInsets();

            availR.x = insets.left;
            availR.y = insets.top;
            availR.width -= insets.left + insets.right;
            availR.height -= insets.top + insets.bottom;

            /* Get the scrollPane's orientation.
             */
            boolean leftToRight = 
                scrollPane.getComponentOrientation().isLeftToRight();
	    
            /* If there's a visible column header remove the space it 
             * needs from the top of availR.  The column header is treated 
             * as if it were fixed height, arbitrary width.
             */

            Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);
	    
            if ((colHead != null) && (colHead.isVisible())) {
                int colHeadHeight = colHead.getPreferredSize().height;

                colHeadR.height = colHeadHeight; 
                availR.y += colHeadHeight;
                availR.height -= colHeadHeight;
            }

            /* If there's a visible column footer remove the space it 
             * needs from the top of availR.  The column header is treated 
             * as if it were fixed height, arbitrary width.
             */

            Rectangle colFootR = new Rectangle(0, 0, 0, 0);
	    
            if ((colFoot != null) && (colFoot.isVisible())) {
                int colFootHeight = colFoot.getPreferredSize().height;

                availR.height -= colFootHeight;
                colFootR.y = availR.y + availR.height;
                colFootR.height = colFootHeight; 
            }

            /* If there's a visible row header remove the space it needs
             * from the left or right of availR.  The row header is treated 
             * as if it were fixed width, arbitrary height.
             */

            Rectangle rowHeadR = new Rectangle(0, 0, 0, 0);
	    
            if ((rowHead != null) && (rowHead.isVisible())) {
                int rowHeadWidth = rowHead.getPreferredSize().width;

                rowHeadR.width = rowHeadWidth;
                availR.width -= rowHeadWidth;
                if (leftToRight) {
                    rowHeadR.x = availR.x;
                    availR.x += rowHeadWidth;
                } else {
                    rowHeadR.x = availR.x + availR.width;
                }
            }
	    
            /* If there's a JScrollPane.viewportBorder, remove the
             * space it occupies for availR.
             */
	    
            Border viewportBorder = scrollPane.getViewportBorder();
            Insets vpbInsets;

            if (viewportBorder != null) {
                vpbInsets = viewportBorder.getBorderInsets(parent);
                availR.x += vpbInsets.left;
                availR.y += vpbInsets.top;
                availR.width -= vpbInsets.left + vpbInsets.right;
                availR.height -= vpbInsets.top + vpbInsets.bottom;
            } else {
                vpbInsets = new Insets(0, 0, 0, 0);
            }
	    
            /* At this point availR is the space available for the viewport
             * and scrollbars. rowHeadR is correct except for its height and y
             * and colHeadR is correct except for its width and x.  Once we're 
             * through computing the dimensions  of these three parts we can 
             * go back and set the dimensions of rowHeadR.height, rowHeadR.y,
             * colHeadR.width, colHeadR.x and the bounds for the corners.
             * 
             * We'll decide about putting up scrollbars by comparing the 
             * viewport views preferred size with the viewports extent
             * size (generally just its size).  Using the preferredSize is
             * reasonable because layout proceeds top down - so we expect
             * the viewport to be layed out next.  And we assume that the
             * viewports layout manager will give the view it's preferred
             * size.  One exception to this is when the view implements 
             * Scrollable and Scrollable.getViewTracksViewport{Width,Height}
             * methods return true.  If the view is tracking the viewports
             * width we don't bother with a horizontal scrollbar, similarly
             * if view.getViewTracksViewport(Height) is true we don't bother
             * with a vertical scrollbar.
             */
	    

	    
            Component view = (viewport != null) ? viewport.getView() : null;
            Dimension viewPrefSize =  
                (view != null) ? view.getPreferredSize() 
                : new Dimension(0, 0);
	    
            Dimension extentSize = 
                (viewport != null) 
                ? viewport.toViewCoordinates(availR.getSize()) 
                : new Dimension(0, 0);
	    
            boolean viewTracksViewportWidth = false;
            boolean viewTracksViewportHeight = false;
            Scrollable sv;

            if (view instanceof Scrollable) {
                sv = (Scrollable) view;
                viewTracksViewportWidth = 
                        sv.getScrollableTracksViewportWidth();
                viewTracksViewportHeight = 
                        sv.getScrollableTracksViewportHeight();
            } else {
                sv = null;
            }

            /* If there's a vertical scrollbar and we need one, allocate
             * space for it (we'll make it visible later). A vertical 
             * scrollbar is considered to be fixed width, arbitrary height.
             */
	    
            Rectangle vsbR = new Rectangle(0, availR.y - vpbInsets.top, 0, 0);
	    
            boolean vsbNeeded;

            if (vsbPolicy == VERTICAL_SCROLLBAR_ALWAYS) {
                vsbNeeded = true;
            } else if (vsbPolicy == VERTICAL_SCROLLBAR_NEVER) {
                vsbNeeded = false;
            } else {  // vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED
                vsbNeeded = !viewTracksViewportHeight 
                        && (viewPrefSize.height > extentSize.height);
            }
	    
            if ((vsb != null) && vsbNeeded) {
                adjustForVSB(true, availR, vsbR, vpbInsets, leftToRight);
                extentSize = viewport.toViewCoordinates(availR.getSize());
            }
	
            /* If there's a horizontal scrollbar and we need one, allocate
             * space for it (we'll make it visible later). A horizontal 
             * scrollbar is considered to be fixed height, arbitrary width.
             */
	    
            Rectangle hsbR = new Rectangle(availR.x - vpbInsets.left, 0, 0, 0);
            boolean hsbNeeded;

            if (hsbPolicy == HORIZONTAL_SCROLLBAR_ALWAYS) {
                hsbNeeded = true;
            } else if (hsbPolicy == HORIZONTAL_SCROLLBAR_NEVER) {
                hsbNeeded = false;
            } else {  // hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED
                hsbNeeded = !viewTracksViewportWidth 
                        && (viewPrefSize.width > extentSize.width);
            }
	    
            if ((hsb != null) && hsbNeeded) {
                adjustForHSB(true, availR, hsbR, vpbInsets);
		
                /* If we added the horizontal scrollbar then we've implicitly 
                 * reduced  the vertical space available to the viewport. 
                 * As a consequence we may have to add the vertical scrollbar, 
                 * if that hasn't been done so already.  Ofcourse we
                 * don't bother with any of this if the vsbPolicy is NEVER.
                 */
                if ((vsb != null) && !vsbNeeded &&
                    (vsbPolicy != VERTICAL_SCROLLBAR_NEVER)) {
		    
                    extentSize = viewport.toViewCoordinates(availR.getSize());
                    vsbNeeded = viewPrefSize.height > extentSize.height;
		    
                    if (vsbNeeded) {
                        adjustForVSB(true, availR, vsbR, vpbInsets, 
                            leftToRight);
                    }
                }
            }
	    
            /* Set the size of the viewport first, and then recheck
             * the Scrollable methods. Some components base their
             * return values for the Scrollable methods on the size of
             * the Viewport, so that if we don't ask after resetting
             * the bounds we may have gotten the wrong answer.  */
	
            if (viewport != null) {
                viewport.setBounds(availR);

                if (sv != null) {
                    extentSize = viewport.toViewCoordinates(availR.getSize());
		    
                    boolean oldHSBNeeded = hsbNeeded;
                    boolean oldVSBNeeded = vsbNeeded;

                    viewTracksViewportWidth = sv.getScrollableTracksViewportWidth();
                    viewTracksViewportHeight = sv.getScrollableTracksViewportHeight();
                    if (vsb != null 
                        && vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED) {
                        boolean newVSBNeeded = !viewTracksViewportHeight &&
                            (viewPrefSize.height > extentSize.height);

                        if (newVSBNeeded != vsbNeeded) {
                            vsbNeeded = newVSBNeeded;
                            adjustForVSB(vsbNeeded, availR, vsbR, vpbInsets,
                                leftToRight);
                            extentSize = viewport.toViewCoordinates
                                    (availR.getSize());
                        }
                    }
                    if (hsb != null 
                        && hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED) {
                        boolean newHSBbNeeded = !viewTracksViewportWidth &&
                            (viewPrefSize.width > extentSize.width);

                        if (newHSBbNeeded != hsbNeeded) {
                            hsbNeeded = newHSBbNeeded;
                            adjustForHSB(hsbNeeded, availR, hsbR, vpbInsets);
                            if ((vsb != null) && !vsbNeeded &&
                                (vsbPolicy != VERTICAL_SCROLLBAR_NEVER)) {
				
                                extentSize = viewport.toViewCoordinates
                                        (availR.getSize());
                                vsbNeeded = viewPrefSize.height >
                                        extentSize.height;
				
                                if (vsbNeeded) {
                                    adjustForVSB(true, availR, vsbR, vpbInsets,
                                        leftToRight);
                                }
                            }
                        }
                    }
                    if (oldHSBNeeded != hsbNeeded ||
                        oldVSBNeeded != vsbNeeded) {
                        viewport.setBounds(availR);
                        // You could argue that we should recheck the
                        // Scrollable methods again until they stop changing,
                        // but they might never stop changing, so we stop here
                        // and don't do any additional checks.
                    }
                }
            }
	    
            /* We now have the final size of the viewport: availR.
             * Now fixup the header and scrollbar widths/heights.
             */
            vsbR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
            hsbR.width = availR.width + vpbInsets.left + vpbInsets.right;
            rowHeadR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
            rowHeadR.y = availR.y - vpbInsets.top;
            colHeadR.width = availR.width + vpbInsets.left + vpbInsets.right;
            colHeadR.x = availR.x - vpbInsets.left;
            colFootR.width = availR.width + vpbInsets.left + vpbInsets.right;
            colFootR.x = availR.x - vpbInsets.left;

            /* Set the bounds of the remaining components.  The scrollbars
             * are made invisible if they're not needed.
             */
	    
            if (rowHead != null) {
                rowHead.setBounds(rowHeadR);
            }
	    
            if (colHead != null) {
                colHead.setBounds(colHeadR);
            }

            if (colFoot != null) {
                colFoot.setBounds(colFootR);
            }

            if (vsb != null) {
                if (vsbNeeded) {
                    vsb.setVisible(true);
                    vsb.setBounds(vsbR);
                } else {
                    vsb.setVisible(false);
                }
            }
	    
            if (hsb != null) {
                if (hsbNeeded) {
                    hsb.setVisible(true);
                    hsb.setBounds(hsbR);
                } else {
                    hsb.setVisible(false);
                }
            }
	    
            if (lowerLeft != null) {
                lowerLeft.setBounds(leftToRight ? rowHeadR.x : vsbR.x,
                    hsbR.y,
                    leftToRight ? rowHeadR.width : vsbR.width,
                    hsbR.height);
            }
	    
            if (lowerRight != null) {
                lowerRight.setBounds(leftToRight ? vsbR.x : rowHeadR.x,
                    hsbR.y,
                    leftToRight ? vsbR.width : rowHeadR.width,
                    hsbR.height);
            }
	    
            if (upperLeft != null) {
                upperLeft.setBounds(leftToRight ? rowHeadR.x : vsbR.x,
                    colHeadR.y,
                    leftToRight ? rowHeadR.width : vsbR.width,
                    colHeadR.height);
            }
	    
            if (upperRight != null) {
                upperRight.setBounds(leftToRight ? vsbR.x : rowHeadR.x,
                    colHeadR.y,
                    leftToRight ? vsbR.width : rowHeadR.width,
                    colHeadR.height);
            }
        }

        /**
         * Adjusts the Rectangle <code>available</code> based on if
         * the vertical scrollbar is needed (<code>wantsVSB</code>).
         * The location of the vsb is updated in <code>vsbR</code>,
         * and the viewport border insets (<code>vpbInsets</code>) are
         * used to offset the vsb.  */
        private void adjustForVSB(boolean wantsVSB, Rectangle available,
            Rectangle vsbR, Insets vpbInsets, 
            boolean leftToRight) {
            int vsbWidth = vsb.getPreferredSize().width;

            if (wantsVSB) {
                available.width -= vsbWidth;
                vsbR.width = vsbWidth;
		
                if (leftToRight) {
                    vsbR.x = available.x + available.width + vpbInsets.right;
                } else {
                    vsbR.x = available.x - vpbInsets.left;
                    available.x += vsbWidth;
                }
            } else {
                available.width += vsbWidth;
            }
        }
	
        /**
         * Adjusts the Rectangle <code>available</code> based on if
         * the horizontal scrollbar is needed (<code>wantsHSB</code>).
         * The location of the hsb is updated in <code>hsbR</code>,
         * and the viewport border insets (<code>vpbInsets</code>) are
         * used to offset the hsb.  */
        private void adjustForHSB(boolean wantsHSB, Rectangle available,
            Rectangle hsbR, Insets vpbInsets) {
            int hsbHeight = hsb.getPreferredSize().height;

            if (wantsHSB) {
                available.height -= hsbHeight;
                hsbR.y = available.y + available.height + vpbInsets.bottom;
                hsbR.height = hsbHeight;
            } else {
                available.height += hsbHeight;
            }
        }

    }
}

