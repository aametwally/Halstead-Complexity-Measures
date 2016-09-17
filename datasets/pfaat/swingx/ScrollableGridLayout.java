package com.neogenesis.pfaat.swingx;


import java.awt.*;
import java.util.*;
import javax.swing.*;


/**
 * LayoutManager which is like a grid, but fixes the sizes of certain
 * rows and columns to their preferred size.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class ScrollableGridLayout implements LayoutManager2 {
    // gap between rows, columns
    private int hgap, vgap;
    // number of rows, columns
    private int width, height;
    // which rows, columns are fixed size
    private boolean[] fix_size_row, fix_size_col;
    // map of where components appear
    private Map comp_map;
    // the target container
    private Container target;
    // sizes of children
    private SizeRequirements[] x_children, y_children;
    private SizeRequirements x_total, y_total;

    // constructor
    public ScrollableGridLayout(Container target,
        int width, int height, 
        int hgap, int vgap) {
        this.target = target;
        this.width = width;
        this.height = height;
        fix_size_row = new boolean[height];
        Arrays.fill(fix_size_row, false);
        fix_size_col = new boolean[width];
        Arrays.fill(fix_size_col, false);
        this.hgap = hgap;
        this.vgap = vgap;
        comp_map = new HashMap(2 * width * height + 1);
        x_children = y_children = null;
        x_total = y_total = null;
    }

    // utility
    private void checkContainer(Container target) {
        if (target != this.target)
            throw new RuntimeException("bound to incorrect container");
    }

    // mutators
    public void setRowFixed(int row, boolean b) {
        fix_size_row[row] = b;
    }

    public void setColumnFixed(int col, boolean b) {
        fix_size_col[col] = b;
    }

    // set a component position
    public void setPosition(Component comp, int x, int y) {
        comp_map.put(comp, new Point(x, y));
        x_children = y_children = null;
    }

    // LayoutManager2 interface
    public float getLayoutAlignmentX(Container target) {
        return 0.0f;
    }

    public float getLayoutAlignmentY(Container target) {
        return 0.0f;
    }

    public void invalidateLayout(Container target) {
        checkContainer(target);
        x_children = null;
        y_children = null;
        x_total = null;
        y_total = null;
    }

    public void addLayoutComponent(Component comp, Object constraints) {
        if (comp_map.get(comp) == null)
            throw new RuntimeException("component with unknown "
                    + "location added");
    }

    public void addLayoutComponent(String name, Component comp) {
        if (comp_map.get(comp) == null)
            throw new RuntimeException("component with unknown "
                    + "location added");
    }
    
    public void removeLayoutComponent(Component comp) { 
        comp_map.remove(comp);
    }

    // check that thew row/column sizes have been calculated
    private void checkRequests() {
        if (x_children == null || y_children == null) {
            x_children = new SizeRequirements[width];
            for (int i = x_children.length - 1; i >= 0; i--)
                x_children[i] = new SizeRequirements(0, 0, 0, 0.0f);
            y_children = new SizeRequirements[height];
            for (int i = y_children.length - 1; i >= 0; i--)
                y_children[i] = new SizeRequirements(0, 0, 0, 0.0f);

            for (int i = target.getComponentCount() - 1; i >= 0; i--) {
                Component comp = target.getComponent(i);

                if (!comp.isVisible()) 
                    continue;

                Point pos = (Point) comp_map.get(comp);

                if (pos == null)
                    throw new RuntimeException("component " + comp.toString()
                            + "with unknown "
                            + "location present");
                int x = pos.x;
                int y = pos.y;
                Dimension minimum_d = comp.getMinimumSize();
                Dimension preferred_d = comp.getPreferredSize();
                Dimension maximum_d = comp.getMaximumSize();

                if (fix_size_col[x]) 
                    maximum_d.width = minimum_d.width = preferred_d.width;
                if (fix_size_row[y])
                    maximum_d.height = minimum_d.height = preferred_d.height;

                if (minimum_d.width > x_children[x].minimum)
                    x_children[x].minimum = minimum_d.width;
                if (minimum_d.height > y_children[y].minimum)
                    y_children[y].minimum = minimum_d.height;
                if (preferred_d.width > x_children[x].preferred)
                    x_children[x].preferred = preferred_d.width;
                if (preferred_d.height > y_children[y].preferred)
                    y_children[y].preferred = preferred_d.height;
                if (maximum_d.width > x_children[x].maximum)
                    x_children[x].maximum = maximum_d.width;
                if (maximum_d.height > y_children[y].maximum)
                    y_children[y].maximum = maximum_d.height;		
            }

            x_total = SizeRequirements.getTiledSizeRequirements(x_children);
            y_total = SizeRequirements.getTiledSizeRequirements(y_children);
        }
    }

    // preferred size
    public Dimension preferredLayoutSize(Container target) {
        checkContainer(target);
        checkRequests();
	
        Dimension size = new Dimension(x_total.preferred, y_total.preferred);
        Insets insets = target.getInsets();

        size.width = (int) Math.min((long) size.width 
                    + (long) insets.left 
                    + (long) insets.right, 
                    Integer.MAX_VALUE);
        size.height = (int) Math.min((long) size.height 
                    + (long) insets.top 
                    + (long) insets.bottom, 
                    Integer.MAX_VALUE);
        return size;
    }

    // minimum size
    public Dimension minimumLayoutSize(Container target) {
        checkContainer(target);
        checkRequests();
	
        Dimension size = new Dimension(x_total.minimum, y_total.minimum);
        Insets insets = target.getInsets();

        size.width = (int) Math.min((long) size.width 
                    + (long) insets.left 
                    + (long) insets.right, 
                    Integer.MAX_VALUE);
        size.height = (int) Math.min((long) size.height 
                    + (long) insets.top 
                    + (long) insets.bottom, 
                    Integer.MAX_VALUE);
        return size;
    }

    // maximum size
    public Dimension maximumLayoutSize(Container target) {
        checkContainer(target);
        checkRequests();
	
        Dimension size = new Dimension(x_total.maximum, y_total.maximum);
        Insets insets = target.getInsets();

        size.width = (int) Math.min((long) size.width 
                    + (long) insets.left 
                    + (long) insets.right, 
                    Integer.MAX_VALUE);
        size.height = (int) Math.min((long) size.height 
                    + (long) insets.top 
                    + (long) insets.bottom, 
                    Integer.MAX_VALUE);
        return size;
    }

    // layout the container
    public void layoutContainer(Container target) {
        checkContainer(target);
        checkRequests();

        // determine the child placements
        int[] x_offsets = new int[width];
        int[] x_spans = new int[width];
        int[] y_offsets = new int[height];
        int[] y_spans = new int[height];
        Dimension alloc = target.getSize();
        Insets in = target.getInsets();

        alloc.width -= in.left + in.right;
        alloc.height -= in.top + in.bottom;
        SizeRequirements.calculateTiledPositions(alloc.width, 
            x_total,
            x_children, 
            x_offsets,
            x_spans);
        SizeRequirements.calculateTiledPositions(alloc.height, 
            y_total,
            y_children, 
            y_offsets,
            y_spans);

        for (int i = target.getComponentCount() - 1; i >= 0; i--) {
            Component comp = target.getComponent(i);
            Point pos = (Point) comp_map.get(comp);

            if (pos == null)
                throw new RuntimeException("component with unknown "
                        + "location present");
            int x = pos.x;
            int y = pos.y;

            comp.setBounds((int) Math.min((long) in.left 
                    + (long) x_offsets[x], 
                    Integer.MAX_VALUE),
                (int) Math.min((long) in.top 
                    + (long) y_offsets[y], 
                    Integer.MAX_VALUE),
                x_spans[x], 
                y_spans[y]);
        }
    }
}
