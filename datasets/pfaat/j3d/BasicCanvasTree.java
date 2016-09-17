package com.neogenesis.pfaat.j3d;


import javax.swing.tree.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.*;


/**
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $
 */
public class BasicCanvasTree extends JTree {
    private BasicCanvas canvas;
    private static final int font_size = 10;
    private Font default_font = new Font(null, Font.BOLD, font_size);

    /** Creates new SmartListBox */
    public BasicCanvasTree(BasicCanvas canvas, DefaultTreeModel model) {
        super(model);
        this.canvas = canvas;
        // tree preferences
        getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);
        setRootVisible(false);
        putClientProperty("JTree.lineStyle", "Angled");
        setShowsRootHandles(true);
        // only as of jdk1.3!
        // setToggleClickCount(3);

        addMouseListener(new MouseHandler());

        setCellRenderer(new CustomRenderer());
        setFont(default_font);
    }

    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();

        dim.width = (dim.width < 200) ? 200 : dim.width;
        return dim;
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    private class MouseHandler extends MouseAdapter {
        MouseState event_state, popup_state, double_click_state;
        public MouseHandler() {
            event_state = new MouseState();
            // popup trigger for windows
            popup_state = new MouseState(MouseState.FALSE,
                        MouseState.FALSE,
                        MouseState.FALSE,
                        MouseState.FALSE,
                        MouseState.FALSE,
                        MouseState.TRUE,
                        1);  
            double_click_state = new MouseState(MouseState.FALSE,
                        MouseState.FALSE,
                        MouseState.FALSE,
                        MouseState.TRUE,
                        MouseState.FALSE,
                        MouseState.FALSE,
                        2);
        }

        public void mouseClicked(MouseEvent e) {
            int clicked_row = getRowForLocation(e.getX(), e.getY());
            TreePath clicked_path = getPathForLocation(e.getX(), e.getY());

            if (e.isPopupTrigger() || popup_state.isCompatible(e)) {
                if (clicked_row != -1) {
                    setSelectionPath(clicked_path);
                    BasicCanvasNode node = (BasicCanvasNode)
                        clicked_path.getLastPathComponent();

                    node.getPopup().show(e.getComponent(), e.getX(), e.getY());
                }
            }

            if (double_click_state.isCompatible(e)) {
                if (clicked_row != -1) {
                    setSelectionPath(clicked_path);
                    BasicCanvasNode node = (BasicCanvasNode)
                        clicked_path.getLastPathComponent();

                    if (node.isLeaf())
                        node.toggleVisibility();
                }
            }
        }
    }


    private class CustomRenderer extends DefaultTreeCellRenderer {
        Color hidden_color;
        Color mixed_color;
        Font hidden_font;
        Font mixed_font;

        public CustomRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            hidden_font = new Font(null, Font.ITALIC, font_size);
            hidden_color = Color.red;
            mixed_font = new Font(null, Font.ITALIC, font_size);
            mixed_color = Color.gray;
        }

        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
            super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);

            setIcon(null);
            BasicCanvasNode node = (BasicCanvasNode) value;
            int visibility = node.getVisibility();

            if (visibility == BasicCanvas.VISIBLE) {
                setFont(default_font);
                setText(node.getName());
            } else if (visibility == BasicCanvas.HIDDEN) {
                setFont(hidden_font);
                setForeground(hidden_color);
                setText("x  " + node.getName());
            } else if (visibility == BasicCanvas.MIXED) {
                setFont(mixed_font);
                setForeground(mixed_color);
                setText(node.getName());
            }

            return this;
        }
    }

}
