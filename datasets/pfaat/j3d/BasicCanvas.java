package com.neogenesis.pfaat.j3d;


import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.tree.*;


/**
 * A J3D canvas which contains multiple named nodes which can be either
 * displayed or hidden
 * todo: think about behaviors
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:29:08 $ */
public class BasicCanvas extends Canvas3D {
    private SimpleUniverse universe;
    private View view;
    private TransformGroup root_tg;
    private TransformGroup view_tg;
    private BranchGroup root_node;
    private double clip_distance;
    private BoundingSphere behavior_bounds; // default bounds for behaviors
    private BoundingSphere light_bounds;
    private Map node_map, behavior_map;
    private BasicCanvasTree tree;
    private BasicCanvasNode tree_root;
    private DefaultTreeModel tree_model;
    private JPanel panel = null;

    public static final int VISIBLE = 1;
    public static final int HIDDEN = 2;
    public static final int MIXED = 3;
    public static final int UNKNOWN = 4;

    public BasicCanvas() {
        super(SimpleUniverse.getPreferredConfiguration());
        tree_root = new BasicCanvasNode(this, "ROOT", false);
        tree_model = new DefaultTreeModel(tree_root);
        tree = new BasicCanvasTree(this, tree_model);
        setDefaultCursor();
        universe = new SimpleUniverse(this);

        node_map = new HashMap();
        behavior_map = new HashMap();

        // need to figure this out
        clip_distance = 100.0;
        // need to figure this out
        behavior_bounds = new BoundingSphere(new Point3d(0, 0, 0), 500.0);
        // need to figure this out
        light_bounds = new BoundingSphere(new Point3d(0, 0, 0), 500.0);

        // setup top level nodes
        root_node = new BranchGroup();
        root_node.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
        root_node.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        root_node.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        root_node.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        root_tg = new TransformGroup();
        root_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        root_tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        root_tg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        root_tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        root_tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        root_node.addChild(root_tg);

        setupLights();

        root_node.compile();

        universe.addBranchGraph(root_node);

        universe.getViewingPlatform().setNominalViewingTransform();
        ViewingPlatform vp = universe.getViewingPlatform();

        view = universe.getViewer().getView();
        view.setBackClipDistance(clip_distance);
        view_tg = vp.getViewPlatformTransform();

    }

