package com.neogenesis.pfaat.seqspace;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.print.*;
import com.neogenesis.pfaat.j3d.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.pdb.*;
import com.neogenesis.pfaat.colorscheme.ColorScheme;


public class SeqSpaceFrame extends JFrame implements DisplayPropertiesListener {
    private SeqSpace ss;
    private DisplayProperties props;
    private BasicCanvas basic_canvas;

    // nodes in display
    private static final String AXES_NODE = "Axes";
    private static final String SEQUENCES_NODE = "Sequences";
    private static final String SEQUENCE_POINTS_NODE = "Points";
    private static final String SEQUENCE_LABELS_NODE = "Labels";
    private static final String RESIDUES_NODE = "Residues";
    private static final String RESIDUE_POINTS_NODE = "Points";
    private static final String RESIDUE_LABELS_NODE = "Labels";
    private static final String SELECTION_NODE = "Selection";
    private static final String ROOT_NODE = "Main Scene";
    private static final String SELECTION_BEHAVIOR1 = "Selection Behavior 1";
    private static final String SELECTION_BEHAVIOR2 = "Selection Behavior 2";

    private BasicCanvasNode root_node, axes_node, 
        sequences_node, sequence_points_node, sequence_labels_node,
        residues_node, residue_points_node, residue_labels_node,
        selection_node;

    private AlignmentFrame owner;

    // transform groups with need to be rotated to face the viewer
    private Axes axes;
    private Set sequence_labels = new HashSet();
    private Set residue_labels = new HashSet();

    // which dimensions are currently used
    private int x_dim, y_dim, z_dim;

    // sequence coordinates as prescaled Point3d
    public PointSet seq_points = new PointSet(POINT_DIMENSION);
    public PointSet res_points = new PointSet(POINT_DIMENSION);
    public boolean[] res_is_labelled;

    // axis selectors
    private JPanel info_panel;
    private JComboBox x_dim_cb, y_dim_cb, z_dim_cb;
    // label for total variance on display
    private JLabel total_variance_label;
    // for residue selection
    private ButtonGroup residue_button_group;
    private static final String DISTANCE_METHOD = "DISTANCE";
    private DoubleSlider distance_slider;
    private static final String NEIGHBOR_METHOD = "NEIGHBOR";
    private JTextField neighbor_text_field;

    // dimension of the scene is [-SCENE_DIMENSION,SCENE_DIMENSION]^3
    private static final float SCENE_DIMENSION = 10.0f;
    // dimension of each plotted point is [-POINT_DIMENSION,POINT_DIMENSION]^3
    private static final float POINT_DIMENSION = SCENE_DIMENSION / 100.0f;
    private static final float POINT_DIMENSION_2 = 
        POINT_DIMENSION * POINT_DIMENSION;

