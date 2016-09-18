package com.neogenesis.pfaat;


import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;
import java.lang.Character;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.image.BufferedImage;

import com.neogenesis.pfaat.*;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.util.*;
                                    

/**
 * Global column annotation editor.
 **/

/* throughout, the use of the word "line" in reference to annotation
 * specifically means that annotation which is imposed upon a sequence through
 * a set of residue annotations
 */

public class EditAnnotationsDialog extends JDialog implements ActionListener, MouseListener {
    private Alignment alignment;
    private DisplayProperties props;
    private AlignmentFrame owner;
    private ArrayList existing_annotations, annotations;
    private JComboBox existing_annotations_box, new_annotation_box;
    private JTable columnTable; 
    private lineJTable lineTable;
    private DefaultTableModel model, lineModel;
    private JPopupMenu popup, linepopup;
    private JMenuItem new_menuitem, delete_menuitem;
    private JMenuItem line_new_menuitem, line_add_menuitem, line_delete_menuitem;
    private JTabbedPane tabbedPane;
    private JButton ok_button, apply_button, cancel_button;
    private JButton colorButton;
    private Color savedColor;
    private Vector cq, cz;
    private Vector sequenceList;
    private final int numCol = 0;
    private final int seqCol = 1;
    private final int colorCol = 2;
    private final int symbolCol = 3;
    private final int rangeCol = 4;
    private final int annotationCol = 5;
    private final int lineNumCol = 0;
    private final int lineSeqCol = 1;
    private final int lineNameCol = 2;
    private final int lineStartCol = 3;
    private final int lineEndCol = 4;
    private final int lineSymbolCol = 5;
    private final Object headers[] =  
        {"#", "sequence", "color", "symbol", "range", "annotation"};
    private final Object lineHeaders[] = 
        {"#", "sequence", "annotation name", "start index", "end index", "symbol"};
    public EditAnnotationsDialog(AlignmentFrame owner, Alignment alignment, 
        DisplayProperties props) {
        super(owner, "Edit Annotations: " + alignment.getName());
        this.owner = owner;
        this.props = props;
        this.alignment = alignment;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
        // collect unique annotations		
        boolean inAnnotationList = false;
        Sequence sequence;
        Sequence.ColumnAnnotation annotation, otherAnnotation;
        Sequence.ColumnAnnotation[] columnAnnotations;
        Vector uniqueAnnotations = new Vector();

        cq = new Vector();	
        for (int i = 0; i < alignment.size(); i++) {
            sequence = alignment.getSequence(i);
            if (sequence.getColumnAnnotationsCount() > 0) {	
                uniqueAnnotations = new Vector();
                columnAnnotations = sequence.getColumnAnnotations();
                for (int j = 0; j < columnAnnotations.length; j++) {
                    annotation = columnAnnotations[j];
                    inAnnotationList = false;
                    for (int k = 0; k < uniqueAnnotations.size(); k++) 
                        if (annotation.equals((Sequence.ColumnAnnotation) uniqueAnnotations.get(k))) 
                            inAnnotationList = true;					
                    if (!inAnnotationList) uniqueAnnotations.addElement(annotation);
                }
                for (int j = 0; j < uniqueAnnotations.size(); j++) {
                    annotation = (Sequence.ColumnAnnotation) uniqueAnnotations.get(j);
                    cq.addElement(new CollatedColumnAnnotation(
                            i, annotation.getSymbol(), annotation.getColor(), 
                            annotation.getAnnotation(), new Vector()));
                    for (int k = 0; k < columnAnnotations.length; k++) {
                        otherAnnotation = columnAnnotations[k];
                        if (annotation.equals(otherAnnotation)) {
                            ((CollatedColumnAnnotation) cq.lastElement()).addColumnIndex(otherAnnotation.getColumn());		
                        }
                    } 
                }
            }
        }
        Object rowData[][] = getRowData(cq);
        JButton ColorButton[] = new JButton[cq.size()];

        model = new UneditableTableModel(rowData, headers, true);	
        columnTable = new JTable(model);
        formatTable();
	
        columnTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);	
        columnTable.addMouseListener(this);		
	
