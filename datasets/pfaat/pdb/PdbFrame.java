package com.neogenesis.pfaat.pdb;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.j3d.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.DisplayPropertiesListener;
import com.neogenesis.pfaat.colorscheme.*;


/**
 * Frame for display of protein objects.
 *
 * @author $Author: xih $
 * @version $Revision: 1.5 $, $Date: 2002/10/15 13:58:28 $ */
public class PdbFrame extends JFrame implements DisplayPropertiesListener {
    private AlignmentFrame owner;
    private PdbStructure structure;
    private Sequence sequence;
    private DisplayProperties props;
    private Map chain_alignments;
    private PdbAtomContainer selection;

    // display widgets
    private BasicCanvas basic_canvas;
    private JPanel info_panel;
    private JLabel selected_label;

    // nodes in display
    private static final String ROOT_NODE = "Main Scene";
    private static final String STRUCTURE_NODE = "Structure";
    private static final String SELECTION_NODE = "Selection";

    private BasicCanvasNode root_node, structure_node, selection_node;
    private GroupManager structure_mode, selection_mode;

    // internal state
    private PdbAtom selected_atom;
    private ArrayList atom_list = new ArrayList();
    private PointSet atom_points = new PointSet(1.0);
    private Set selected_residues = new HashSet();

    public PdbFrame(AlignmentFrame owner,
        DisplayProperties props,
        Sequence sequence,
        PdbStructure structure) {
        super("Structure: " + structure.getName());

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    PdbFrame.this.props.removeListener(PdbFrame.this);
                    dispose();
                }
            }
        );

        // setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        this.owner = owner;
        this.structure = structure;
        this.props = props;
        this.sequence = sequence;

        chain_alignments = new HashMap(2 * structure.getChainCount() + 1);
        boolean aligned = false;

        for (int i = structure.getChainCount() - 1; i >= 0; i--) {
            PdbChain chain = structure.getChain(i);
            int idx = structure.getChain(i).getSequenceAlignment(sequence);

            if (idx >= 0) aligned = true;
            chain_alignments.put(chain, new Integer(idx));
        }
        if (!aligned)
            JOptionPane.showMessageDialog(owner,
                "PDB structure does not "
                + "align to sequence",
                "Warning",
                JOptionPane.WARNING_MESSAGE);

        JMenuBar menu_bar = new JMenuBar();

        setJMenuBar(menu_bar);
        JMenu view_menu = new JMenu("View");

        menu_bar.add(view_menu);
        structure_mode =
                new PdbStructureModeManager(PdbStructureModeManager.BACKBONE,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            drawStructure();
                        }
                    }
                );
        view_menu.add(structure_mode.getJMenu());
        selection_mode =
                new PdbSelectionModeManager(PdbSelectionModeManager.WIREFRAME,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            drawSelection();
                        }
                    }
                );
        view_menu.add(selection_mode.getJMenu());
        view_menu.addSeparator();
        JMenuItem import_seq_menu_item = new JMenuItem("Import Chain");

        import_seq_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (selected_atom == null) {
                        ErrorDialog.showErrorDialog(PdbFrame.this,
                            "No selected chain "
                            + "to import.");
                        return;
                    }
                    PdbChain chain = PdbFrame.this.structure.getChain(selected_atom);
                    AminoAcid[] aa = chain.getAAs();
                    Sequence new_sequence =
                        new Sequence("PDB_" +
                            PdbFrame.this.structure.getName() +
                            "_" +
                            chain.getName().trim(),
                            aa);

                    try {
                        PdbFrame.this.owner.getAlignment().insertSequence(0, new_sequence);
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(PdbFrame.this,
                            "Unable to insert "
                            + "new sequence.",
                            exp);
                        return;
                    }
                    PdbFrame.this.owner.setAlignmentPanelPosition(0, 0);
                }
            }
        );
        view_menu.add(import_seq_menu_item);

        allocateInfoPanel();
        allocateCanvas();
        resetState();

        basic_canvas.setSize(600, 400);
        JSplitPane main_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        main_pane.setTopComponent(info_panel);
        main_pane.setBottomComponent(basic_canvas.getJPanel());
        getContentPane().add(main_pane, BorderLayout.CENTER);

        pack();
    }

    private void resetState() {
        basic_canvas.removeAllNodes();
        root_node = basic_canvas.allocateNode(ROOT_NODE,
                    BasicCanvas.VISIBLE);
        structure_node = root_node.allocateLeaf(STRUCTURE_NODE,
                    BasicCanvas.VISIBLE);
        selection_node = root_node.allocateLeaf(SELECTION_NODE,
                    BasicCanvas.VISIBLE);
        atom_list.clear();
        atom_points.clear();
        selected_residues.clear();
        selected_atom = null;
        drawStructure();
        drawSelection();
        updateSelectionLabel();
    }

    private void drawStructure() {
        structure_node.clear();
        PdbAtomContainerRenderer renderer =
            (PdbAtomContainerRenderer) structure_mode.getSelection(); //this is set when user select the display mode from the menu
        PdbColorScheme cs = new PdbColorScheme() { //define the color scheme
            private Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
            private Color3f red = new Color3f(1.0f, 0.0f, 0.0f);
            private PdbColorScheme atomTypeCS = new PdbAtomTypeColorScheme();
            public void initialize(PdbStructure structure) {}

            public Color3f color(PdbAtom atom) {
                PdbChain chain = structure.getChain(atom);
                Integer align_idx = (Integer) chain_alignments.get(chain);
                if (align_idx.intValue() <0 ) //for the chains other than chain A
                    return white;
                else
                {
                    return atomTypeCS.color(atom);
                }
            }
        };

        try {
            BranchGroup bg = new BranchGroup();

            atom_list.clear();
            atom_points.clear();
            renderer.render(structure, cs, bg, atom_list); //draw the structure here
            Point3d[] points = new Point3d[atom_list.size()];

            for (int i = points.length - 1; i >= 0; i--)
                points[i] = ((PdbAtom) atom_list.get(i)).getPosition();
            boolean[] is_visible = new boolean[points.length];

            Arrays.fill(is_visible, true);
            atom_points.set(points, is_visible);
            structure_node.addBranchGroup(bg);
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(this, "Error drawing structure. " + e.getMessage());
        }
    }

    private void drawSelection() {
        selection_node.clear();
        //if (selected_residues.size() < 1) return;

        PdbAtomContainerRenderer renderer =
            (PdbAtomContainerRenderer) selection_mode.getSelection();
        PdbColorScheme cs = new PdbMonotoneColorScheme(Color.yellow);
        PdbAtomContainerRenderer atom_renderer =
            new PdbSpaceFillRenderer(0.5f);
        PdbColorScheme atom_cs = new PdbMonotoneColorScheme(Color.orange);

        try {
            PdbAtomContainer atom_container = new PdbAtomContainer();
            if (selected_atom != null)
                atom_container.addAtom(selected_atom); //do not select atom
            selection = new PdbAtomContainer();
            for (Iterator i = selected_residues.iterator(); i.hasNext();) {
                PdbResidue r = (PdbResidue) i.next();

                for (int k = r.getAtomCount() - 1; k >= 0; k--)
                    selection.addAtom(r.getAtom(k));
            }

            BranchGroup bg = new BranchGroup();

            renderer.render(selection, cs, bg, new ArrayList());
            atom_renderer.render(atom_container, atom_cs, bg, new ArrayList());
            selection_node.addBranchGroup(bg);
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(this, "Error drawing selection." + e.getMessage());
        }
    }

    private void updateSelectionLabel() {
        if (selected_atom == null)
            selected_label.setText("Nothing selected.");
        else {
            PdbResidue residue = structure.getResidue(selected_atom);
            PdbChain chain = structure.getChain(selected_atom);

            selected_label.setText("Atom: " + selected_atom.getType()
                + "." + selected_atom.getName()
                + " Residue: " + residue.getRawType()
                + "." + residue.getName()
                + " Chain: " + chain.getName());
        }
    }

    private void resetView() {
        Point3d center = structure.getCenter();
        double radius = structure.getRadius(center);

        basic_canvas.setDefaultViewPoint(new BoundingSphere(center, radius));
        basic_canvas.getRootTransformGroup().setTransform(new Transform3D());
    }

    private void allocateInfoPanel() {
        info_panel = new JPanel();
        GridBagLayout gb = new GridBagLayout();

        info_panel.setLayout(gb);
        int row = 0;

        selected_label = new JLabel("");
        Utils.addToGridBag(info_panel, gb,
            selected_label,
            0, row, 1, 1, 1, 1,
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
            1, row, 1, 1, 1, 1,
            GridBagConstraints.NONE, GridBagConstraints.CENTER);
        row++;
    }

    private void allocateCanvas() {
        basic_canvas = new BasicCanvas();
        resetView();

        // selection
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
                    selectClosestResidue(ray, true);
                }

                public void rayFired(PickRay ray1, PickRay ray2,
                    PickRay ray3, PickRay ray4) {
                    selectClosestResidue(ray1, ray2, ray3, ray4, true);
                }
            }
        );
        basic_canvas.addBehavior("Select and Clear", selection_behavior1);

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
                    selectClosestResidue(ray, false);
                }

                public void rayFired(PickRay ray1, PickRay ray2,
                    PickRay ray3, PickRay ray4) {
                    selectClosestResidue(ray1, ray2, ray3, ray4, false);
                }
            }
        );
        basic_canvas.addBehavior("Select", selection_behavior2);

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

    }

    public void selectClosestResidue(PickRay ray, boolean clear) {
        boolean redraw = false;

        if (clear) {
            selected_atom = null;
            selected_residues.clear();
            redraw = true;
        }
        if (atom_points.size() > 0
            && structure_node.getVisibility() == BasicCanvas.VISIBLE) {
            int point_idx = atom_points.getClosestPoint(ray);

            if (point_idx >= 0) {
                PdbAtom atom = (PdbAtom) atom_list.get(point_idx);

                selected_atom = atom;
                PdbResidue residue = structure.getResidue(atom);

                selected_residues.add(residue);
                redraw = true;
            }
        }
        if (redraw) {
            // drawSelection();
            // updateSelectionLabel();
            setAlignmentToResidue(); // update the displayProperty which will notify the structure view to refresh
        }
    }

    public void selectClosestResidue(PickRay ray1, PickRay ray2,
        PickRay ray3, PickRay ray4,
        boolean clear) {
        boolean redraw = false;

        if (clear) {
            selected_atom = null;
            selected_residues.clear();
            redraw = true;
        }
        if (atom_points.size() > 0
            && structure_node.getVisibility() == BasicCanvas.VISIBLE) {
            int[] indexs =
                atom_points.getPointsInRegion(ray1, ray2, ray3, ray4);

            if (indexs != null && indexs.length > 0) {
                selected_atom = (PdbAtom) atom_list.get(indexs[0]);
                for (int i = indexs.length - 1; i >= 0; i--) {
                    PdbAtom atom = (PdbAtom) atom_list.get(indexs[i]);
                    PdbResidue residue = structure.getResidue(atom);

                    selected_residues.add(residue);
                }
                redraw = true;
            }
        }
        if (redraw) {
            // drawSelection();
            // updateSelectionLabel();
            setAlignmentToResidue(); // update the displayProperty which will notify the structure view to refresh
        }
    }

    private void setAlignmentToResidue() {
        props.clearHighlights();
        if (selected_residues.size() < 1)
            return;

        Alignment alignment = owner.getAlignment();

        // set highlights
        PdbResidue residue = null;
        int r = -1;
        Object[] residues = selected_residues.toArray();

        for (int i = 0; i < residues.length; i++) {
            residue = (PdbResidue) residues[i];
            if (residue.getAtomCount() < 0)
                continue;
            PdbChain chain = structure.getChain(residue.getAtom(0));
            Integer align_idx = (Integer) chain_alignments.get(chain);

            if (align_idx == null || align_idx.intValue() < 0)
                continue;
            r = chain.getOffset(residue);
            if (r < 0) break;
            r = sequence.getAlignedIndex(r + align_idx.intValue());
            if (i != residues.length - 1)
                props.setSeqHighlight(sequence, r, true, false); // be careful with dead loops
            else
                props.setSeqHighlight(sequence, r, true, true); // now notify the displayProperty listener
        }

        // set the position
        r = -1;
        if (selected_atom != null) {
            PdbChain chain = structure.getChain(selected_atom);
            Integer align_idx = (Integer) chain_alignments.get(chain);

            if (align_idx != null && align_idx.intValue() >= 0) {
                r = chain.getOffset(structure.getResidue(selected_atom));
                if (r >= 0) {
                    r = sequence.getAlignedIndex(r + align_idx.intValue());
                    owner.setAlignmentPanelPosition(alignment.getIndex(sequence),
                        r);
                }
            }
        }
    }

    // DisplayPropertyListener interface
    public void displayAnnViewChanged(DisplayProperties dp,
        Sequence seq,
        boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp,
        Sequence seq,
        boolean select) {}

    public void displayColorSchemeChanged(DisplayProperties dp,
        ColorScheme old) {}

    public void displayFontChanged(DisplayProperties dp) {}

    public void displayRenderGapsChanged(DisplayProperties dp) {}

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq) {
        // get the alignment position, translate that to sequence number
        if (seq != sequence)
            return;
        boolean redraw = false;
        int[] idx = props.getHighlights(seq);

        if (idx == null) {
            if (selected_residues.size() != 0) { // clear selections
                selected_residues.clear();
                selected_atom = null;
                redraw = true;
            }
        }

        if (idx != null)
            for (int i = 0; i < idx.length; i++) {
                // !!!the index for sequence starts from 1 but idx for pdb residues starts from 0
                PdbResidue residue = structure.getChain(0).getResidue(seq.getRawIndex(idx[i]) - 1);

                if (residue != null/* && !selected_residues.contains(residue)*/) {
                    selected_residues.add(residue);
                    selected_atom = residue.getAtom(0); // set selected atom to the first atom for the residue.
                    redraw = true;
                }
            }

        if (redraw) {
            drawSelection();
            updateSelectionLabel();
        }

    }

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs) {
      // get the alignment position, translate that to sequence number
      displayHighlightsChanged(dp, sequence);
    }
}

