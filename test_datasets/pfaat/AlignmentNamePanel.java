package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;
import com.neogenesis.pfaat.pdb.*;
import com.neogenesis.pfaat.srs.*;


/**
 * Display component for alignment sequence names.
 *
 * @author $Author: xih $
 * @version $Revision: 1.7 $, $Date: 2002/10/11 18:28:03 $ */
public class AlignmentNamePanel extends JPanel
    implements AlignmentListener, MouseListener, ActionListener {
    // underlying alignment
    protected Alignment alignment;
    // underlying display properties
    protected DisplayProperties props;
    protected AlignmentFrame owner;

    // popup menu
    private JPopupMenu popup;
    private JMenuItem add_seq_menuitem;
    private JMenuItem add_pdb_seq_menuitem;
    private JMenuItem copy_seq_menuitem;
    private JMenuItem edit_seq_menuitem;
    private JMenuItem del_seq_menuitem;
    private JMenuItem color_seq_menuitem;
    private JMenuItem associate_pdb_menuitem;
    private JMenuItem startindex_seq_menuitem;
    private JMenuItem add_ann_menuitem;
    private JMenuItem add_pdb_ann_menuitem;
    private JMenuItem del_ann_menuitem;
    private JMenuItem edit_ann_menuitem;
    private JMenuItem set_pdb_menuitem;
    private JMenuItem set_and_view_pdb_menuitem;
    private JMenuItem view_pdb_menuitem;
    private JMenuItem srs_menuitem;

    private PdbLoader loader;
    private SRSServer srs_server;

    public AlignmentNamePanel(AlignmentFrame owner,
        Alignment alignment,
        DisplayProperties props) {
        super();
        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.props = props;
        this.owner = owner;
        this.alignment = alignment;
        alignment.addListener(this);
        for (int i = 0; i < alignment.size(); i++)
            add(new SequenceNameComponent(alignment.getSequence(i), props));

        if (!owner.isAppletMode()) {// only for standalone version
            srs_server = new SRSServer();
            loader = new PdbLoader();
        }

        // popup menus
        popup = new JPopupMenu();

        add_seq_menuitem = new JMenuItem("Add Sequence");
        add_seq_menuitem.addActionListener(this);
        popup.add(add_seq_menuitem);
        add_pdb_seq_menuitem = new JMenuItem("Add Sequence from PDB File");
        add_pdb_seq_menuitem.addActionListener(this);
        popup.add(add_pdb_seq_menuitem);
        copy_seq_menuitem = new JMenuItem("Copy Sequence");
        copy_seq_menuitem.addActionListener(this);
        popup.add(copy_seq_menuitem);
        edit_seq_menuitem = new JMenuItem("Edit Sequence");
        edit_seq_menuitem.addActionListener(this);
        popup.add(edit_seq_menuitem);
        del_seq_menuitem = new JMenuItem("Delete Sequence");
        del_seq_menuitem.addActionListener(this);
        popup.add(del_seq_menuitem);

        popup.addSeparator();
        color_seq_menuitem = new JMenuItem("Color Sequence");
        color_seq_menuitem.addActionListener(this);
        popup.add(color_seq_menuitem);
        associate_pdb_menuitem = new JMenuItem("Associate PDB to Sequence");
        associate_pdb_menuitem.addActionListener(this);
        popup.add(associate_pdb_menuitem);

        popup.addSeparator();
        startindex_seq_menuitem = new JMenuItem("Set Start Index");
        startindex_seq_menuitem.addActionListener(this);
        popup.add(startindex_seq_menuitem);

        popup.addSeparator();
        add_ann_menuitem = new JMenuItem("Add Line Annotation");
        add_ann_menuitem.addActionListener(this);
        popup.add(add_ann_menuitem);
        add_pdb_ann_menuitem = new JMenuItem("Add PDB Line Annotation");
        add_pdb_ann_menuitem.addActionListener(this);
        popup.add(add_pdb_ann_menuitem);
        del_ann_menuitem = new JMenuItem("Delete Line Annotation");
        del_ann_menuitem.addActionListener(this);
        popup.add(del_ann_menuitem);
        edit_ann_menuitem = new JMenuItem("Edit Line Annotation");
        edit_ann_menuitem.addActionListener(this);
        popup.add(edit_ann_menuitem);

        popup.addSeparator();

        /*
         set_pdb_menuitem = new JMenuItem("Set PDB File");
         set_pdb_menuitem.addActionListener(this);
         popup.add(set_pdb_menuitem);

         set_and_view_pdb_menuitem = new JMenuItem("Set and View PDB Structure");
         set_and_view_pdb_menuitem.addActionListener(this);
         popup.add(set_and_view_pdb_menuitem);
         */
        view_pdb_menuitem = new JMenuItem("View PDB Structure");
        view_pdb_menuitem.addActionListener(this);
        popup.add(view_pdb_menuitem);

        popup.addSeparator();
        srs_menuitem = new JMenuItem("Import SRS Features");
        srs_menuitem.addActionListener(this);
        popup.add(srs_menuitem);

        addMouseListener(this);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        return getLayout().minimumLayoutSize(this);
    }

    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {}

    public void alignmentSeqInserted(Alignment align, int i) {
        if (alignment != this.alignment)
            throw new RuntimeException("bound to incorrect alignment");
        add(new SequenceNameComponent(alignment.getSequence(i), props), i);
        revalidate();
        repaint();
    }

    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq) {
        if (alignment != this.alignment)
            throw new RuntimeException("bound to incorrect alignment");
        remove(i);
        revalidate();
        repaint();
    }

    public void alignmentSeqSwapped(Alignment alignment, int i, int j) {
        if (alignment != this.alignment)
            throw new RuntimeException("bound to incorrect alignment");
        ((SequenceNameComponent) getComponent(i)).setSequence(alignment.getSequence(i));
        ((SequenceNameComponent) getComponent(j)).setSequence(alignment.getSequence(j));
    }

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {}

    // MouseListener interface
    private boolean mouse_pressed = false;
    private int mouse_row_pos;
    private int mouse_ann_pos;
    private int last_selected_row = -1;

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        mouse_row_pos = findRow(e.getX(), e.getY());
        mouse_pressed = mouse_row_pos >= 0;
        if (!mouse_pressed) return;
        SequenceNameComponent snc =
            (SequenceNameComponent) getComponent(mouse_row_pos);
        Point p = snc.getLocation();

        mouse_ann_pos = snc.findLineAnnotation(e.getX() - p.x,
                    e.getY() - p.y);

        if (maybeShowPopup(e))
            return;
        else if (e.getClickCount() >= 2) {
            Sequence seq = alignment.getSequence(mouse_row_pos);

            if (seq.getLineAnnotationsCount() > 0) {
                if (props.getAnnView(seq)) {
                    props.setAnnView(seq, false);
                    owner.redrawAlignmentAnnPanel();
                    owner.redrawAlignmentGroupPanel();
                } else
                    props.setAnnView(seq, true);
            }
        } else if (e.getClickCount() == 1) {
            Sequence seq = alignment.getSequence(mouse_row_pos);

            if (e.isControlDown()) {
                if (props.getSeqSelect(seq))
                    props.setSeqSelect(seq, false);
                else {
                    props.setSeqSelect(seq, true);
                    last_selected_row = mouse_row_pos;
                }
            } else if (e.isShiftDown()
                && last_selected_row >= 0
                && last_selected_row < alignment.size()) {
                props.clearSelections();
                int start = Math.min(mouse_row_pos, last_selected_row);
                int end = Math.max(mouse_row_pos, last_selected_row);

                for (int i = start; i <= end; i++)
                    props.setSeqSelect(alignment.getSequence(i), true);
            } else {
                props.clearSelections();
                props.setSeqSelect(seq, true);
                last_selected_row = mouse_row_pos;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (maybeShowPopup(e))
            return;
        if (popup.isVisible())
            return;
        mouse_pressed = false;
    }

    private int findRow(int x, int y) {
        Component c = findComponentAt(x, y);
        Component[] comps = getComponents();
        int idx;

        for (idx = comps.length - 1; idx >= 0; idx--) {
            if (comps[idx] == c) break;
        }
        return idx;
    }

    private boolean maybeShowPopup(MouseEvent e) {
        // check for applet property here and do not pop up if it is running in applet.
        if (owner.isAppletMode())
            return false; // do not pop up.
        if (e.isPopupTrigger()) {
            boolean show_ann_menu = mouse_pressed && mouse_ann_pos >= 0;

            edit_ann_menuitem.setEnabled(show_ann_menu);
            del_ann_menuitem.setEnabled(show_ann_menu);
            view_pdb_menuitem.setEnabled(mouse_pressed &&
                alignment.getSequence(mouse_row_pos).getPDBFile()
                != null);
            add_pdb_ann_menuitem.setEnabled(mouse_pressed &&
                alignment.getSequence(mouse_row_pos).getPDBFile()
                != null);
            int popupHeight = (int) (popup.getComponent()).getSize().getHeight();
            int aHeight = (int) owner.getSize().getHeight();
            int yOffset = -popupHeight * e.getY() / aHeight;

            yOffset = Math.min(0, Math.max(yOffset, -popupHeight));
            Utils.showPopup(popup, e.getComponent(), e.getX(), e.getY() + yOffset);
            return true;
        }
        return false;
    }

    // ActionListener interface
    public void actionPerformed(ActionEvent e) {
        if (!mouse_pressed) return;
        if (e.getSource() == add_seq_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            EditSequenceDialog d = new EditSequenceDialog(owner,
                    seq, alignment,
                    mouse_row_pos);

            d.show();
        } else if (e.getSource() == add_pdb_seq_menuitem) {
            String seqName = new String();
            JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

            fc.setFileFilter(new DirectoryFileFilter(
                    new javax.swing.filechooser.FileFilter() {
                        public boolean accept(File f) {
                            return f.getName().endsWith(".pdb") && !f.getName().startsWith(".");
                        }

                        public String getDescription() {
                            return "PDB (*.pdb)";
                        }
                    }
                )
            );
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileHidingEnabled(true);// don't show hidden file
            if (fc.showDialog(owner, "Select PDB File") == JFileChooser.APPROVE_OPTION) {
                PathManager.setAlignmentPath(fc.getCurrentDirectory());
            }
            // allow user to select chains
            File newPdbFile = null;

            try {
                if (fc.getSelectedFile() == null)
                    return;
                String[] chainNames = PdbUtil.getProChainNames(fc.getSelectedFile());
                ChoosePdbChainDialog chainChooser = new ChoosePdbChainDialog(owner, chainNames);

                chainChooser.show();
                String chainName = chainChooser.getSelectedChain();

                if (chainName == null) {
                    ErrorDialog.showErrorDialog(this, "No chain selected");
                    return;
                }
                newPdbFile = PdbUtil.renumberPDB(fc.getSelectedFile(), chainName);// to get by the pdb numbering problem
            } catch (Exception exp) {
                ErrorDialog.showErrorDialog(this, "Error in parsing pdb file.", exp);
                return;
            }

            seqName = newPdbFile.getName();
            AminoAcid[] aa = AminoAcid.stringToAA("");
            Sequence seq = new Sequence(seqName, aa);

            try {
                alignment.insertSequence(mouse_row_pos, seq);
                seq.setPDBFile(newPdbFile);
            } catch (Exception exp) {
                ErrorDialog.showErrorDialog(this,
                    "Sequence " + seqName + " already exists.",
                    exp);
            }
            // account for gap offset
            String gap = "-";

            aa = AminoAcid.stringToAA(gap.concat(seq.getAlignmentFromPDBFile()));
            seq.setAA(aa);
        } else if (e.getSource() == copy_seq_menuitem) {
            Sequence old_seq = alignment.getSequence(mouse_row_pos);
            Sequence new_seq = new Sequence(old_seq.getName() + "_COPY",
                    old_seq.getAllAA());

            try {
                alignment.insertSequence(mouse_row_pos + 1, new_seq);
            } catch (Exception exp) {
                ErrorDialog.showErrorDialog(this, "Error copying "
                    + "sequence.",
                    exp);
            }
        } else if (e.getSource() == edit_seq_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            EditSequenceDialog d = new EditSequenceDialog(owner, seq);

            d.show();
        } else if (e.getSource() == del_seq_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);

            if (JOptionPane.showConfirmDialog(this,
                    "Delete sequence "
                    + seq.getName() + "?",
                    "Delete Sequence",
                    JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION)
                alignment.deleteSequence(mouse_row_pos);
        } else if (e.getSource() == color_seq_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            ColorSequenceDialog d = new ColorSequenceDialog(owner, seq);

            d.show();
        } else if (e.getSource() == associate_pdb_menuitem) {
            JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());

            fc.setFileFilter(new DirectoryFileFilter(
                    new javax.swing.filechooser.FileFilter() {
                        public boolean accept(File f) {
                            return f.getName().endsWith(".pdb") && !f.getName().startsWith(".");
                        }

                        public String getDescription() {
                            return "PDB (*.pdb)";
                        }
                    }
                )
            );
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileHidingEnabled(true);// don't show hidden file
            if (fc.showDialog(owner, "Select PDB File") == JFileChooser.APPROVE_OPTION) {
                PathManager.setAlignmentPath(fc.getCurrentDirectory());
            }
            Sequence seq = alignment.getSequence(mouse_row_pos);

            // allow user to select chains
            File newPdbFile = null;

            try {
                if (fc.getSelectedFile() == null)
                    return;
                String[] chainNames = PdbUtil.getProChainNames(fc.getSelectedFile());
                ChoosePdbChainDialog chainChooser = new ChoosePdbChainDialog(owner, chainNames);

                chainChooser.show();
                String chainName = chainChooser.getSelectedChain();

                if (chainName == null) {
                    ErrorDialog.showErrorDialog(this, "No chain selected");
                    return;
                }
                newPdbFile = PdbUtil.renumberPDB(fc.getSelectedFile(), chainName);
            } catch (Exception exp) {
                ErrorDialog.showErrorDialog(this, "Error in parsing pdb file.", exp);
                return;
            }
            // compare existing sequence and the pdb sequence
            String pdb_seq = "";

            try {
                pdb_seq = Sequence.getSequenceFromPDBFile(newPdbFile);
            } catch (IOException ex) {
                ErrorDialog.showErrorDialog(this, "Unable to read sequence from the pdb file", ex);
                return;
            }

            String ali_seq = AminoAcid.aaToString(seq.getRawAA());

            if (pdb_seq != null && ali_seq.equalsIgnoreCase(pdb_seq)) {
                seq.setPDBFile(newPdbFile);
            } else {
                ErrorDialog.showErrorDialog(this,
                    "Different sequence found in the PDB file. Please make sure sequence in pdb file is the same as sequence in the alignment file \n"
                    + pdb_seq + "\n" + ali_seq + "\n");
                return;
            }
        } else if (e.getSource() == startindex_seq_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            StartIndexDialog d = new StartIndexDialog(owner, seq);

            d.show();
            props.updateCursor(props.getCursorRow(), props.getCursorColumn());
        } else if (e.getSource() == add_ann_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            EditLineAnnDialog d = new EditLineAnnDialog(owner, seq, -1);

            d.show();
        } else if (e.getSource() == add_pdb_ann_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            String la = seq.getLineAnnFromPDBFile();

            if (la != null)
                seq.addLineAnnotation("sec. struct.", la);
        } else if (e.getSource() == del_ann_menuitem && mouse_ann_pos >= 0) {
            Sequence seq = alignment.getSequence(mouse_row_pos);

            seq.deleteLineAnnotation(mouse_ann_pos);
        } else if (e.getSource() == edit_ann_menuitem && mouse_ann_pos >= 0) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            EditLineAnnDialog d =
                new EditLineAnnDialog(owner, seq, mouse_ann_pos);

            d.show();
        } /* else if (e.getSource() == set_pdb_menuitem) {
         Sequence seq = alignment.getSequence(mouse_row_pos);
         JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());
         fc.setFileFilter(new DirectoryFileFilter(loader.getFileFilter()));
         fc.setDialogType(JFileChooser.OPEN_DIALOG);
         if (seq.getPDBFile() != null)
         fc.setSelectedFile(seq.getPDBFile());

         if (fc.showDialog(owner, "Select PDB File")
         == JFileChooser.APPROVE_OPTION) {
         seq.setPDBFile(fc.getSelectedFile());
         PathManager.setAlignmentPath(fc.getCurrentDirectory());
         }
         }
         else if (e.getSource() == set_and_view_pdb_menuitem) {
         Sequence seq = alignment.getSequence(mouse_row_pos);
         JFileChooser fc = new JFileChooser(PathManager.getAlignmentPath());
         fc.setFileFilter(new DirectoryFileFilter(loader.getFileFilter()));
         fc.setDialogType(JFileChooser.OPEN_DIALOG);
         if (seq.getPDBFile() != null)
         fc.setSelectedFile(seq.getPDBFile());

         if (fc.showDialog(owner, "Select PDB File")
         == JFileChooser.APPROVE_OPTION) {
         seq.setPDBFile(fc.getSelectedFile());
         PathManager.setAlignmentPath(fc.getCurrentDirectory());
         }

         if (seq.getPDBFile() == null)
         return;
         try {
         Reader r = new FileReader(seq.getPDBFile());
         PdbStructure structure = loader.loadStructure(r);
         r.close();
         PdbFrame frame = new PdbFrame(owner, props,
         seq, structure);
         frame.show();
         }
         catch (Exception exp) {
         ErrorDialog.showErrorDialog(this, "Error displaying "
         + "PDB structure.",
         exp);
         }
         }
         */ else if (e.getSource() == view_pdb_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);

            if (seq.getPDBFile() == null)
                return;
            try {
                Reader r = new FileReader(seq.getPDBFile());
                PdbStructure structure = loader.loadStructure(r);

                r.close();
                PdbFrame frame = new PdbFrame(owner, props,
                        seq, structure);

                props.addListener(frame);
                frame.show();
            } catch (Exception exp) {
                ErrorDialog.showErrorDialog(this, "Error displaying "
                    + "PDB structure.",
                    exp);
            }
        } else if (e.getSource() == srs_menuitem) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            String id = seq.getName();

            try {
                SRSEntry entry = new SRSEntry(srs_server.getQueryReader(id));

                if (seq.getRawAlignment(entry.getAAs()) != 0)
                    JOptionPane.showMessageDialog(this,
                        "Sequence is not "
                        + "identical to SRS "
                        + "sequence",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                SRSFeatureDialog d = new SRSFeatureDialog(owner,
                        seq,
                        entry);

                d.show();
            } catch (Exception exp) {
                ErrorDialog.showErrorDialog(this, "Error retrieving PDB "
                    + "features. Perhaps no entry "
                    + "exists? The sequence name has to be the same as the ID in the SRS entry, e.g. LDH_BACST \n"
                    + "If you are running behind a firewall, please check the proxy setting the pfaat.properties file.",
                    exp);

            }
        }
    }
}