        JScrollPane scrollPane = new JScrollPane(columnTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	
        tabbedPane.addTab("Column Annotations", scrollPane);
	
        cz = new Vector();
        for (int i = 0; i < alignment.size(); i++) {
            sequence = alignment.getSequence(i);
            if (sequence.getLineAnnotationsCount() > 0) {
                for (int j = 0; j < sequence.getLineAnnotationsCount(); j++) {
                    Sequence.LineAnnotation lineAnnotation = sequence.getLineAnnotation(j);
                    String ann = lineAnnotation.getAnnotation();
                    int k = 0;

                    while (k < ann.length()) {
                        StringBuffer symbol = new StringBuffer();
                        int start = k;
                        char tc = ann.charAt(k);				 

                        if (!Character.isWhitespace(ann.charAt(k))) 
                            symbol.append(ann.charAt(k));
                        while ((k < ann.length()) && (ann.charAt(k) == tc)) k++;	
                        if (symbol.toString().trim().length() != 0)
                            cz.add(new CollatedLineAnnotation(i, j, start, k - 1, lineAnnotation.getName(), symbol.toString()));  		       
			
                    }							
                }
            }	
        }
	
        lineModel = new UneditableTableModel(getLineRowData(cz), lineHeaders, false);
        lineTable = new lineJTable(lineModel);
        formatLineTable();
        lineTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);	
        lineTable.addMouseListener(this);		
	
        JScrollPane lineScrollPane = new JScrollPane(lineTable,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	
        tabbedPane.addTab("Residue Annotations", lineScrollPane);
        getContentPane().add(tabbedPane, BorderLayout.NORTH);
				
        JPanel subpanel = new JPanel();

        ok_button = new JButton("OK");
        ok_button.addActionListener(this);
        subpanel.add(ok_button);
	
        cancel_button = new JButton("Cancel");
        cancel_button.addActionListener(this);
        subpanel.add(cancel_button);		
	
        apply_button = new JButton("Apply");
        apply_button.addActionListener(this);
        subpanel.add(apply_button);

        getContentPane().add(subpanel, BorderLayout.SOUTH);
        setSize(150, 400);
        setLocationRelativeTo(owner);
        pack();			
        makePopups();
    }
    
    public boolean isNewLineAnnotation(int i) {
        if (i == 0) return true;
        if (i >= cz.size()) return true;
        else {
            CollatedLineAnnotation ctemp1 = (CollatedLineAnnotation) cz.get(i);
            CollatedLineAnnotation ctemp2 = (CollatedLineAnnotation) cz.get(i - 1);

            if ((ctemp1.getIndex() != ctemp2.getIndex()) || 
                (ctemp1.getAnnotationIndex() != ctemp2.getAnnotationIndex()))
                return true;
            else return false;
        }
    }
    
    public Object[][] getLineRowData(Vector cz) {
        Object rowData[][] = new Object[cz.size()][lineHeaders.length];

        for (int i = 0; i < cz.size(); i++) {
            CollatedLineAnnotation ca = (CollatedLineAnnotation) cz.get(i);

            rowData[i][lineNumCol] = Integer.toString(i + 1);
            rowData[i][lineSeqCol]
                    = (String) alignment.getSequence(ca.getIndex()).getName();
            rowData[i][lineNameCol] = (String) ca.getName();
            rowData[i][lineStartCol] = Integer.toString(ca.getStart());
            rowData[i][lineEndCol] = Integer.toString(ca.getEnd());
            rowData[i][lineSymbolCol] = (String) ca.getSymbol();
        }
        return rowData;
    }
    
