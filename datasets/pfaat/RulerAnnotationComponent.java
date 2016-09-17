package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.JColorChooser;
import java.util.*;
import java.lang.Character;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.colorscheme.*;
import com.neogenesis.pfaat.util.*;


public class RulerAnnotationComponent extends JPanel
    implements ActionListener, AlignmentListener, 
        MouseListener, KeyListener, DisplayPropertiesListener {
    private Alignment alignment;
    private AlignmentFrame owner;
    private DisplayProperties props;
    private int size;
    private JPopupMenu popup;
    private JMenuItem change_color_menuitem;
    private JMenu delete_columns_menu;
    private JMenuItem delete_columnsleft_menuitem;
    private JMenuItem delete_columnsright_menuitem;
    private JMenuItem delete_columnsselected_menuitem;
    private JMenuItem delete_columnsallbutselected_menuitem;
    private JMenu justify_columns_menu;
    private JMenuItem justify_columnsleft_menuitem;
    private JMenuItem justify_columnsright_menuitem;
    private boolean mouse_pressed = false;
    
    public RulerAnnotationComponent(Alignment alignment, DisplayProperties props,
        AlignmentFrame owner) {
        super();
        this.props = props;
        this.alignment = alignment;
        this.owner = owner;
        size = alignment.maxLength();
        setBackground(Color.white);
        alignment.addListener(this);
        props.addListener(this);
        owner.addKeyListener(this);
        addMouseListener(this);
    
        popup = new JPopupMenu();

        change_color_menuitem = new JMenuItem("Change color");
        change_color_menuitem.addActionListener(this);
        popup.add(change_color_menuitem);
  
        delete_columns_menu = new JMenu("Delete columns");
        delete_columns_menu.addActionListener(this);
      
        delete_columnsleft_menuitem = new JMenuItem("to the left");
        delete_columnsleft_menuitem.addActionListener(this);
        delete_columns_menu.add(delete_columnsleft_menuitem);
      
        delete_columnsright_menuitem = new JMenuItem("to the right");
        delete_columnsright_menuitem.addActionListener(this);
        delete_columns_menu.add(delete_columnsright_menuitem);
      
        delete_columnsselected_menuitem = new JMenuItem("selected");
        delete_columnsselected_menuitem.addActionListener(this);
        delete_columns_menu.add(delete_columnsselected_menuitem);

        delete_columnsallbutselected_menuitem = new JMenuItem("all but selected");
        delete_columnsallbutselected_menuitem.addActionListener(this);
        delete_columns_menu.add(delete_columnsallbutselected_menuitem);

        popup.add(delete_columns_menu); 

        justify_columns_menu = new JMenu("Justify columns");
        justify_columns_menu.addActionListener(this);
      
        justify_columnsleft_menuitem = new JMenuItem("to the left");
        justify_columnsleft_menuitem.addActionListener(this);
        justify_columns_menu.add(justify_columnsleft_menuitem);
      
        justify_columnsright_menuitem = new JMenuItem("to the right");
        justify_columnsright_menuitem.addActionListener(this);
        justify_columns_menu.add(justify_columnsright_menuitem);
        popup.add(justify_columns_menu);        
    }

    public int findMouseColumn(int x) {
        return (int) ((float) x / ((float) (props.getResidueWidth())));
    }
    
    public void actionPerformed(ActionEvent e) {
        // if (!mouse_pressed) return;
        if (e.getSource() == change_color_menuitem) {
            boolean letterInSelection = false;

            letterInSelection = true;

            /* for (int i = 0; i < alignment.rulerAnnotationsSize(); i++) {
             if (alignment.getRulerAnnotation(i).getLetter() != null)
             letterInSelection = true;
             } */
            if (letterInSelection) {
                Color newColor = JColorChooser.showDialog(this, 
                        "Select new column annotation color", Color.red); 

                if (newColor != null) setRulerAnnotationColors(newColor);
            }
        } else if (e.getSource() == delete_columnsleft_menuitem) { 
            Sequence sequence = alignment.getSequence(0);
            int firstIdx = alignment.getFirstColumnSelected();

            if (firstIdx != -1) {
                for (int i = 0; i < alignment.size(); i++) {
                    sequence = alignment.getSequence(i);
                    try { 
                        sequence.shiftAA(firstIdx, -firstIdx + 1);
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(owner, 
                            "Unable to delete " +
                            "column in sequence " + 
                            sequence.getName(), exp);
                    }
                }
                alignment.removeColumnRange(1, firstIdx);
                alignment.removeRulerAnnotationRange(1, firstIdx);
            }
            alignment.deselectAllColumns(props);
        } else if (e.getSource() == delete_columnsright_menuitem) { 
            Sequence sequence = alignment.getSequence(0);
            int lastIdx = alignment.getLastColumnSelected();

            if ((lastIdx != -1) && 
                (lastIdx < alignment.getColumnsSize() - 1)) {
                for (int i = 0; i < alignment.size(); i++) {
                    sequence = alignment.getSequence(i);
                    try {
                        sequence.shiftAA(sequence.length() - 1,
                            -sequence.length() + lastIdx + 2);
                        sequence.deleteAA(sequence.length() - 1);
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(owner, 
                            "Unable to delete " +
                            "column in sequence " + 
                            sequence.getName(), exp);
                    }
                }
                alignment.removeColumnRange(lastIdx + 1,
                    alignment.getColumnsSize());
                alignment.removeRulerAnnotationRange(lastIdx + 1, 
                    alignment.rulerAnnotationsSize());
            }
            alignment.deselectAllColumns(props);
        } else if (e.getSource() == delete_columnsselected_menuitem) {
            Sequence sequence;
            ArrayList cols = new ArrayList();
            ArrayList rangeCols = new ArrayList();
            int left, right, column, neighbors;

            for (int col = 0; col <= alignment.getLastColumnSelected(); col++)
                if (alignment.isColumnSelected(col))
                    cols.add(new Integer(col));
            for (int i = 0; i < cols.size(); i++) {
                neighbors = 0;
                column = ((Integer) cols.get(i)).intValue();
                left = 
                        i == 0 ?
                        column :
                        ((Integer) cols.get(i - 1)).intValue();
                right = 
                        i == cols.size() - 1 ?
                        column :
                        ((Integer) cols.get(i + 1)).intValue();
                if (column - left == 1) neighbors++;
                if (right - column == 1) neighbors++;
                if (neighbors == 0) {
                    rangeCols.add(new Integer(column));
                    rangeCols.add(new Integer(column));
                }
                if (neighbors == 1)
                    rangeCols.add(new Integer(column));
            }
            int start, end;

            for (int row = 0; row < alignment.size(); row++) {
                sequence = alignment.getSequence(row);
                for (int i = rangeCols.size() - 1; i >= 1; i -= 2) {
                    start = ((Integer) rangeCols.get(i)).intValue();
                    end = ((Integer) rangeCols.get(i - 1)).intValue();
                    try {
                        if (row == 0) {
                            alignment.removeColumnRange(end, start + 1);
                            alignment.removeRulerAnnotationRange(end, start + 1);
                        }
                        sequence.shiftAA(start + 1, end - start - 1);
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(owner, 
                            "Unable to delete " +
                            "column in sequence " + 
                            sequence.getName(), exp);
                    }
                }
            }
            alignment.deselectAllColumns(props);
        } else if (e.getSource() == delete_columnsallbutselected_menuitem) {
            Sequence sequence;
            ArrayList cols = new ArrayList();
            ArrayList rangeCols = new ArrayList();
            int left, right, column, neighbors;

            for (int col = 1; col < alignment.maxLength(); col++)
                if (!alignment.isColumnSelected(col))
                    cols.add(new Integer(col));
            for (int i = 0; i < cols.size(); i++) {
                neighbors = 0;
                column = ((Integer) cols.get(i)).intValue();
                left = 
                        i == 0 ? 
                        column : 
                        ((Integer) cols.get(i - 1)).intValue();
                right = 
                        i == cols.size() - 1 ?
                        column : 
                        ((Integer) cols.get(i + 1)).intValue();
                if (column - left == 1) neighbors++;
                if (right - column == 1) neighbors++;
                if (neighbors == 0) {
                    rangeCols.add(new Integer(column));
                    rangeCols.add(new Integer(column));
                }
                if (neighbors == 1)
                    rangeCols.add(new Integer(column));
            }
            int start, end;

            for (int row = 0; row < alignment.size(); row++) {
                sequence = alignment.getSequence(row);
                for (int i = rangeCols.size() - 1; i >= 1; i -= 2) {
                    start = ((Integer) rangeCols.get(i)).intValue();
                    end = ((Integer) rangeCols.get(i - 1)).intValue();
                    try {
                        if (row == 0) {
                            alignment.removeColumnRange(end, start + 1);
                            alignment.removeRulerAnnotationRange(end, start + 1);
                        }
                        sequence.shiftAA(start + 1, end - start - 1);
                    } catch (Exception exp) {
                        ErrorDialog.showErrorDialog(owner, 
                            "Unable to delete " +
                            "column in sequence " + 
                            sequence.getName(), exp);
                    }
                }
            }
            alignment.deselectAllColumns(props); 
        } else if ((e.getSource() == justify_columnsleft_menuitem) ||
            (e.getSource() == justify_columnsright_menuitem)) {
            boolean continuous = true;
            int start = alignment.getFirstColumnSelected();
            int end = alignment.getLastColumnSelected();
            Sequence sequence = alignment.getSequence(0);

            if ((start < 0) || (start > alignment.maxLength()) ||
                (end < 0) || (end > alignment.maxLength()) ||
                alignment.columnsSelected() <= 2) {
                ErrorDialog.showErrorDialog(owner, "Invalid column range.");
                return;
            }
            for (int idx = start; idx <= end; idx++) {
                if (!alignment.isColumnSelected(idx)) continuous = false;
            }
            if (continuous) {
                for (int i = 0; i < alignment.size(); i++) {
                    sequence = alignment.getSequence(i);
                    int deletes = 0;

                    for (int col = start; col <= end; col++) 
                        if (sequence.getAA(col).isGap()) deletes++;
                    int initDeletes = deletes; 	 

                    try {
                        // if (props.getSeqSelect(alignment.getSequence(i))) {
                        int col = start;

                        while ((deletes > 0) && (col < sequence.length())) {
                            while (sequence.getAA(col).isGap() && (deletes > 0) && 
                                (col < sequence.length())) {
                                sequence.deleteAA(col);
                                if (e.getSource() == justify_columnsleft_menuitem) 
                                    sequence.shiftAA(end, 1);
                                deletes--;
                            }
                            col++;
                        }
                        // }
                    } catch (Exception exp) {}
                    if (e.getSource() == justify_columnsright_menuitem) {
                        try { 
                            // if (props.getSeqSelect(alignment.getSequence(i)))
                            sequence.shiftAA(start, initDeletes); 
                        } catch (Exception exp) {}
                    }
                }
            } else {
                ErrorDialog.showErrorDialog(owner, "Selected column range must be continuous.");
                return;
            }
        }
    }

    public void setRulerAnnotationColors(Color newColor) {
        for (int i = 0; i < alignment.rulerAnnotationsSize(); i++) {
            Alignment.RulerAnnotation rann = alignment.getRulerAnnotation(i);

            if (alignment.isColumnSelected(i)) {
                if (rann.getLetter() == null)
                    rann.setLetter(" ");
                rann.setColor(newColor);
            }
        }
    }
    
    public void mouseClicked(MouseEvent e) {}    

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) { 
        mouse_pressed = false;
        if ((e.getModifiers() == InputEvent.BUTTON3_MASK && maybeShowPopup(e)) &&
            !props.isSeqAnnEditing()) return;
        if (popup.isVisible()) return;
    }

    public void mousePressed(MouseEvent e) {
        mouse_pressed = true;
        if (((e.getModifiers() == InputEvent.BUTTON1_MASK) 
                || e.isShiftDown() || e.isControlDown()) &&
            !props.isSeqAnnEditing() && findMouseColumn(e.getX()) >= 1) {       
            if (e.getClickCount() == 1) {
                int columnsSize = alignment.getColumnsSize();

                props.setCursorHidden(true); 
                props.setRulerEditing(true);
                // if (props.getCursorRow() != -1)
                int idx = findMouseColumn(e.getX());

                props.updateCursor(-1, idx);
                owner.displayHighlightsChanged(props, alignment.getSequence(0));

                alignment.setLastRulerAnnIdxSelected(-1);
                alignment.deselectAllRulerSelections();
        
                if (!e.isShiftDown() && !e.isControlDown()) {
                    if (!alignment.isColumnSelected(idx)) {
                        alignment.deselectAllColumns(props);
                        props.clearHighlights();
                    }
                    alignment.setColumnSelected(idx, true, props);
                    alignment.setLastColumnIdxSelected(idx);
                } else if (e.isShiftDown() && !e.isControlDown()) {
                    int lastColIdx = alignment.getLastColumnIdxSelected();

                    if (lastColIdx != -1) {
                        int sgn = sign(lastColIdx - idx);

                        for (int i = idx; i != (lastColIdx + sgn); i = i + sgn) 
                            alignment.setColumnSelected(i, true, props);
                        alignment.setLastColumnIdxSelected(idx);
                    }
                } else if (!e.isShiftDown() && e.isControlDown()) {
                    if (alignment.isColumnSelected(idx)) 
                        alignment.setColumnSelected(idx, false, props);
                    else {
                        alignment.setColumnSelected(idx, true, props);
                        alignment.setLastColumnIdxSelected(idx);
                    }
                } else if (e.isShiftDown() && e.isControlDown()) {
                    alignment.setColumnSelected(idx, true, props);
                    alignment.setLastColumnIdxSelected(idx);
                }
                revalidate();
                repaint();
            } else if (e.getClickCount() == 2) {
                props.setCursorHidden(true); 
                props.setRulerEditing(true);
                alignment.deselectAllRulerSelections();
                int idx = findMouseColumn(e.getX());

                props.updateCursor(-1, idx);
                if (!alignment.isColumnSelected(idx)) {
                    alignment.deselectAllColumns(props);
                    props.clearHighlights();
                } else 
                    for (int i = 0; i < alignment.rulerAnnotationsSize(); i++)
                        if (alignment.isColumnSelected(i))
                            alignment.getRulerAnnotation(i).setSelected(true);
                        else 
                            alignment.getRulerAnnotation(i).setSelected(false);
                revalidate();
                repaint();  
            }
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if (props.isCursorHidden() && !props.isSeqAnnEditing()) {
            char letterChar = e.getKeyChar();

            if (!Character.isISOControl(letterChar) || letterChar == ' ' || letterChar == '\b') {
                int leftmostCol = alignment.maxLength();

                for (int col = 0; col < alignment.rulerAnnotationsSize(); col++) {
                    Alignment.RulerAnnotation rann = alignment.getRulerAnnotation(col);

                    if (rann.isSelected()) {
                        if (!Character.isISOControl(letterChar) || letterChar == ' ')
                            rann.setLetter((new Character(letterChar)).toString());    
                        else if (letterChar == '\b')
                            rann.setLetter(null);  
                        if (col < leftmostCol) leftmostCol = col;
                    }
                }
                // props.updateCursor(-1, leftmostCol);
                revalidate();
                repaint();
            }
        }
    }
    
    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    private void resizeToAlignment() {
        int new_size = alignment.maxLength();

        if (new_size != size) {
            size = new_size;
            revalidate();
            repaint();
        }
    }

    // SequenceListener interface
    public void sequenceAAChanged(Sequence aaseq) {
        resizeToAlignment();
    }

    public void sequenceNameChanged(Sequence aaseq) {}

    public void sequenceAnnotationChanged(Sequence aaseq) {}

    public void sequenceGroupChanged(Sequence aaseq) {}

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {}

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {}

    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {}

    public void alignmentSeqInserted(Alignment align, int i) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        resizeToAlignment();
    }

    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        resizeToAlignment();
    }

    public void alignmentSeqSwapped(Alignment alignment, int i, int j) {}

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {
        if (align != this.alignment) 
            throw new RuntimeException("bound to incorrect alignment");
        resizeToAlignment();
    }

    // DisplayPropertiesListener interface
    public void displayAnnViewChanged(DisplayProperties dp, 
        Sequence seq,
        boolean show) {}

    public void displaySeqSelectChanged(DisplayProperties dp, 
        Sequence seq,
        boolean select) {}

    public void displayColorSchemeChanged(DisplayProperties dp, 
        ColorScheme old) {}

    public void displayFontChanged(DisplayProperties dp) { 
        if (dp != props)
            throw new RuntimeException("bound to incorrect DisplayProperties");
        revalidate();
        repaint();
    }

    public void displayRenderGapsChanged(DisplayProperties dp) {}

    public void displayGroupEditingChanged(DisplayProperties dp) {}

    public void displayOverwriteChanged(DisplayProperties dp) {}

    public void displayHighlightsChanged(DisplayProperties dp, Sequence seq) {}

    public void displayHighlightsChanged(DisplayProperties dp, Sequence[] seqs) {}

    public Dimension getPreferredSize() {
        int height = props.getResidueHeight();
        int width = props.getResidueWidth() * size;

        return new Dimension(width, height);
    }
   
    private boolean maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            boolean colsel = alignment.isColumnSelected();

            change_color_menuitem.setEnabled(colsel);
            delete_columns_menu.setEnabled(colsel);
            delete_columnsleft_menuitem.setEnabled(colsel);
            delete_columnsright_menuitem.setEnabled(colsel);
            delete_columnsselected_menuitem.setEnabled(colsel);
            delete_columnsallbutselected_menuitem.setEnabled(colsel);
            justify_columns_menu.setEnabled(colsel);
            justify_columnsleft_menuitem.setEnabled(colsel);
            justify_columnsright_menuitem.setEnabled(colsel);
            Utils.showPopup(popup, e.getComponent(), e.getX(), e.getY());
            return true;
        }
        return false;
    }
   
    // render the sequence
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color fgColor, bgColor, swap;

        bgColor = Color.white;
        int y = 0;
        int font_y = y + props.getFontYOffset() + 2;
        int residue_height = props.getResidueHeight();
        int residue_width = props.getResidueWidth();
        FontMetrics fm = props.getFontMetrics();

        g.setFont(props.getFont());
        String str = "";

        if (alignment.maxLength() > alignment.rulerAnnotationsSize()) {
            for (int i = 0; i < (alignment.maxLength() - alignment.rulerAnnotationsSize());
                i++) {
                alignment.addRulerAnnotation();
                alignment.addColumn();
            }
        } else if (alignment.rulerAnnotationsSize() > alignment.maxLength()) {
            for (int i = 0; 
                i < (alignment.rulerAnnotationsSize() - alignment.maxLength()); i++) {
                alignment.removeRulerAnnotationFromEnd();
                alignment.removeColumnFromEnd();
            }
        }
    
        if (alignment.isRulerAnnotationSelected() || alignment.isColumnSelected()) { 
            props.setCursorHidden(true);
            props.setRulerEditing(true);
        } else {
            props.setCursorHidden(false);
            props.setRulerEditing(false);
        }
        Alignment.RulerAnnotation rann = 
            new Alignment.RulerAnnotation(null, Color.white, false);

        for (int idx = 0; idx < alignment.rulerAnnotationsSize(); idx++) {
            str = " "; 
            int x = idx * residue_width - 1;

            if (idx < alignment.rulerAnnotationsSize()) {
                rann = alignment.getRulerAnnotation(idx);
                if (rann.getLetter() != null) {
                    str = rann.getLetter();
                    bgColor = rann.getColor();
                    g.setColor(bgColor);
                    g.fillRect(x + 1, 0, residue_width, residue_height);
                    g.setColor(props.inverseRGB(bgColor));
                    g.drawString(str, x + 1, font_y);		  
                }
            }
        }
        for (int idx = 0; idx < alignment.rulerAnnotationsSize(); idx++) {
            int x = idx * residue_width - 1;

            if (idx < alignment.rulerAnnotationsSize()) {
                rann = alignment.getRulerAnnotation(idx);
                if (rann.isSelected()) {
                    bgColor = rann.getColor();
                    g.setColor(props.inverseRGB(bgColor));
                    g.drawRect(x + 1, 0, residue_width, residue_height);
                }
            }
        }
    }
   
    public int sign(int a) { 
        if (a == 0) return 1;
        else return a / Math.abs(a); 
    }
}

