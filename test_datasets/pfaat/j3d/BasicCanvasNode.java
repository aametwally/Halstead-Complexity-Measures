package com.neogenesis.pfaat.j3d;


import javax.swing.tree.*;
import javax.media.j3d.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class BasicCanvasNode extends DefaultMutableTreeNode {
    BasicCanvas canvas;
    String name;
    JPopupMenu popup;
    Font  menu_font = new Font(null, Font.PLAIN, 10);

    public BasicCanvasNode(BasicCanvas canvas, String name, boolean is_leaf) {
        this.canvas = canvas;
        this.name = name;
        this.setAllowsChildren(!is_leaf);

        // setup pop-up support
        popup = new JPopupMenu();

        JMenuItem toggle_visibility_menu = new JMenuItem("Toggle Visibility");

        toggle_visibility_menu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleVisibility();
                }
            }
        );

        addToPopup(toggle_visibility_menu);

    }

    /**
     *Use to add items to the popup menu.  The item's display properties will be
     *changed to to fit the popup look n feel
     */
    public void addToPopup(JMenuItem menu_item) {
        menu_item.setFont(menu_font);
        popup.add(menu_item);
    }

    public void addSeparatorToPopup() {
        popup.addSeparator();
    }

    public JPopupMenu getPopup() {
        return popup;
    }

    public void  detach() {
        canvas.removeNode(this);
    }

    public void  hide() {
        canvas.hideNode(this);
    }

    public void  show() {
        canvas.showNode(this);
    }

    public void toggleVisibility() {
        int visibility = getVisibility();

        if (visibility == BasicCanvas.HIDDEN) {
            setVisibility(BasicCanvas.VISIBLE);
        } else { // treat MIXED as VISIBLE
            setVisibility(BasicCanvas.HIDDEN);
        }
    }

    public void  setVisibility(int state) {
        canvas.setNodeVisibility(this, state);
    }

    public int getVisibility() {
        return canvas.getVisibility(this);
    }

    public boolean isLeaf() {
        return (!getAllowsChildren() && super.isLeaf());
    }

    public void addBranchGroup(BranchGroup branch) {
        if (!isLeaf())
            throw new RuntimeException("BasicCanvasNode.addBranchGroup: " +
                    "Cannot add to non-leaf");
        canvas.addToNode(this, branch);
    }

    public BasicCanvasNode allocateLeaf(String name, int visibility) {
        if (isLeaf())
            throw new RuntimeException("BasicCanvasNode.allocateLeaf: " +
                    "Cannot add to leaf");
        return canvas.allocateLeaf(this, name, visibility);
    }

    public void attach(BasicCanvasNode node, int visibility) {
        if (isLeaf())
            throw new RuntimeException("BasicCanvasNode.allocateLeaf: " +
                    "Cannot add to leaf");
        if (node.isLeaf())
            canvas.attachLeaf(this, node, visibility);
        else
            canvas.attachNode(this, node, visibility);
    }

    public BasicCanvasNode allocateNode(String name, int visibility) {
        if (isLeaf())
            throw new RuntimeException("BasicCanvasNode.allocateLeaf: " +
                    "Cannot add to leaf");
        return canvas.allocateNode(this, name, visibility);
    }

    public ArrayList getChildren() {
        ArrayList children = new ArrayList();

        for (Enumeration children_enum = children();
            children_enum.hasMoreElements();)
            children.add(children_enum.nextElement());
        return children;
    }

    public String getName() {
        return name;
    }

    public void clear() {
        canvas.clearNode(this);
    }

    public int hashCode() {
        return name.hashCode();
    }

}