    public Object[][] getRowData(Vector cq) {
        Object rowData[][] = new Object[cq.size()][headers.length];				

        for (int i = 0; i < cq.size(); i++) {
            CollatedColumnAnnotation ca = (CollatedColumnAnnotation) cq.get(i);

            rowData[i][numCol] = Integer.toString(i + 1);
            rowData[i][seqCol] = 
                    (String) alignment.getSequence(ca.getIndex()).getName();	
            rowData[i][colorCol] = (Color) ca.getColor();
            rowData[i][symbolCol] = (String) ca.getSymbol();				
            rowData[i][rangeCol] = 
                    ((Vector) ca.getRange() != null ? (String) ca.getFormattedRange() : "");					
            rowData[i][annotationCol] = (String) ca.getAnnotation(); 
        }
        return rowData;
    }
    
    public void formatTable() {
        TableColumnModel columnModel = columnTable.getColumnModel();
        TableColumn column;
        int annotationColumnWidth = 300;
        int sequenceColumnWidth = 200;		

        columnTable.setDefaultRenderer(Object.class, new ShadedRenderer());
        column = columnModel.getColumn(seqCol);
        column.setPreferredWidth(sequenceColumnWidth);
        column = columnModel.getColumn(colorCol);
        column.setCellRenderer(new ButtonRenderer());
        column.setCellEditor(new ColorChooserEditor());
        column = columnModel.getColumn(annotationCol);
        column.setPreferredWidth(annotationColumnWidth);
    }
    
    public void formatLineTable() {
        TableColumnModel columnModel = lineTable.getColumnModel();
        TableColumn column;
        int sequenceColumnWidth = 200;
        int nameColumnWidth = 50;

        lineTable.setDefaultRenderer(Object.class, new lineModelShadedRenderer());
        column = columnModel.getColumn(lineSeqCol);
        column.setPreferredWidth(sequenceColumnWidth);
        column = columnModel.getColumn(lineNameCol);
        column.setPreferredWidth(nameColumnWidth); 
    }
    
    public class UneditableTableModel extends DefaultTableModel {
        private boolean columnFormat;
        public UneditableTableModel(Object[][] rowData, Object[] headers, 
            boolean columnFormat) {
            super(rowData, headers);
            this.columnFormat = columnFormat;
        }

        public boolean isCellEditable(int row, int column) {
            if (columnFormat)      
                return isColumnModelCellEditable(row, column);
            else 
                return isLineModelCellEditable(row, column);
        }		
    }
    
    public void makePopups() { 
        popup = new JPopupMenu();
        new_menuitem = new JMenuItem("New Annotation");
        new_menuitem.addActionListener(menuActionListener);
        popup.add(new_menuitem);
        delete_menuitem = new JMenuItem("Delete Annotation");
        delete_menuitem.addActionListener(menuActionListener);
        popup.add(delete_menuitem);
	
        linepopup = new JPopupMenu();
        line_new_menuitem = new JMenuItem("New Annotation");
        line_new_menuitem.addActionListener(menuActionListener);
        linepopup.add(line_new_menuitem);
        line_add_menuitem = new JMenuItem("Add to Existing Annotation");
        line_add_menuitem.addActionListener(menuActionListener);
        linepopup.add(line_add_menuitem);
        line_delete_menuitem = new JMenuItem("Delete Annotation");
        line_delete_menuitem.addActionListener(menuActionListener);
        linepopup.add(line_delete_menuitem);
    }
    
    public int sequenceSelector(String title) {
        JOptionPane newSequencePane = new JOptionPane();
        Object sequenceNameObj = 
            newSequencePane.showInputDialog(owner,
                "Select sequence to annotate", 
                title, 
                JOptionPane.PLAIN_MESSAGE, null, 
                alignment.getAllSequenceNames(), 
                null);		

        if (sequenceNameObj != null) 
            return alignment.getIndex(alignment.getSequence(sequenceNameObj.toString()));
        else return -1;
    }
    