    public SeqSpaceFrame(AlignmentFrame owner, 
        SeqSpace ss, 
        DisplayProperties props) {
        super("Sequence Space");
        this.owner = owner;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        this.ss = ss;
        this.props = props;
        props.addListener(this);
        addWindowListener(new WindowListener() {
                public void windowActivated(WindowEvent e) {}

                public void windowClosed(WindowEvent e) {
                    SeqSpaceFrame.this.props.removeListener(SeqSpaceFrame.this);
                }

                public void windowClosing(WindowEvent e) {}

                public void windowDeactivated(WindowEvent e) {}

                public void windowDeiconified(WindowEvent e) {}

                public void windowIconified(WindowEvent e) {}

                public void windowOpened(WindowEvent e) {}
            }
        );

        JMenuBar menu_bar = new JMenuBar();

        setJMenuBar(menu_bar);
        JMenu file_menu = new JMenu("File");

        menu_bar.add(file_menu);
        JMenuItem save_menu_item = new JMenuItem("Save as Text File");

        save_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JFileChooser fc = 
                        new JFileChooser(PathManager.getAlignmentPath());

                    fc.setDialogType(JFileChooser.SAVE_DIALOG);
		    
                    if (fc.showDialog(SeqSpaceFrame.this, "Save") 
                        == JFileChooser.APPROVE_OPTION) {
                        try {
                            PathManager.setAlignmentPath(fc.getCurrentDirectory());
                            SeqSpaceFrame.this.ss.save(fc.getSelectedFile());
                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(SeqSpaceFrame.this,
                                "Unable to save "
                                + "seq space "
                                + "analysis: ",
                                e);
                        }
                    }
                }
            }
        );
        file_menu.add(save_menu_item);

        JMenu user_dimension_menu = new JMenu("User Dimensions"); {
            JMenuItem write_template_menu_item = new JMenuItem("Write Template");

            write_template_menu_item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                        fc.setDialogType(JFileChooser.SAVE_DIALOG);
			
                        if (fc.showDialog(SeqSpaceFrame.this, "Write") == JFileChooser.APPROVE_OPTION) {
                            try {
                                PathManager.setAlignmentPath(fc.getCurrentDirectory());
                                SeqSpaceFrame.this.ss.writeTemplate(fc.getSelectedFile());
                            } catch (Exception e) {
                                ErrorDialog.showErrorDialog(SeqSpaceFrame.this,
                                    "Unable to write user dimension template", e);
                            }
                        }
                    }
                }
            );
            user_dimension_menu.add(write_template_menu_item);
	    
            JMenuItem load_data_menu_item = new JMenuItem("Load Data");

            load_data_menu_item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                        fc.setDialogType(JFileChooser.OPEN_DIALOG);
			
                        if (fc.showDialog(SeqSpaceFrame.this, "Load") == JFileChooser.APPROVE_OPTION) {
                            try {
                                PathManager.setAlignmentPath(fc.getCurrentDirectory());
                                SeqSpace new_ss = new SeqSpace(SeqSpaceFrame.this.ss);

                                new_ss.loadData(fc.getSelectedFile());

                                SeqSpace.Dimension[] dims = new_ss.getDimensions();
                                String[] dim_labels = new String[dims.length];
                                DecimalFormat df = new DecimalFormat("0.00%");

                                for (int i = dims.length; --i >= 0;) {
                                    dim_labels[i] = dims[i].getName() + " (" + df.format(dims[i].getVariance() / new_ss.getTotalVariance()) + ")";
                                }
                                SeqSpaceFrame.this.x_dim_cb.setModel(new DefaultComboBoxModel(dim_labels));
                                SeqSpaceFrame.this.x_dim_cb.setSelectedIndex(0);
                                SeqSpaceFrame.this.y_dim_cb.setModel(new DefaultComboBoxModel(dim_labels));
                                SeqSpaceFrame.this.y_dim_cb.setSelectedIndex(1);
                                SeqSpaceFrame.this.z_dim_cb.setModel(new DefaultComboBoxModel(dim_labels));
                                SeqSpaceFrame.this.z_dim_cb.setSelectedIndex(2);

                                SeqSpaceFrame.this.ss = null;
                                SeqSpaceFrame.this.ss = new_ss;

                                SeqSpaceFrame.this.resetCoordinates();
                            } catch (Exception e) {
                                ErrorDialog.showErrorDialog(SeqSpaceFrame.this,
                                    "Unable to load user dimension data", e);
                            }
                        }
                    }
                }
            );
            user_dimension_menu.add(load_data_menu_item);
        }
        file_menu.add(user_dimension_menu);

        file_menu.addSeparator();
        JMenuItem print_menu_item = new JMenuItem("Print");

        print_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Thread printThread = 
                        new Thread(new 
                            Runnable() {
                                public void run() {
                                    final BufferedImage img = 
                                        basic_canvas.getBufferedImage();

                                    SwingUtilities.invokeLater(new 
                                        Runnable() {
                                            public void run() {
                                                SeqSpaceFrame.this.print(img);
                                            }
                                        }
                                    );
                                }
                            }
                        );

                    printThread.start();
                    // force a repaint -- hack
                    basic_canvas.setSize(basic_canvas.getSize());
                }
            }
        );
        file_menu.add(print_menu_item);

        allocateInfoPanel();
        allocateCanvas();
        resetCoordinates();

        basic_canvas.setSize(600, 400);
        JSplitPane main_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        main_pane.setTopComponent(info_panel);
        main_pane.setBottomComponent(basic_canvas.getJPanel());
        getContentPane().add(main_pane, BorderLayout.CENTER);

        pack();
    }

    // print an image
    private void print(Image im) {
        PrintGrid printable = new PrintGrid(1, 1);

        printable.addComponent(im, 0, 0);
        PrintPSDialog pd = new PrintPSDialog(this, 
                printable);

        pd.show();
    }

    private void allocateInfoPanel() {
        info_panel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        info_panel.setLayout(gb);
        int row = 0;

        total_variance_label = new JLabel("");
        Utils.addToGridBag(info_panel, gb, 
            total_variance_label,
            0, row, 2, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.CENTER);
        JButton display_button = new JButton("Display");

        display_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    resetCoordinates(); 
                }
            }
        );
        Utils.addToGridBag(info_panel, gb, 
            display_button,
            2, row, 2, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.CENTER);
        JButton reset_view_button = new JButton("Reset View");

        reset_view_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    resetView();
                }
            }
        );		
        Utils.addToGridBag(info_panel, gb, 
            reset_view_button,
            4, row, 2, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.CENTER);
        row++;

        SeqSpace.Dimension[] dims = ss.getDimensions();
        String[] dim_labels = new String[dims.length];
        DecimalFormat df = new DecimalFormat("0.00%");

        for (int i = dims.length; --i >= 0;) {
            dim_labels[i] = dims[i].getName() + " (" + df.format(dims[i].getVariance() / ss.getTotalVariance()) + ")";
        }

        Utils.addToGridBag(info_panel, gb, 
            new JLabel("X Axis: "),
            0, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        x_dim_cb = new JComboBox(dim_labels);
        x_dim_cb.setSelectedIndex(0);
        Utils.addToGridBag(info_panel, gb, 
            x_dim_cb,
            1, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        Utils.addToGridBag(info_panel, gb, 
            new JLabel("Y Axis: "),
            2, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        y_dim_cb = new JComboBox(dim_labels);
        y_dim_cb.setSelectedIndex(1);
        Utils.addToGridBag(info_panel, gb, 
            y_dim_cb,
            3, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);

        Utils.addToGridBag(info_panel, gb, 
            new JLabel("Z Axis: "),
            4, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        z_dim_cb = new JComboBox(dim_labels);
        z_dim_cb.setSelectedIndex(2);
        Utils.addToGridBag(info_panel, gb, 
            z_dim_cb,
            5, row, 1, 1, 1, 1, 
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        row++;

        residue_button_group = new ButtonGroup();

        JRadioButton distance_radio_button = new JRadioButton("Minimum Residue Distance", false);

        distance_radio_button.setActionCommand(DISTANCE_METHOD);
        residue_button_group.add(distance_radio_button);
        Utils.addToGridBag(info_panel, gb,
            distance_radio_button,
            1, row, 2, 1, 1, 1,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        distance_slider = new DoubleSlider(0.0, 1.0, "0%");
        distance_slider.setDouble(0.0);
        Utils.addToGridBag(info_panel, gb,
            distance_slider,
            3, row, 2, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        JLabel distance_label = new JLabel("");

        distance_slider.addValueLabel(distance_label);
        Utils.addToGridBag(info_panel, gb,
            distance_label,
            5, row, 1, 1, 1, 1,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        row++;

        JRadioButton neighbor_radio_button = new JRadioButton("Nearest Neighbor Residues", true);

        neighbor_radio_button.setActionCommand(NEIGHBOR_METHOD);
        residue_button_group.add(neighbor_radio_button);
        Utils.addToGridBag(info_panel, gb,
            neighbor_radio_button,
            1, row, 2, 1, 1, 1,
            GridBagConstraints.NONE, GridBagConstraints.EAST);
        neighbor_text_field = new JTextField("", 8);
        neighbor_text_field.setHorizontalAlignment(JTextField.CENTER);
        Utils.addToGridBag(info_panel, gb,
            neighbor_text_field,
            3, row, 1, 1, 1, 1,
            GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        JLabel neighbor_label = new JLabel("" + ss.getResCoords().length);

        Utils.addToGridBag(info_panel, gb,
            neighbor_label,
            4, row, 1, 1, 1, 1,
            GridBagConstraints.NONE, GridBagConstraints.WEST);
        row++;
    }

    private void resetView() {
        basic_canvas.setDefaultViewPoint(new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                SCENE_DIMENSION));
        basic_canvas.getRootTransformGroup().setTransform(new Transform3D());
        updateViewerTransforms();
    }

    private void allocateCanvas() {
        basic_canvas = new BasicCanvas();
        resetView();

        MouseRayBehavior selection_behavior1 = 
            new MouseRayBehavior(new MouseState(MouseState.FALSE,
                    MouseState.FALSE, 
                    MouseState.FALSE, 
                    MouseState.TRUE, 
                    MouseState.FALSE, 
                    MouseState.FALSE, 
                    MouseState.UNSPECIFIED), 
                basic_canvas, 
                3);

        selection_behavior1.addListener(new MouseRayListener() {
                public void rayFired(PickRay ray) {
                    selectClosestSeqPoint(ray, true);
                    selectClosestResPoint(ray, true);
                }

                public void rayFired(PickRay ray1, PickRay ray2, 
                    PickRay ray3, PickRay ray4) {
                    selectClosestSeqPoint(ray1, ray2, ray3, ray4, true);
                    selectClosestResPoint(ray1, ray2, ray3, ray4, true);
                }
            }
        );
        basic_canvas.addBehavior(SELECTION_BEHAVIOR1, selection_behavior1);

        MouseRayBehavior selection_behavior2 = 
            new MouseRayBehavior(new MouseState(MouseState.TRUE,
                    MouseState.FALSE, 
                    MouseState.FALSE, 
                    MouseState.TRUE, 
                    MouseState.FALSE, 
                    MouseState.FALSE, 
                    MouseState.UNSPECIFIED), 
                basic_canvas, 
                3);

        selection_behavior2.addListener(new MouseRayListener() {
                public void rayFired(PickRay ray) {
                    selectClosestSeqPoint(ray, false);
                    selectClosestResPoint(ray, false);
                }

                public void rayFired(PickRay ray1, PickRay ray2, 
                    PickRay ray3, PickRay ray4) {
                    selectClosestSeqPoint(ray1, ray2, ray3, ray4, false);
                    selectClosestResPoint(ray1, ray2, ray3, ray4, false);
                }
            }
        );
        basic_canvas.addBehavior(SELECTION_BEHAVIOR2, selection_behavior2);

        // navigation
        TransformGroup root_tg = basic_canvas.getRootTransformGroup();
        TransformGroup view_tg = basic_canvas.getViewTransformGroup();

        MouseRotateBehavior rotate = 
            new MouseRotateBehavior(new MouseState(MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.FALSE, 
                    MouseState.FALSE, 
                    MouseState.TRUE, 
                    MouseState.UNSPECIFIED),
                root_tg);

        basic_canvas.addBehavior("Rotate", rotate);
        MouseTranslateBehavior translate = 
            new MouseTranslateBehavior(new MouseState(MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED,
                    MouseState.FALSE, 
                    MouseState.TRUE, 
                    MouseState.FALSE, 
                    MouseState.UNSPECIFIED),
                view_tg);

        basic_canvas.addBehavior("Translate", translate);
        translate.setFactor(-translate.getXFactor(), -translate.getYFactor());
        MouseZoomBehavior zoom = 
            new MouseZoomBehavior(new MouseState(MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.FALSE, 
                    MouseState.TRUE, 
                    MouseState.TRUE, 
                    MouseState.UNSPECIFIED),
                view_tg);

        basic_canvas.addBehavior("Zoom", zoom);
        zoom.setFactor(-zoom.getFactor());

        // update labels whenever the mouse is released
        basic_canvas.addBehavior("Label Update", new 
            BasicMouseBehavior(new MouseState(MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED, 
                    MouseState.UNSPECIFIED),
                false,
                false,
                true) {
                public void processRelease(MouseEvent e) {
                    updateViewerTransforms();
                }
            }
        );

    }
    
    private void resetState() {
        basic_canvas.removeAllNodes();
        root_node = basic_canvas.allocateNode(ROOT_NODE,
                    BasicCanvas.VISIBLE);
        axes_node = root_node.allocateLeaf(AXES_NODE, 
                    BasicCanvas.VISIBLE);
        sequences_node = root_node.allocateNode(SEQUENCES_NODE,
                    BasicCanvas.VISIBLE);
        sequence_points_node =  
                sequences_node.allocateLeaf(SEQUENCE_POINTS_NODE,
                    BasicCanvas.VISIBLE);
        sequence_labels_node =  
                sequences_node.allocateLeaf(SEQUENCE_LABELS_NODE,
                    BasicCanvas.VISIBLE);
        residues_node = root_node.allocateNode(RESIDUES_NODE,
                    BasicCanvas.VISIBLE);
        residue_points_node = residues_node.allocateLeaf(RESIDUE_POINTS_NODE,
                    BasicCanvas.VISIBLE);
        residue_labels_node = residues_node.allocateLeaf(RESIDUE_LABELS_NODE,
                    BasicCanvas.VISIBLE);
        selection_node = root_node.allocateLeaf(SELECTION_NODE,
                    BasicCanvas.VISIBLE);

        seq_points.clear();
        res_points.clear();
        res_is_labelled = null;
        scene_scale = -1.0;
        axes = null;
        sequence_labels.clear();
        residue_labels.clear();
    }
    
    private void resetCoordinates() {
        x_dim = x_dim_cb.getSelectedIndex();
        y_dim = y_dim_cb.getSelectedIndex();
        z_dim = z_dim_cb.getSelectedIndex();
	
        resetState();
        drawAxes();
        drawSequencePoints();
        drawSequenceLabels();
        drawResiduePoints();
        drawResidueLabels();

        DecimalFormat df = new DecimalFormat("0.00%");
        double variance_percentage = ss.getDimensions()[x_dim].getVariance();

        if (x_dim != y_dim)
            variance_percentage += ss.getDimensions()[y_dim].getVariance();
        if (x_dim != z_dim && y_dim != z_dim)
            variance_percentage += ss.getDimensions()[z_dim].getVariance();
        variance_percentage /= ss.getTotalVariance();
        total_variance_label.setText(df.format(variance_percentage)
            + " of total variance");
    }

    private void drawAxes() {
        axes_node.clear();

        LineAttributes la = new LineAttributes();

        la.setLineAntialiasingEnable(true);
        ColoringAttributes ca = new ColoringAttributes();

        ca.setColor(new Color3f(1.0f, 1.0f, 0.0f));
        Appearance app = new Appearance();

        app.setLineAttributes(la);
        app.setColoringAttributes(ca);
        Point3d center = new Point3d(0.0, 0.0, 0.0);

        axes = new Axes(center,
                    SCENE_DIMENSION,
                    true, 
                    app);
        BranchGroup bg = new BranchGroup();

        bg.addChild(axes);
	
        axes_node.addBranchGroup(bg);
        updateViewerTransforms();
    }

    // the scale of the scene
    private double scene_scale = -1.0;

    private double getSceneScale() {
        if (scene_scale < 0.0) {
            double max = 0.0;
            SeqSpace.SequenceCoordinates[] seq_coords = ss.getSeqCoords();

            for (int i = seq_coords.length - 1; i >= 0; i--) {
                double[] coords = seq_coords[i].getCoordinates();
                double x;

                if ((x = Math.abs(coords[x_dim])) > max) 
                    max = x;
                if ((x = Math.abs(coords[y_dim])) > max) 
                    max = x;
                if ((x = Math.abs(coords[z_dim])) > max) 
                    max = x;
            }
            SeqSpace.ResidueCoordinates[] res_coords = ss.getResCoords();

            for (int i = res_coords.length - 1; i >= 0; i--) {
                double[] coords = res_coords[i].getCoordinates();
                double x;

                if ((x = Math.abs(coords[x_dim])) > max) 
                    max = x;
                if ((x = Math.abs(coords[y_dim])) > max) 
                    max = x;
                if ((x = Math.abs(coords[z_dim])) > max) 
                    max = x;
            }
            scene_scale = SCENE_DIMENSION / max;
        }
	
        return scene_scale;
    }

    private void drawSequencePoints() {
        sequence_points_node.clear();
        BranchGroup bg = new BranchGroup();
        Color3f color = new Color3f(1.0f, 0.0f, 0.0f);

        LineAttributes la = new LineAttributes();

        la.setLineAntialiasingEnable(true);
        ColoringAttributes ca = new ColoringAttributes();

        ca.setColor(color);
        Appearance app = new Appearance();

        app.setLineAttributes(la);
        app.setColoringAttributes(ca);
	
        double scale = getSceneScale();

        SeqSpace.SequenceCoordinates[] seq_coords = ss.getSeqCoords();
        Point3d[] points = new Point3d[seq_coords.length];
        boolean[] is_visible = new boolean[seq_coords.length];

        for (int i = seq_coords.length - 1; i >= 0; i--) {
            double[] coords = seq_coords[i].getCoordinates();
            Point3d pos = new Point3d(coords[x_dim] * scale,
                    coords[y_dim] * scale,
                    coords[z_dim] * scale);

            bg.addChild(new Axes(pos, POINT_DIMENSION, false, app));
            points[i] = pos;
            is_visible[i] = true;
        }
        seq_points.set(points, is_visible);
        sequence_points_node.addBranchGroup(bg);
    }

    private void drawSequenceLabels() {
        sequence_labels_node.clear();
        sequence_labels.clear();
        BranchGroup bg = new BranchGroup();

        Font3D f3d = new Font3D(new Font("TestFont", Font.PLAIN, 1),
                new FontExtrusion());
        Appearance f3d_app = new Appearance();
        Material mm = new Material();

        mm.setLightingEnable(true);
        f3d_app.setMaterial(mm);

        for (int i = ss.getNumSequences() - 1; i >= 0; i--) {
            Sequence seq = ss.getSequence(i);

            if (props.getSeqSelect(seq)) {
                Point3d pos = new Point3d(seq_points.getPoint(i));

                pos.z += POINT_DIMENSION;
                RotatingText3D label = new RotatingText3D(pos,
                        2.0f
                        * POINT_DIMENSION,
                        false,
                        seq.getName(),
                        f3d,
                        f3d_app);

                sequence_labels.add(label);
                bg.addChild(label);
            }
        }

        sequence_labels_node.addBranchGroup(bg);
        updateViewerTransforms();
    }	    

    private class ResPointComparator implements Comparator {
        private Point3d centroid;

        public ResPointComparator(Point3d centroid) {
            this.centroid = centroid;
        }

        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof Point3d) || !(o2 instanceof Point3d)) throw new ClassCastException();

            double dist = ((Point3d) o1).distance(centroid) - ((Point3d) o2).distance(centroid);

            if (dist == 0)
                return (0);
            else if (dist > 0)
                return (1);
            else
                return (-1);
        }
	
        public boolean equals(Object obj) {
            if (!(obj instanceof ResPointComparator)) return false;

            return (((ResPointComparator) obj).centroid.equals(centroid));
        }
    }

    private void drawResiduePoints() {
        residue_points_node.clear();

        Point3d[] selected_points = new Point3d[props.getSelectedCount()];

        if (selected_points.length < 1)
            return;
        int cnt = 0;

        for (int i = seq_points.size() - 1; i >= 0; i--) {
            if (props.getSeqSelect(ss.getSequence(i)))
                selected_points[cnt++] = seq_points.getPoint(i);
        }

        BranchGroup bg = new BranchGroup();

        bg.setCapability(BranchGroup.ALLOW_DETACH);

        Color3f color = new Color3f(0.0f, 1.0f, 0.0f);

        Appearance app = new Appearance();
        // LineAttributes la = new LineAttributes();
        // la.setLineAntialiasingEnable(true);
        // app.setLineAttributes(la);
        ColoringAttributes ca = new ColoringAttributes();

        ca.setColor(color);
        app.setColoringAttributes(ca);

        String residue_display_method = residue_button_group.getSelection().getActionCommand();

        if (residue_display_method.equals(DISTANCE_METHOD)) {
            double scale = getSceneScale();
            SeqSpace.ResidueCoordinates[] res_coords = ss.getResCoords();
            Point3d[] points = new Point3d[res_coords.length];
            boolean[] is_visible = new boolean[res_coords.length];

            res_is_labelled = new boolean[res_coords.length];
            double dist2 = distance_slider.getDouble() * 2.0 * SCENE_DIMENSION;

            dist2 = dist2 * dist2 * 3.0;
            for (int i = res_coords.length - 1; i >= 0; i--) {
                double[] coords = res_coords[i].getCoordinates();
                Point3d pos = new Point3d(coords[x_dim] * scale,
                        coords[y_dim] * scale,
                        coords[z_dim] * scale);
		
                boolean close = false;

                for (int j = selected_points.length - 1; j >= 0; j--) {
                    if (pos.distanceSquared(selected_points[j]) <= dist2) {
                        close = true;
                        break;
                    }
                }
                if (close) 
                    bg.addChild(new Axes(pos, POINT_DIMENSION, false, app));
                points[i] = pos;
                is_visible[i] = close;
                res_is_labelled[i] = false;
            }

            res_points.set(points, is_visible);
            residue_points_node.addBranchGroup(bg);
        } else if (residue_display_method.equals(NEIGHBOR_METHOD)) {
            double scale = getSceneScale();
            Point3d centroid = new Point3d(0, 0, 0);

            if (selected_points.length > 0) {
                for (int i = selected_points.length; --i >= 0;) {
                    centroid.add(selected_points[i]);
                }
                centroid.scale(1.0 / selected_points.length);
            }
	    
            SeqSpace.ResidueCoordinates[] res_coords = ss.getResCoords();
            Point3d[] points = new Point3d[res_coords.length];
            boolean[] is_visible = new boolean[res_coords.length];

            res_is_labelled = new boolean[res_coords.length];
	    
            for (int i = points.length; --i >= 0;) {
                double[] coords = res_coords[i].getCoordinates();

                points[i] = new Point3d(coords[x_dim] * scale,
                            coords[y_dim] * scale,
                            coords[z_dim] * scale);
                is_visible[i] = false;
                res_is_labelled[i] = false;
            }
	    
            Arrays.sort(points, new ResPointComparator(centroid));
	    
            try {
                int numNeighbors = Integer.parseInt(neighbor_text_field.getText());
		
                if (numNeighbors > ss.getResCoords().length) return;
		
                for (int i = numNeighbors; --i >= 0;) {
                    bg.addChild(new Axes(points[i], POINT_DIMENSION, false, app));
                    is_visible[i] = true;
                }
		
                res_points.set(points, is_visible);
                residue_points_node.addBranchGroup(bg);
            } catch (NumberFormatException nfe) {
                ErrorDialog.showErrorDialog(SeqSpaceFrame.this,
                    "neighbor residue number must be an integer");
            }
        } else {
            ErrorDialog.showErrorDialog(SeqSpaceFrame.this,
                "must select a residue display method");
        }
    }

    private void drawResidueLabels() {
        residue_labels_node.clear();
        residue_labels.clear();
        if (res_is_labelled == null) return;
        BranchGroup bg = new BranchGroup();
	
        Font3D f3d = new Font3D(new Font("TestFont", Font.PLAIN, 1),
                new FontExtrusion());
        Appearance f3d_app = new Appearance();
        Material mm = new Material();

        mm.setLightingEnable(true);
        f3d_app.setMaterial(mm);

        SeqSpace.ResidueCoordinates[] res_coords = ss.getResCoords();

        for (int i = res_is_labelled.length - 1; i >= 0; i--) {
            if (res_is_labelled[i]) {
                Point3d pos = new Point3d(res_points.getPoint(i));

                pos.z += POINT_DIMENSION;
                RotatingText3D label = 
                    new RotatingText3D(pos,
                        2.0f
                        * POINT_DIMENSION,
                        false,
                        res_coords[i].getName(),
                        f3d,
                        f3d_app);

                residue_labels.add(label);
                bg.addChild(label);
            }
        }

        residue_labels_node.addBranchGroup(bg);
        updateViewerTransforms();
    }

    public void updateViewerTransforms() {
        Transform3D t3d = new Transform3D();

        basic_canvas.getVworldToImagePlate(t3d);
        if (axes != null) {
            axes.getXLabel().alignToImagePlate(t3d);
            axes.getYLabel().alignToImagePlate(t3d);
            axes.getZLabel().alignToImagePlate(t3d);
        }
        for (Iterator i = sequence_labels.iterator(); i.hasNext();) {
            RotatingText3D label = (RotatingText3D) i.next();

            label.alignToImagePlate(t3d);
        }
        for (Iterator i = residue_labels.iterator(); i.hasNext();) {
            RotatingText3D label = (RotatingText3D) i.next();

            label.alignToImagePlate(t3d);
        }
    }

    // draw the selection box
    private void drawSelection(PickRay ray1, PickRay ray2, 
        PickRay ray3, PickRay ray4) {
        selection_node.clear();
        LineAttributes la = new LineAttributes();

        la.setLineAntialiasingEnable(true);
        ColoringAttributes ca = new ColoringAttributes();

        ca.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        Appearance app = new Appearance();

        app.setLineAttributes(la);
        app.setColoringAttributes(ca);
        BranchGroup bg = new BranchGroup();

        bg.addChild(new RayRange(ray1, ray2, ray3, ray4,
                100.0f * SCENE_DIMENSION, app));
        selection_node.addBranchGroup(bg);
    }

    public void selectClosestSeqPoint(PickRay ray, boolean clear) {
        selection_node.clear();
        if (clear)
            props.clearSelections();

        if (seq_points.size() > 0
            && sequence_points_node.getVisibility() == BasicCanvas.VISIBLE) {
            int point_idx = seq_points.getClosestPoint(ray);

            if (point_idx >= 0) {
                Sequence seq = ss.getSequence(point_idx);

                props.setSeqSelect(seq,
                    !props.getSeqSelect(seq));
            }
        }
    }

    public void selectClosestSeqPoint(PickRay ray1, PickRay ray2, 
        PickRay ray3, PickRay ray4,
        boolean clear) {
        drawSelection(ray1, ray2, ray3, ray4);
        if (clear)
            props.clearSelections();

        if (seq_points.size() > 0
            && sequence_points_node.getVisibility() == BasicCanvas.VISIBLE) {
            int[] indexs = 
                seq_points.getPointsInRegion(ray1, ray2, ray3, ray4);

            if (indexs != null) {
                for (int i = indexs.length - 1; i >= 0; i--)
                    props.setSeqSelect(ss.getSequence(indexs[i]), true);
            }
        }
    }

    public void selectClosestResPoint(PickRay ray, boolean clear) {
        selection_node.clear();
        if (res_is_labelled == null)
            return;
        if (clear)
            Arrays.fill(res_is_labelled, false);

        if (res_points.size() > 0
            && residue_points_node.getVisibility() == BasicCanvas.VISIBLE) {
            int point_idx = res_points.getClosestPoint(ray);

            if (point_idx >= 0) {
                res_is_labelled[point_idx] = !res_is_labelled[point_idx];
                setAlignmentToResidue();
            }
            if (clear || point_idx >= 0)
                drawResidueLabels();
        } else if (clear)
            drawResidueLabels();
    }

    public void selectClosestResPoint(PickRay ray1, PickRay ray2, 
        PickRay ray3, PickRay ray4,
        boolean clear) {
        drawSelection(ray1, ray2, ray3, ray4);
        if (res_is_labelled == null)
            return;
        if (clear)
            Arrays.fill(res_is_labelled, false);

        if (res_points.size() > 0
            && residue_points_node.getVisibility() == BasicCanvas.VISIBLE) {
            int[] indexs = 
                res_points.getPointsInRegion(ray1, ray2, ray3, ray4);

            if (indexs != null) {
                for (int i = indexs.length - 1; i >= 0; i--)
                    res_is_labelled[indexs[i]] = true;
                setAlignmentToResidue();
            }
            if (clear || indexs != null)
                drawResidueLabels();
        } else if (clear)
            drawResidueLabels();
    }

    private void setAlignmentToResidue() {
        props.clearHighlights();

        Alignment alignment = owner.getAlignment();
        int min_s = alignment.size();
        int min_r = -1;
        SeqSpace.ResidueCoordinates[] res_coords = ss.getResCoords();

        for (int s = alignment.size() - 1; s >= 0; s--) {
            Sequence seq = alignment.getSequence(s);

            for (int k = res_is_labelled.length - 1; k >= 0; k--) {
                if (!res_is_labelled[k]) continue;

                int offset = res_coords[k].getIndex();

                if (offset < seq.length() 
                    && seq.getAA(offset).equals(res_coords[k].getAA())) {
                    props.setSeqHighlight(seq, offset, true);
                    if (s < min_s) {
                        min_s = s;
                        min_r = offset;
                    } else if (s == min_s && offset < min_r) 
                        min_r = offset;
                }
            }
        }
        if (min_s < alignment.size())
            owner.setAlignmentPanelPosition(min_s, min_r);
    }

    // DisplayPropertiesListener interface
    // DisplayPropertiesListener interface
    public void displayAnnViewChanged(DisplayProperties dp, 
        Sequence seq,
        boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp, 
        Sequence seq,
        boolean select) {
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        drawSequenceLabels();
    }

    public void displayColorSchemeChanged(DisplayProperties dp, 
        ColorScheme old) {}

    public void displayFontChanged(DisplayProperties dp) {}

    public void displayRenderGapsChanged(DisplayProperties dp) {}

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp, Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp, Sequence[] seqs) {}

}

