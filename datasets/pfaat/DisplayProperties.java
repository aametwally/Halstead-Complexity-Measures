package com.neogenesis.pfaat;


import java.awt.*;
import java.util.*;

import com.neogenesis.pfaat.colorscheme.*;


/**
 * Holds generic alignment display information.
 *
 * @author $Author: xih $
 * @version $Revision: 1.3 $, $Date: 2002/10/11 18:28:02 $ */
public class DisplayProperties implements AlignmentListener, SequenceListener {
    private Alignment align;
    // residue coloring scheme
    private ColorScheme color_scheme;
    // residue text font
    private Font font;
    // residue text font metrics
    private FontMetrics font_metrics;
    // residue height and width
    private int residue_width, residue_height, font_y_offset, font_x_offset;
    // fast rendering mode?
    private boolean fast_render;
    // render the gaps?
    private boolean render_gaps;
    // show residue annotations?
    private boolean show_res_ann = true;
    // show residue annotation panel?
    private boolean show_res_ann_panel = true;
    // is cursor in RulerAnnotationComponent or SequenceAnnComponent?
    private boolean cursorHidden = false;

    // initial cursor position
    private int cursorRow = 0;
    private int prevCursorRow = 0;
    private int cursorColumn = 1;
    private int prevCursorColumn = 1;

    // set of selected sequences
    private Set seq_selected = new HashSet();
    // set of residues with line annotations showing
    private Set seq_ann_view = new HashSet();

    // is in group editing mode
    private boolean group_editing = false;

    // can we overwrite sequence data in shifts?
    private boolean overwrite_shifts = false;

    // Is annotation popup window enabled?
    private boolean annotation_popup = true;
    private boolean isSeqAnnEditing = false;
    private boolean isRulerEditing = false;
    // map of highlighted residues, by sequence
    private Map highlights = new HashMap();

    // listeners
    private java.util.List listeners = new ArrayList();

    public DisplayProperties(Alignment align,
        // AlignmentFrame frame,
        ColorScheme color_scheme,
        Font font,
        FontMetrics font_metrics,
        boolean fast_render,
        boolean render_gaps) {
        this.render_gaps = render_gaps;
        this.align = align;
        setFont(font, font_metrics, fast_render);
        setColorScheme(color_scheme);
        align.addListener(this);
        for (int i = align.size() - 1; i >= 0; i--)
            align.getSequence(i).addListener(this);
    }

    // accessors
    public ColorScheme getColorScheme() {
        return color_scheme;
    }

    public Font getFont() {
        return font;
    }

    public int getResidueWidth() {
        return residue_width;
    }

    public int getResidueHeight() {
        return residue_height;
    }

    public int getFontXOffset() {
        return font_x_offset;
    }

    public int getFontYOffset() {
        return font_y_offset;
    }

    public FontMetrics getFontMetrics() {
        return font_metrics;
    }

    public int clipChannel(int channel) {
        int newChannel = channel;

        newChannel = newChannel > 128 && newChannel <= 192 ? 192 : newChannel;
        newChannel = newChannel <= 128 && newChannel > 64 ? 64 : newChannel;
        return newChannel;
    }

    public Color inverseRGB(Color c) {
        int brightness = c.getRed() + c.getGreen() + c.getBlue();

        if (brightness < 382)
            return Color.white;
        else return Color.black;
    }

    public boolean isGapRendered() {
        return render_gaps;
    }

    public boolean showResAnn() {
        return show_res_ann;
    }

    public boolean showResAnnPanel() {
        return show_res_ann_panel;
    }

    public boolean getAnnView(Sequence s) {
        return seq_ann_view.contains(s);
    }

    public boolean getSeqSelect(Sequence s) {
        return seq_selected.contains(s);
    }

    public Sequence[] getAllSelected() {
        Sequence[] seqs = new Sequence[seq_selected.size()];
        int cnt = 0;

        for (Iterator i = seq_selected.iterator(); i.hasNext();) {
            Sequence s = (Sequence) i.next();

            seqs[cnt++] = s;
        }
        return seqs;
    }

    public int getSelectedCount() {
        return seq_selected.size();
    }

    public boolean isSequenceSelected() {
        if (seq_selected.isEmpty()) return false;
        return (getSelectedCount() > 0);
    }

    public boolean isSequenceSelected(int row) {
        return seq_selected.contains(align.getSequence(row));
    }

    public boolean isGroupEditing() {
        return group_editing;
    }

    public boolean isOverwrite() {
        return overwrite_shifts;
    }

    public boolean isAnnotationPopupEnabled() {
        return annotation_popup;
    }

    public boolean isFastRender() {
        return fast_render;
    }

    public boolean isSeqAnnEditing() {
        return (isSeqAnnEditing);
    }