    ActionListener menuActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {				
                if (!mouse_pressed) return;
                if (e.getSource() == new_menuitem) {
                    int index = sequenceSelector("New Column Annotation");

                    if (index != -1) { 
                        int idx = 0;
                        boolean isInTable = false;

                        for (int i = 0; i < cq.size(); i++) {
                            if (((CollatedColumnAnnotation) cq.get(i)).getIndex() == index) {
                                isInTable = true;
                                idx = i;
                            }
                        }
                        if (!isInTable) { 
                            if (index < ((CollatedColumnAnnotation) cq.get(0)).getIndex())
                                idx = 0;
                            else if (index > ((CollatedColumnAnnotation) cq.get(cq.size() - 1)).getIndex())
                                idx = cq.size();
                            else 
                                for (int i = 1; i < cq.size(); i++) {
                                    if ((index > ((CollatedColumnAnnotation) cq.get(i - 1)).getIndex()) &&
                                        (index < ((CollatedColumnAnnotation) cq.get(i)).getIndex()))
                                        idx = i;		 
                                }
                            CollatedColumnAnnotation newAnnotation = 
                                new CollatedColumnAnnotation(index, "", Color.red, "", null);

                            cq.add(idx, newAnnotation);
                        } else if (isInTable) {
                            CollatedColumnAnnotation copiedAnnotation = 
                                (CollatedColumnAnnotation) cq.get(idx);
                            CollatedColumnAnnotation newAnnotation = 
                                new CollatedColumnAnnotation(
                                    copiedAnnotation.getIndex(), "", 
                                    (Color) copiedAnnotation.getColor(), "", null);

                            idx++;
                            cq.add(idx, newAnnotation);
                        }
                        model.setDataVector(getRowData(cq), headers);					
                        formatTable();
                        JOptionPane optionPane = 
                            new JOptionPane("New column annotation inserted at row #"
                                + (Integer.toString(idx + 1)), JOptionPane.PLAIN_MESSAGE, 
                                JOptionPane.DEFAULT_OPTION);

                        (optionPane.createDialog(owner, "Message")).show();
                    } 
                } else if (e.getSource() == delete_menuitem) {
                    int idx[] = columnTable.getSelectedRows();					

                    for (int i = 0; i < idx.length; i++) {
                        model.removeRow(idx[i]);
                        cq.remove(idx[i]);
                        for (int j = 0; j < idx.length; j++)
                            idx[j]--;
                    }
                    for (int i = 0; i < cq.size(); i++)
                        model.setValueAt(Integer.toString(i + 1), i, numCol);
                } else if (e.getSource() == line_new_menuitem) {
                    int index = sequenceSelector("New Line Annotation");

                    if (index != -1) { 
                        int idx = 0, annIndex = 0;
                        boolean isInTable = false; 

                        for (int i = 0; i < cz.size(); i++) {
                            if (((CollatedLineAnnotation) cz.get(i)).getIndex() == index) {
                                isInTable = true;
                                idx = i;
                                annIndex = ((CollatedLineAnnotation) cz.get(i)).getAnnotationIndex() + 1;
                            }
                        }
                        if (isInTable) {
                            CollatedLineAnnotation copiedAnnotation = 
                                (CollatedLineAnnotation) cz.get(idx);
                            CollatedLineAnnotation newAnnotation = 
                                new CollatedLineAnnotation(
                                    copiedAnnotation.getIndex(), annIndex, 0, 0, "", "");

                            idx++;
                            cz.add(idx, newAnnotation);
                        } else if (!isInTable) { 
                            if (index < ((CollatedLineAnnotation) cz.get(0)).getIndex())
                                idx = 0;
                            else if (index > 
                                ((CollatedLineAnnotation) cz.get(cz.size() - 1)).getIndex())
                                idx = cz.size();
                            else {
                                int i = 1;
                                boolean flag = false;

                                while (i < cz.size() && !flag) {
                                    if ((index > ((CollatedLineAnnotation) cz.get(i - 1)).getIndex()) &&
                                        (index < ((CollatedLineAnnotation) cz.get(i)).getIndex())) {
                                        idx = i;
                                        flag = true;
                                    }
                                    i++;
                                }
                            }
                            CollatedLineAnnotation newAnnotation = 
                                new CollatedLineAnnotation(index, 0, 0, 0, "", "");							

                            cz.add(idx, newAnnotation);
                        }
                        lineModel.setDataVector(getLineRowData(cz), headers);					
                        formatLineTable();
                        JOptionPane optionPane = 
                            new JOptionPane("New line annotation inserted at row #"
                                + (Integer.toString(idx + 1)), JOptionPane.PLAIN_MESSAGE, 
                                JOptionPane.DEFAULT_OPTION);

                        (optionPane.createDialog(owner, "Message")).show();
                    }
                } else if (e.getSource() == line_add_menuitem) {
                    int idx = lineTable.getSelectedRow();
                    CollatedLineAnnotation copiedAnnotation
                        = (CollatedLineAnnotation) cz.get(idx);
                    CollatedLineAnnotation newAnnotation = 
                        new CollatedLineAnnotation(
                            copiedAnnotation.getIndex(), copiedAnnotation.getAnnotationIndex(),
                            0, 0, copiedAnnotation.getName(), "");

                    cz.add(idx + 1, newAnnotation);
                    lineModel.setDataVector(getLineRowData(cz), lineHeaders);	
                } else if (e.getSource() == line_delete_menuitem) {
                    int idx[] = lineTable.getSelectedRows();					

                    for (int i = 0; i < idx.length; i++) {
                        lineModel.removeRow(idx[i]);
                        cz.remove(idx[i]);
                        for (int j = 0; j < idx.length; j++)
                            idx[j]--;
                    }
                    for (int row = 0; row < lineModel.getRowCount(); row++)
                        lineTable.setValueAt(Integer.toString(row + 1), row, lineNumCol);
                }
            }
        };		
    
    private boolean mouse_pressed = false;
    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        mouse_pressed = true;
        if (!mouse_pressed) return;
        if (maybeShowPopup(e)) return;		
    }		
    
    public void mouseReleased(MouseEvent e) { 
        if (maybeShowPopup(e)) return;
        if (popup.isVisible()) return;
        mouse_pressed = false;
    }
    
    private boolean maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            boolean show_edit_menu = mouse_pressed;

            new_menuitem.setEnabled(true);
            delete_menuitem.setEnabled(columnTable.getSelectedRowCount() > 0);
	    
            line_new_menuitem.setEnabled(true);
            line_add_menuitem.setEnabled(lineTable.getSelectedRowCount() == 1);
            line_delete_menuitem.setEnabled(lineTable.getSelectedRowCount() > 0);
	    
            if (tabbedPane.getSelectedIndex() == 0)
                Utils.showPopup(popup, e.getComponent(), e.getX(), e.getY());
            else if (tabbedPane.getSelectedIndex() == 1)
                Utils.showPopup(linepopup, e.getComponent(), e.getX(), e.getY());
            return true;
        }
        return false;
    }
    
    public class lineJTable extends JTable {
        public lineJTable(TableModel tm) {
            super(tm);
        }

        public void tableChanged(TableModelEvent e) {
            super.tableChanged(e);
            if ((e.getType() == TableModelEvent.UPDATE)) {
                if (e.getColumn() == lineNameCol) {
                    int row = e.getFirstRow();
                    String newName = (String) lineTable.getValueAt(row, lineNameCol);

                    row++;
                    while ((row < lineModel.getRowCount()) && !isNewLineAnnotation(row)) {
                        lineTable.setValueAt(newName, row, lineNameCol);
                        row++;
                    }
                }
                if (e.getColumn() >= lineNameCol) cz = readLineAnnotations(cz);
            }
        }
    }
    
    public boolean isColumnModelCellEditable(int row, int column) {
        if ((column == numCol) || (column == seqCol))
            return false;
        else return true;
    }
    
    public boolean isLineModelCellEditable(int row, int column) {
        if ((column == lineNumCol) ||
            (column == lineSeqCol) ||
            ((column == lineNameCol) && !isNewLineAnnotation(row)))
            return false;
        else return true;
    }
    
    public class ShadedRenderer implements TableCellRenderer {	
        public DefaultTableCellRenderer DEFAULT_RENDERER =
            new DefaultTableCellRenderer();
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);					
            Color foreground, background, swap; 

            ((JLabel) renderer).setOpaque(true);
            if (!isColumnModelCellEditable(row, column)) {
                foreground = Color.black;
                background = Color.lightGray;
            } else { 
                foreground = Color.black; 
                background = Color.white; 
            }
            if (isSelected) {
                foreground = Color.white;
                background = Color.blue.brighter();
            }
            renderer.setForeground(foreground);
            renderer.setBackground(background);
            return renderer;
        }
    }	 		
		

    public class lineModelShadedRenderer implements TableCellRenderer {	
        public DefaultTableCellRenderer DEFAULT_RENDERER =
            new DefaultTableCellRenderer();
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer = 
                DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);					
            Color foreground, background, swap; 

            ((JLabel) renderer).setOpaque(true);
            if (!isLineModelCellEditable(row, column)) {
                foreground = Color.black;
                background = Color.lightGray;
            } else { 
                foreground = Color.black; 
                background = Color.white; 
            }
            if (isSelected) {
                foreground = Color.white;
                background = Color.blue.brighter();				
            }
            renderer.setForeground(foreground);
            renderer.setBackground(background);
            return renderer;
        }
    }	
    

    public class ButtonRenderer extends JButton implements TableCellRenderer {		
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            if ((value != null) && (value instanceof Color))
                this.setBackground((Color) value);
            return this;
        }
    }
    

    public class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton delegate = new JButton();
        Color savedColor;
        public ColorChooserEditor() {
            ActionListener actionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Color color = JColorChooser.showDialog(delegate, "Color Chooser", savedColor);

                        ColorChooserEditor.this.changeColor(color);
                    }
                };

            delegate.addActionListener(actionListener);
        }

        public Object getCellEditorValue() {
            return savedColor;
        }

        private void changeColor(Color color) {
            if (color != null) {
                savedColor = color;
                delegate.setBackground(color);
            }
        }

        public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected, int row, int column) {
            changeColor((Color) value);
            return delegate;
        }
    }
    
    public void replaceAnnotations(int idx, Color newColor, String newSymbol, 
        String newRange, String newAnnotation) {
        Vector unformattedRange;

        try {
            unformattedRange = CollatedColumnAnnotation.parseRangeString(
                        newRange, alignment.getSequence(idx).length());
        } catch (Exception exception) {
            ErrorDialog.showErrorDialog(owner, "Invalid column range.");
            return;
        }
        Sequence sequence = alignment.getSequence(idx);

        for (int i = 0; i < unformattedRange.size(); i++)
            sequence.setColumnAnnotation(
                ((Integer) unformattedRange.get(i)).intValue(), newSymbol, newColor, newAnnotation);		
    }
    
    public Vector readLineAnnotations(Vector cz) { 
        Vector ctemp = new Vector();

        for (int i = 0; i < lineModel.getRowCount(); i++) {
            ctemp.add(new CollatedLineAnnotation(
                    ((CollatedLineAnnotation) cz.get(i)).getIndex(), 
                    ((CollatedLineAnnotation) cz.get(i)).getAnnotationIndex(), 
                    Integer.parseInt((String) lineTable.getValueAt(i, lineStartCol)),
                    Integer.parseInt((String) lineTable.getValueAt(i, lineEndCol)),
                    (String) lineTable.getValueAt(i, lineNameCol),
                    (String) lineTable.getValueAt(i, lineSymbolCol)));
        }
        return ctemp;
    }
    
    public void replaceLineAnnotations() {
        // delete all line annotations
        for (int i = 0; i < alignment.size(); i++) {
            Sequence sequence = alignment.getSequence(i);

            while (sequence.getLineAnnotationsCount() > 0) {
                try {
                    sequence.deleteLineAnnotation(0);
                } catch (Exception e) {}
            }
        }
        cz = readLineAnnotations(cz);

        /* replace line annotations */
        int idx, prevIdx, annIdx, prevAnnIdx;
        int k = 0;
        CollatedLineAnnotation cla;
        Sequence sequence;

        idx = prevIdx = annIdx = prevAnnIdx = 0;
        while (k < cz.size()) {	
            cla = (CollatedLineAnnotation) cz.get(k);
            prevIdx = cla.getIndex();
            prevAnnIdx = cla.getAnnotationIndex();
            sequence = alignment.getSequence(cla.getIndex());
            StringBuffer ann = new StringBuffer();

            for (int l = 0; l < sequence.length(); l++) ann.append(' ');

            /* while same annotation */
            while ((k < cz.size()) && 
                (((CollatedLineAnnotation) cz.get(k)).getIndex() == prevIdx) &&
                (((CollatedLineAnnotation) cz.get(k)).getAnnotationIndex() == prevAnnIdx)) {  
                cla = (CollatedLineAnnotation) cz.get(k); 		
                StringBuffer word = new StringBuffer();
                int len = cla.getEnd() - cla.getStart() + 1;

                for (int m = 0; m < len; m++)
                    word.append(cla.getSymbol());
                ann.replace(cla.getStart(), cla.getEnd(), word.toString()); 
                k++;
            }			
            cla = (CollatedLineAnnotation) cz.get(k - 1);
            sequence = alignment.getSequence(cla.getIndex());
            sequence.addLineAnnotation(cla.getName(), ann.toString());				
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Vector unformattedRange;
        CollatedColumnAnnotation ca;

        if ((e.getSource() == ok_button) || (e.getSource() == apply_button)) {		
            int badAnnotationNum = -1;

            for (int i = 0; i < cq.size(); i++) {
                ca = (CollatedColumnAnnotation) cq.get(i);
                Sequence sequence = alignment.getSequence(ca.getIndex());

                try {
                    unformattedRange = 
                            CollatedColumnAnnotation.parseRangeString((String) columnTable.getValueAt(i, rangeCol), sequence.length());
                } catch (Exception exception) {
                    ErrorDialog.showErrorDialog(owner, 
                        "Invalid column range in column annotation #" + (i + 1));
                    return;
                }
                if (unformattedRange == null) badAnnotationNum = i;
                if (((String) columnTable.getValueAt(i, symbolCol)).length() != 1) {
                    ErrorDialog.showErrorDialog(owner, 
                        "Symbol must be a single character in column annotation #" + (i + 1));
                    return;
                }
            }
	    
            if (badAnnotationNum == -1) { 
                // if range is ok delete all sequences
                for (int i = 0; i < alignment.size(); i++) {
                    Sequence sequence = alignment.getSequence(i);

                    for (int j = 0; j < sequence.length(); j++) {
                        sequence.deleteColumnAnnotation(j);
                    }
                }
		
                for (int i = 0; i < cq.size(); i++) {
                    ca = (CollatedColumnAnnotation) cq.get(i);
                    replaceAnnotations(ca.getIndex(), 
                        (Color) columnTable.getValueAt(i, colorCol), 
                        (String) columnTable.getValueAt(i, symbolCol), 
                        (String) columnTable.getValueAt(i, rangeCol),
                        (String) columnTable.getValueAt(i, annotationCol));
                }
            } else 
                ErrorDialog.showErrorDialog(owner, 
                    "Invalid column range in column annotation # " + 
                    (badAnnotationNum + 1));
            replaceLineAnnotations();
        }
	
        if ((e.getSource() == ok_button) || (e.getSource() == cancel_button)) 
            dispose();			
    }
}
