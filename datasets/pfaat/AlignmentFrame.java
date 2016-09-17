package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.text.JTextComponent;

import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.tree.*;
import com.neogenesis.pfaat.srs.*;
import com.neogenesis.pfaat.print.*;
import com.neogenesis.pfaat.io.*;
import com.neogenesis.pfaat.seqspace.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.colorscheme.*;


/**
 * Main class for multiple sequence alignment tool.
 *
 * @author $Author: xih $
 * @version $Revision: 1.33 $, $Date: 2002/12/06 17:10:59 $ */
public class AlignmentFrame extends JFrame
    implements DisplayPropertiesListener, AlignmentListener,
        NJTreeWorkerLauncher, SequenceListener, MouseListener {

    // define running mode constants
    public final static int APPLET_MODE = 0;
    public final static int PFIZER_MODE = 1;
    public final static int DEFAULT_MODE = PFIZER_MODE;
    private static int s_current_mode = 1; // default running mode

    // keep track of how many frames are open
    private static int frame_count = 0;
    JMenuItem nj_tree_menu_item;

    // menus
    private JCheckBoxMenuItem group_editing_menu_item, overwrite_menu_item,
        ann_popup_menu_item;
    private JRadioButtonMenuItem
        cons_none_menu_item,
        cons_pid_menu_item,
        cons_similarity_menu_item,
        cons_information_gapsrandomized_menu_item,
        cons_information_gapsexcluded_menu_item,
        cons_information_gapsincluded_menu_item,
        group_comparison_menu_item;

    private JCheckBoxMenuItem
        show_group_menu_item,
        show_ann_menu_item,
        show_resann_menu_item,
        show_resann_panel_menu_item,
        show_jnet_panel_menu_item,
        cons_autocalc_menu_item;

    private JPanel top_panel, bottom_left_panel, left_panel;
    private ButtonGroup bg;

    // group comparison stuff
    private JComboBox jcb3, jcb2;
    private GroupSelectionActionListener groupActionListener;
    private JComboBox negJComboBox3, negJComboBox2;
    private JLabel consensusTitle;

    private JPopupMenu popup;
    private FontManager fm;
    private ColorSchemeManager csm;

    // list of menus dependent on an alignment being loaded
    private Set alignment_dependent_menus = new HashSet();

    // sequence stuff
    private Alignment alignment;
    private ConsensusSequence consensus, consensus3, consensus2;
    private AlignmentPanel alignment_panel;
    private AlignmentNamePanel alignment_name_panel;
    private AlignmentGroupPanel alignment_group_panel;
    private AlignmentAnnPanel alignment_ann_panel;
    private ConsensusComponent consensus_component,
        consensus_component2, consensus_component3;
    private DisplayProperties props;
    private ScrollableViewport alignment_vp, left_vp, bottom_left_vp,
        top_vp, bottom_vp, analysis_vp, status_vp, analysis_name_vp;
    private ScrollableGridLayout overall_layout;
    private JPanel overall_panel, scroll_grid_panel;
    private JPanel statusBarPanel;
    private JScrollBar alignment_hsb, alignment_vsb;
    private LazyBoundedRangeModel hsb_model, vsb_model;
    private JMenuItem save_menu_item;

    // set file to null if you don't have a file.
    public AlignmentFrame(File file, Alignment alignment, String cs_hint)
        throws Exception {
        this(DEFAULT_MODE);
        setAlignment(file, alignment, cs_hint);
    }

    public AlignmentFrame(File file, Alignment alignment, String cs_hint, int mode)
        throws Exception {
        this(mode);
        setAlignment(file, alignment, cs_hint);
    }

    public AlignmentFrame() {
        this(DEFAULT_MODE);
    }

    public AlignmentFrame(int mode) {
        super("Protein Family Alignment Annotation Tool");
        // set mode
        if (mode >= 0)
            s_current_mode = mode;
        // keep track of when to exit
        frame_count++;

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (isAppletMode()) {
                        int option = JOptionPane.showConfirmDialog(AlignmentFrame.this,
                                "Close the Pfaat alignment?", "Closing Pfaat",
                                JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.NO_OPTION) {
                            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        } else {
                            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            dispose();
                        }
                    } else {
                        if (alignment != null) {

                            if (alignment.hasChanged()) {
                                JOptionPane.showMessageDialog(AlignmentFrame.this,
                                    "Please save current file.");
                                alignmentSaveAsQuery();
                            }
                        }
                        if (--frame_count < 1)
                            System.exit(0);
                        else
                            dispose();

                    }
                }
            }
        );

        // menus
        JMenuBar menu_bar = new JMenuBar();

        setJMenuBar(menu_bar);

        // add menus
        initFileMenu(menu_bar);

        initEditMenu(menu_bar);
        initViewMenu(menu_bar);
        initAnalysisMenu(menu_bar);
        initHelpMenu(menu_bar);

        // components
        scroll_grid_panel = new JPanel();
        scroll_grid_panel.setBackground(Color.white);
        ScrollableGridLayout layout = new ScrollableGridLayout(scroll_grid_panel, 3, 5, 0, 0);

        layout.setRowFixed(0, true);
        layout.setRowFixed(2, true);
        layout.setRowFixed(3, true);
        layout.setRowFixed(4, true);
        layout.setColumnFixed(0, true);
        layout.setColumnFixed(2, true);
        scroll_grid_panel.setLayout(layout);

        overall_panel = new JPanel();
        overall_layout = new ScrollableGridLayout(overall_panel, 1, 2, 0, 0);
        overall_layout.setRowFixed(0, true);
        // overall_layout.setRowFixed(1, true);
        // overall_layout.setColumnFixed(0, true);
        overall_panel.setLayout(overall_layout);

        // add alignment view panel
        alignment_vp = new ScrollableViewport();
        alignment_vp.setBackingStoreEnabled(false);
        alignment_vp.putClientProperty("EnableWindowBlit", null);
        alignment_vp.setBackground(Color.white);
        layout.setPosition(alignment_vp, 1, 1);
        scroll_grid_panel.add(alignment_vp);

        // add other panels
        left_vp = new ScrollableViewport();
        left_vp.setBackingStoreEnabled(false);
        left_vp.putClientProperty("EnableWindowBlit", null);
        left_vp.setBackground(Color.white);
        layout.setPosition(left_vp, 0, 1);
        scroll_grid_panel.add(left_vp);

        bottom_left_vp = new ScrollableViewport();
        bottom_left_vp.setBackingStoreEnabled(false);
        bottom_left_vp.putClientProperty("EnableWindowBlit", null);
        bottom_left_vp.setBackground(Color.white);
        bottom_left_vp.setAlignmentX(Component.RIGHT_ALIGNMENT);
        layout.setPosition(bottom_left_vp, 0, 3);
        scroll_grid_panel.add(bottom_left_vp);

        top_vp = new ScrollableViewport();
        top_vp.setBackingStoreEnabled(false);
        top_vp.putClientProperty("EnableWindowBlit", null);
        top_vp.setBackground(Color.white);
        layout.setPosition(top_vp, 1, 0);
        scroll_grid_panel.add(top_vp);

        bottom_vp = new ScrollableViewport();
        bottom_vp.setBackingStoreEnabled(false);
        bottom_vp.putClientProperty("EnableWindowBlit", null);
        bottom_vp.setBackground(Color.white);
        layout.setPosition(bottom_vp, 1, 3);
        scroll_grid_panel.add(bottom_vp);

        analysis_name_vp = new ScrollableViewport();
        analysis_name_vp.setBackingStoreEnabled(false);
        analysis_name_vp.putClientProperty("EnableWindowBlit", null);
        analysis_name_vp.setBackground(Color.white);
        layout.setPosition(analysis_name_vp, 0, 4);
        scroll_grid_panel.add(analysis_name_vp);

        analysis_vp = new ScrollableViewport();
        analysis_vp.setBackingStoreEnabled(false);
        analysis_vp.putClientProperty("EnableWindowBlit", null);
        analysis_vp.setBackground(Color.white);
        layout.setPosition(analysis_vp, 1, 4);
        scroll_grid_panel.add(analysis_vp);

        // add scrollbars
        alignment_hsb = new JScrollBar(JScrollBar.HORIZONTAL);
        hsb_model = new LazyBoundedRangeModel(false);
        alignment_hsb.setModel(hsb_model);
        layout.setPosition(alignment_hsb, 1, 2);
        scroll_grid_panel.add(alignment_hsb);

        alignment_vp.setHorizontalScrollbar(alignment_hsb);
        top_vp.setHorizontalViewport(alignment_vp);
        bottom_vp.setHorizontalViewport(alignment_vp);
        analysis_vp.setHorizontalViewport(alignment_vp);
        // status_vp.setHorizontalViewPort(left_vp);

        alignment_vsb = new JScrollBar(JScrollBar.VERTICAL);
        vsb_model = new LazyBoundedRangeModel(false);
        alignment_vsb.setModel(vsb_model);
        layout.setPosition(alignment_vsb, 2, 1);
        scroll_grid_panel.add(alignment_vsb);

        alignment_vp.setVerticalScrollbar(alignment_vsb);
        left_vp.setVerticalViewport(alignment_vp);
        // status_vp.setVerticalViewPort(bottom_vp);

        // add a border
        // Border raisedbevel = BorderFactory.createRaisedBevelBorder();
        // Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        // Border vp_border = BorderFactory.createCompoundBorder(raisedbevel,
        // loweredbevel);
        // scroll_grid_panel.setViewportBorder(vp_border);
        // Insets vp_insets = scroll_grid_panel.getInsets();
        // alignment_vp.setInsets(vp_insets);
        // left_vp.setInsets(vp_insets);
        // top_vp.setInsets(vp_insets);
        // bottom_vp.setInsets(vp_insets);
        String dispRow = alignment != null ? Integer.toString(props.getCursorRow()) : "";
        String dispCol = alignment != null ? Integer.toString(props.getCursorColumn()) : "";

        statusBarPanel = new JPanel();
        statusBarPanel.setLayout(new GridLayout(1, 2));
        statusBarPanel.add(new JLabel("", SwingConstants.LEFT));
        statusBarPanel.add(new JLabel("", SwingConstants.RIGHT));
        overall_layout.setPosition(statusBarPanel, 0, 0);
        overall_panel.add(statusBarPanel);
        overall_layout.setPosition(scroll_grid_panel, 0, 1);
        overall_panel.add(scroll_grid_panel);
        getContentPane().add(overall_panel);
        pack();
    }

    public void setConsensus() {
        if (cons_none_menu_item.isSelected()) {
            consensus_component3.setVisible(false);
            consensus_component2.setVisible(false);
            consensus_component.setVisible(false);
            setComparisonComponentsVisible(false, true);
            consensus_component.setConsensusType(ConsensusComponent.CONSENSUS_NONE);
        } else {
            if (!group_comparison_menu_item.isSelected()) {
                consensus_component.setComparison(false);
                consensus_component3.setVisible(false);
                consensus_component2.setVisible(false);
                consensus_component.setVisible(true);
                setComparisonComponentsVisible(false, false);
            }
            if (group_comparison_menu_item.isSelected()) {
                consensus_component3.setConsensusType(ConsensusComponent.CONSENSUS_PID);
                consensus_component2.setConsensusType(ConsensusComponent.CONSENSUS_PID);
                consensus_component.setConsensusType(ConsensusComponent.CONSENSUS_PIDDIF);
                consensus_component.setComparison(true);
                consensus_component.setComparisonConsensuses(consensus3, consensus2);
                consensus_component3.setVisible(true);
                consensus_component2.setVisible(true);
                consensus_component.setVisible(true);
                setComparisonComponentsVisible(true, false);
            }
            if (cons_pid_menu_item.isSelected()) {
                consensus_component.setConsensusType(ConsensusComponent.CONSENSUS_PID);
            } else if (cons_similarity_menu_item.isSelected()) {
                consensus_component.setConsensusType(ConsensusComponent.CONSENSUS_SIMILARITY);
            } else if (cons_information_gapsrandomized_menu_item.isSelected()) {
                consensus_component.setConsensusType(ConsensusComponent.CONSENSUS_INFORMATION_GAPSRANDOMIZED);
            } else if (cons_information_gapsexcluded_menu_item.isSelected()) {
                consensus_component.setConsensusType(ConsensusComponent.CONSENSUS_INFORMATION_GAPSEXCLUDED);
            } else if (cons_information_gapsincluded_menu_item.isSelected()) {
                consensus_component.setConsensusType(ConsensusComponent.CONSENSUS_INFORMATION_GAPSINCLUDED);
            }
        }
    }

    public void banishGroupSelectionComponentsFocus() {
        negJComboBox3.setNextFocusableComponent(alignment_panel);
        negJComboBox2.setNextFocusableComponent(alignment_panel);
        jcb3.setNextFocusableComponent(alignment_panel);
        jcb2.setNextFocusableComponent(alignment_panel);
        negJComboBox3.transferFocus();
        negJComboBox2.transferFocus();
        jcb3.transferFocus();
        jcb2.transferFocus();
    }

    // in order to let focus-changing keys pass to the alignment panel
    // send combobox focus to action_panel, which is unfocusable
    public class GroupSelectionActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == jcb3 || e.getSource() == negJComboBox3) {
                if (negJComboBox3.getSelectedIndex() == 1)
                    consensus3.setSeqs(alignment.getAllSequencesNotInGroup(jcb3.getSelectedItem().toString()));
                else if (negJComboBox3.getSelectedIndex() == 0)
                    consensus3.setSeqs(alignment.getAllGroupSequences(jcb3.getSelectedItem().toString()));
                consensus3.recalc();
            } else if (e.getSource() == jcb2 || e.getSource() == negJComboBox2) {
                if (negJComboBox2.getSelectedIndex() == 1)
                    consensus2.setSeqs(alignment.getAllSequencesNotInGroup(jcb2.getSelectedItem().toString()));
                else if (negJComboBox2.getSelectedIndex() == 0)
                    consensus2.setSeqs(alignment.getAllGroupSequences(jcb2.getSelectedItem().toString()));
                consensus2.recalc();
            }

            consensus.recalc();
            banishGroupSelectionComponentsFocus();
            getContentPane().validate();
            getContentPane().repaint();
        }
    }

    // set the active alignment
    private void setAlignment(File file, Alignment alignment, String cs_hint)
        throws Exception {
        this.alignment = alignment;
        if (file != null)
            alignment.setName(file.getName());
        else
            alignment.setName("Untitle" + frame_count + ".pfam");

        setTitle("Protein Family Alignment Annotation Tool: "
            + alignment.getName());
        if (alignment.getName().endsWith(".pfam")) {
            save_menu_item.setEnabled(true);
        } else {
            save_menu_item.setEnabled(false);
        }

        alignment.addListener(this);
        consensus = new ConsensusSequence(alignment, alignment.getAllSequences());
        boolean csloadfailed = false;
        ColorScheme cs = null;

        if (cs_hint != null && cs_hint.length() > 0) {
            if (cs_hint.equals("Blosum62"))
                cs = new Blosum62ColorScheme(consensus);
            else if (cs_hint.equals("Percentage Identity"))
                cs = new PIDColorScheme(consensus);
            else if (cs_hint.equals("default"))
                cs = new DefaultColorScheme(consensus);
            else
            if (!isAppletMode()) { // need to be modified to facilitate applet version of pfaat
                try {
                    File f = new File(PathManager.getColorSchemeDirectory(),
                            cs_hint + ".csm");

                    if (f.exists())
                        cs = new ResidueColorScheme(consensus, f);
                    else {
                        f = new File(file.getParentFile(),
                                    cs_hint + ".csm");
                        if (f.exists())
                            cs = new ResidueColorScheme(consensus, f);
                        else {
                            ErrorDialog.showErrorDialog(this,
                                "Unable to load color scheme "
                                + cs_hint + ".csm");
                            csloadfailed = true;
                        }
                    }
                } catch (Exception e) {
                    ErrorDialog.showErrorDialog(this,
                        "Unable to load color scheme "
                        + cs_hint + ".csm"
                        + ": ",
                        e);
                    csloadfailed = true;
                }
            }
        }
        if (cs == null || csloadfailed)
            cs = new DefaultColorScheme(consensus); // change it to clustx as default
        if (props != null)
            props.removeListener(this);
        Font font = new Font("Courier", Font.PLAIN, 12);

        clearStatusBarPanel();
        props = new DisplayProperties(alignment, cs, font, getFontMetrics(font), true, true);
        props.addListener(this);
        fm.setDisplayProperties(props);
        csm.setDisplayProperties(props);

        // set up the display
        alignment_panel = new AlignmentPanel(AlignmentFrame.this, this.alignment, props);
        alignment_vp.setView(alignment_panel);

        alignment_name_panel = new AlignmentNamePanel(this, alignment, props);
        alignment_group_panel = new AlignmentGroupPanel(alignment, props);
        alignment_group_panel.setVisible(show_group_menu_item.getState());
        alignment_ann_panel = new AlignmentAnnPanel(alignment, props);
        alignment_ann_panel.setVisible(show_ann_menu_item.getState());
        left_panel = new JPanel();
        left_panel.setBackground(Color.white);
        props.updateCursor(0, 1);
        // we should just be able to use a BoxLayout, but this doesn't
        // work correctly with invisible components on the SGI
        // left_panel.setLayout(new BoxLayout(left_panel, BoxLayout.X_AXIS));
        // alignment_name_panel.setAlignmentY(0.0f);
        // left_panel.add(alignment_name_panel);
        // alignment_ann_panel.setAlignmentY(0.0f);
        // left_panel.add(alignment_ann_panel);
        ScrollableGridLayout layout = new ScrollableGridLayout(left_panel, 3, 1, 0, 0);

        layout.setRowFixed(0, true);
        layout.setColumnFixed(0, true);
        layout.setColumnFixed(1, true);
        layout.setColumnFixed(2, true);
        left_panel.setLayout(layout);
        layout.setPosition(alignment_name_panel, 0, 0);
        left_panel.add(alignment_name_panel);
        layout.setPosition(alignment_group_panel, 1, 0);
        left_panel.add(alignment_group_panel);
        layout.setPosition(alignment_ann_panel, 2, 0);
        left_panel.add(alignment_ann_panel);

        left_vp.setView(left_panel);
        bottom_left_panel = new JPanel();
        bottom_left_panel.setBackground(Color.white);

        GridBagLayout gb = new GridBagLayout();

        bottom_left_panel.setLayout(gb);
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;

        font = props.getFont();
        Font boldFont = new Font(font.getName(), Font.BOLD, font.getSize());
        String[] groupNames;

        if (alignment.getAllGroupNames() != null)
            groupNames = alignment.getAllDisplayGroupNames((alignment.getAllGroupNames())[0]);
        else
            groupNames = alignment.getAllDisplayGroupNames(null);
        JPanel jcb3Panel = new JPanel();

        jcb3Panel.setLayout(new BoxLayout(jcb3Panel, BoxLayout.X_AXIS));
        jcb3 = new JComboBox(groupNames);
        groupActionListener = new GroupSelectionActionListener();
        jcb3.addActionListener(groupActionListener);
        jcb3.addMouseListener(this);
        jcb3.setFont(boldFont);
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        gb.setConstraints(jcb3Panel, c);
        negJComboBox3 = new JComboBox();
        negJComboBox3.addItem(new String("in"));
        negJComboBox3.addItem(new String("not in"));
        negJComboBox3.setFont(props.getFont());
        negJComboBox3.addActionListener(groupActionListener);
        jcb3Panel.add(negJComboBox3);
        jcb3Panel.add(jcb3);
        bottom_left_panel.add(jcb3Panel);

        JPanel jcb2Panel = new JPanel();

        jcb2Panel.setLayout(new BoxLayout(jcb2Panel, BoxLayout.X_AXIS));
        jcb2 = new JComboBox(groupNames);
        jcb2.addActionListener(groupActionListener);
        jcb2.addMouseListener(this);
        jcb2.setFont(boldFont);

        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(2 * props.getResidueHeight(), 0, 0, 0);
        gb.setConstraints(jcb2Panel, c);
        negJComboBox2 = new JComboBox();
        negJComboBox2.addItem(new String("in"));
        negJComboBox2.addItem(new String("not in"));
        negJComboBox2.setFont(props.getFont());
        negJComboBox2.addActionListener(groupActionListener);
        jcb2Panel.add(negJComboBox2);
        jcb2Panel.add(jcb2);
        bottom_left_panel.add(jcb2Panel);
        consensusTitle = new JLabel("Group Difference");
        consensusTitle.setForeground(Color.black);
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets((int) (1.8 * (float) props.getResidueHeight()), 0, 0, 0);
        gb.setConstraints(consensusTitle, c);
        bottom_left_panel.add(consensusTitle);
        bottom_left_vp.setView(bottom_left_panel);

        top_panel = new JPanel();
        top_panel.setBackground(Color.white);
        top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.Y_AXIS));
        top_panel.add(new ColumnAnnotationComponent(alignment, props));
        top_panel.add(new RulerComponent(alignment, props));
        top_panel.add(new RulerAnnotationComponent(alignment, props, AlignmentFrame.this));
        top_vp.setView(top_panel);

        JPanel bottom_panel = new JPanel();

        bottom_panel.setBackground(Color.white);
        bottom_panel.setLayout(new BoxLayout(bottom_panel, BoxLayout.Y_AXIS));

        consensus3 = new ConsensusSequence(alignment, alignment.getAllGroupSequences(jcb3.getSelectedItem().toString()));
        consensus2 = new ConsensusSequence(alignment, alignment.getAllGroupSequences(jcb2.getSelectedItem().toString()));
        consensus_component3 = new ConsensusComponent(consensus3, props);
        consensus_component2 = new ConsensusComponent(consensus2, props);
        consensus_component = new ConsensusComponent(consensus, props);
        setConsensus();

        bottom_panel.add(consensus_component3);
        bottom_panel.add(consensus_component2);
        bottom_panel.add(consensus_component);
        bottom_vp.setView(bottom_panel);
        analysis_name_vp.setView(new AnalysisNamePanel());
        analysis_vp.setView(new AnalysisPanel(props));
        // reenable some menu items
        for (Iterator i = alignment_dependent_menus.iterator(); i.hasNext();)
            ((JMenuItem) i.next()).setEnabled(true);

        group_editing_menu_item.setState(true);
        props.setGroupEditing(group_editing_menu_item.getState());

        padAlignment();
        alignment.setChanged(false);

    }

    public void redrawAlignmentAnnPanel() {
        alignment_ann_panel.revalidate();
        alignment_ann_panel.repaint();
    }

    public void redrawAlignmentGroupPanel() {
        alignment_group_panel.revalidate();
        alignment_group_panel.repaint();
    }

    public void setComparisonComponentsVisible(boolean b, boolean noneb) {
        for (int i = 0; i < bottom_left_panel.countComponents(); i++) {
            Component c = bottom_left_panel.getComponent(i);

            if (!b && c.equals(consensusTitle) && !noneb) c.setVisible(true);
            else c.setVisible(b);
            if (!noneb) {
                if (b) consensusTitle.setText("Group Difference");
                else if (!b) {
                    consensusTitle.setText(getConsensusName());
                }
            }
        }
    }

    public String getConsensusName() {
        if (cons_pid_menu_item.isSelected()) return "% Identity";
        else if (cons_similarity_menu_item.isSelected()) return "Similarity";
        else if (cons_information_gapsrandomized_menu_item.isSelected()) return "Info - Gaps Randomized";
        else if (cons_information_gapsexcluded_menu_item.isSelected()) return "Info - Gaps Excluded";
        else if (cons_information_gapsincluded_menu_item.isSelected()) return "Info - Gaps Included";
        else return "none";
    }

    public void alignmentSaveAsQuery() {
        if (isAppletMode()) {
            // put up a dialog warning read-only mode
            return;
        }

        JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

        AlignmentLoader.addFileFilters(fc);
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setSelectedFile(new File(PathManager.getAlignmentPath(),
                AlignmentFrame.this.alignment.getName()));
        if (fc.showDialog(AlignmentFrame.this, "Save As") == JFileChooser.APPROVE_OPTION) {

            try {
                if (!(AlignmentLoader.getAlignmentLoader(fc.getSelectedFile())
                        instanceof PfamLoader)) {
                    if (JOptionPane.showConfirmDialog(AlignmentFrame.this,
                            "Warning: annotations will not be saved in this file format.",
                            "Save file " + fc.getSelectedFile(),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE)
                        == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                PathManager.setAlignmentPath(fc.getCurrentDirectory());
                AlignmentLoader.saveAlignmentFile(fc.getSelectedFile(),
                    props.getColorScheme(),
                    AlignmentFrame.this.alignment);
                AlignmentFrame.this.alignment.setName(fc.getSelectedFile().getName());
                AlignmentFrame.this.alignment.setChanged(false);
            } catch (Exception e) {
                ErrorDialog.showErrorDialog(AlignmentFrame.this,
                    "Unable to save "
                    + "alignment: ",
                    e);
            }
        }
    }

    // initialize file menu bar
    private void initFileMenu(JMenuBar menu_bar) {

        JMenu file_menu = new JMenu("File");

        menu_bar.add(file_menu);
        JMenuItem open_menu_item = new JMenuItem("Open");

        open_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (alignment != null) {
                        if (alignment.hasChanged()) {
                            JOptionPane.showMessageDialog(AlignmentFrame.this,
                                "Opening a new file will overwrite current file. " +
                                "Please save current file.");
                            alignmentSaveAsQuery();
                        }
                    }

                    JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                    AlignmentLoader.addFileFilters(fc);
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    if (fc.showDialog(AlignmentFrame.this, "Open") == JFileChooser.APPROVE_OPTION) {
                        try {
                            PathManager.setAlignmentPath(fc.getCurrentDirectory());
                            File f = fc.getSelectedFile();
                            Alignment new_alignment = AlignmentLoader.loadAlignmentFile(f);
                            String cs_hint = AlignmentLoader.getColorSchemeHint(f);

                            setAlignment(f, new_alignment, cs_hint);

                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                "Unable to open " + "alignment: ", e);
                        }
                    }
                }

            }
        );

        file_menu.add(open_menu_item);
        JMenuItem open_new_menu_item = new JMenuItem("Open in New Window");

        open_new_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                    AlignmentLoader.addFileFilters(fc);
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);

                    if (fc.showDialog(AlignmentFrame.this, "Open") == JFileChooser.APPROVE_OPTION) {
                        try {
                            PathManager.setAlignmentPath(fc.getCurrentDirectory());
                            File f = fc.getSelectedFile();
                            Alignment new_alignment = AlignmentLoader.loadAlignmentFile(f);
                            String cs_hint = AlignmentLoader.getColorSchemeHint(f);
                            AlignmentFrame af = new AlignmentFrame(f, new_alignment, cs_hint);

                            af.show();
                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                "Unable to open " + "alignment: ", e);
                        }
                    }
                }
            }
        );
        file_menu.add(open_new_menu_item);

        JMenuItem save_as_menu_item = new JMenuItem("Save As");

        addAlignmentDependentMenuItem(save_as_menu_item);
        save_as_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    alignmentSaveAsQuery();
                }
            }
        );
        file_menu.add(save_as_menu_item);

        // need to filter activation of this item on .pfam format
        save_menu_item = new JMenuItem("Save");
        save_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (JOptionPane.showConfirmDialog(AlignmentFrame.this,
                                "Save " + AlignmentFrame.this.alignment.getName(),
                                "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            AlignmentLoader.saveAlignmentFile(new File(PathManager.getAlignmentPath(),
                                    AlignmentFrame.this.alignment.getName()),
                                props.getColorScheme(),
                                AlignmentFrame.this.alignment);
                            JOptionPane.showMessageDialog(AlignmentFrame.this,
                                "Saved file "
                                + AlignmentFrame.this.alignment.getName());
                            AlignmentFrame.this.alignment.setChanged(false);
                        }
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Unable to save "
                            + "alignment: ",
                            e);
                    }
                }
            }
        );
        save_menu_item.setEnabled(false);
        file_menu.add(save_menu_item);

        file_menu.addSeparator();
        JMenuItem print_menu_item = new JMenuItem("Print");

        addAlignmentDependentMenuItem(print_menu_item);
        print_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (props.isFastRender()) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Cannot print in "
                            + "fast rendering mode.");
                        return;
                    }
                    consensus3.recalc();
                    consensus2.recalc();
                    consensus.recalc();	 // re-calculate consensus
                    getContentPane().validate();
                    getContentPane().repaint();

                    PrintGrid printable = new PrintGrid(2, 5);

                    printable.addComponent(top_vp.getView(), 1, 0);
                    printable.addComponent(left_vp.getView(), 0, 1);
                    printable.addComponent(bottom_left_vp.getView(), 0, 3);
                    printable.addComponent(alignment_vp.getView(), 1, 1);
                    printable.addComponent(bottom_vp.getView(), 1, 2);
                    printable.addComponent(analysis_name_vp.getView(), 0, 4);
                    printable.addComponent(analysis_vp.getView(), 1, 3);
                    PrintPSDialog pd = new PrintPSDialog(AlignmentFrame.this, printable);

                    pd.show();
                }
            }
        );
        file_menu.add(print_menu_item);

        file_menu.addSeparator();

        JMenuItem srs_query_menu_item = new JMenuItem("SRS Query");

        addAlignmentDependentMenuItem(srs_query_menu_item);
        srs_query_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    SRSQueryFrame f = new SRSQueryFrame(AlignmentFrame.this);

                    f.show();
                }
            }
        );
        file_menu.add(srs_query_menu_item);

        file_menu.addSeparator();
        JMenuItem quit_menu_item = new JMenuItem("Quit");

        quit_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (JOptionPane.showConfirmDialog(AlignmentFrame.this,
                            "Quit Pfaat?",
                            "Quit",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        if (alignment != null) {
                            if (alignment.hasChanged()) {
                                if (JOptionPane.showConfirmDialog(AlignmentFrame.this,
                                        "Save Changes?",
                                        "Save Changes",
                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                    JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                                    AlignmentLoader.addFileFilters(fc);
                                    fc.setDialogType(JFileChooser.SAVE_DIALOG);
                                    fc.setSelectedFile(new File(PathManager.getAlignmentPath(),
                                            AlignmentFrame.this.alignment.getName()));

                                    if (fc.showDialog(AlignmentFrame.this, "Save As")
                                        == JFileChooser.APPROVE_OPTION) {
                                        try {
                                            if (!(AlignmentLoader.getAlignmentLoader(fc.getSelectedFile())
                                                    instanceof PfamLoader)) {
                                                if (JOptionPane.showConfirmDialog(AlignmentFrame.this,
                                                        "Warning: annotations will not be saved in this file format.",
                                                        "Save file " + fc.getSelectedFile(),
                                                        JOptionPane.OK_CANCEL_OPTION,
                                                        JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
                                                    return;
                                                }
                                            }
                                            PathManager.setAlignmentPath(fc.getCurrentDirectory());
                                            AlignmentLoader.saveAlignmentFile(fc.getSelectedFile(),
                                                props.getColorScheme(),
                                                AlignmentFrame.this.alignment);
                                            AlignmentFrame.this.alignment.setName(fc.getSelectedFile().getName());
                                        } catch (Exception e) {
                                            ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                                "Unable to save "
                                                + "alignment: ",
                                                e);
                                        }
                                    }
                                }
                            }
                        }
                        System.exit(0);
                    }
                }
            }
        );
        file_menu.add(quit_menu_item);
        if (isAppletMode())
            file_menu.setVisible(false);
    }

    // initialize Edit menu bar
    private void initEditMenu(JMenuBar menu_bar) {
        JMenu edit_menu = new JMenu("Edit");

        menu_bar.add(edit_menu);

        JMenuItem select_all_menu_item = new JMenuItem("Select All");

        addAlignmentDependentMenuItem(select_all_menu_item);
        select_all_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.allSelections(AlignmentFrame.this.alignment);
                }
            }
        );
        edit_menu.add(select_all_menu_item);

        JMenuItem deselect_all_menu_item = new JMenuItem("Deselect All");

        addAlignmentDependentMenuItem(deselect_all_menu_item);
        deselect_all_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.clearSelections();
                }
            }
        );
        edit_menu.add(deselect_all_menu_item);

        JMenuItem invert_select_menu_item = new JMenuItem("Invert Selections");

        addAlignmentDependentMenuItem(invert_select_menu_item);
        invert_select_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.invertSelections(AlignmentFrame.this.alignment);
                }
            }
        );
        edit_menu.add(invert_select_menu_item);

        JMenuItem delete_select_menu_item = new JMenuItem("Delete Selections");

        addAlignmentDependentMenuItem(delete_select_menu_item);
        delete_select_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (props.getSelectedCount() > 0
                        && JOptionPane.showConfirmDialog(AlignmentFrame.this,
                            "Delete selected sequences (" + props.getSelectedCount() + " total)?",
                            "Delete Sequences",
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        Alignment a = AlignmentFrame.this.alignment;

                        for (int i = a.size() - 1; i >= 0; i--) {
                            if (props.getSeqSelect(a.getSequence(i)))
                                a.deleteSequence(i);
                        }
                    }
                }
            }
        );
        edit_menu.add(delete_select_menu_item);

        JMenuItem clustalw_align_menu_item = new JMenuItem("ClustalW Alignment");

        addAlignmentDependentMenuItem(clustalw_align_menu_item);
        clustalw_align_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ClustalWDialog clustalwDialog = new ClustalWDialog(AlignmentFrame.this,
                            AlignmentFrame.this.alignment, props);

                    clustalwDialog.show();
                }
            }
        );
        edit_menu.add(clustalw_align_menu_item);
        JMenuItem hmmer_align_menu_item = new JMenuItem("HMMER Alignment");

        addAlignmentDependentMenuItem(hmmer_align_menu_item);

        hmmer_align_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    HMMERDialog hmmerDialog = new HMMERDialog(AlignmentFrame.this,
                            AlignmentFrame.this.alignment, props);

                    hmmerDialog.show();
                }
            }
        );
        edit_menu.add(hmmer_align_menu_item);

        JMenuItem remove_redundant_seqs_menu_item = new JMenuItem("Remove Redundant Sequences");

        addAlignmentDependentMenuItem(remove_redundant_seqs_menu_item);
        remove_redundant_seqs_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JDialog d = new RemoveRedundantSequencesDialog(AlignmentFrame.this, alignment, props);

                    d.show();
                }
            }
        );
        // edit_menu.add(remove_redundant_seqs_menu_item);

        edit_menu.addSeparator();

        JMenuItem add_to_group_menu_item =
            new JMenuItem("Add Selections to Group");

        addAlignmentDependentMenuItem(add_to_group_menu_item);
        add_to_group_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (props.getSelectedCount() < 1) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "No sequences selected.");
                        return;
                    } else {
                        GroupDialog f = new GroupDialog(AlignmentFrame.this, alignment, props);

                        f.show();
                    }
                }
            }
        );
        edit_menu.add(add_to_group_menu_item);

        JMenuItem remove_from_group_menu_item = new JMenuItem("Remove Selections from Group");

        addAlignmentDependentMenuItem(remove_from_group_menu_item);
        remove_from_group_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (props.getAllSelected().length < 1) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Selected sequence/s do not belong to any groups.");
                        return;
                    } else {
                        RemoveGroupDialog f =
                            new RemoveGroupDialog(AlignmentFrame.this, alignment, props);

                        f.show();
                    }
                }
            }
        );
        edit_menu.add(remove_from_group_menu_item);

        JMenuItem display_in_new_frame_menu_item = new JMenuItem("Display Selections in New Frame");

        addAlignmentDependentMenuItem(display_in_new_frame_menu_item);
        display_in_new_frame_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    // get the selected sequences and open in a new frame
                    if (props.getAllSelected().length < 1) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "No sequence selected.");
                        return;
                    } else {
                        Sequence[] sub_seqs = props.getAllSelected();

                        try {
                            Alignment sub_align_temp = new Alignment("SubAlignment.pfam", sub_seqs, false, null);
                            String cs_hint = props.getColorScheme().getName();

                            StringWriter writer = new StringWriter();
                            PfamLoader loader = new PfamLoader();

                            loader.saveAlignment(writer, sub_align_temp, props.getColorScheme());
                            writer.flush();

                            // reload the alignment file
                            // File file = new File(PathManager.getAlignmentPath(), "SubAlignment.pfam");
                            StringReader reader = new StringReader(writer.toString());
                            Alignment sub_align = loader.loadAlignment(reader);
                            AlignmentFrame f = new AlignmentFrame(null, sub_align, cs_hint, getCurrentMode());

                            f.show();
                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                "Unable to show the sub-alignment."
                                + "alignment: ",
                                e);
                        }
                    }
                }
            }
        );

        edit_menu.add(display_in_new_frame_menu_item);

        edit_menu.addSeparator();

        JMenuItem clear_highlights_menu_item = new JMenuItem("Clear Highlighted Residues");

        addAlignmentDependentMenuItem(clear_highlights_menu_item);
        clear_highlights_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.clearHighlights();
                }
            }
        );
        edit_menu.add(clear_highlights_menu_item);

        group_editing_menu_item = new JCheckBoxMenuItem("Group Editing", false);
        addAlignmentDependentMenuItem(group_editing_menu_item);
        group_editing_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.setGroupEditing(group_editing_menu_item.getState());
                }
            }
        );
        edit_menu.add(group_editing_menu_item);
        overwrite_menu_item = new JCheckBoxMenuItem("Allow Overwrite", false);
        addAlignmentDependentMenuItem(overwrite_menu_item);
        overwrite_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.setOverwrite(overwrite_menu_item.getState());
                }
            }
        );
        edit_menu.add(overwrite_menu_item);
        edit_menu.addSeparator();

        JMenuItem find_menu_item = new JMenuItem("Find Sequence String");

        addAlignmentDependentMenuItem(find_menu_item);
        find_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FindDialog d = new FindDialog(AlignmentFrame.this, props, AlignmentFrame.this.alignment);

                    d.show();
                }
            }
        );
        edit_menu.add(find_menu_item);

        edit_menu.addSeparator();
        JMenuItem edit_annotations_menu_item = new JMenuItem("Edit Annotations");

        addAlignmentDependentMenuItem(edit_annotations_menu_item);
        edit_annotations_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    EditAnnotationsDialog f = new EditAnnotationsDialog(null, alignment, props);

                    f.show();
                }
            }
        );
        edit_menu.add(edit_annotations_menu_item);
        edit_menu.addSeparator();

        JMenuItem pad_menu_item = new JMenuItem("Pad Alignment with Gaps");

        addAlignmentDependentMenuItem(pad_menu_item);
        pad_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    padAlignment();
                }
            }
        );

        edit_menu.add(pad_menu_item);

        JMenuItem delete_gap_menu_item = new JMenuItem("Delete Alignment Gap Columns");

        addAlignmentDependentMenuItem(delete_gap_menu_item);
        delete_gap_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane.showConfirmDialog(AlignmentFrame.this,
                            "Delete gap columns in alignment?",
                            "Delete Gap Columns",
                            JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                        int i = 1;
                        int shift = 0;
                        int maxLength = alignment.maxLength();
                        Sequence s = alignment.getSequence(0);

                        while (i < maxLength) {
                            shift = 0;
                            while (i + shift < maxLength && alignment.isColumnAllGaps(i + shift)) shift++;
                            if (shift > 0) {
                                maxLength -= shift;
                                for (int k = 0; k < alignment.size(); k++) {
                                    s = alignment.getSequence(k);
                                    if (i + shift < s.length()) {
                                        try {
                                            s.shiftAA(i + shift, -shift);
                                        } catch (Exception exp) {
                                            ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                                "Unable to delete column in sequence " +
                                                s.getName());
                                        }
                                    }
                                }
                            }
                            i++;
                        }

                        // trim C-terminus gaps
                        i = alignment.maxLength();
                        while (i > 1 && alignment.isColumnAllGaps(i)) {
                            for (int k = 0; k < alignment.size(); k++) {
                                s = alignment.getSequence(k);
                                try {
                                    if (i < s.length())
                                        s.deleteAA(i);
                                } catch (Exception exp) {
                                    ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                        "Unable to delete column in sequence " +
                                        s.getName());
                                }
                            }
                            i--;
                        }
                    }
                }
            }
        );
        edit_menu.add(delete_gap_menu_item);
        if (isAppletMode())
            edit_menu.setVisible(false);
        // disable this function

    }

    private void initViewMenu(JMenuBar menu_bar) {
        JMenu view_menu = new JMenu("View");

        menu_bar.add(view_menu);

        show_group_menu_item = new JCheckBoxMenuItem("Show Group Memberships", true);
        addAlignmentDependentMenuItem(show_group_menu_item);
        show_group_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    alignment_group_panel.setVisible(show_group_menu_item.getState());
                }
            }
        );
        view_menu.add(show_group_menu_item);

        show_ann_menu_item = new JCheckBoxMenuItem("Show Sequence Annotations", false);
        addAlignmentDependentMenuItem(show_ann_menu_item);
        show_ann_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    alignment_ann_panel.setVisible(show_ann_menu_item.getState());
                }
            }
        );
        view_menu.add(show_ann_menu_item);

        show_resann_menu_item = new JCheckBoxMenuItem("Show Residue Annotations", true);
        addAlignmentDependentMenuItem(show_resann_menu_item);
        show_resann_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.setShowResAnn(show_resann_menu_item.getState());
                    // hack until I build listener
                    displayHighlightsChanged(props, alignment.getSequence(0));
                }
            }
        );
        view_menu.add(show_resann_menu_item);

        show_resann_panel_menu_item = new JCheckBoxMenuItem("Show Residue Annotation Panel", true);
        addAlignmentDependentMenuItem(show_resann_panel_menu_item);
        show_resann_panel_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.setShowResAnnPanel(top_panel.getComponent(0), show_resann_panel_menu_item.getState());
                    // hack until I build listener
                    displayHighlightsChanged(props, alignment.getSequence(0));
                }
            }
        );
        view_menu.add(show_resann_panel_menu_item);

        show_jnet_panel_menu_item = new JCheckBoxMenuItem("Show Jnet Analysis Panel", true);
        addAlignmentDependentMenuItem(show_jnet_panel_menu_item);
        show_jnet_panel_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    analysis_name_vp.setVisible(show_jnet_panel_menu_item.getState());
                    analysis_vp.setVisible(show_jnet_panel_menu_item.getState());
                    // hack until I build listener
                    displayHighlightsChanged(props, alignment.getSequence(0));
                }
            }
        );
        view_menu.add(show_jnet_panel_menu_item);

        ann_popup_menu_item = new JCheckBoxMenuItem("Annotation Pop-Up Enabled", true);
        addAlignmentDependentMenuItem(ann_popup_menu_item);
        ann_popup_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    props.setAnnotationPopup(ann_popup_menu_item.getState());
                }
            }
        );
        view_menu.add(ann_popup_menu_item);
        view_menu.addSeparator();

        JMenu cons_display_menu = new JMenu("Consensus Display");

        addAlignmentDependentMenuItem(cons_display_menu);
        view_menu.add(cons_display_menu);

        bg = new ButtonGroup();

        ActionListener cons_listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    setConsensus();
                }
            };

        cons_none_menu_item = new JRadioButtonMenuItem("None", false);
        cons_none_menu_item.addActionListener(cons_listener);
        bg.add(cons_none_menu_item);
        cons_display_menu.add(cons_none_menu_item);

        cons_pid_menu_item =
                new JRadioButtonMenuItem("Percentage Identity", false);
        cons_pid_menu_item.addActionListener(cons_listener);
        bg.add(cons_pid_menu_item);
        cons_display_menu.add(cons_pid_menu_item);

        cons_similarity_menu_item =
                new JRadioButtonMenuItem("Similarity", true);
        cons_similarity_menu_item.addActionListener(cons_listener);
        bg.add(cons_similarity_menu_item);
        cons_display_menu.add(cons_similarity_menu_item);

        cons_information_gapsrandomized_menu_item =
                new JRadioButtonMenuItem("Information Content - Gaps Randomized", false);
        cons_information_gapsrandomized_menu_item.addActionListener(cons_listener);
        bg.add(cons_information_gapsrandomized_menu_item);
        cons_display_menu.add(cons_information_gapsrandomized_menu_item);

        cons_information_gapsexcluded_menu_item =
                new JRadioButtonMenuItem("Information Content - Gaps Excluded", false);
        cons_information_gapsexcluded_menu_item.addActionListener(cons_listener);
        bg.add(cons_information_gapsexcluded_menu_item);
        cons_display_menu.add(cons_information_gapsexcluded_menu_item);

        cons_information_gapsincluded_menu_item =
                new JRadioButtonMenuItem("Information Content - Gaps Included", false);
        cons_information_gapsincluded_menu_item.addActionListener(cons_listener);
        bg.add(cons_information_gapsincluded_menu_item);
        cons_display_menu.add(cons_information_gapsincluded_menu_item);

        group_comparison_menu_item = new JRadioButtonMenuItem("Group Comparison", false);
        group_comparison_menu_item.addActionListener(cons_listener);
        bg.add(group_comparison_menu_item);
        cons_display_menu.add(group_comparison_menu_item);
        cons_display_menu.addSeparator();

        JMenu color_scheme_menu = new JMenu("Color Scheme");

        addAlignmentDependentMenuItem(color_scheme_menu);
        csm = new ColorSchemeManager(this, color_scheme_menu, props);

        view_menu.add(color_scheme_menu);

        view_menu.addSeparator();
        JMenu render_menu = new JMenu("Fonts/Rendering");

        fm = new FontManager(this, render_menu, props);
        addAlignmentDependentMenuItem(render_menu);
        view_menu.add(render_menu);

        view_menu.addSeparator();
        JCheckBoxMenuItem lazy_scrollbar_menu_item =

            new JCheckBoxMenuItem("Lazy Scrollbars", false);

        lazy_scrollbar_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                        hsb_model.setLazy(true);
                        vsb_model.setLazy(true);
                    } else {
                        hsb_model.setLazy(false);
                        vsb_model.setLazy(false);
                    }
                }
            }
        );
        view_menu.add(lazy_scrollbar_menu_item);

        // disable the informatioin content analysis
        //cons_information_gapsrandomized_menu_item.setVisible(false);
        //cons_information_gapsexcluded_menu_item.setVisible(false);
        //cons_information_gapsincluded_menu_item.setVisible(false);
        show_jnet_panel_menu_item.setVisible(false);

    }

    private void initAnalysisMenu(JMenuBar menu_bar) {
        JMenu analysis_menu = new JMenu("Analysis");

        menu_bar.add(analysis_menu);
        analysis_menu.setVisible(true);

        JMenuItem cons_recalc_menu_item =
            new JMenuItem("Recalculate Consensus");

        addAlignmentDependentMenuItem(cons_recalc_menu_item);
        cons_recalc_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    consensus3.recalc();
                    consensus2.recalc();
                    consensus.setSeqs(alignment.getAllSequences());
                    consensus.recalc();
                    getContentPane().validate();
                    getContentPane().repaint();
                }
            }
        );
        analysis_menu.add(cons_recalc_menu_item);

        cons_autocalc_menu_item =
                new JCheckBoxMenuItem("Autocalculate Consensus",
                    false);
        addAlignmentDependentMenuItem(cons_autocalc_menu_item);
        cons_autocalc_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (cons_autocalc_menu_item.getState()) {
                        consensus3.recalc();
                        consensus2.recalc();
                        consensus.setSeqs(alignment.getAllSequences());
                        consensus.recalc();
                        getContentPane().validate();
                        getContentPane().repaint();

                    }
                }
            }
        );
        analysis_menu.add(cons_autocalc_menu_item);

        // Make Sequence Space Analysis unavailable until it has been fully tested and debugged
        /* analysis_menu.addSeparator();
         JMenuItem ss_menu_item = new JMenuItem("SeqSpace Analysis");
         addAlignmentDependentMenuItem(ss_menu_item);
         ss_menu_item.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
         Sequence[] seqs;
         if (props.isGroupEditing()) {
         if (props.getSelectedCount() < 2) {
         seqs = new Sequence [alignment.size()];
         for (int k = seqs.length - 1; k >= 0; k--)
         seqs[k] = alignment.getSequence(k);
         }
         else {
         seqs = props.getAllSelected();
         }
         try {
         SeqSpaceDialog ssd = new SeqSpaceDialog(AlignmentFrame.this,
         seqs,
         props);
         ssd.show();
         }
         catch (Exception e) {
         ErrorDialog.showErrorDialog(AlignmentFrame.this,
         "Unable to perform "
         + "SeqSpace analysis: ",
         e);
         }
         }
         else {
         ErrorDialog.showErrorDialog(AlignmentFrame.this,
         "Unable to perform SeqSpace analysis\n" +
         "Group Editing must be enabled");
         }
         }
         });
         analysis_menu.add(ss_menu_item);
         */
        if (!isAppletMode()) // extra separator
            analysis_menu.addSeparator();

        JMenuItem sort_by_name_menu_item = new JMenuItem("Sort By Name");

        addAlignmentDependentMenuItem(sort_by_name_menu_item);
        sort_by_name_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Comparator comp = new Comparator() {
                            public int compare(Object o1, Object o2) {
                                return ((Sequence) o1).getName().compareTo(((Sequence) o2).getName());
                            }
                        };
                    AlignmentSorter sorter = new AlignmentSorter(alignment, comp);

                    try {
                        sorter.sort();
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Unable to perform "
                            + "sort: ",
                            e);
                    }
                }
            }
        );
        analysis_menu.add(sort_by_name_menu_item);

        JMenuItem sort_by_pid_menu_item = new JMenuItem("Sort By Percentage Identity");

        addAlignmentDependentMenuItem(sort_by_pid_menu_item);
        sort_by_pid_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    final Map pid_map = new HashMap();

                    for (int i = alignment.size() - 1; i >= 0; i--) {
                        Sequence s = alignment.getSequence(i);

                        pid_map.put(s,
                            new Double(consensus.getPID(s)));
                    }
                    Comparator comp = new Comparator() {
                            public int compare(Object o1, Object o2) {
                                return ((Double) pid_map.get(o1)).compareTo(pid_map.get(o2));
                            }
                        };
                    AlignmentSorter sorter = new AlignmentSorter(alignment, comp);

                    try {
                        sorter.sort();
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Unable to perform "
                            + "sort: ",
                            e);
                    }
                }
            }
        );
        analysis_menu.add(sort_by_pid_menu_item);

        analysis_menu.addSeparator();
        nj_tree_menu_item = new JMenuItem("Neighbor Joining Tree");
        addAlignmentDependentMenuItem(nj_tree_menu_item);
        nj_tree_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {

                    // get sequences
                    Sequence[] seqs = props.getSelectedCount() > 1
                        ? props.getAllSelected()
                        : alignment.getAllSequences();

                    // allow user to choose NJ options
                    NJOptions options = NJOptionsChooser.showDialog(
                            AlignmentFrame.this, "Select Neighbor Joining Options");

                    if (options == null)
                        return;

                    // create tree object
                    NeighborJoiningTree tree = new NeighborJoiningTree(seqs, options);

                    // open progress dialog
                    ProgressDialog progress = ProgressDialog.createDialog(
                            AlignmentFrame.this, "Calculating Neighbor Joining Tree");

                    // disable our menu until calculation is complete
                    nj_tree_menu_item.setEnabled(false);

                    // spawn worker
                    NJTreeWorker.calcNJTree(AlignmentFrame.this, tree, progress);

                    // when worker is done it will call the method njWorkerFinished()
                }
            }
        );
        analysis_menu.add(nj_tree_menu_item);

        JMenuItem import_tree_menu_item = new JMenuItem("Import New Hampshire Tree");

        addAlignmentDependentMenuItem(import_tree_menu_item);
        import_tree_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    NHTreeLoader loader = new NHTreeLoader();
                    JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                    fc.setFileFilter(new DirectoryFileFilter(loader.getFileFilter()));
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    if (fc.showDialog(AlignmentFrame.this, "Select Tree File") == JFileChooser.APPROVE_OPTION) {
                        try {
                            Reader r = new FileReader(fc.getSelectedFile());
                            String tree = loader.loadTree(r);
                            TreeFrame tf = new TreeFrame(AlignmentFrame.this, props, tree);

                            tf.setTitle("Imported Tree: " + fc.getSelectedFile().getName());
                            tf.show();
                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                "Unable to load "
                                + "tree.",
                                e);
                        }
                    }
                }
            }
        );
        analysis_menu.add(import_tree_menu_item);

        // if ( !isAppletMode() )
        // analysis_menu.addSeparator();

        /* The Jnet analysis is being made unavailable until it has been fully tested and Debugged. */

        JMenuItem import_jnet_menu_item = new JMenuItem("Import Jnet Analysis");

        addAlignmentDependentMenuItem(import_jnet_menu_item);
        import_jnet_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JnetLoader loader = new JnetLoader();
                    JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                    fc.setFileFilter(new DirectoryFileFilter(loader.getFileFilter()));
                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    if (fc.showDialog(AlignmentFrame.this, "Select Jnet Analysis File") == JFileChooser.APPROVE_OPTION) {
                        // overridden by bottom_left_panel. have to figure out where to put this
                        try {
                            Reader r = new FileReader(fc.getSelectedFile());
                            JnetAnalysis analysis = loader.loadAnalysis(r);
                            AnalysisPanel a_p = (AnalysisPanel) analysis_vp.getView();
                            AnalysisNamePanel an_p = (AnalysisNamePanel) analysis_name_vp.getView();

                            for (int i = 0; i < analysis.getEntryCount(); i++) {
                                an_p.add(new AnalysisNameComponent(analysis.getName(i), props));
                                a_p.add(new	AnalysisComponent(analysis.getData(i), props));
                            }
                            analysis_vp.getParent().validate();
                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(AlignmentFrame.this,
                                "Unable to display "
                                + "jnet analysis.",
                                e);
                        }
                    }
                }
            }
        );

        analysis_menu.add(import_jnet_menu_item);
        JMenuItem perform_jnet_menu_item = new JMenuItem("Perform Jnet Analysis");

        addAlignmentDependentMenuItem(perform_jnet_menu_item);
        perform_jnet_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    File tempfile = null;
                    String outtempfilename = null;
                    String s = File.separator;

                    try {
                        tempfile = File.createTempFile("pfaat_jnet_in_tmp", ".fa");
                        outtempfilename = tempfile.getParent() + s + "pfaat_jnet_out_tmp";
                        AlignmentLoader.getAlignmentLoader(tempfile);
                        AlignmentLoader.saveAlignmentFile(tempfile, props.getColorScheme(), AlignmentFrame.this.alignment);
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Unable to save temp file.",
                            e);
                    }

                    try {
                        String dir = System.getProperty("pfaat.home") + s + "bin" + s + Utils.getOSDirectoryName() + s;
                        Runtime run = Runtime.getRuntime();
                        String command = dir + "jnet -p  " + tempfile.getAbsolutePath() + " > " + outtempfilename;
                        Process prc = run.exec(command);

                        prc.waitFor();
                        tempfile.delete();
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this, "Call to jnet failed. jnet only works on Linux/Unix platform", e);
                    }

                    try {
                        File outtempfile = new File(outtempfilename);
                        JnetLoader loader = new JnetLoader();
                        Reader r = new FileReader(outtempfile);
                        JnetAnalysis analysis = loader.loadAnalysis(r);
                        AnalysisPanel a_p = (AnalysisPanel) analysis_vp.getView();
                        AnalysisNamePanel an_p = (AnalysisNamePanel) analysis_name_vp.getView();

                        for (int i = 0; i < analysis.getEntryCount(); i++) {
                            an_p.add(new	AnalysisNameComponent(analysis.getName(i), props));
                            a_p.add(new	AnalysisComponent(analysis.getData(i), props));
                        }
                        analysis_vp.getParent().validate();
                        outtempfile.delete();
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Unable to display "
                            + "jnet analysis.",
                            e);
                    }
                }
            }
        );
        analysis_menu.add(perform_jnet_menu_item);
        analysis_menu.addSeparator();

        JMenuItem extract_profile_menu_item = new JMenuItem("Extract Regular Expression");

        addAlignmentDependentMenuItem(extract_profile_menu_item);
        extract_profile_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ExtractProfileDialog f = new ExtractProfileDialog(AlignmentFrame.this, alignment, props);

                    f.show();
                }
            }
        );
        analysis_menu.add(extract_profile_menu_item);

        JMenuItem mutual_information_menu_item = new JMenuItem("Load/View Mutual Information");

        addAlignmentDependentMenuItem(mutual_information_menu_item);
        mutual_information_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

                    fc.setDialogType(JFileChooser.OPEN_DIALOG);
                    if (fc.showDialog(AlignmentFrame.this, "Open") == JFileChooser.APPROVE_OPTION) {
                        File f = null;

                        try {
                            f = fc.getSelectedFile();
                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(AlignmentFrame.this, "Unable to open selected file: ", e);
                        }
                        if (f != null) {
                            MutualInformationDialog MIDialog =
                                new MutualInformationDialog(AlignmentFrame.this, alignment, props, f);

                            MIDialog.show();
                        }
                    }
                }
            }
        );
        analysis_menu.add(mutual_information_menu_item);

        JMenuItem seq_comp_menu_item = new JMenuItem("Comparison Matrix");

        addAlignmentDependentMenuItem(seq_comp_menu_item);
        seq_comp_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Sequence[] seqs = props.getSelectedCount() > 0
                        ? props.getAllSelected()
                        : alignment.getAllSequences();
                    Object[] options = { "Blosum62", "Percentage Identity" };
                    boolean blosum62 =
                        JOptionPane.showOptionDialog(AlignmentFrame.this,
                            "Select scoring scheme:",
                            "Tree Analysis",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[0])
                        == 0;
                    ComparisonFrame frame = new ComparisonFrame(seqs, blosum62);

                    frame.show();
                }
            }
        );
        analysis_menu.add(seq_comp_menu_item);

        JMenuItem pair_align_menu_item = new JMenuItem("Pairwise Alignment");

        addAlignmentDependentMenuItem(pair_align_menu_item);
        pair_align_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (props.getSelectedCount() < 2) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Please select at least "
                            + "2 sequences.");
                        return;
                    }
                    Sequence[] seqs = props.getAllSelected();
                    PairwiseAlignmentFrame frame =
                        new PairwiseAlignmentFrame(seqs);

                    frame.show();
                }
            }
        );
        analysis_menu.add(pair_align_menu_item);

        // turn off some analysis in the applet mode
        if (isAppletMode()) {
            import_tree_menu_item.setVisible(false);
        }
        // turn this analysis off because it is buggy right now
        perform_jnet_menu_item.setVisible(false);
        import_jnet_menu_item.setVisible(false);

    }

    private void initHelpMenu(JMenuBar menu_bar) {
        JMenu help_menu = new JMenu("Help");

        menu_bar.add(help_menu);

        JMenuItem help_menu_item = new JMenuItem("Help Documentation");

        help_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        if (!isAppletMode()) {
                            String pfaat_path = PathManager.getPfaatHome();
                            if (pfaat_path != null) {
                                if (!pfaat_path.endsWith("/"))
                                    pfaat_path = pfaat_path + "/";
                                BrowserControl.displayURL("file://" + pfaat_path + "/doc/pfaat-documentation.htm");
                            } else
                            {
                                BrowserControl.displayURL("http://sourceforge.net/docman/?group_id=67235");
                            }
                        } else
                            BrowserControl.displayURL("http://sourceforge.net/docman/?group_id=67235");
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(AlignmentFrame.this,
                            "Unable to open pfaat-documentation.htm in web browser.");
                    }
                }
            }
        );
        help_menu.add(help_menu_item);
    }

    public void mouseClicked(MouseEvent e) {
        banishGroupSelectionComponentsFocus();
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        banishGroupSelectionComponentsFocus();
    }

    public void mouseReleased(MouseEvent e) {
        banishGroupSelectionComponentsFocus();
    }

    public void mouseMoved(MouseEvent e) {}

    private void addAlignmentDependentMenuItem(JMenuItem menu_item) {
        menu_item.setEnabled(alignment != null);
        alignment_dependent_menus.add(menu_item);
    }

    public void padAlignment() {
        int max_length = alignment.maxLength();

        for (int i = alignment.size() - 1; i >= 0; i--) {
            Sequence s = alignment.getSequence(i);

            try {
                if (s.length() < max_length)
                    s.padWithGaps(max_length);
            } catch (Exception exp) {
                ErrorDialog.showErrorDialog(AlignmentFrame.this,
                    "Unable to pad "
                    + "sequence "
                    + s.getName()
                    + " with gaps",
                    exp);
            }
        }
    }

    public ConsensusSequence getConsensus() {
        return consensus;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    // set the display to a particular sequence/residue
    public void setAlignmentPanelPosition(int seq_idx, int res_idx) {
        Point p = alignment_panel.getResiduePosition(seq_idx, res_idx);
        Dimension vp_dim = alignment_vp.getSize();
        Dimension panel_dim = alignment_panel.getSize();

        // make sure the panel fills the screen at all times
        if (p.x + vp_dim.width > panel_dim.width)
            p.x = panel_dim.width - vp_dim.width;
        if (p.y + vp_dim.height > panel_dim.height)
            p.y = panel_dim.height - vp_dim.height;
        alignment_vp.setViewPosition(p);
    }

    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }

    // SequenceListener interface
    public void sequenceColorChanged(Sequence aaseq) {}

    public void sequenceAAChanged(Sequence aaseq) {}

    public void sequenceNameChanged(Sequence aaseq, String s) {}

    public void sequenceAnnotationChanged(Sequence aaseq) {}

    public void sequenceGroupChanged(Sequence aaseq) {
        // temporarily disable action listener
        jcb3.removeActionListener(groupActionListener);
        jcb2.removeActionListener(groupActionListener);

        String[] groupNames3 = alignment.getAllDisplayGroupNames(jcb3.getSelectedItem().toString());

        jcb3.removeAllItems();
        for (int i = 0; i < groupNames3.length; i++)
            jcb3.insertItemAt(new String(groupNames3[i]), i);
        jcb3.setSelectedIndex(0);

        String[] groupNames2 =
            alignment.getAllDisplayGroupNames(jcb2.getSelectedItem().toString());

        jcb2.removeAllItems();
        for (int i = 0; i < groupNames2.length; i++)
            jcb2.insertItemAt(new String(groupNames2[i]), i);
        jcb2.setSelectedIndex(0);

        if (negJComboBox3.getSelectedIndex() == 1)
            consensus3.setSeqs(alignment.getAllSequencesNotInGroup(jcb3.getSelectedItem().toString()));
        else if (negJComboBox3.getSelectedIndex() == 0)
            consensus3.setSeqs(alignment.getAllGroupSequences(jcb3.getSelectedItem().toString()));

        if (negJComboBox2.getSelectedIndex() == 1)
            consensus2.setSeqs(alignment.getAllSequencesNotInGroup(jcb2.getSelectedItem().toString()));
        else if (negJComboBox2.getSelectedIndex() == 0)
            consensus2.setSeqs(alignment.getAllGroupSequences(jcb2.getSelectedItem().toString()));
        consensus.setSeqs(alignment.getAllSequences());
        consensus3.recalc();
        consensus2.recalc();
        consensus.recalc();
        // reinstate actionlisteners
        jcb3.addActionListener(groupActionListener);
        jcb2.addActionListener(groupActionListener);
        getContentPane().validate();
        getContentPane().repaint();

        alignment_group_panel.revalidate();
        alignment_group_panel.repaint();

    }

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {}

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {}

    // DisplayPropertiesListener interface
    public void displayAnnViewChanged(DisplayProperties dp, Sequence seq, boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp, Sequence seq, boolean select) {}

    // TODO- these should set menu entries
    public void displayColorSchemeChanged(DisplayProperties dp, ColorScheme old) {}

    public void displayFontChanged(DisplayProperties dp) {
        Font font = props.getFont();
        Font boldFont = new Font(font.getName(), Font.BOLD, font.getSize());

        jcb3.setFont(boldFont);
        jcb2.setFont(boldFont);
        bottom_left_vp.setView(bottom_left_panel);
        getContentPane().validate();
        getContentPane().repaint();
    }

    public void displayRenderGapsChanged(DisplayProperties dp) {}

    public void displayGroupEditingChanged(DisplayProperties dp) {
        if (dp != props)
            throw	new RuntimeException("bound to incorrect display properties");
        group_editing_menu_item.setState(props.isGroupEditing());
    }

    public void displayOverwriteChanged(DisplayProperties dp) {
        if (dp != props)
            throw	new RuntimeException("bound to incorrect display properties");
        overwrite_menu_item.setState(props.isOverwrite());
    }

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq) {
        String dispRow, dispCol, dispRawCol, dispRowSize, dispSeqName;
        String dispGroups = new String("");;
        // refresh cursor location in status bar
        if (alignment != null) {
            int row = props.getCursorRow();
            int col = Math.max(1, props.getCursorColumn());

            if (props.isRulerEditing() || row == -1) {
                dispRow = "--";
                dispSeqName = "--";
                dispRawCol = "--";
                dispGroups = "";
            } else {
                dispRow = Integer.toString(row + 1);
                Sequence sequence = alignment.getSequence(row);

                dispGroups = sequence.getFormattedGroupString();
                int rawCol = sequence.getRawIndex(col);

                dispRawCol = rawCol != -1 && col > 0 && col < sequence.length() ?
                        Integer.toString(rawCol) : "--";
                dispSeqName = sequence.getName();
            }
            dispCol = Integer.toString(col);
            dispRowSize = Integer.toString(alignment.size());
        } else {
            dispRow = dispRawCol = dispCol = dispRowSize = dispSeqName = "";
        }
        overall_panel.remove(statusBarPanel);
        statusBarPanel = new JPanel();
        statusBarPanel.setLayout(new GridLayout(1, 2));
        JLabel seqName = new JLabel("[" + dispSeqName + "]   " + dispGroups, SwingConstants.LEFT);
        Color fgColor = new Color(0, 0, 16);

        seqName.setForeground(fgColor);
        statusBarPanel.add(seqName);
        JLabel cursorInfo = new JLabel("res " + dispRawCol + "   aln " + dispCol +
                "   seq " + dispRow + " of " + dispRowSize + " ", SwingConstants.RIGHT);

        cursorInfo.setForeground(fgColor);
        statusBarPanel.add(cursorInfo);
        overall_layout.setPosition(statusBarPanel, 0, 0);
        overall_panel.add(statusBarPanel);
        getContentPane().repaint();
        getContentPane().validate();

        // move scrollbar to follow cursor
        int alignW = alignment_vp.getSize().width;
        int alignH = alignment_vp.getSize().height;
        int scrollW = alignment_hsb.getValue();
        int scrollH = alignment_vsb.getValue();
        int minScrollW = alignment_hsb.getMinimum();
        int maxScrollW = alignment_hsb.getMaximum();
        int minScrollH = alignment_vsb.getMinimum();
        int maxScrollH = alignment_vsb.getMaximum();
        int cursorX = props.getCursorColumn() * props.getResidueWidth();
        int cursorY = props.getCursorRow() * props.getResidueHeight();
        int row = props.getCursorRow();

        if (cursorX < scrollW) {
            alignment_hsb.setValue((int) (Math.max((float) minScrollW,
                        cursorX - 0.2 * (float) alignW)));
        } else if (cursorX > (scrollW + alignW)) {
            alignment_hsb.setValue((int) (Math.min((float) maxScrollW,
                        cursorX - 0.8 * (float) alignW)));
        }
        if (cursorY < scrollH && row != -1)
            alignment_vsb.setValue((int) (Math.max((float) minScrollH,
                        cursorY - 0.2 * (float) alignH)));

        else if (cursorY > (scrollH + alignH) && row != -1)
            alignment_vsb.setValue((int) (Math.min((float) maxScrollH,
                        cursorY - 0.8 * (float) alignH)));
    }

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs) {
        String dispRow, dispCol, dispRawCol, dispRowSize, dispSeqName;
        String dispGroups = new String("");;
        // refresh cursor location in status bar
        if (alignment != null) {
            int row = props.getCursorRow();
            int col = Math.max(1, props.getCursorColumn());

            if (props.isRulerEditing() || row == -1) {
                dispRow = "--";
                dispSeqName = "--";
                dispRawCol = "--";
                dispGroups = "";
            } else {
                dispRow = Integer.toString(row + 1);
                Sequence sequence = alignment.getSequence(row);

                dispGroups = sequence.getFormattedGroupString();
                int rawCol = sequence.getRawIndex(col);

                dispRawCol = rawCol != -1 && col > 0 && col < sequence.length() ?
                        Integer.toString(rawCol) : "--";
                dispSeqName = sequence.getName();
            }
            dispCol = Integer.toString(col);
            dispRowSize = Integer.toString(alignment.size());
        } else {
            dispRow = dispRawCol = dispCol = dispRowSize = dispSeqName = "";
        }
        overall_panel.remove(statusBarPanel);
        statusBarPanel = new JPanel();
        statusBarPanel.setLayout(new GridLayout(1, 2));
        JLabel seqName = new JLabel("[" + dispSeqName + "]   " + dispGroups, SwingConstants.LEFT);
        Color fgColor = new Color(0, 0, 16);

        seqName.setForeground(fgColor);
        statusBarPanel.add(seqName);
        JLabel cursorInfo = new JLabel("res " + dispRawCol + "   aln " + dispCol +
                "   seq " + dispRow + " of " + dispRowSize + " ", SwingConstants.RIGHT);

        cursorInfo.setForeground(fgColor);
        statusBarPanel.add(cursorInfo);
        overall_layout.setPosition(statusBarPanel, 0, 0);
        overall_panel.add(statusBarPanel);
        getContentPane().repaint();
        getContentPane().validate();

        // move scrollbar to follow cursor
        int alignW = alignment_vp.getSize().width;
        int alignH = alignment_vp.getSize().height;
        int scrollW = alignment_hsb.getValue();
        int scrollH = alignment_vsb.getValue();
        int minScrollW = alignment_hsb.getMinimum();
        int maxScrollW = alignment_hsb.getMaximum();
        int minScrollH = alignment_vsb.getMinimum();
        int maxScrollH = alignment_vsb.getMaximum();
        int cursorX = props.getCursorColumn() * props.getResidueWidth();
        int cursorY = props.getCursorRow() * props.getResidueHeight();
        int row = props.getCursorRow();

        if (cursorX < scrollW) {
            alignment_hsb.setValue((int) (Math.max((float) minScrollW,
                        cursorX - 0.2 * (float) alignW)));
        } else if (cursorX > (scrollW + alignW)) {
            alignment_hsb.setValue((int) (Math.min((float) maxScrollW,
                        cursorX - 0.8 * (float) alignW)));
        }
        if (cursorY < scrollH && row != -1)
            alignment_vsb.setValue((int) (Math.max((float) minScrollH,
                        cursorY - 0.2 * (float) alignH)));

        else if (cursorY > (scrollH + alignH) && row != -1)
            alignment_vsb.setValue((int) (Math.min((float) maxScrollH,
                        cursorY - 0.8 * (float) alignH)));
    }

    public void clearStatusBarPanel() {
        overall_panel.remove(statusBarPanel);
        statusBarPanel = new JPanel();
        statusBarPanel.setLayout(new GridLayout(1, 2));
        statusBarPanel.add(new JLabel("", SwingConstants.LEFT));
        statusBarPanel.add(new JLabel("", SwingConstants.RIGHT));
        overall_layout.setPosition(statusBarPanel, 0, 0);
        overall_panel.add(statusBarPanel);
        getContentPane().repaint();
        getContentPane().validate();
    }

    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {
        if (alignment != align)
            throw new RuntimeException("bound to incorrect alignment");
        setTitle("Protein Family Alignment Annotation Tool: "
            + alignment.getName());
    }

    public void alignmentSeqInserted(Alignment align, int i) {
        if (alignment != align)
            throw new RuntimeException("bound to incorrect alignment");
        if (cons_autocalc_menu_item.getState()) {
            consensus.setSeqs(alignment.getAllSequences());
            if (consensus_component3.isVisible()) consensus3.recalc();
            if (consensus_component2.isVisible()) consensus2.recalc();
            if (!cons_none_menu_item.isSelected()) consensus.recalc();
            getContentPane().validate();
            getContentPane().repaint();
        }
    }

    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq) {
        if (alignment != align)
            throw new RuntimeException("bound to incorrect alignment");
        if (cons_autocalc_menu_item.getState()) {
            consensus.setSeqs(alignment.getAllSequences());
            if (consensus_component3.isVisible()) consensus3.recalc();
            if (consensus_component2.isVisible()) consensus2.recalc();
            if (!cons_none_menu_item.isSelected()) consensus.recalc();
            getContentPane().validate();
            getContentPane().repaint();
        }
    }

    public void alignmentSeqSwapped(Alignment align, int i, int j) {}

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {
        if (alignment != align)
            throw new RuntimeException("bound to incorrect alignment");
        if (cons_autocalc_menu_item.getState()) {
            consensus.setSeqs(alignment.getAllSequences());
            if (consensus_component3.isVisible()) consensus3.recalc();
            if (consensus_component2.isVisible()) consensus2.recalc();
            if (!cons_none_menu_item.isSelected()) consensus.recalc();
        }
    }

    public boolean isAppletMode() {
        if (getCurrentMode() == APPLET_MODE)
            return true;
        else
            return false;
    }

    public static int getCurrentMode() {
        return s_current_mode;
    }

    // command line interface
    public static void main(String[] argv) throws Exception {
        AlignmentFrame frame;

        // ///////////
        // BETE BEGIN
        // ///////////
        // if the only option is '-help' or there are more than 2 params
        // then this is COMMAND LINE driven pfaat!
        if ((argv.length > 0 && argv[0].compareToIgnoreCase("-help") == 0) || argv.length > 1) {
            // for command line driven pfaat we dont even open an
            // alignment frame...we just process the given commands and quit
            commandLinePfaat(argv);
            return;
        } // /////////
        // BETE END
        // /////////
        else {
            int mode = DEFAULT_MODE;
            String temp = System.getProperty("pfaat.mode");

            if (temp != null)
                mode = Integer.parseInt(temp);

            if (argv.length == 1) {
                File f = new File(argv[0]);
                Alignment alignment = AlignmentLoader.loadAlignmentFile(f);
                String cs_hint = AlignmentLoader.getColorSchemeHint(f);

                frame = new AlignmentFrame(f, alignment, cs_hint, mode);
            } else
                frame = new AlignmentFrame(mode);

            frame.show();
        }
    }

    // this method called by worker when work is complete.
    // if there was a failure or calc was canceleed then tree will be null
    // if there was an exception then e will be non null
    public void njWorkerFinished(String tree, Exception e,
        NJOptions options, ProgressDialog progress) {
        // enable our menu now that calculation is complete
        nj_tree_menu_item.setEnabled(true);

        // if there was an exception...report that
        if (e != null) {
            ErrorDialog.showErrorDialog(this, "Unable to perform tree analysis.", e);
            return;
        }

        // close progress dialog
        if (progress != null)
            progress.closeDialog();

        // if operation was canceled...do nothing
        if (tree == null)
            return;

        // display tree
        try {
            TreeFrame tf = new TreeFrame(this, props, tree);

            tf.setTitle(options.getTitle(alignment.getName()));
            tf.show();
        } catch (Exception tree_e) {
            ErrorDialog.showErrorDialog(this, "Unable to display tree.", tree_e);
        }
    }

    // COMMAND LINE driven pfaat
    public static void commandLinePfaat(String[] argv) {
        // help
        if (argv[0].compareToIgnoreCase("-help") == 0) {
            System.out.println("Pfaat command line mode:");
            System.out.println("The currently supported analysis operations are:");
            // System.out.println("  -analysis bete");
            System.out.println("  -analysis nj");
            System.out.println("For more info type -analysis bete -help OR -analysis nj -help");
            return;
        }

        // for command line pfaat the first argument
        // is the name of the sequence file to load
        // which must exist
        File f = new File(argv[0]);

        if (!f.exists()) {
            System.out.println("Pfaat command line mode:");
            System.out.println("The seq. file " + f.getName() + " does not exist.");
            return;
        }

        // presently the commandline pfaat only suppports
        // TWO operations -analysis bete   and -analysis nj
        // therefore the second argument MUST be -analysis
        if (argv[1].compareToIgnoreCase("-analysis") != 0 ||
            (argv[2].compareToIgnoreCase("bete") != 0 &&
                argv[2].compareToIgnoreCase("nj") != 0)
        ) {
            System.out.println("Pfaat command line mode:");
            System.out.println("The only currently supported operations are:");
            System.out.println("  -analysis bete");
            System.out.println("  -analysis nj");
            System.out.println("The " + argv[1] + " " + argv[2] +
                " operation is not supported.");
            System.out.println("For more info type -analysis bete -help OR -analysis nj -help");
            return;
        }

        // read alignment file
        Alignment alignment;

        try {
            alignment = AlignmentLoader.loadAlignmentFile(f);
        } catch (Exception e) {
            System.out.println("Exception occurred while loading: " + f.getName());
            System.out.println(e.getLocalizedMessage());
            return;
        }

        if (argv[2].compareToIgnoreCase("nj") == 0) {
            commandLineNJ(alignment, argv);
            return;
        }
    }

    // get the output file from the command line
    public static File commandLineOutputFile(String[] argv) {
        // the default is a file called "OutputTree.nhx"
        String name = "OutputTree.nhx";

        // loop through commandline options looking for -o
        int n = 3;

        for (; n < argv.length; n++) {
            if (argv[n].compareToIgnoreCase("-o") == 0) {
                if (n + 1 < argv.length)
                    name = argv[n + 1];
                break;
            }
        }

        return new File(name);
    }

    // perform neighbor joining tree analysis from the command line
    public static void commandLineNJ(Alignment alignment, String[] argv) {
        // get options from parameters...this will print error msg
        // and return null if there was an error
        NJOptions options = NJOptions.commandLineNJOptions(argv);

        if (options == null)
            return;

        Sequence[] seqs = alignment.getAllSequences();
        NeighborJoiningTree tree = new NeighborJoiningTree(seqs, options);

        try {
            tree.calcNJTree(null);
        } catch (Exception e) {
            System.out.println("Exception occurred while calculating tree: ");
            System.out.println(e.getLocalizedMessage());
            return;
        }

        // save tree to output file
        String s = tree.toString();

        saveTree(s, argv);
    }

    // save tree to output filename
    public static void saveTree(String s, String[] argv) {
        File outputFile = commandLineOutputFile(argv);
        PrintStream ps = FileUtil.getPrintStream(outputFile);

        if (ps != null)
            ps.println(s);
        if (ps == null || ps.checkError()) {
            System.out.println("Error saving tree to " + outputFile.getName());
            return;
        }
        ps = null;
    }

    // /////////
    // BETE END
    // /////////
}