    public boolean isRulerEditing() {
        return isRulerEditing;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public int getPrevCursorRow() {
        return prevCursorRow;
    }

    public int getCursorColumn() {
        return cursorColumn;
    }

    public int getPrevCursorColumn() {
        return prevCursorColumn;
    }

    public boolean isCursorHidden() {
        return cursorHidden;
    }

    // mutators
    public void setSeqAnnEditing(boolean b) {
        isSeqAnnEditing = b;
    }

    public void setRulerEditing(boolean b) {
        isRulerEditing = b;
    }

    public void setCursorHidden(boolean b) {
        cursorHidden = b;
    }

    public void updateCursor(int newCursorRow, int newCursorColumn) {
        prevCursorRow = cursorRow;
        prevCursorColumn = cursorColumn;
        cursorRow = newCursorRow;
        cursorColumn = newCursorColumn;
        if (!cursorHidden) {
            clearHighlights();
            setSeqHighlight(align.getSequence(cursorRow), cursorColumn, true);
            align.clearLastRulerAnnIdxSelected();
            align.clearLastColumnIdxSelected();
        }
    }

    // set group editing mode
    public void setGroupEditing(boolean b) {
        if (group_editing != b) {
            group_editing = b;
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displayGroupEditingChanged(this);
        }
    }

    // set shhift overwriting
    public void setOverwrite(boolean b) {
        if (overwrite_shifts != b) {
            overwrite_shifts = b;
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displayOverwriteChanged(this);
        }
    }

    public void setAnnotationPopup(boolean b) {
        annotation_popup = b;
    }

    // show a line annotations associated with a particular sequence
    public void setAnnView(Sequence s, boolean show) {
        boolean notify = false;

        if (show && seq_ann_view.add(s))
            notify = true;
        else if (!show && seq_ann_view.remove(s))
            notify = true;
        if (notify) {
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displayAnnViewChanged(this, s, show);
        }
    }

    // select all sequences
    public void allSelections(Alignment alignment) {
        for (int i = alignment.size() - 1; i >= 0; i--) {
            Sequence s = alignment.getSequence(i);

            if (seq_selected.add(s)) {
                for (Iterator it = listeners.iterator(); it.hasNext();)
                    ((DisplayPropertiesListener) it.next()).displaySeqSelectChanged(this, s, true);
            }
        }
    }

    // invert the selections
    public void invertSelections(Alignment alignment) {
        for (int i = alignment.size() - 1; i >= 0; i--) {
            Sequence s = alignment.getSequence(i);

            if (seq_selected.remove(s)) {
                for (Iterator it = listeners.iterator(); it.hasNext();)
                    ((DisplayPropertiesListener) it.next()).displaySeqSelectChanged(this, s, false);
            } else {
                seq_selected.add(s);
                for (Iterator it = listeners.iterator(); it.hasNext();)
                    ((DisplayPropertiesListener) it.next()).displaySeqSelectChanged(this, s, true);
            }
        }
    }

    // select a particular sequence
    public void setSeqSelect(Sequence s, boolean select) {
        boolean notify = false;

        if (select && seq_selected.add(s))
            notify = true;
        else if (!select && seq_selected.remove(s))
            notify = true;
        if (notify) {
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displaySeqSelectChanged(this, s, select);
        }
    }

    // clear all selections
    public void clearSelections() {
        if (seq_selected.size() < 1) return;
        ArrayList seqs = new ArrayList(seq_selected.size());

        seqs.addAll(seq_selected);
        seq_selected.clear();
        for (Iterator i = seqs.iterator(); i.hasNext();) {
            Sequence s = (Sequence) i.next();

            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displaySeqSelectChanged(this, s, false);
        }
    }

    // highlight a particular sequence
    public void setSeqHighlight(Sequence s, int idx, boolean highlight) {
        boolean notify = false;

        Set this_h = (Set) highlights.get(s);

        if (highlight) {
            if (this_h == null) {
                this_h = new TreeSet();
                this_h.add(new Integer(idx));
                highlights.put(s, this_h);
                notify = true;
            } else if (this_h.add(new Integer(idx)))
                notify = true;
        } else {
            if (this_h != null && this_h.remove(new Integer(idx))) {
                if (this_h.size() == 0)
                    highlights.remove(s);
                notify = true;
            }
        }
        if (notify) {
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displayHighlightsChanged(this, s);
        }
    }

    // highlight a particular sequence
    public void setSeqHighlight(Sequence s, int idx, boolean highlight, boolean notify) {
        boolean innerNotify = false;

        Set this_h = (Set) highlights.get(s);

        if (highlight) {
            if (this_h == null) {
                this_h = new TreeSet();
                this_h.add(new Integer(idx));
                highlights.put(s, this_h);
                innerNotify = true;
            } else if (this_h.add(new Integer(idx)))
                innerNotify = true;
        } else {
            if (this_h != null && this_h.remove(new Integer(idx))) {
                if (this_h.size() == 0)
                    highlights.remove(s);
                innerNotify = true;
            }
        }
        if (notify && innerNotify) {
            fireDisplayHighlightsChanged(s);
        }
    }

    public void fireDisplayHighlightsChanged(Sequence s) {
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((DisplayPropertiesListener) it.next()).displayHighlightsChanged(this, s);
    }

    public void fireDisplayHighlightsChanged(Sequence[] s) {
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((DisplayPropertiesListener) it.next()).displayHighlightsChanged(this, s);
    }

    // clear all highlights
    public void clearHighlights() {
        if (highlights.size() < 1) return;
        ArrayList seqs = new ArrayList(highlights.size());

        seqs.addAll(highlights.keySet());
        highlights.clear();
        for (Iterator i = seqs.iterator(); i.hasNext();) {
            Sequence s = (Sequence) i.next();

            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displayHighlightsChanged(this, s);
        }
    }

    // get a sorted array of highlights for a particular sequence,
    // null if there are none
    public int[] getHighlights(Sequence s) {
        TreeSet this_h = (TreeSet) highlights.get(s);

        if (this_h == null || this_h.size() == 0) return null;
        int[] h = new int[this_h.size()];
        int idx = 0;

        for (Iterator i = this_h.iterator(); i.hasNext();)
            h[idx++] = ((Integer) i.next()).intValue();
        return h;
    }

    // set a color scheme
    public void setColorScheme(ColorScheme color_scheme) {
        ColorScheme old = this.color_scheme;

        this.color_scheme = color_scheme;
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((DisplayPropertiesListener) it.next()).displayColorSchemeChanged(this, old);
    }

    // set the font
    public void setFont(Font font,
        FontMetrics font_metrics,
        boolean fast_render) {
        this.fast_render = fast_render;
        this.font = font;
        this.font_metrics = font_metrics;
        if (fast_render) {
            this.residue_width = font_metrics.stringWidth("Q");
            this.font_x_offset = 0;
        } else {
            this.residue_width = font_metrics.stringWidth("Q") + 2;
            this.font_x_offset = 1;
        }
        this.residue_height =
                font_metrics.getMaxAscent() + font_metrics.getMaxDescent() + 5;
        this.font_y_offset = font_metrics.getMaxAscent() + 3;
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((DisplayPropertiesListener) it.next()).displayFontChanged(this);
    }

    public void setRenderGaps(boolean render_gaps) {
        if (this.render_gaps != render_gaps) {
            this.render_gaps = render_gaps;
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displayRenderGapsChanged(this);
        }
    }

    public void setShowResAnn(boolean b) {
        show_res_ann = b;
    }

    public void setShowResAnnPanel(Component c, boolean b) {
        c.setVisible(b);
        show_res_ann_panel = b;
    }

    // AlignmentListener interface
    public void alignmentNameChanged(Alignment align) {}

    public void alignmentSeqInserted(Alignment align, int i) {
        align.getSequence(i).addListener(this);
    }

    public void alignmentSeqDeleted(Alignment align, int i, Sequence aaseq) {
        aaseq.removeListener(this);
        highlights.remove(aaseq);
        seq_ann_view.remove(aaseq);
        seq_selected.remove(aaseq);
    }

    public void alignmentSeqSwapped(Alignment align, int i, int j) {}

    public void alignmentSeqAAChanged(Alignment align, Sequence aaseq) {}

    // SequenceListener interface
    public void sequenceNameChanged(Sequence aaseq, String old_name) {}

    public void sequenceAAChanged(Sequence aaseq) {
        highlights.remove(aaseq);
        for (Iterator it = listeners.iterator(); it.hasNext();)
            ((DisplayPropertiesListener) it.next()).displayHighlightsChanged(this, aaseq);
    }

    public void sequenceAnnotationChanged(Sequence aaseq) {}

    public void sequenceGroupChanged(Sequence aaseq) {}

    public void sequenceLineAnnotationsChanged(Sequence aaseq) {
        if (seq_ann_view.add(aaseq)) {
            for (Iterator it = listeners.iterator(); it.hasNext();)
                ((DisplayPropertiesListener) it.next()).displayAnnViewChanged(this, aaseq, true);
        }
    }

    public void sequenceColumnAnnotationsChanged(Sequence aaseq, int column) {}

    public void sequenceColorChanged(Sequence aaseq) {}

    public void addListener(DisplayPropertiesListener l) {
        listeners.add(l);
    }

    public void removeListener(DisplayPropertiesListener l) {
        listeners.remove(l);
    }

    // phdana: begin
    public Alignment getAlignment() {
        return align;
    }
    // phdana: end

}