    public JPanel getJPanel() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BorderLayout());

            JScrollPane tree_scroll = new JScrollPane(tree,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            JScrollBar bar = tree_scroll.getVerticalScrollBar();
            Dimension dim = bar.getSize();

            dim.width = 8;
            bar.setPreferredSize(dim);
            Dimension tree_dimension = new Dimension(230, 600);

            tree_scroll.setMinimumSize(tree_dimension);
            tree_scroll.setPreferredSize(tree_dimension);

            JSplitPane split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

            split_pane.setLeftComponent(tree_scroll);
            split_pane.setRightComponent(this);
            split_pane.setDividerLocation(0.80);

            panel.add(split_pane, BorderLayout.CENTER);
        }
        return panel;
    }
    private Dimension minimum_dimension = new Dimension(800, 600);
    public Dimension getMinimumSize() {
        return minimum_dimension;
    }
 
    // Core Methods
    private BranchGroup getNodeBG(BasicCanvasNode node) {
        BranchGroup node_bg = (BranchGroup) node_map.get(node);

        if (node_bg == null)
            throw new RuntimeException("BasicCanvas.getNodeBG: Node "
                    + node.getName() + " does not exist.");
        return node_bg;
    }

    private Switch getNodeSwitch(BasicCanvasNode node) {
        return (Switch) getNodeBG(node).getChild(0);
    }

    private BranchGroup getBehaviorBG(String name) {
        BranchGroup b_bg = (BranchGroup) behavior_map.get(name);

        if (b_bg == null)
            throw new RuntimeException("BasicCanvas.getBehaviorBG: Behavior " +
                    name + " does not exist.");
        return b_bg;
    }

    public BasicCanvasNode allocateNode(String name, int visibility) {
        return allocateNode(tree_root, name, visibility);
    }

    public BasicCanvasNode allocateNode(BasicCanvasNode parent, String name,
        int visibility) {
        BasicCanvasNode node = new BasicCanvasNode(this, name, false);

        return attachNode(parent, node, visibility);
    }

    public BasicCanvasNode attachNode(BasicCanvasNode parent,
        BasicCanvasNode node,
        int visibility) {
        tree_model.insertNodeInto(node, parent, parent.getChildCount());
        TreePath tree_path = new TreePath(tree_model.getPathToRoot(node));

        tree.expandPath(tree_path);
        node.setVisibility(visibility);
        return node;
    }

    public BasicCanvasNode allocateLeaf(BasicCanvasNode parent, String name,
        int visibility) {
        BasicCanvasNode node = new BasicCanvasNode(this, name, true);

        return attachLeaf(parent, node, visibility);
    }

    public BasicCanvasNode attachLeaf(BasicCanvasNode parent,
        BasicCanvasNode leaf, int visibility) {

        BranchGroup node_bg = new BranchGroup();

        node_bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        node_bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        node_bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        node_bg.setCapability(BranchGroup.ALLOW_DETACH);

        Switch new_node = new Switch();

        new_node.setWhichChild(Switch.CHILD_ALL);
        new_node.setCapability(Switch.ALLOW_SWITCH_READ);
        new_node.setCapability(Switch.ALLOW_SWITCH_WRITE);
        new_node.setCapability(Switch.ALLOW_CHILDREN_EXTEND);
        new_node.setCapability(Switch.ALLOW_CHILDREN_READ);
        new_node.setCapability(Switch.ALLOW_CHILDREN_WRITE);

        // wrap switch in a branchgroup to allow detatching and live additions
        node_bg.addChild(new_node);
        root_tg.addChild(node_bg);

        Object old = node_map.put(leaf, node_bg);

        if (old != null)
            throw new RuntimeException("BasicCanvas.allocateNode: Node " +
                    leaf.getName() + " already exists.");

        tree_model.insertNodeInto(leaf, parent, parent.getChildCount());
        TreePath tree_path = new TreePath(tree_model.getPathToRoot(leaf));

        tree.expandPath(tree_path);
        leaf.setVisibility(visibility);
        return leaf;
    }

    /**
     * removes a node from the scene
     * @param node The node to be removed
     */
    public void removeNode(BasicCanvasNode node) {
        if (node.isLeaf()) {
            BranchGroup node_bg = getNodeBG(node);

            node_bg.detach();
            node_map.remove(node);
            tree_model.removeNodeFromParent(node);
        } else {
            ArrayList children = node.getChildren();

            for (Iterator iterator = children.iterator(); iterator.hasNext();) {
                BasicCanvasNode child = (BasicCanvasNode) iterator.next();

                removeNode(child);
            }
            tree_model.removeNodeFromParent(node);
        }
    }

    /**
     *removes all nodes from the scene
     */
    public void removeAllNodes() {
        for (Enumeration children = tree_root.children();
            children.hasMoreElements();) {
            BasicCanvasNode child = (BasicCanvasNode) children.nextElement();

            removeNode(child);
        }
    }

    /**
     * returns current state of the given node
     */
    public int getVisibility(BasicCanvasNode node) {
        if (node.isLeaf()) {
            Switch node_switch = getNodeSwitch(node);

            if (node_switch.getWhichChild() == Switch.CHILD_ALL)
                return VISIBLE;
            else
                return HIDDEN;
        } else
            return computeNodeState(node);
    }

    private int computeNodeState(BasicCanvasNode node) {
        int state = UNKNOWN;

        for (Enumeration children = node.depthFirstEnumeration();
            children.hasMoreElements();) {
            BasicCanvasNode child = (BasicCanvasNode) children.nextElement();

            if (child.isLeaf()) {
                if (state == UNKNOWN) {
                    state = getVisibility(child);
                }
                if (state != getVisibility(child))
                    return MIXED;
            }
        }
        return state;
    }

    /**
     * sets node visibility to true
     */
    public void showNode(BasicCanvasNode node) {
        setNodeVisibility(node, VISIBLE);
    }

    /**
     *hides the node
     */
    public void hideNode(BasicCanvasNode node) {
        setNodeVisibility(node, HIDDEN);
    }

    public void setNodeVisibility(BasicCanvasNode node, int state) {
        coreSetNodeVisibility(node, state);
        tree_model.nodeStructureChanged(node);
    }

    private void coreSetNodeVisibility(BasicCanvasNode node, int state) {
        if (!node.isLeaf()) {
            for (Enumeration children = node.children();
                children.hasMoreElements();) {
                BasicCanvasNode child = (BasicCanvasNode) children.nextElement();

                coreSetNodeVisibility(child, state);
            }
        } else {
            if (state == VISIBLE)
                getNodeSwitch(node).setWhichChild(Switch.CHILD_ALL);
            else if (state == HIDDEN)
                getNodeSwitch(node).setWhichChild(Switch.CHILD_NONE);
            else
                throw new RuntimeException("BasicCanvas.coreSetNodeVisibility: " +
                        "invalid visibility state.");
        }
    }

    /**
     * clears the node
     */
    public void clearNode(BasicCanvasNode node) {
        if (!node.isLeaf()) {
            for (Enumeration children = node.children();
                children.hasMoreElements();) {
                BasicCanvasNode child = (BasicCanvasNode) children.nextElement();

                clearNode(child);
            }
        } else {
            Switch node_switch = getNodeSwitch(node);

            while (node_switch.numChildren() > 0)
                node_switch.removeChild(0);
        }
    }

    /**
     * adds a branch to the node
     * @param node The node of the node to add to
     * @param bg The BranchGroup to add
     */
    public void addToNode(BasicCanvasNode node, BranchGroup bg) {
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        bg.compile();
        Switch node_switch = getNodeSwitch(node);

        node_switch.addChild(bg);
    }

    // Misc Methods
    /**
     *Sets root transform
     */
    public void setRootTransform(Transform3D trans) {
        root_tg.setTransform(trans);
    }

    /**
     *Sets view transform
     */
    public void setViewTransform(Transform3D trans) {
        view_tg.setTransform(trans);
    }

    /**
     *Gets root transform group
     */
    public TransformGroup getRootTransformGroup() {
        return root_tg;
    }

    /**
     *Gets view transform group
     */
    public TransformGroup getViewTransformGroup() {
        return view_tg;
    }

    /**
     *Sets the cursor to the default (a hand)
     */
    public void setDefaultCursor() {
        // default cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     *Add a behavior to the root node
     */
    public void addBehavior(String name, Behavior b) {
        b.setSchedulingBounds(behavior_bounds);
        BranchGroup b_bg = new BranchGroup();

        b_bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        b_bg.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        b_bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        b_bg.setCapability(BranchGroup.ALLOW_DETACH);

        // wrap behavior in a branchgroup to allow detatching
        b_bg.addChild(b);
        root_node.addChild(b_bg);
        Object old = behavior_map.put(name, b_bg);

        if (old != null)
            throw new RuntimeException("BasicCanvas.addBehavior: Behavior " +
                    name + " already exists.");
    }

    /**
     *remove a behavior to the root node
     */
    public void removeBehavior(String name) {
        BranchGroup b_bg = getBehaviorBG(name);

        b_bg.detach();
        behavior_map.remove(name);
    }

    private Vector3d view_point_temp = new Vector3d();
    private Point3d view_point_center = new Point3d();

    /**
     *Adds a set of default behaviors to the root node
     */
    public void setDefaultViewPoint(BoundingSphere scene_bounds) {
        Transform3D view_transform = new Transform3D();
        Transform3D eye_transform = new Transform3D();
        double eye_distance;

        // point the view at the center of the object
        scene_bounds.getCenter(view_point_center);
        view_point_temp.set(view_point_center);
        view_transform.set(view_point_temp);
      
        double radius = scene_bounds.getRadius();

        eye_distance = 1.4 * radius / Math.tan(view.getFieldOfView() / 2.0);
      
        view_point_temp.x = 0.0;
        view_point_temp.y = 0.0;
        view_point_temp.z = eye_distance;
      
        eye_transform.set(view_point_temp);
        view_transform.mul(eye_transform);
        // set the view transform
        view_tg.setTransform(view_transform);
    }

    public void addDefaultNavigation() {
        MouseState rotate_state = new MouseState(MouseState.FALSE,
                MouseState.FALSE,
                MouseState.FALSE,
                MouseState.TRUE,
                MouseState.FALSE,
                MouseState.FALSE,
                MouseState.UNSPECIFIED);
        MouseState translate_state = new MouseState(MouseState.FALSE,
                MouseState.FALSE,
                MouseState.FALSE,
                MouseState.FALSE,
                MouseState.FALSE,
                MouseState.TRUE,
                MouseState.UNSPECIFIED);
        MouseState zoom_state = new MouseState(MouseState.FALSE,
                MouseState.FALSE,
                MouseState.TRUE,
                MouseState.TRUE,
                MouseState.FALSE,
                MouseState.FALSE,
                MouseState.UNSPECIFIED);
        MouseRotateBehavior rotate = new MouseRotateBehavior(rotate_state, 
                root_tg);
        MouseTranslateBehavior translate =
            new MouseTranslateBehavior(translate_state, root_tg);
        MouseZoomBehavior zoom = new MouseZoomBehavior(zoom_state, root_tg);

        addBehavior("DEFAULT ROTATE", rotate);
        addBehavior("DEFAULT TRANSLATE", translate);
        addBehavior("DEFAULT ZOOM", zoom);
    }

    private void setupLights() {
        // Set up the background
        BranchGroup light_node = new BranchGroup();

        Color3f bgColor = new Color3f(0.05f, 0.05f, 0.2f);
        Background bgNode = new Background(bgColor);

        bgNode.setApplicationBounds(light_bounds);
        light_node.addChild(bgNode);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(0.1f, 0.1f, 0.1f);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);

        ambientLightNode.setInfluencingBounds(light_bounds);
        light_node.addChild(ambientLightNode);

        // Set up the directional lights
        Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        Color3f light2Color = new Color3f(0.3f, 0.3f, 0.4f);
        Vector3f light2Direction = new Vector3f(-6.0f, -2.0f, -1.0f);

        DirectionalLight light1
            = new DirectionalLight(light1Color, light1Direction);

        light1.setInfluencingBounds(light_bounds);
        light_node.addChild(light1);

        DirectionalLight light2
            = new DirectionalLight(light2Color, light2Direction);

        light2.setInfluencingBounds(light_bounds);
        light_node.addChild(light2);

        root_node.addChild(light_node);
    }

    // for fetching images
    boolean read_raster = false;
    BufferedImage swap_image = null;

    // get the canvas image, must be called from a different thread
    public BufferedImage getBufferedImage() {
        read_raster = true;
        // setSize(getWidth() - 1, getHeight());
        repaint();
        while (read_raster == true)
            Thread.currentThread().yield();
        BufferedImage b = swap_image;

        swap_image = null;
        return b;
    }

    // get the canvas immediately
    public BufferedImage getImmediateBufferedImage() {
        BufferedImage bImage = 
            new BufferedImage(getWidth(), getHeight(), 
                BufferedImage.TYPE_INT_ARGB);
        ImageComponent2D imageComponent = 
            new ImageComponent2D(ImageComponent.FORMAT_RGB, bImage);
        Raster raster = 
            new Raster(new Point3f(0.0f, 0.0f, 0.0f), 
                Raster.RASTER_COLOR, 0, 0, 
                bImage.getWidth(), bImage.getHeight(), 
                imageComponent, null);

        getGraphicsContext3D().readRaster(raster);
        return raster.getImage().getImage();
    }
    
    public void postSwap() {
        super.postSwap();
        if (read_raster) {
            swap_image = getImmediateBufferedImage();
            read_raster = false;
        }
    }

}
