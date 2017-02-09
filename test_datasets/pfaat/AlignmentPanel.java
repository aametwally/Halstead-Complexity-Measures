package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.lang.Character;
import javax.swing.*;

import com.neogenesis.pfaat.swingx.*;    


/**
 * Display component for alignment sequence data.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:03 $ */
public class AlignmentPanel extends JPanel 
    implements MouseListener, 
        MouseMotionListener,
        KeyListener,
        AlignmentListener,
        Scrollable {
    // underlying alignment
    protected Alignment alignment;
    // display properties
    protected DisplayProperties props;

    protected Frame owner;	
    private long tabWhen = -1;
    private int pageHeight = 15;
    private int tabSize = 10;
    private long dblclkRes = 400;
    
    public AlignmentPanel(Frame owner, Alignment alignment,
        DisplayProperties props) {
        super();

        setBackground(Color.white);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.props = props;
        this.alignment = alignment;
        this.owner = owner;
        alignment.addListener(this);
	
        javax.swing.FocusManager.setCurrentManager(new CustomFocusManager());
        setToolTipText("");
	
        for (int i = 0; i < alignment.size(); i++) 
            add(new SequenceComponent(alignment.getSequence(i), props));

        addMouseListener(this);
        addMouseMotionListener(this);
        owner.addKeyListener(this); 
    }
		
    public boolean isManagingFocus() {
        return true;
    }
		
    public Dimension getPreferredSize() {
        return getLayout().preferredLayoutSize(this);
    }
    // MouseListener, MouseMotionListener interface
    private boolean mouse_pressed = false;
    private int mouse_row_pos;
    private int mouse_col_pos;

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) { 
        mouse_row_pos = findRow(e.getX(), e.getY());
        if (mouse_row_pos >= 0) {
            SequenceComponent sc = (SequenceComponent) getComponent(mouse_row_pos);
            Point p = sc.getLocation();

            mouse_col_pos = sc.findColumn(e.getX() - p.x, e.getY() - p.y);			
        } else
            mouse_col_pos = -1;
        mouse_pressed = mouse_row_pos >= 0 && mouse_col_pos >= 1;

        if (e.getClickCount() == 1 && mouse_pressed) {
            props.setSeqAnnEditing(false);
            props.setCursorHidden(false);
            alignment.deselectAllRulerSelections();
            alignment.deselectAllColumns(props);
            props.updateCursor(mouse_row_pos, mouse_col_pos);
        }
	
        if (e.getClickCount() >= 2 && mouse_pressed) {
            Sequence seq = alignment.getSequence(mouse_row_pos);
            EditResAnnDialog d = 
                new EditResAnnDialog(owner, props, 
                    alignment, seq, mouse_col_pos);

            d.show();
        }
    }
    
    public void mouseReleased(MouseEvent e) { 
        mouse_pressed = false;
    }

    public void mouseDragged(MouseEvent e) {
        int cursorRow = props.getCursorRow();
        int cursorColumn = props.getCursorColumn();
	
        if (mouse_pressed && e.isShiftDown()) {
            int idx = findRow(e.getX(), e.getY());

            if (idx >= 1 && idx != mouse_row_pos) {
                int shift = idx - mouse_row_pos;
                Sequence[] seqs;

                seqs = props.getAllSelected();
                boolean isInSelection = false;
                Sequence seq = alignment.getSequence(mouse_row_pos);

                for (int i = 0; i < seqs.length; i++)
                    if (seq.equals(seqs[i])) {
                        isInSelection = true;
                        break;
                    }
                if ((props.isGroupEditing() && props.getSelectedCount() > 0 && isInSelection)) 
                    seqs = props.getAllSelected();
                else {
                    seqs = new Sequence[1];
                    seqs[0] = alignment.getSequence(mouse_row_pos);
                }
		 
                int[] idxs = new int[seqs.length];

                for (int i = idxs.length - 1; i >= 0; i--) 
                    idxs[i] = alignment.getIndex(seqs[i]);
                Arrays.sort(idxs);
                if (e.isControlDown()) {
                    try {
                        if (shift < 0 && idxs[0] + shift >= 0) {
                            for (int i = 0; i < idxs.length; i++) 
                                alignment.swapSequence(idxs[i], idxs[i] + shift);
                            cursorRow += shift;
                            props.updateCursor(cursorRow, cursorColumn);
                        } else if (shift > 0 
                            && idxs[idxs.length - 1] + shift 
                            < alignment.size()) {
                            for (int i = idxs.length - 1; i >= 0; i--) 
                                alignment.swapSequence(idxs[i], idxs[i] + shift);
                            cursorRow += shift;
                            props.updateCursor(cursorRow, cursorColumn);
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                            return;
                        }
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(this,
                            "Unable to move "
                            + "sequence in alignment.",
                            exp);
                        return;
                    }
                }
                mouse_row_pos = idx;
            } else if (!e.isControlDown()) {
                SequenceComponent sc = (SequenceComponent) getComponent(mouse_row_pos);
                Point p = sc.getLocation();
                int col = sc.findColumn(e.getX() - p.x, e.getY() - p.y);
                int row = findRow(e.getX(), e.getY());

                if (mouse_col_pos != col) {
                    Sequence[] seqs = props.getAllSelected();
                    Sequence seq = alignment.getSequence(mouse_row_pos);
                    boolean isInSelection = false;
				
                    if (props.isGroupEditing() && props.getSelectedCount() > 0) {
                        for (int i = 0; i < seqs.length; i++)
                            if (seq.equals(seqs[i])) isInSelection = true;
                    }
                    if (((props.isGroupEditing() &&
                                props.getSelectedCount() > 0 && 
                                !isInSelection)) || 
                        !props.isGroupEditing() || 
                        (props.isGroupEditing() && props.getSelectedCount() == 0)) {
                        seqs = new Sequence[1];
                        seqs[0] = alignment.getSequence(mouse_row_pos);
                    }
                    int shift = col - mouse_col_pos;

                    if (shift == 0 || mouse_row_pos != props.getCursorRow())
                        return;
                    if (!props.isOverwrite()) {
                        for (int i = seqs.length - 1; i >= 0; i--) {
                            if (!seqs[i].gapsShiftedOnly(mouse_col_pos, shift)) {
                                Toolkit.getDefaultToolkit().beep();
                                return;
                            }
                        }
                    }
                    if (mouse_col_pos + shift < 1) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    for (int i = seqs.length - 1; i >= 0; i--) {

                        try {
                            seqs[i].shiftAA(mouse_col_pos, shift);

                        } catch (Exception exp) {
                            ErrorDialog.showErrorDialog(this,
                                "Unable to shift "
                                + "sequence "
                                + seqs[i].getName(),
                                exp);
                        }
                    }
                    cursorColumn += shift;
                    props.updateCursor(cursorRow, cursorColumn);
                    mouse_col_pos = col;
                }
            }
        }
    }

    // javax.swing.FocusManager manager = javax.swing.FocusManager.getCurrentManager();
		 
    public void mouseMoved(MouseEvent e) {}

    public void TabKeyPressed(boolean shiftDown) {
        if (!props.isSeqAnnEditing() && !props.isRulerEditing() && !props.isCursorHidden()) {
            int cursorRow = props.getCursorRow();
            int cursorColumn = props.getCursorColumn();

            if (!shiftDown)
                cursorColumn = 
                        (cursorColumn + tabSize) < alignment.getSequence(cursorRow).length() ?
                        cursorColumn + tabSize : (alignment.getSequence(cursorRow).length() - 1);
            else if (shiftDown)
                cursorColumn = 
                        (cursorColumn - tabSize) >= 1 ? cursorColumn - tabSize : 1;
            props.updateCursor(cursorRow, cursorColumn);					
        }
    }
		 
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        boolean backspace = false;
        String code = (String.valueOf(e.getKeyChar())).toUpperCase();

        if ((!props.isSeqAnnEditing() && !props.isCursorHidden()) && (
                keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN || 
                keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_PAGE_DOWN || 
                keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE ||
                AminoAcid.isAminoAcidSequence(code))) {
            int cursorRow = props.getCursorRow();
            int cursorColumn = props.getCursorColumn();

            if (keyCode == KeyEvent.VK_UP && cursorRow > 0) 
                cursorRow--;
            else if (keyCode == KeyEvent.VK_DOWN && cursorRow < (alignment.size() - 1)) 
                cursorRow++;
            else if (keyCode == KeyEvent.VK_PAGE_UP) 
                cursorRow = (cursorRow - pageHeight) < 0 ? 
                        0 : (cursorRow - pageHeight);
            else if (keyCode == KeyEvent.VK_PAGE_DOWN) 
                cursorRow = (cursorRow + pageHeight) >= (alignment.size() - 1) ? 
                        (alignment.size() - 1) : (cursorRow + pageHeight);
            else if (keyCode == KeyEvent.VK_LEFT && cursorColumn > 1 && !e.isShiftDown()) 
                cursorColumn--;
            else if (keyCode == KeyEvent.VK_RIGHT && cursorColumn < alignment.getSequence(cursorRow).length()) 
                cursorColumn++;
            else if ((keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) &&
                (cursorColumn > 1) && 
                ((!props.isOverwrite() && 
                        alignment.getSequence(cursorRow).getAA(cursorColumn - 1).isGap()) ||
                    props.isOverwrite())) backspace = true;
	    
            if (backspace || (AminoAcid.isAminoAcidSequence(code) && 
                    (props.isOverwrite() || 
                        (!props.isOverwrite() && 
                            AminoAcid.lookupByCode(code).isGap())))) {
                boolean cursorRowInSelected = false;
                Sequence[] seqs = props.getAllSelected();
      
                for (int i = 0; i < seqs.length; i++) 
                    if (alignment.getIndex(seqs[i]) == cursorRow) cursorRowInSelected = true;

                if (!props.isGroupEditing() || !cursorRowInSelected) {
                    if (!backspace) {
                        AminoAcid aa = AminoAcid.lookupByCode(code);

                        (alignment.getSequence(cursorRow)).insertAA(aa, cursorColumn);
                        cursorColumn++;
                    } else if (backspace) {
                        if (!props.isOverwrite() &&
                            !alignment.getSequence(cursorRow).gapsShiftedOnly(cursorColumn, -1))
                            Toolkit.getDefaultToolkit().beep();
                        else {
                            try {
                                alignment.getSequence(cursorRow).shiftAA(cursorColumn, -1); 
                            } catch (Exception exp) {}
                            cursorColumn--;
                        }
                    }
                } else if (props.isGroupEditing() && cursorRowInSelected) {
                    if (!backspace) {
                        AminoAcid aa = AminoAcid.lookupByCode(code);

                        for (int i = 0; i < seqs.length; i++) 
                            seqs[i].insertAA(aa, cursorColumn);
                        cursorColumn++; 
                    } else if (backspace) {
                        boolean allowBackSpace = true;

                        if (!props.isOverwrite()) {
                            for (int i = 0; i < seqs.length; i++) {
                                if (!seqs[i].gapsShiftedOnly(cursorColumn, -1))
                                    allowBackSpace = false;
                            }
                        }
                        if (!allowBackSpace)
                            Toolkit.getDefaultToolkit().beep();
                        else {
                            for (int i = 0; i < seqs.length; i++) {
                                try {
                                    seqs[i].shiftAA(cursorColumn, -1); 
                                } catch (Exception exp) {}       
                            }
                            cursorColumn--;
                        }
                    }
                }
            }
            props.updateCursor(cursorRow, cursorColumn);
        }		
    }
		
    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}
		
    private int findRow(int x, int y) {
        Component c = findComponentAt(x, y);
        Component[] comps = getComponents();
        int idx;

        for (idx = comps.length - 1; idx >= 1; idx--) {
            if (comps[idx] == c) break;
        }
        return idx;
    }

    // tooltips for column annotations
    public String getToolTipText(MouseEvent e) {
        int row = findRow(e.getX(), e.getY());

        if (row >= 0) {
            SequenceComponent sc = 
                (SequenceComponent) getComponent(row);
            Point p = sc.getLocation();
            int col = sc.findColumn(e.getX() - p.x,
                    e.getY() - p.y);
            Sequence seq = alignment.getSequence(row);
            Sequence.ColumnAnnotation ca = seq.getColumnAnnotation(col);

            if (col >= 1 && ca != null && props.isAnnotationPopupEnabled() && 
                props.showResAnn()) {
                StringBuffer ResidueInfo;

                ResidueInfo = new StringBuffer("");
                ResidueInfo.append("<html> <font face = 'Courier' size = 1>");
                ResidueInfo.append(ca.getAnnotation());
                ResidueInfo.append("</font> </html>");
                return ResidueInfo.toString();
            } else return null;
        } else return null;
    }
		
    // upper left hand corner of a particular residue
    public Point getResiduePosition(int seq_idx, int res_idx) {
        SequenceComponent sc = 
            (SequenceComponent) getComponent(seq_idx);
        Point base = sc.getLocation();
        Point p = sc.getResiduePosition(res_idx);

        p.x += base.x;
        p.y += base.y;
        return p;
    }

    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {}

    public void alignmentSeqInserted(Alignment align, int i) {
        if (alignment != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        add(new SequenceComponent(alignment.getSequence(i), props), i);
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
        ((SequenceComponent) getComponent(i)).setSequence(alignment.getSequence(i));
        ((SequenceComponent) getComponent(j)).setSequence(alignment.getSequence(j));
    }

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {}

    // Scrollable interface
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, 
        int orientation, 
        int direction) {
        return orientation == SwingConstants.VERTICAL
            ? visibleRect.height : visibleRect.width;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, 
        int orientation, 
        int direction) {
        return orientation == SwingConstants.VERTICAL
            ? props.getResidueHeight() : props.getResidueWidth();
    }

    // override default focusmanager to allow tab & shift-tab events to
    // pass to keylistener
    class CustomFocusManager extends javax.swing.FocusManager {
        public CustomFocusManager() {
            super();
        }

        public void processKeyEvent(Component component, KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_TAB || event.getKeyChar() == '\t') {
                // hack: put in time delay to prevent tab key from reigstering multiple
                // times
                if ((event.getWhen() - tabWhen) > dblclkRes || tabWhen == -1) {
                    TabKeyPressed(event.isShiftDown());
                    tabWhen = event.getWhen();
                }
            }
        }

        public void focusNextComponent(Component component) {}

        public void focusPreviousComponent(Component component) {}
    }
}

