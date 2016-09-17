package com.neogenesis.pfaat.tree;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import forester.tree.*;
import forester.atv.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.print.*;
import com.neogenesis.pfaat.colorscheme.*;
import com.neogenesis.pfaat.util.*;


/**
 * A frame for phylogenetic trees.
 *
 * @author $Author: xih $
 * @version $Revision: 1.7 $, $Date: 2002/10/11 18:31:50 $ */
public class TreeFrame extends JFrame
    implements NodeSelectionListener, DisplayPropertiesListener {
    private AlignmentFrame owner;
    private Tree tree;
    private ATVpanel atvpanel;
    private DisplayProperties props;

    public TreeFrame(AlignmentFrame owner,
        DisplayProperties props,
        String nh_string) throws Exception {
        super("Tree");
        this.owner = owner;
        this.props = props;
        props.addListener(this);

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    TreeFrame.this.props.removeListener(TreeFrame.this);
                    dispose();
                }
            }
        );
        getContentPane().setLayout(new BorderLayout());

        tree = new Tree(nh_string);
        atvpanel = new ATVpanel(tree);
        atvpanel.getATVgraphic().addNodeSelectionListener(this);
        getContentPane().add(atvpanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    atvpanel.getATVgraphic().setParametersForPainting(atvpanel.getATVgraphic().getWidth(),
                        atvpanel.getATVgraphic().getHeight());
                }
            }
        );

        JMenuBar menu_bar = new JMenuBar();

        setJMenuBar(menu_bar);
        JMenu file_menu = new JMenu("File");

        menu_bar.add(file_menu);

        JMenuItem save_menu_item = new JMenuItem("Save");

        save_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    JFileChooser fc =
                        new JFileChooser(PathManager.getAlignmentPath());
                    NHTreeLoader nhloader = new NHTreeLoader();

                    fc.addChoosableFileFilter(new
                        DirectoryFileFilter(nhloader.getFileFilter()));
                    fc.setDialogType(JFileChooser.SAVE_DIALOG);
                    if (fc.showDialog(TreeFrame.this, "Save") ==
                        JFileChooser.APPROVE_OPTION) {
                        try {
                            nhloader.saveNHTree(fc.getSelectedFile(),
                                tree.toString());
                        } catch (Exception e) {
                            ErrorDialog.showErrorDialog(TreeFrame.this,
                                "Unable to save NH Tree: ", e);
                        }
                    }
                }
            }
        );
        file_menu.add(save_menu_item);

        JMenuItem print_menu_item = new JMenuItem("Print");

        print_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    PrintGrid printable = new PrintGrid(1, 1);

                    printable.addComponent(atvpanel.getATVgraphic(), 0, 0);
                    PrintPSDialog pd =
                        new PrintPSDialog(TreeFrame.this, printable);

                    pd.show();
                }
            }
        );
        file_menu.add(print_menu_item);

        file_menu.addSeparator();
        JMenuItem sort_menu_item = new JMenuItem("Sort Original Alignment");

        sort_menu_item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        final Alignment alignment =
                            TreeFrame.this.owner.getAlignment();
                        final Map tree_pos_map = new HashMap();
                        int cnt = 0;

                        for (PreorderTreeIterator i =
                                new PreorderTreeIterator(tree);
                            !i.isDone();
                            i.next()) {
                            Sequence s = alignment.getSequence(i.currentNode().getSeqName());

                            if (s != null)
                                tree_pos_map.put(s, new Integer(cnt++));
                        }
                        Comparator comp = new Comparator() {
                                public int compare(Object o1, Object o2) {
                                    Sequence s1 = (Sequence) o1;
                                    Integer n1 =
                                        (Integer) tree_pos_map.get(s1);
                                    Sequence s2 = (Sequence) o2;
                                    Integer n2 =
                                        (Integer) tree_pos_map.get(s2);

                                    if (n1 != null) {
                                        if (n2 != null)
                                            return n1.compareTo(n2);
                                        return -1;
                                    }
                                    if (n2 != null)
                                        return 1;
                                    int i1 = alignment.getIndex(s1);
                                    int i2 = alignment.getIndex(s2);

                                    return i1 > i2
                                        ? 1
                                        : i1 < i2
                                        ? -1
                                        : 0;
                                }
                            };
                        AlignmentSorter sorter = new AlignmentSorter(alignment,
                                comp);

                        sorter.sort();
                    } catch (Exception e) {
                        ErrorDialog.showErrorDialog(TreeFrame.this,
                            "Unable to sort "
                            + "alignment.",
                            e);
                    }
                }
            }
        );
        file_menu.add(sort_menu_item);

        // hide the save and print menu item in the applet mode
        if (owner.isAppletMode()) {
            print_menu_item.setVisible(false);
            save_menu_item.setVisible(false);
        }

        setSize(640, 580);

    }

    public void nodeSelected(Node node, MouseEvent e) {
        ArrayList all_sequences = new ArrayList();
        Alignment alignment = owner.getAlignment();

        if (node != null) {
            for (PreorderTreeIterator i = new PreorderTreeIterator(node);
                !i.isDone();
                i.next()) {
                Sequence s = alignment.getSequence(i.currentNode().getSeqName());

                if (s != null)
                    all_sequences.add(s);
            }
        }

        if (e.isControlDown()) {
            for (int i = all_sequences.size() - 1; i >= 0; i--) {
                Sequence s = (Sequence) all_sequences.get(i);

                props.setSeqSelect(s, !props.getSeqSelect(s));
            }
        } else {
            props.clearSelections();
            for (int i = all_sequences.size() - 1; i >= 0; i--)
                props.setSeqSelect((Sequence) all_sequences.get(i), true);
        }

        if (all_sequences.size() > 0) {
            int idx = alignment.size();

            for (int i = all_sequences.size() - 1; i >= 0; i--) {
                Sequence s = (Sequence) all_sequences.get(i);

                if (alignment.getIndex(s) < idx) {
                    idx = alignment.getIndex(s);
                }
            }
            owner.setAlignmentPanelPosition(idx, 0);
        }
    }

    public void displayAnnViewChanged(DisplayProperties dp,
        Sequence seq,
        boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp,
        Sequence seq,
        boolean select) {
        try {
            Vector v = tree.getNodes(seq.getName());
            ATVgraphic gr = atvpanel.getATVgraphic();

            for (int i = v.size() - 1; i >= 0; i--)
                gr.setNodeHighlight((Node) v.get(i), select);
        } catch (Exception e) {
            ErrorDialog.showErrorDialog(this,
                "Internal error selecting tree nodes.",
                e);
        }

    }

    public void displayColorSchemeChanged(DisplayProperties dp,
        ColorScheme old) {}

    public void displayFontChanged(DisplayProperties dp) {}

    public void displayRenderGapsChanged(DisplayProperties dp) {}

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs) {}
}

