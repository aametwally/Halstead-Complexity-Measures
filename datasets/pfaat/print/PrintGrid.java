package com.neogenesis.pfaat.print;


import java.awt.*;
import java.util.*;


/*
 * A grid-layout for printing components.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:30:23 $ */
public class PrintGrid {
    // list of child components
    private ArrayList components = new ArrayList();
    // map of where components appear
    private Map comp_map = new HashMap();
    // row heights
    private int[] row_heights;
    // column_widths
    private int[] col_widths;
    // cell positions
    private int[] row_offsets, col_offsets;
    // total width, height

    // constructor
    public PrintGrid(int width, int height) {
        row_heights = new int[height];
        Arrays.fill(row_heights, 0);
        col_widths = new int[width];
        Arrays.fill(col_widths, 0);
    }

    // add a component to the grid
    public void addComponent(Object comp, int x, int y) {
        if (comp instanceof Component && !((Component) comp).isVisible())
            return;
        components.add(comp);
        Point pos = new Point(x, y);

        comp_map.put(comp, pos);
        Dimension d;

        if (comp instanceof Image) 
            d = new Dimension(((Image) comp).getWidth(null), 
                        ((Image) comp).getHeight(null));
        else
            d = ((Component) comp).getSize();
        if (d.width > col_widths[x]) {
            col_widths[x] = d.width;
            col_offsets = null;
        }
        if (d.height > row_heights[y]) {
            row_heights[y] = d.height;
            row_offsets = null;
        }
    }

    // accessors
    public int getComponentCount() {
        return components.size();
    }

    public Object getComponent(int i) {
        return (Object) components.get(i);
    }

    public Point getPosition(Object comp) {
        calcRows();
        calcColumns();
        Point grid_position = (Point) comp_map.get(comp);

        return new Point(col_offsets[grid_position.x],
                row_offsets[grid_position.y]);
    }

    public int getWidth() {
        calcColumns();
        return col_offsets[col_offsets.length - 1];
    }

    public int getHeight() {
        calcRows();
        return row_offsets[row_offsets.length - 1];
    }

    // internal functions
    private void calcRows() {
        if (row_offsets == null) {
            row_offsets = new int[row_heights.length + 1];
            row_offsets[0] = 0;
            for (int i = 1; i < row_offsets.length; i++) 
                row_offsets[i] = row_offsets[i - 1] + row_heights[i - 1];
        }
    }

    private void calcColumns() {
        if (col_offsets == null) {
            col_offsets = new int[col_widths.length + 1];
            col_offsets[0] = 0;
            for (int i = 1; i < col_offsets.length; i++)
                col_offsets[i] = col_offsets[i - 1] + col_widths[i - 1];
        }
    }

}

