package com.neogenesis.pfaat;
import java.util.*;
import java.awt.Color;
import java.lang.Boolean;
import com.neogenesis.pfaat.swingx.*;
import com.neogenesis.pfaat.colorscheme.*;
/**
 * An aligned collection of <code>Sequence</code> objects.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:03 $ */
public class Alignment implements SequenceListener {
    private boolean changed = false;
    // name
    private String name;
    // Only used for database records.
    private int id;
    private String desc;
    // underlying sequence data
    private Sequence[] sequences;
    // ruler annotations
    private ArrayList rulerAnnotations = new ArrayList();
    private ArrayList columnsSelected = new ArrayList();
    private int lastRannIdxSelected = -1;
    private int lastColumnIdxSelected = -1;
    private static String backgroundStr = "all seqs";
    // map by sequence name
    public Map sequence_name_map = new HashMap();
    // map by position in alignment
    public Map sequence_idx_map = new HashMap();
    // listeners
    private List listeners = new ArrayList();
    // to do: fuse RulerAnnotation & columnsSelect
    public static class RulerAnnotation {
        private String letter;
        private Color color;
        private boolean selected;
        public RulerAnnotation(String letter, Color color, boolean selected) {
            this.letter = letter;
            this.color = color;
            this.selected = selected;
        }
        public String getLetter() {            return letter;        }
        public Color getColor() {            return color;        }
        public boolean isSelected() {            return selected;        }
        public void setLetter(String newLetter) {            this.letter = newLetter;        }
        public void setColor(Color newColor) {            this.color = newColor;        }
        public void setSelected(boolean newSelected) {            this.selected = newSelected;        }
    }
    // constructor
    public Alignment(String name, Sequence[] sequences) throws Exception {
        this(name, sequences, true, null);
    }
    /**
     * up to the user to decide whether to shift the sequence or not
     */
    public Alignment(String name, Sequence[] sequences, boolean shift, HashMap rulerMap) throws Exception {
        this.name = name;
        this.sequences = sequences;
        for (int i = sequences.length - 1; i >= 0; i--) {
            Sequence s = sequences[i];
            if (shift)
                s.shiftAA(0, 1);
            if (sequence_name_map.put(s.getName(), s) != null)
                throw new Exception("duplicate sequence names in alignment");
            sequence_idx_map.put(s, new Integer(i));
            s.addListener(this);
        }
        if (rulerMap != null) {
            for (int col = 0; col < maxLength(); col++) {
                Object rannObj = rulerMap.get(new Integer(col));
                columnsSelected.add(new Boolean(false));
                if (rannObj != null)
                    rulerAnnotations.add((RulerAnnotation) rannObj);
                else if (rannObj == null)
                    rulerAnnotations.add(new RulerAnnotation(null, Color.blue, false));
            }
        } else {
            for (int col = 0; col < maxLength(); col++) {
                columnsSelected.add(new Boolean(false));
                rulerAnnotations.add(new RulerAnnotation(null, Color.blue, false));
            }
        }
    }
    public Alignment(String name, Sequence[] sequences, HashMap rulerMap) throws Exception {
        this(name, sequences, true, rulerMap);
    }
    // return the number of sequences
    public int size() {        return sequences.length;    }
    // the name
    public String getName() {        return name;    }
    public String getBackgroundString() {        return backgroundStr;    }
    public RulerAnnotation getRulerAnnotation(int idx) {
        if (idx != -1 && idx < rulerAnnotations.size())
            return (RulerAnnotation) rulerAnnotations.get(idx);
        else return (new RulerAnnotation(null, Color.white, false));
    }
    public boolean isRulerAnnotated(int idx) {
        String letter = ((RulerAnnotation) rulerAnnotations.get(idx)).getLetter();
        return (letter != null);
    }
    public void deselectAllRulerSelections() {
        for (int i = 0; i < rulerAnnotations.size(); i++) {
            (getRulerAnnotation(i)).setSelected(false);
        }
        lastRannIdxSelected = -1;
    }
    public int rulerAnnotationsSize() {        return rulerAnnotations.size();    }
    public boolean isRulerAnnotationSelected() {
        boolean rannselected = false;
        for (int i = 0; i < rulerAnnotations.size(); i++) {
            if (((RulerAnnotation) rulerAnnotations.get(i)).isSelected())
                rannselected = true;
        }
        return rannselected;
    }
    public void addRulerAnnotation() {
        rulerAnnotations.add(new RulerAnnotation(null, Color.blue, false));
    }
    public void removeRulerAnnotation(int idx) {
        if (idx < rulerAnnotations.size())  rulerAnnotations.remove(idx);
    }
    public void removeRulerAnnotationFromEnd() {
        rulerAnnotations.remove(rulerAnnotations.size() - 1);
    }
    public void removeRulerAnnotationRange(int from, int to) {
        for (int i = 0; i < to - from; i++)
            rulerAnnotations.remove(from);
    }
    public boolean isColumnSelected(int idx) {
        if (columnsSelected.isEmpty()) return false;
        return ((Boolean) columnsSelected.get(idx)).booleanValue();
    }
    public boolean isColumnSelected() {
        if (columnsSelected.isEmpty()) return false;
        return columnsSelected.contains(new Boolean(true));
    }
    public int columnsSelected() {
        int columns = 0;
        for (int i = 0; i < columnsSelected.size(); i++)
            if (isColumnSelected(i)) columns++;
        return columns;
    }
    public int getFirstColumnSelected() {
        return columnsSelected.indexOf(new Boolean(true));
    }
    public int getLastColumnSelected() {
        return columnsSelected.lastIndexOf(new Boolean(true));
    }
    /* public int[] getColumnsSelected() {
     if (!isColumnSelected()) return null;
     Object[] colObjs = columnsSelected.toArray();
     ArrayList colSet;
     int[] selCols;
     for (int i = 0; i < colObjs.length; i++)
     if (((Boolean)colObjs[i]).booleanValue()) colSet.add(new Integer(i));
     selCols = new Intger[colSet.size()];
     for (int i = 0; i < selCols.length; i++)
     selCols[i] = ((Integer)colSet.get(i)).intValue();
     return selCols;
     }
     */
    public int getColumnsSize() {
        return columnsSelected.size();
    }
    public void setColumnSelected(int idx, boolean b, DisplayProperties p) {
        if (idx < columnsSelected.size()) {
            columnsSelected.set(idx, new Boolean(b));
            for (int row = 0; row < size(); row++) {
                p.setSeqHighlight(this.getSequence(row), idx, b, false);
            }
            p.fireDisplayHighlightsChanged(this.getAllSequences());
        }
    }
    public void deselectAllColumns(DisplayProperties p) {
        for (int idx = 0; idx < columnsSelected.size(); idx++)
            columnsSelected.set(idx, new Boolean(false));
        lastColumnIdxSelected = -1;
        // p.clearHighlights();
    }
    public void addColumn() {        columnsSelected.add(new Boolean(false));    }
    public void removeColumnFromEnd() {        columnsSelected.remove(columnsSelected.size() - 1);    }
    public void removeColumn(int idx) {        columnsSelected.remove(idx);    }
    public void removeColumnRange(int from, int to) {
        for (int i = 0; i < to - from; i++)
            columnsSelected.remove(from);
    }
    public int getLastRulerAnnIdxSelected() {
        return lastRannIdxSelected;
    }
    public int getLastColumnIdxSelected() {
        return lastColumnIdxSelected;
    }
    public void clearLastRulerAnnIdxSelected() {
        lastRannIdxSelected = -1;
    }
    public void clearLastColumnIdxSelected() {
        lastColumnIdxSelected = -1;
    }
    public void setLastRulerAnnIdxSelected(int idx) {
        lastRannIdxSelected = idx;
    }
    public void setLastColumnIdxSelected(int idx) {
        lastColumnIdxSelected = idx;
    }
    // get all sequences for specified group
    public Sequence[] getAllGroupSequences(String group) {
        if (group.equals(backgroundStr)) return getAllSequences();
        if (getAllGroups().isEmpty()) return null;
        HashSet seqset = new HashSet();
        for (int row = 0; row < size(); row++) {
            HashSet groups = getSequence(row).getGroups();
            if (groups.contains(new String(group)))
                seqset.add(getSequence(row));
        }
        Sequence seqs[] = new Sequence[seqset.size()];
        int m = 0;
        for (Iterator i = seqset.iterator(); i.hasNext();)
            seqs[m++] = (Sequence) i.next();
        return seqs;
    }
    // get all sequences for specified group
    public Sequence[] getAllSequencesNotInGroup(String group) {
        if (group.equals(backgroundStr)) return getAllSequences();
        if (getAllGroups().isEmpty()) return null;
        HashSet seqset = new HashSet();
        for (int row = 0; row < size(); row++) {
            HashSet groups = getSequence(row).getGroups();
            if (!groups.contains(new String(group)))
                seqset.add(getSequence(row));
        }
        Sequence seqs[] = new Sequence[seqset.size()];
        int m = 0;
        for (Iterator i = seqset.iterator(); i.hasNext();)
            seqs[m++] = (Sequence) i.next();
        return seqs;
    }
    // get all groups over the entire alignment
    public HashSet getAllGroups() {
        HashSet groupSet = new HashSet();
        for (int row = 0; row < size(); row++) {
            String[] groups = getSequence(row).getGroupNames();
            if (groups != null) {
                for (int i = 0; i < groups.length; i++)
                    groupSet.add(groups[i]);
            }
        }
        return groupSet;
    }
    // get all group names over entire alignmet
    public String[] getAllGroupNames() {
        HashSet groupSet = getAllGroups();
        if (groupSet.isEmpty()) return null;
        Object[] returnObj = groupSet.toArray();
        String[] returnStr = new String[returnObj.length];
        for (int i = 0; i < returnStr.length; i++)
            returnStr[i] = returnObj[i].toString();
        return returnStr;
    }
    // get all group names, ordered with first as the first member in
    // the array and 'background' appended at the end. argument for
    // jcombobox
    public String[] getAllDisplayGroupNames(String first) {
        HashSet groupSet = getAllGroups();
        String[] returnStr;
        if (!groupSet.isEmpty() && !first.equals(null)) {
            if (!groupSet.contains(first))
                first = ((groupSet.toArray())[0]).toString();
            groupSet.remove(new String(first));
            Object[] returnObj = groupSet.toArray();
            returnStr = new String[returnObj.length + 2];
            returnStr[0] = first;
            returnStr[returnStr.length - 1] = backgroundStr;
            for (int i = 1; i < returnStr.length - 1; i++)
                returnStr[i] = returnObj[i - 1].toString();
        } else {
            returnStr = new String[1];
            returnStr[0] = backgroundStr;
        }
        return returnStr;
    }
    // does a group already exist in the alignment?
    public boolean groupExists(String group) {
        return getAllGroups().contains(new String(group));
    }
    // get all of the sequences
    public Sequence[] getAllSequences() {
        Sequence[] all = new Sequence[sequences.length];
        System.arraycopy(sequences, 0, all, 0, sequences.length);
        return all;
    }
    public String[] getAllSequenceNames() {
        String[] all = new String[sequences.length];
        for (int i = 0; i < sequences.length; i++)
            all[i] = sequences[i].getName();
        return all;
    }
    // get non-redundant array of annotations
    public ArrayList getExistingColumnAnnotations() {
        ArrayList existing_annotations = new ArrayList();
        Set existing_annotations_set = new HashSet();
        for (int i = 0; i < this.size(); i++) {
            Sequence seq = this.getSequence(i);
            if (seq.getColumnAnnotationsCount() > 0) {
                Sequence.ColumnAnnotation[] this_ca = seq.getColumnAnnotations();
                for (int j = 0; j < this_ca.length; j++) {
                    CachedAnnotation new_ca =
                        new CachedAnnotation(this_ca[j].getSymbol(),
                            this_ca[j].getAnnotation(),
                            this_ca[j].getColor());
                    if (existing_annotations_set.add(new_ca))
                        existing_annotations.add(new_ca);
                }
            }
        }
        return  existing_annotations;
    }
    public int getNumberExistingColumnAnnotations() {
        return this.getExistingColumnAnnotations().size();
    }
    // maximum length, computed if necessary
    public int maxLength() {
        int max_length = 0;
        for (int i = sequences.length - 1; i >= 0; i--) {
            int l = sequences[i].length();
            if (l > max_length) max_length = l;
        }
        return max_length;
    }
    public int minLength() {
        int min_length = sequences[0].length();
        for (int i = 0; i < sequences.length; i++) {
            int l = sequences[i].length();
            if (l < min_length) min_length = l;
        }
        return min_length;
    }
    public boolean isColumnAllGaps(int idx) {
        for (int j = 0; j < size(); j++) {
            Sequence s = getSequence(j);
            if (idx < s.length() && !s.getAA(idx).isGap()) {
                return false;
            }
        }
        return true;
    }
    // return a specific sequence
    public Sequence getSequence(int idx) {        return sequences[idx];    }
    public Sequence getSequence(String seq_name) {
        return (Sequence) sequence_name_map.get(seq_name);
    }
    // set the name
    public void setName(String name) {
        this.name = name;
        setChanged(true);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((AlignmentListener) it.next()).alignmentNameChanged(this);
    }
    // insert a sequence into the alignment
    public void insertSequence(int idx, Sequence s) throws Exception {
        Sequence[] new_seqs = new Sequence[sequences.length + 1];
        if (idx > 0)
            System.arraycopy(sequences, 0, new_seqs, 0, idx);
        new_seqs[idx] = s;
        if (idx < sequences.length)
            System.arraycopy(sequences, idx, new_seqs, idx + 1,
                sequences.length - idx);
        if (sequence_name_map.put(s.getName(), s) != null)
            throw new Exception("duplicate sequence names in alignment");
        sequences = new_seqs;
        for (int i = sequences.length - 1; i >= idx; i--)
            sequence_idx_map.put(sequences[i], new Integer(i));
        s.addListener(this);
        setChanged(true);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((AlignmentListener) it.next()).alignmentSeqInserted(this, idx);
    }
    // find the index of a sequence
    public int getIndex(Sequence s) {
        Integer idx = (Integer) sequence_idx_map.get(s);
        return idx != null ? idx.intValue() : -1;
    }
    // delete a sequence into the alignment
    public void deleteSequence(int idx) {
        Sequence[] new_seqs = new Sequence[sequences.length - 1];
        if (idx > 0)
            System.arraycopy(sequences, 0, new_seqs, 0, idx);
        if (idx < sequences.length - 1)
            System.arraycopy(sequences, idx + 1, new_seqs, idx,
                sequences.length - 1 - idx);
        Sequence s = sequences[idx];
        sequence_name_map.remove(s.getName());
        sequence_idx_map.remove(s);
        for (int i = sequences.length - 1; i > idx; i--)
            sequence_idx_map.put(sequences[i], new Integer(i - 1));
        s.removeListener(this);
        sequences = new_seqs;
        setChanged(true);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((AlignmentListener) it.next()).alignmentSeqDeleted(this, idx, s);
    }
    public void deleteSequence(Sequence s) {
        deleteSequence(getIndex(s));
    }
    // swap two sequences
    public void swapSequence(int i, int j) throws Exception {
        if (i < 0 || i >= sequences.length || j < 0 || j >= sequences.length)
            throw new Exception("bad indexes for sequence swap");
        if (i == j)
            return;
        Sequence s = sequences[i];
        sequences[i] = sequences[j];
        sequences[j] = s;
        sequence_idx_map.put(sequences[i], new Integer(i));
        sequence_idx_map.put(sequences[j], new Integer(j));
        setChanged(true);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((AlignmentListener) it.next()).alignmentSeqSwapped(this, i, j);
    }
    // add/remove listeners
    public void addListener(AlignmentListener list) {        listeners.add(list);    }
    public void removeListener(AlignmentListener list) {
        listeners.remove(list);
    }
    // SequenceListener interface
    public void sequenceNameChanged(Sequence aaseq, String old_name)
        throws Exception {
        setChanged(true);
        if (sequence_name_map.get(old_name) != aaseq)
            throw new Exception("name bound to incorrect sequence");
        Sequence old_seq = (Sequence) sequence_name_map.get(aaseq.getName());
        if (old_seq != null && old_seq != aaseq)
            throw new Exception("duplicate sequence name");
        sequence_name_map.remove(old_name);
        sequence_name_map.put(aaseq.getName(), aaseq);
    }
    public void sequenceAAChanged(Sequence aaseq) {
        setChanged(true);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((AlignmentListener) it.next()).alignmentSeqAAChanged(this, aaseq);
    }
    public void sequenceAnnotationChanged(Sequence aaseq) {        setChanged(true);    }
    public void sequenceGroupChanged(Sequence aaseq) {        setChanged(true);    }
    public void sequenceLineAnnotationsChanged(Sequence aaseq) {        setChanged(true);    }
    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {        setChanged(true);    }
    public void sequenceColorChanged(Sequence aaseq) {        setChanged(true);    }
    public boolean hasChanged() {        return changed;    }
    public void setChanged(boolean b) {
        changed = b;
    }
    // set the database id
    public void setId(int id) {
        this.id = id;
    }
    // get the database id
    public int getId() {
        return this.id;
    }
    // set the database desc
    public void setDesc(String desc) {
        this.desc = desc;
    }
    // get the database desc
    public String getDesc() {
        return this.desc;
    }
}
