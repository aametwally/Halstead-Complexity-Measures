package com.neogenesis.pfaat;


import com.neogenesis.pfaat.colorscheme.ColorScheme;


/**
 * Listener for changes in <code>DisplayProperties</code>.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:28:02 $ */
public interface DisplayPropertiesListener {
    public void displayAnnViewChanged(DisplayProperties dp, 
        Sequence seq,
        boolean show);
    public void displaySeqSelectChanged(DisplayProperties dp, 
        Sequence seq,
        boolean select);
    public void displayColorSchemeChanged(DisplayProperties dp, 
        ColorScheme old);
    public void displayFontChanged(DisplayProperties dp);
    public void displayRenderGapsChanged(DisplayProperties dp);
    public void displayGroupEditingChanged(DisplayProperties dp);
    public void displayOverwriteChanged(DisplayProperties dp);
    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence seq);
    public void displayHighlightsChanged(DisplayProperties dp,
        Sequence[] seqs);
}    
